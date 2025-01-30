package com.ticketflow.service;

import com.ticketflow.client.BaseDataClient;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.core.RedisKeyManage;
import com.ticketflow.dto.GetChannelDataByCodeDto;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.ArgumentError;
import com.ticketflow.exception.ArgumentException;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.redis.RedisCache;
import com.ticketflow.redis.RedisKeyBuild;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.vo.GetChannelDataVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.*;

import static com.ticketflow.constant.GatewayConstant.CODE;

/**
 * @Description: 渠道数据获取
 * @Author: rickey-c
 * @Date: 2025/1/29 20:50
 */
@Service
@Slf4j
public class ChannelDataService {

    private final static String EXCEPTION_MESSAGE = "code参数为空";

    @Lazy
    @Autowired
    private BaseDataClient baseDataClient;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public void checkCode(String code) {
        if (StringUtil.isNotEmpty(code)) {
            ArgumentError argumentError = new ArgumentError();
            argumentError.setArgumentName(CODE);
            argumentError.setMessage(EXCEPTION_MESSAGE);
            ArrayList<ArgumentError> argumentErrors = new ArrayList<>();
            argumentErrors.add(argumentError);
            throw new ArgumentException(BaseCode.ARGUMENT_EMPTY.getCode(), argumentErrors);
        }
    }

    public GetChannelDataVo getChannelDataByCode(String code) {
        checkCode(code);
        GetChannelDataVo channelDataVo = getChannelDataByRedis(code);
        if ((Objects.isNull(channelDataVo))) {
            channelDataVo = getChannelDataVoByClient(code);
            setChannelDataRedis(code, channelDataVo);
        }
        return channelDataVo;
    }


    public GetChannelDataVo getChannelDataByRedis(String code) {
        return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.CHANNEL_DATA, code), GetChannelDataVo.class);
    }

    private void setChannelDataRedis(String code, GetChannelDataVo channelDataVo) {
        redisCache.set(RedisKeyBuild.createRedisKey(RedisKeyManage.CHANNEL_DATA, code), channelDataVo);
    }

    private GetChannelDataVo getChannelDataVoByClient(String code) {
        GetChannelDataByCodeDto getChannelDataByCodeDto = new GetChannelDataByCodeDto();
        getChannelDataByCodeDto.setCode(code);
        Future<ApiResponse<GetChannelDataVo>> future =
                threadPoolExecutor.submit(() -> baseDataClient.getByCode(getChannelDataByCodeDto));
        try {
            ApiResponse<GetChannelDataVo> apiResponse = future.get(10, TimeUnit.SECONDS);
            if (Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
                return apiResponse.getData();
            }
        } catch (InterruptedException e) {
            log.error("baseDataClient getByCode Interrupted", e);
            throw new TicketFlowFrameException(BaseCode.THREAD_INTERRUPTED);
        } catch (ExecutionException e) {
            log.error("baseDataClient getByCode execution exception", e);
            throw new TicketFlowFrameException(BaseCode.SYSTEM_ERROR);
        } catch (TimeoutException e) {
            log.error("baseDataClient getByCode timeout exception", e);
            throw new TicketFlowFrameException(BaseCode.EXECUTE_TIME_OUT);
        }
        throw new TicketFlowFrameException(BaseCode.CHANNEL_DATA_NOT_EXIST);
    }

}
