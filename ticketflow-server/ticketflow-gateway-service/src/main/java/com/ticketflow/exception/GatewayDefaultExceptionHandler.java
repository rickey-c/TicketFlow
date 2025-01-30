package com.ticketflow.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.conf.RequestTemporaryWrapper;
import com.ticketflow.enums.BaseCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Description: 自定义异常
 * @Author: rickey-c
 * @Date: 2025/1/30 20:05
 */
@Slf4j
public class GatewayDefaultExceptionHandler implements ErrorWebExceptionHandler {
    
    @NotNull
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, @NotNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        boolean exceptionFlag = false;
        RequestTemporaryWrapper requestTemporaryWrapper = new RequestTemporaryWrapper();
        if (ex instanceof ResponseStatusException responseStatusException) {
            if (responseStatusException.getStatusCode() == HttpStatus.NOT_FOUND) {
                String path = exchange.getRequest().getPath().value();
                String methodValue = exchange.getRequest().getMethod().name();
                ApiResponse.error(BaseCode.NOT_FOUND.getCode(), String.format(BaseCode.NOT_FOUND.getMsg(), methodValue, path));
            }
        } else if (ex instanceof TicketFlowFrameException TicketFlowFrameException) {
            ApiResponse<String> apiResponse = ApiResponse.error(TicketFlowFrameException.getCode(), TicketFlowFrameException.getMessage());
            requestTemporaryWrapper.setApiResponse(apiResponse);
            exceptionFlag = true;
        } else if (ex instanceof ArgumentException ae) {
            ApiResponse<Object> apiResponse = ApiResponse.error(ae.getCode(), ae.getMessage());
            apiResponse.setData(ae.getArgumentErrorList());
            requestTemporaryWrapper.setApiResponse(apiResponse);
            exceptionFlag = true;
        } else if (ex instanceof Exception) {
            ApiResponse<String> apiResponse = ApiResponse.error(-100, "网络异常!");
            requestTemporaryWrapper.setApiResponse(apiResponse);
            exceptionFlag = true;
        }
        if (exceptionFlag) {
            response.setStatusCode(HttpStatus.OK);
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response
                .writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        //设置响应到response的数据
                        return bufferFactory.wrap(objectMapper.writeValueAsBytes(requestTemporaryWrapper.getApiResponse()));
                    } catch (JsonProcessingException e) {
                        log.error("response error", e);
                        return null;
                    }
                }));
    }
}
