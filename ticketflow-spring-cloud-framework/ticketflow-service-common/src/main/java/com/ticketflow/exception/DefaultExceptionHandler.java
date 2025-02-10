package com.ticketflow.exception;


import com.ticketflow.common.ApiResponse;
import com.ticketflow.enums.BaseCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 异常处理器
 * @Author: rickey-c
 * @Date: 2025/1/24 16:55
 */
@Slf4j
@RestControllerAdvice
public class DefaultExceptionHandler {

    /**
     * 业务异常
     *
     * @param request
     * @param ticketFlowFrameException
     * @return
     */
    @ExceptionHandler(value = TicketFlowFrameException.class)
    public ApiResponse<String> toolkitExceptionHandler(HttpServletRequest request, TicketFlowFrameException ticketFlowFrameException) {
        log.error("业务异常 method : {} url : {} query : {} ", request.getMethod(), getRequestUrl(request), getRequestQuery(request), ticketFlowFrameException);
        return ApiResponse.error(ticketFlowFrameException.getCode(), ticketFlowFrameException.getMessage());
    }

    /**
     * 参数校验异常
     *
     * @param request
     * @param ex
     * @return
     */
    @SneakyThrows
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResponse<List<ArgumentError>> validExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException ex) {
        log.error("参数验证异常 method : {} url : {} query : {} ", request.getMethod(), getRequestUrl(request), getRequestQuery(request), ex);
        BindingResult bindingResult = ex.getBindingResult();
        List<ArgumentError> argumentErrorList =
                bindingResult.getFieldErrors()
                        .stream()
                        .map(fieldError -> {
                            ArgumentError argumentError = new ArgumentError();
                            argumentError.setArgumentName(fieldError.getField());
                            argumentError.setMessage(fieldError.getDefaultMessage());
                            return argumentError;
                        }).collect(Collectors.toList());
        return ApiResponse.error(BaseCode.PARAMETER_ERROR.getCode(), argumentErrorList);
    }

    /**
     * 其他异常
     *
     * @param request
     * @param throwable
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public ApiResponse<String> defaultErrorHandler(HttpServletRequest request, Throwable throwable) {
        log.error("全局异常 method : {} url : {} query : {} ", request.getMethod(), getRequestUrl(request), getRequestQuery(request), throwable);
        return ApiResponse.error();
    }

    /**
     * 获取请求路径
     *
     * @param request
     * @return
     */
    private String getRequestUrl(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    /**
     * 获取请求路径拼接参数
     *
     * @param request
     * @return
     */
    private String getRequestQuery(HttpServletRequest request) {
        return request.getQueryString();
    }
}
