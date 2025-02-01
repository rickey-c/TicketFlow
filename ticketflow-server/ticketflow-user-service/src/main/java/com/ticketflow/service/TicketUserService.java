package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.TicketUserDto;
import com.ticketflow.dto.TicketUserIdDto;
import com.ticketflow.dto.TicketUserListDto;
import com.ticketflow.entity.TicketUser;
import com.ticketflow.entity.User;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.mapper.TicketUserMapper;
import com.ticketflow.mapper.UserMapper;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.vo.TicketUserVo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @Description: 购票人 service
 * @Author: rickey-c
 * @Date: 2025/2/1 15:25
 */
@Service
public class TicketUserService extends ServiceImpl<TicketUserMapper, TicketUser> {

    @Autowired
    private TicketUserMapper ticketUserMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询购票人列表
     *
     * @param ticketUserListDto 购票人dto
     * @return 购票人列表vo
     */
    public List<TicketUserVo> list(@NotNull TicketUserListDto ticketUserListDto) {
        //先从缓存中查询
        List<TicketUserVo> ticketUserVoList = redisCache.getValueIsList(RedisKeyBuild.createRedisKey(
                RedisKeyManage.TICKET_USER_LIST, ticketUserListDto.getUserId()), TicketUserVo.class);
        if (CollectionUtil.isNotEmpty(ticketUserVoList)) {
            return ticketUserVoList;
        }
        LambdaQueryWrapper<TicketUser> ticketUserLambdaQueryWrapper = Wrappers.lambdaQuery(TicketUser.class)
                .eq(TicketUser::getUserId, ticketUserListDto.getUserId());
        List<TicketUser> ticketUsers = ticketUserMapper.selectList(ticketUserLambdaQueryWrapper);
        return BeanUtil.copyToList(ticketUsers, TicketUserVo.class);
    }

    /**
     * 添加购票人
     *
     * @param ticketUserDto 购票人dto
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(@NotNull TicketUserDto ticketUserDto) {
        User user = userMapper.selectById(ticketUserDto.getUserId());
        if (Objects.isNull(user)) {
            throw new TicketFlowFrameException(BaseCode.USER_EMPTY);
        }
        LambdaQueryWrapper<TicketUser> ticketUserLambdaQueryWrapper = Wrappers.lambdaQuery(TicketUser.class)
                .eq(TicketUser::getUserId, ticketUserDto.getUserId())
                .eq(TicketUser::getIdType, ticketUserDto.getIdType())
                .eq(TicketUser::getIdNumber, ticketUserDto.getIdNumber());
        TicketUser ticketUser = ticketUserMapper.selectOne(ticketUserLambdaQueryWrapper);
        if (Objects.nonNull(ticketUser)) {
            throw new TicketFlowFrameException(BaseCode.TICKET_USER_EXIST);
        }
        TicketUser addTicketUser = new TicketUser();
        BeanUtil.copyProperties(ticketUserDto, addTicketUser);
        addTicketUser.setId(uidGenerator.getUid());
        ticketUserMapper.insert(addTicketUser);
        // 删除缓存
        delTicketUserVoListCache(String.valueOf(ticketUserDto.getUserId()));
    }

    /**
     * 删除购票人信息
     *
     * @param ticketUserIdDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(TicketUserIdDto ticketUserIdDto) {
        TicketUser ticketUser = ticketUserMapper.selectById(ticketUserIdDto.getId());
        if (Objects.isNull(ticketUser)) {
            throw new TicketFlowFrameException(BaseCode.TICKET_USER_EMPTY);
        }
        ticketUserMapper.deleteById(ticketUserIdDto.getId());
        delTicketUserVoListCache(String.valueOf(ticketUser.getUserId()));
    }

    public void delTicketUserVoListCache(String userId) {
        redisCache.del(RedisKeyBuild.createRedisKey(RedisKeyManage.TICKET_USER_LIST, userId));
    }
}
