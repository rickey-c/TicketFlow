package com.ticketflow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.ParentProgramCategoryDto;
import com.ticketflow.dto.ProgramCategoryAddDto;
import com.ticketflow.dto.ProgramCategoryDto;
import com.ticketflow.entity.ProgramCategory;
import com.ticketflow.mapper.ProgramCategoryMapper;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.servicelock.LockType;
import com.ticketflow.servicelock.annotion.ServiceLock;
import com.ticketflow.vo.ProgramCategoryVo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ticketflow.core.DistributedLockConstants.PROGRAM_CATEGORY_LOCK;

/**
 * @Description: 节目类型 service
 * @Author: rickey-c
 * @Date: 2025/2/3 20:55
 */
@Service
public class ProgramCategoryService extends ServiceImpl<ProgramCategoryMapper, ProgramCategory> {

    @Autowired
    private ProgramCategoryMapper programCategoryMapper;

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询所有节目分类
     *
     * @return 节目分类 vo
     */
    public List<ProgramCategoryVo> selectAll() {
        QueryWrapper<ProgramCategory> wrapper = Wrappers.emptyWrapper();
        List<ProgramCategory> programCategories = programCategoryMapper.selectList(wrapper);
        return BeanUtil.copyToList(programCategories, ProgramCategoryVo.class);
    }

    /**
     * 通过类型查找节目类型
     *
     * @param programCategoryDto 节目类型 dto
     * @return 节目类型 vo
     */
    public List<ProgramCategoryVo> selectByType(@NotNull ProgramCategoryDto programCategoryDto) {
        LambdaQueryWrapper<ProgramCategory> wrapper = Wrappers.lambdaQuery(ProgramCategory.class)
                .eq(ProgramCategory::getType, programCategoryDto.getType());
        List<ProgramCategory> programCategories = programCategoryMapper.selectList(wrapper);
        return BeanUtil.copyToList(programCategories, ProgramCategoryVo.class);
    }

    /**
     * 通过父节目类型查找节目类型
     *
     * @param parentProgramCategoryDto 父节目类型dto
     * @return 节目类型vo
     */
    public List<ProgramCategoryVo> selectByParentProgramCategoryId(@NotNull ParentProgramCategoryDto parentProgramCategoryDto) {
        LambdaQueryWrapper<ProgramCategory> wrapper = Wrappers.lambdaQuery(ProgramCategory.class)
                .eq(ProgramCategory::getParentId, parentProgramCategoryDto.getParentProgramCategoryId());
        List<ProgramCategory> programCategories = programCategoryMapper.selectList(wrapper);
        return BeanUtil.copyToList(programCategories, ProgramCategoryVo.class);
    }

    /**
     * 批量添加票档
     *
     * @param programCategoryAddDtoList 添加票档集合
     */
    @Transactional(rollbackFor = Exception.class)
    @ServiceLock(lockType = LockType.Write, name = PROGRAM_CATEGORY_LOCK, keys = {"#all"})
    public void saveBatch(final List<ProgramCategoryAddDto> programCategoryAddDtoList) {

        List<ProgramCategory> programCategoryList = programCategoryAddDtoList.stream().map(programCategoryAddDto -> {
            ProgramCategory programCategory = new ProgramCategory();
            BeanUtil.copyProperties(programCategoryAddDto, programCategory);
            programCategory.setId(uidGenerator.getUid());
            return programCategory;
        }).collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(programCategoryList)) {
            this.saveBatch(programCategoryList);
            Map<String, ProgramCategory> programCategoryMap = programCategoryList.stream().collect(
                    Collectors.toMap(p -> String.valueOf(p.getId()), p -> p, (v1, v2) -> v2));
            redisCache.putHash(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_CATEGORY_HASH), programCategoryMap);
        }
    }

    /**
     * 根据节目id获取节目类型
     *
     * @param programCategoryId 节目id
     * @return 节目类型
     */
    public ProgramCategory getProgramCategory(Long programCategoryId) {
        ProgramCategory programCategory = redisCache.getForHash(
                RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_CATEGORY_HASH), String.valueOf(programCategoryId), ProgramCategory.class);
        if (Objects.isNull(programCategory)) {
            Map<String, ProgramCategory> programCategoryMap = programCategoryRedisInit();
            programCategory = programCategoryMap.get(String.valueOf(programCategoryId));
        }
        return programCategory;
    }

    /**
     * 加载节目类型信息到redis
     *
     * @return 节目类型 map
     */
    @ServiceLock(lockType = LockType.Write, name = PROGRAM_CATEGORY_LOCK, keys = {"#all"})
    public Map<String, ProgramCategory> programCategoryRedisInit() {
        Map<String, ProgramCategory> programCategoryMap = new HashMap<>(64);
        QueryWrapper<ProgramCategory> wrapper = Wrappers.emptyWrapper();
        List<ProgramCategory> programCategories = programCategoryMapper.selectList(wrapper);
        if (CollectionUtil.isNotEmpty(programCategories)) {
            programCategoryMap = programCategories.stream().collect(
                    Collectors.toMap(p -> String.valueOf(p.getId()), p -> p, (v1, v2) -> v2));
            redisCache.putHash(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_CATEGORY_HASH), programCategoryMap);
        }
        return programCategoryMap;
    }

}
