package com.ticketflow.feign;

import com.ticketflow.utils.StringUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

import static com.ticketflow.constant.Constant.*;

/**
 * @Description: 定制feign参数传递
 * @Author: rickey-c
 * @Date: 2025/1/25 16:50
 */
@Slf4j
@AllArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {

    private final String serverGray;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (Objects.nonNull(requestAttributes)) {
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
                HttpServletRequest request = servletRequestAttributes.getRequest();
                String traceId = request.getHeader(TRACE_ID);
                String code = request.getHeader(CODE);
                String gray = request.getHeader(GRAY_PARAMETER);
                if (StringUtil.isNotEmpty(gray)) {
                    gray = serverGray;
                }
                requestTemplate.header(TRACE_ID, traceId);
                requestTemplate.header(CODE, code);
                requestTemplate.header(GRAY_PARAMETER, gray);
            }
        } catch (Exception e) {
            log.error("FeignRequestInterceptor apply error", e);
        }
    }
}
