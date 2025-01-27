package com.ticketflow.service;


import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.ChannelDataAddDto;
import com.ticketflow.dto.GetChannelDataByCodeDto;
import com.ticketflow.entity.ChannelTableData;
import com.ticketflow.enums.Status;
import com.ticketflow.mapper.ChannelDataMapper;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.utils.DateUtils;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.vo.GetChannelDataVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @Description: 渠道 Service
 * @Author: rickey-c
 * @Date: 2025/1/27 21:07
 */
@Slf4j
@Service
public class ChannelDataService {

    @Autowired
    private ChannelDataMapper channelDataMapper;

    @Autowired
    private UidGenerator uidGenerator;
    
    @Autowired
    private RedisCache redisCache;

    /**
     * 通过code获取渠道数据，pc、小程序等，主要是用于参数验证
     *
     * @param dto
     * @return
     */
    public GetChannelDataVo getByCode(GetChannelDataByCodeDto dto) {
        GetChannelDataVo getChannelDataVo = new GetChannelDataVo();
        LambdaQueryWrapper<ChannelTableData> wrapper = Wrappers.lambdaQuery(ChannelTableData.class)
                .eq(ChannelTableData::getStatus, Status.RUN.getCode())
                .eq(ChannelTableData::getCode, dto.getCode());
        Optional.ofNullable(channelDataMapper.selectOne(wrapper)).ifPresent(channelData -> {
            BeanUtils.copyProperties(channelData, getChannelDataVo);
        });
        return getChannelDataVo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(ChannelDataAddDto channelDataAddDto) {
        ChannelTableData channelData = new ChannelTableData();
        BeanUtils.copyProperties(channelDataAddDto, channelData);
        channelData.setId(uidGenerator.getUid());
        channelData.setCreateTime(DateUtils.now());
        channelDataMapper.insert(channelData);
        addRedisChannelData(channelData);
    }

    private void addRedisChannelData(ChannelTableData channelData) {
        GetChannelDataVo getChannelDataVo = new GetChannelDataVo();
        BeanUtils.copyProperties(channelData, getChannelDataVo);
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.CHANNEL_DATA, getChannelDataVo.getCode()), getChannelDataVo);
    }
}
