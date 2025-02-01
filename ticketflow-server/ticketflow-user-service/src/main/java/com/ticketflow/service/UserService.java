package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.client.BaseDataClient;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.*;
import com.ticketflow.entity.TicketUser;
import com.ticketflow.entity.User;
import com.ticketflow.entity.UserEmail;
import com.ticketflow.entity.UserMobile;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.enums.BusinessStatus;
import com.ticketflow.enums.CompositeCheckType;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.handler.BloomFilterHandler;
import com.ticketflow.impl.composite.CompositeContainer;
import com.ticketflow.jwt.TokenUtil;
import com.ticketflow.mapper.TicketUserMapper;
import com.ticketflow.mapper.UserEmailMapper;
import com.ticketflow.mapper.UserMapper;
import com.ticketflow.mapper.UserMobileMapper;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ticketflow.core.DistributedLockConstants.REGISTER_USER_LOCK;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/31 23:23
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserMobileMapper userMobileMapper;

    @Autowired
    private UserEmailMapper userEmailMapper;

    @Autowired
    private TicketUserMapper ticketUserMapper;

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private BloomFilterHandler bloomFilterHandler;

    @Autowired
    private CompositeContainer compositeContainer;

    @Autowired
    private BaseDataClient baseDataClient;

    @Value("${token.expire.time:40}")
    private Long tokenExpireTime;

    private static final Integer ERROR_COUNT_THRESHOLD = 5;


    /**
     * 用户注册
     *
     * @param userRegisterDto 用户注册dto
     * @return 注册成功Boolean
     */
    @Transactional(rollbackFor = Exception.class)
    @ServiceLock(lockType = LockType.Write, name = REGISTER_USER_LOCK, keys = {"#userRegisterDto.mobile"})
    public Boolean register(UserRegisterDto userRegisterDto) {
        compositeContainer.execute(CompositeCheckType.USER_REGISTER_CHECK.getValue(), userRegisterDto);
        log.info("用户注册手机号:{}", userRegisterDto.getMobile());
        // 用户表添加
        User user = new User();
        BeanUtils.copyProperties(userRegisterDto, user);
        user.setId(uidGenerator.getUid());
        userMapper.insert(user);
        // 用户收集表加
        UserMobile userMobile = new UserMobile();
        userMobile.setId(uidGenerator.getUid());
        userMobile.setUserId(user.getId());
        userMobile.setMobile(userRegisterDto.getMobile());
        userMobileMapper.insert(userMobile);
        // bloomFilter添加
        bloomFilterHandler.add(userMobile.getMobile());
        return true;
    }

    /**
     * 判断用户是否存在
     *
     * @param userExistDto 用户手机号dto
     */
    @ServiceLock(lockType = LockType.Read, name = REGISTER_USER_LOCK, keys = {"#mobile"})
    public void exist(UserExistDto userExistDto) {
        doExist(userExistDto.getMobile());
    }

    public void doExist(String mobile) {
        // 不存在一定不存在
        boolean contains = bloomFilterHandler.contains(mobile);
        // 存在就再查数据库
        if (contains) {
            LambdaQueryWrapper<UserMobile> wrapper = Wrappers.lambdaQuery(UserMobile.class)
                    .eq(UserMobile::getMobile, mobile);
            UserMobile userMobile = userMobileMapper.selectOne(wrapper);
            // 手机号已经注册，抛出异常
            if (Objects.nonNull(userMobile)) {
                throw new TicketFlowFrameException(BaseCode.USER_EXIST);
            }
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginDto 登录dto
     * @return 用户vo
     */
    public UserLoginVo login(UserLoginDto userLoginDto) {
        UserLoginVo userLoginVo = new UserLoginVo();
        // 从Dto中获取用户信息
        String code = userLoginDto.getCode();
        String mobile = userLoginDto.getMobile();
        String email = userLoginDto.getEmail();
        String password = userLoginDto.getPassword();
        // 非空判断
        if (StringUtil.isEmpty(mobile) && StringUtil.isEmpty(email)) {
            throw new TicketFlowFrameException(BaseCode.USER_MOBILE_AND_EMAIL_NOT_EXIST);
        }
        Long userId;
        if (StringUtil.isNotEmpty(mobile)) {
            // 登录操作是否达到了阈值
            String errorCountStr =
                    redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_MOBILE_ERROR, mobile), String.class);
            if (StringUtil.isNotEmpty(errorCountStr) && Integer.parseInt(errorCountStr) >= ERROR_COUNT_THRESHOLD) {
                throw new TicketFlowFrameException(BaseCode.MOBILE_ERROR_COUNT_TOO_MANY);
            }
            // 查询手机号
            LambdaQueryWrapper<UserMobile> queryWrapper = Wrappers.lambdaQuery(UserMobile.class)
                    .eq(UserMobile::getMobile, mobile);
            UserMobile userMobile = userMobileMapper.selectOne(queryWrapper);
            if (Objects.isNull(userMobile)) {
                // 手机号不存在，放到Redis中进行错误计数
                redisCache.incrBy(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_MOBILE_ERROR, mobile), 1);
                redisCache.expire(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_MOBILE_ERROR, mobile), 1, TimeUnit.MINUTES);
                throw new TicketFlowFrameException(BaseCode.USER_MOBILE_EMPTY);
            }
            // 如果手机号存在，就用手机号查 userId
            userId = userMobile.getUserId();
        } else {
            String errorCountStr =
                    redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_EMAIL_ERROR, email), String.class);
            if ((StringUtil.isNotEmpty(errorCountStr) && Integer.parseInt(errorCountStr) >= ERROR_COUNT_THRESHOLD)) {
                throw new TicketFlowFrameException(BaseCode.EMAIL_ERROR_COUNT_TOO_MANY);
            }
            LambdaQueryWrapper<UserEmail> wrapper = Wrappers.lambdaQuery(UserEmail.class)
                    .eq(UserEmail::getEmail, email);
            UserEmail userEmail = userEmailMapper.selectOne(wrapper);
            if (Objects.isNull(userEmail)) {
                redisCache.incrBy(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_EMAIL_ERROR, email), 1);
                redisCache.expire(RedisKeyBuild.createRedisKey(RedisKeyManage.LOGIN_USER_EMAIL_ERROR, email), 1, TimeUnit.MINUTES);
                throw new TicketFlowFrameException(BaseCode.USER_EMAIL_NOT_EXIST);
            }
            userId = userEmail.getUserId();
        }
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getId, userId).eq(User::getPassword, password);
        User user = userMapper.selectOne(wrapper);
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.NAME_PASSWORD_ERROR);
        }
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, code, user.getId()), user, tokenExpireTime, TimeUnit.MINUTES);
        userLoginVo.setUserId(userId);
        userLoginVo.setToken(createToken(user.getId(), getChannelDataByCode(code).getTokenSecret()));
        return userLoginVo;
    }

    /**
     * 用户退出登录
     *
     * @param userLogoutDto 退出登录dto
     * @return 退出成功Boolean
     */
    public Boolean logout(UserLogoutDto userLogoutDto) {
        String userStr = TokenUtil.parseToken(userLogoutDto.getToken(), getChannelDataByCode(userLogoutDto.getCode()).getTokenSecret());
        if (StringUtil.isEmpty(userStr)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        String userId = JSONObject.parseObject(userStr).getString("userId");
        redisCache.del(RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, userLogoutDto.getCode(), userId));
        return true;
    }

    /**
     * 用户更新信息
     *
     * @param userUpdateDto 更新信息dto
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateDto userUpdateDto) {
        User user = userMapper.selectById(userUpdateDto.getId());
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        User updateUser = new User();
        BeanUtil.copyProperties(userUpdateDto, updateUser);
        userMapper.updateById(updateUser);
    }

    /**
     * 用户更新密码
     *
     * @param userUpdatePasswordDto 更新密码dto
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserUpdatePasswordDto userUpdatePasswordDto) {
        User user = userMapper.selectById(userUpdatePasswordDto.getId());
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        User updateUser = new User();
        BeanUtil.copyProperties(userUpdatePasswordDto, updateUser);
        userMapper.updateById(updateUser);
    }

    /**
     * 用户更新邮箱
     *
     * @param userUpdateEmailDto 更新邮箱dto
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(UserUpdateEmailDto userUpdateEmailDto) {
        User user = userMapper.selectById(userUpdateEmailDto.getId());
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        // 更新用户表
        User updateUser = new User();
        BeanUtil.copyProperties(userUpdateEmailDto, updateUser);
        updateUser.setEmailStatus(BusinessStatus.YES.getCode());
        userMapper.updateById(updateUser);

        String oldEmail = user.getEmail();
        LambdaQueryWrapper<UserEmail> userEmailLambdaQueryWrapper = Wrappers.lambdaQuery(UserEmail.class)
                .eq(UserEmail::getEmail, userUpdateEmailDto.getEmail());
        UserEmail userEmail = userEmailMapper.selectOne(userEmailLambdaQueryWrapper);
        if (Objects.isNull(userEmail)) {
            // 查不到之前的用户邮箱关系->插入
            userEmail = new UserEmail();
            userEmail.setId(uidGenerator.getUid());
            userEmail.setUserId(user.getId());
            userEmail.setEmail(userUpdateEmailDto.getEmail());
            userEmailMapper.insert(userEmail);
        } else {
            // 查得到用户邮箱关系->更新
            LambdaUpdateWrapper<UserEmail> userEmailLambdaUpdateWrapper = Wrappers.lambdaUpdate(UserEmail.class)
                    .eq(UserEmail::getEmail, oldEmail);
            UserEmail updateUserEmail = new UserEmail();
            updateUserEmail.setEmail(userUpdateEmailDto.getEmail());
            userEmailMapper.update(updateUserEmail, userEmailLambdaUpdateWrapper);
        }
    }

    /**
     * 用户更新手机号
     *
     * @param userUpdateMobileDto 更新手机号dto
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMobile(UserUpdateMobileDto userUpdateMobileDto) {
        User user = userMapper.selectById(userUpdateMobileDto.getId());
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        String oldMobile = user.getMobile();
        User updateUser = new User();
        BeanUtil.copyProperties(userUpdateMobileDto, updateUser);
        userMapper.updateById(updateUser);
        LambdaQueryWrapper<UserMobile> userMobileLambdaQueryWrapper = Wrappers.lambdaQuery(UserMobile.class)
                .eq(UserMobile::getMobile, userUpdateMobileDto.getMobile());
        UserMobile userMobile = userMobileMapper.selectOne(userMobileLambdaQueryWrapper);
        if (Objects.isNull(userMobile)) {
            userMobile = new UserMobile();
            userMobile.setId(uidGenerator.getUid());
            userMobile.setUserId(user.getId());
            userMobile.setMobile(userUpdateMobileDto.getMobile());
            userMobileMapper.insert(userMobile);
        } else {
            LambdaUpdateWrapper<UserMobile> userMobileLambdaUpdateWrapper = Wrappers.lambdaUpdate(UserMobile.class)
                    .eq(UserMobile::getMobile, oldMobile);
            UserMobile updateUserMobile = new UserMobile();
            updateUserMobile.setMobile(userUpdateMobileDto.getMobile());
            userMobileMapper.update(updateUserMobile, userMobileLambdaUpdateWrapper);
        }
    }

    /**
     * 实名认证
     *
     * @param userAuthenticationDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void authentication(UserAuthenticationDto userAuthenticationDto) {
        User user = userMapper.selectById(userAuthenticationDto.getId());
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        if (Objects.equals(user.getRelAuthenticationStatus(), BusinessStatus.YES.getCode())) {
            throw new TicketFlowFrameException(BaseCode.USER_AUTHENTICATION);
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setRelName(userAuthenticationDto.getRelName());
        updateUser.setIdNumber(userAuthenticationDto.getIdNumber());
        updateUser.setRelAuthenticationStatus(BusinessStatus.YES.getCode());
        userMapper.updateById(updateUser);
    }

    /**
     * 获取当前用户和购票人信息
     *
     * @param userGetAndTicketUserListDto dto-当前用户id
     * @return 当前用户vo和购票人vo
     */
    public UserGetAndTicketUserListVo getUserAndTicketUserList(UserGetAndTicketUserListDto userGetAndTicketUserListDto) {
        UserIdDto userIdDto = new UserIdDto();
        userIdDto.setId(userGetAndTicketUserListDto.getUserId());
        UserVo userVo = getById(userIdDto);

        LambdaQueryWrapper<TicketUser> wrapper = Wrappers.lambdaQuery(TicketUser.class)
                .eq(TicketUser::getUserId, userGetAndTicketUserListDto.getUserId());
        List<TicketUser> ticketUserList = ticketUserMapper.selectList(wrapper);
        List<TicketUserVo> ticketUserVoList = BeanUtil.copyToList(ticketUserList, TicketUserVo.class);
        UserGetAndTicketUserListVo userGetAndTicketUserListVo = new UserGetAndTicketUserListVo();
        userGetAndTicketUserListVo.setTicketUserVoList(ticketUserVoList);
        userGetAndTicketUserListVo.setUserVo(userVo);
        return userGetAndTicketUserListVo;
    }

    /**
     * 通过手机号获取用户信息
     *
     * @param userMobileDto 手机号dto
     * @return 用户vo
     */
    public UserVo getByMobile(UserMobileDto userMobileDto) {
        LambdaQueryWrapper<UserMobile> queryWrapper = Wrappers.lambdaQuery(UserMobile.class)
                .eq(UserMobile::getMobile, userMobileDto.getMobile());
        UserMobile userMobile = userMobileMapper.selectOne(queryWrapper);
        if (Objects.isNull(userMobile)) {
            throw new TicketFlowFrameException(BaseCode.USER_MOBILE_EMPTY);
        }
        User user = userMapper.selectById(userMobile.getUserId());
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        userVo.setMobile(userMobile.getMobile());
        return userVo;
    }

    /**
     * 通过id获取用户信息
     *
     * @param userIdDto 用户id dto
     * @return 用户vo
     */
    public UserVo getById(UserIdDto userIdDto) {
        User user = userMapper.selectById(userIdDto.getId());
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        return userVo;
    }


    /**
     * 获取所有手机号-布隆过滤器初始化
     *
     * @return 所有用户手机号
     */
    public List<String> getAllMobile() {
        QueryWrapper<User> lambdaQueryWrapper = Wrappers.emptyWrapper();
        List<User> users = userMapper.selectList(lambdaQueryWrapper);
        return users.stream().map(User::getMobile).collect(Collectors.toList());
    }

    private String createToken(Long userId, String tokenSecret) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("userId", userId);
        return TokenUtil.createToken(String.valueOf(uidGenerator.getUid()),
                JSON.toJSONString(map),
                tokenExpireTime * 60 * 1000,
                tokenSecret);
    }

    private GetChannelDataVo getChannelDataByCode(String code) {
        GetChannelDataVo channelDataVo = getChannelDataByRedis(code);
        if (Objects.isNull(channelDataVo)) {
            channelDataVo = getChannelDataByClient(code);
        }
        return channelDataVo;
    }

    private GetChannelDataVo getChannelDataByClient(String code) {
        GetChannelDataByCodeDto getChannelDataByCodeDto = new GetChannelDataByCodeDto();
        getChannelDataByCodeDto.setCode(code);
        ApiResponse<GetChannelDataVo> response = baseDataClient.getByCode(getChannelDataByCodeDto);
        if (Objects.equals(response.getCode(), BaseCode.SUCCESS.getCode())) {
            return response.getData();
        }
        throw new TicketFlowFrameException("没有找到ChannelData");
    }

    private GetChannelDataVo getChannelDataByRedis(String code) {
        return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.CHANNEL_DATA, code), GetChannelDataVo.class);
    }


}
