package com.ticketflow.service.composite.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.ticketflow.client.OrderClient;
import com.ticketflow.client.UserClient;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.AccountOrderCountDto;
import com.ticketflow.dto.ProgramGetDto;
import com.ticketflow.dto.ProgramOrderCreateDto;
import com.ticketflow.dto.TicketUserListDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.service.ProgramService;
import com.ticketflow.service.composite.AbstractProgramCheckHandler;
import com.ticketflow.service.tool.TokenExpireManager;
import com.ticketflow.vo.AccountOrderCountVo;
import com.ticketflow.vo.ProgramVo;
import com.ticketflow.vo.TicketUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: 用户检查
 * @Author: rickey-c
 * @Date: 2025/2/9 00:21
 */
@Slf4j
@Component
public class ProgramUserExistCheckHandler extends AbstractProgramCheckHandler {

    @Autowired
    private UserClient userClient;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private ProgramService programService;

    @Autowired
    private TokenExpireManager tokenExpireManager;

    /**
     * 验证每个用户最多能持有多少订单
     *
     * @param programOrderCreateDto 泛型参数，用于业务执行。
     */
    @Override
    protected void execute(ProgramOrderCreateDto programOrderCreateDto) {
        // 查询用户购票人信息
        List<TicketUserVo> ticketUserVoList = redisCache.getValueIsList(RedisKeyBuild.createRedisKey(
                RedisKeyManage.TICKET_USER_LIST, programOrderCreateDto.getUserId()), TicketUserVo.class);
        if (CollectionUtil.isEmpty(ticketUserVoList)) {
            TicketUserListDto ticketUserListDto = new TicketUserListDto();
            ticketUserListDto.setUserId(programOrderCreateDto.getUserId());
            ApiResponse<List<TicketUserVo>> apiResponse = userClient.list(ticketUserListDto);
            if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                ticketUserVoList = apiResponse.getData();
            } else {
                log.error("user client rpc getUserAndTicketUserList select response : {}", JSON.toJSONString(apiResponse));
                throw new TicketFlowFrameException(apiResponse);
            }
        }
        // 没有购票人，则抛出异常
        if (CollectionUtil.isEmpty(ticketUserVoList)) {
            throw new TicketFlowFrameException(BaseCode.TICKET_USER_EMPTY);
        }
        Map<Long, TicketUserVo> ticketUserVoMap = ticketUserVoList.stream()
                .collect(Collectors.toMap(TicketUserVo::getId, ticketUserVo -> ticketUserVo, (v1, v2) -> v2));
        // 购票人是不是真实存在的
        for (Long ticketUserId : programOrderCreateDto.getTicketUserIdList()) {
            if (Objects.isNull(ticketUserVoMap.get(ticketUserId))) {
                throw new TicketFlowFrameException(BaseCode.TICKET_USER_EMPTY);
            }
        }
        // 查询节目信息获取每个节目的限制条件
        ProgramGetDto programGetDto = new ProgramGetDto();
        programGetDto.setId(programOrderCreateDto.getProgramId());
        ProgramVo programVo = programService.detail(programGetDto);
        if (Objects.isNull(programVo)) {
            throw new TicketFlowFrameException(BaseCode.PROGRAM_NOT_EXIST);
        }
        Integer count = 0;
        // 查询当前用户买的数量
        if (redisCache.hasKey(RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT,
                programOrderCreateDto.getUserId(), programOrderCreateDto.getProgramId()))) {
            count = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT,
                    programOrderCreateDto.getUserId(), programOrderCreateDto.getProgramId()), Integer.class);
        } else {
            AccountOrderCountDto accountOrderCountDto = new AccountOrderCountDto();
            accountOrderCountDto.setUserId(programOrderCreateDto.getUserId());
            accountOrderCountDto.setProgramId(programOrderCreateDto.getProgramId());
            ApiResponse<AccountOrderCountVo> apiResponse = orderClient.accountOrderCount(accountOrderCountDto);
            // 放入缓存
            if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                count = Optional.ofNullable(apiResponse.getData()).map(AccountOrderCountVo::getCount).orElse(0);
                redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.ACCOUNT_ORDER_COUNT,
                                programOrderCreateDto.getUserId(),
                                programOrderCreateDto.getProgramId()),
                        count, tokenExpireManager.getTokenExpireTime() + 1, TimeUnit.MINUTES);
            }
        }

        Integer seatCount = Optional.ofNullable(programOrderCreateDto.getSeatDtoList()).map(List::size).orElse(0);

        Integer ticketCount = Optional.ofNullable(programOrderCreateDto.getTicketCount()).orElse(0);
        // 针对手动选座判断数量是否超额
        if (seatCount != 0) {
            count = count + seatCount;
        } else if (ticketCount != 0) {
            // 针对自动选择进行判断
            count = count + ticketCount;
        }
        // 超额，则抛出异常
        if (count > programVo.getPerAccountLimitPurchaseCount()) {
            throw new TicketFlowFrameException(BaseCode.PER_ACCOUNT_PURCHASE_COUNT_OVER_LIMIT);
        }
    }

    @Override
    public Integer executeParentOrder() {
        return 1;
    }

    @Override
    public Integer executeTier() {
        return 2;
    }

    @Override
    public Integer executeOrder() {
        return 2;
    }
}
