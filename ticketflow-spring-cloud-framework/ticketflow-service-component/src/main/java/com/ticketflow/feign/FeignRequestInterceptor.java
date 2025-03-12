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

@Slf4j
@AllArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {

    private final String serverGray;
    private final HeaderCacheService headerCacheService;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (Objects.nonNull(requestAttributes)) {
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
                HttpServletRequest request = servletRequestAttributes.getRequest();
                String traceId = headerCacheService.getHeaderValue(request, TRACE_ID);
                String code = headerCacheService.getHeaderValue(request, CODE);
                String gray = headerCacheService.getHeaderValue(request, GRAY_PARAMETER);
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
