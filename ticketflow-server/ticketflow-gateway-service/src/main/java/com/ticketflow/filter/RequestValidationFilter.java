package com.ticketflow.filter;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.baidu.fsg.uid.UidGenerator;
import com.ticketflow.config.RequestTemporaryWrapper;
import com.ticketflow.enums.BaseCode;
import com.ticketflow.exception.ArgumentError;
import com.ticketflow.exception.ArgumentException;
import com.ticketflow.exception.TicketFlowFrameException;
import com.ticketflow.limit.RateLimiter;
import com.ticketflow.limit.RateLimiterProperty;
import com.ticketflow.property.GatewayProperty;
import com.ticketflow.service.ApiRestrictService;
import com.ticketflow.service.ChannelDataService;
import com.ticketflow.service.TokenService;
import com.ticketflow.threadlocal.BaseParameterHolder;
import com.ticketflow.utils.RsaSignTool;
import com.ticketflow.utils.RsaTool;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.vo.GetChannelDataVo;
import com.ticketflow.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

import static com.ticketflow.constant.Constant.GRAY_PARAMETER;
import static com.ticketflow.constant.Constant.TRACE_ID;
import static com.ticketflow.constant.GatewayConstant.*;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/29 20:48
 */
@Component
@Slf4j
public class RequestValidationFilter implements GlobalFilter, Ordered {

    @Autowired
    private ServerCodecConfigurer serverCodecConfigurer;

    @Autowired
    private ChannelDataService channelDataService;

    @Autowired
    private ApiRestrictService apiRestrictService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private GatewayProperty gatewayProperty;

    @Autowired
    private UidGenerator uidGenerator;

    @Autowired
    private RateLimiterProperty rateLimiterProperty;

    @Autowired
    private RateLimiter rateLimiter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (rateLimiterProperty.getRateSwitch()) {
            try {
                rateLimiter.acquire();
                return doFilter(exchange, chain);
            } catch (InterruptedException e) {
                log.error("interrupted error", e);
                throw new TicketFlowFrameException(BaseCode.THREAD_INTERRUPTED);
            } finally {
                rateLimiter.release();
            }
        } else {
            return doFilter(exchange, chain);
        }
    }

    public Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String traceId = request.getHeaders().getFirst(TRACE_ID);
        String gray = request.getHeaders().getFirst(GRAY_PARAMETER);
        String noVerify = request.getHeaders().getFirst(NO_VERIFY);
        if (StringUtil.isEmpty(traceId)) {
            traceId = String.valueOf(uidGenerator.getUid());
        }
        MDC.put(TRACE_ID, traceId);
        Map<String, String> headMap = new HashMap<>(8);
        headMap.put(TRACE_ID, traceId);
        headMap.put(GRAY_PARAMETER, gray);
        if (StringUtil.isNotEmpty(noVerify)) {
            headMap.put(NO_VERIFY, noVerify);
        }
        BaseParameterHolder.setParameter(TRACE_ID, traceId);
        BaseParameterHolder.setParameter(GRAY_PARAMETER, gray);
        MediaType contentType = request.getHeaders().getContentType();
        // json请求
        if (Objects.nonNull(contentType) && contentType.toString().toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE.toLowerCase())) {
            return readBody(exchange, chain, headMap);
        } else {
            Map<String, String> map = doExecute("", exchange);
            map.remove(REQUEST_BODY);
            map.putAll(headMap);
            request.mutate().headers(httpHeaders -> {
                map.forEach(httpHeaders::add);
            });
            return chain.filter(exchange);
        }
    }

    private Mono<Void> readBody(ServerWebExchange exchange, GatewayFilterChain chain, Map<String, String> headMap) {
        // 记录当前线程的名称，方便调试 Reactor 线程模型（通常是 Netty 线程）
        log.info("current thread readBody : {}", Thread.currentThread().getName());

        // 创建一个临时包装对象，可能用于存储请求体的中间数据
        RequestTemporaryWrapper requestTemporaryWrapper = new RequestTemporaryWrapper();

        // 使用 ServerWebExchange 创建 ServerRequest，以便从请求体中读取数据
        ServerRequest serverRequest = ServerRequest.create(exchange, serverCodecConfigurer.getReaders());

        // 读取请求体并进行处理
        Mono<String> modifiedBody = serverRequest
                .bodyToMono(String.class) // 将请求体转换为 Mono<String>
                .flatMap(originalBody ->
                        // 执行自定义逻辑（execute 方法），返回处理后的请求体
                        Mono.just(execute(requestTemporaryWrapper, originalBody, exchange))
                )
                .switchIfEmpty(Mono.defer(() ->
                        // 如果请求体为空，则传递一个空字符串，避免 null 处理异常
                        Mono.just(execute(requestTemporaryWrapper, "", exchange))
                ));

        // 创建 BodyInserter，将处理后的请求体转换为响应式数据流
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);

        // 复制原始请求的 Header 并删除 Content-Length，避免请求体修改后出现不匹配的 Content-Length
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        headers.remove(HttpHeaders.CONTENT_LENGTH);

        // 创建 CachedBodyOutputMessage 用于存储修改后的请求体
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);

        // 将修改后的请求体写入 outputMessage，并继续执行过滤器链
        // 捕获异常并返回错误信息，避免请求中断
        return bodyInserter
                .insert(outputMessage, new BodyInserterContext()) // 插入修改后的请求体
                .then(Mono.defer(() ->
                        // 替换原请求对象，使用新的 Header 和请求体，然后继续执行过滤器链
                        chain.filter(
                                exchange.mutate().request(
                                        decorateHead(exchange, headers, outputMessage, requestTemporaryWrapper, headMap)
                                ).build()
                        )
                ))
                .onErrorResume((Function<Throwable, Mono<Void>>) Mono::error
                );
    }

    public String execute(RequestTemporaryWrapper requestTemporaryWrapper, String requestBody, ServerWebExchange exchange) {
        // 进行业务验证，并将相关参数放入map
        Map<String, String> map = doExecute(requestBody, exchange);
        String body = map.get(REQUEST_BODY);
        map.remove(REQUEST_BODY);
        requestTemporaryWrapper.setMap(map);
        return body;
    }

    private Map<String, String> doExecute(String originalBody, ServerWebExchange exchange) {
        log.info("current thread verify: {}", Thread.currentThread().getName());
        ServerHttpRequest request = exchange.getRequest();
        String requestBody = originalBody;
        Map<String, String> bodyContent = new HashMap<>(32);
        if (StringUtil.isNotEmpty(originalBody)) {
            bodyContent = JSON.parseObject(originalBody, Map.class);
        }
        String code = null;
        String token;
        String userId = null;
        String url = request.getPath().value();
        String noVerify = request.getHeaders().getFirst(NO_VERIFY);
        boolean allowNormalAccess = gatewayProperty.isAllowNormalAccess();
        if ((!allowNormalAccess) && (VERIFY_VALUE.equals(noVerify))) {
            throw new TicketFlowFrameException(BaseCode.ONLY_SIGNATURE_ACCESS_IS_ALLOWED);
        }
        if (checkParameter(originalBody, noVerify) && !skipCheckParameter(url)) {

            String encrypt = request.getHeaders().getFirst(ENCRYPT);
            //应用渠道
            code = bodyContent.get(CODE);
            //token
            token = request.getHeaders().getFirst(TOKEN);

            GetChannelDataVo channelDataVo = channelDataService.getChannelDataByCode(code);

            if (StringUtil.isNotEmpty(encrypt) && V2.equals(encrypt)) {
                String decrypt = RsaTool.decrypt(bodyContent.get(BUSINESS_BODY), channelDataVo.getDataSecretKey());
                bodyContent.put(BUSINESS_BODY, decrypt);
            }
            boolean checkFlag = RsaSignTool.verifyRsaSign256(bodyContent, channelDataVo.getSignPublicKey());
            if (!checkFlag) {
                throw new TicketFlowFrameException(BaseCode.RSA_SIGN_ERROR);
            }

            boolean skipCheckTokenResult = skipCheckToken(url);
            if (!skipCheckTokenResult && StringUtil.isEmpty(token)) {
                ArgumentError argumentError = new ArgumentError();
                argumentError.setArgumentName(token);
                argumentError.setMessage("token参数为空");
                List<ArgumentError> argumentErrorList = new ArrayList<>();
                argumentErrorList.add(argumentError);
                throw new ArgumentException(BaseCode.ARGUMENT_EMPTY.getCode(), argumentErrorList);
            }

            if (!skipCheckTokenResult) {
                UserVo userVo = tokenService.getUser(token, code, channelDataVo.getTokenSecret());
                userId = userVo.getId();
            }

            if (StringUtil.isEmpty(userId) && checkNeedUserId(url) && StringUtil.isNotEmpty(token)) {
                UserVo userVo = tokenService.getUser(token, code, channelDataVo.getTokenSecret());
                userId = userVo.getId();
            }

            requestBody = bodyContent.get(BUSINESS_BODY);
        }
        // 限流检测
        apiRestrictService.apiRestrict(userId, url, request);
        Map<String, String> map = new HashMap<>(4);
        map.put(REQUEST_BODY, requestBody);
        if (StringUtil.isNotEmpty(code)) {
            map.put(CODE, code);
        }
        if (StringUtil.isNotEmpty(userId)) {
            map.put(USER_ID, userId);
        }
        return map;
    }

    /**
     * 向后续服务传递参数
     *
     * @param exchange
     * @param headers
     * @param outputMessage
     * @param requestTemporaryWrapper
     * @param headMap
     * @return
     */
    private ServerHttpRequestDecorator decorateHead(ServerWebExchange exchange, HttpHeaders headers, CachedBodyOutputMessage outputMessage, RequestTemporaryWrapper requestTemporaryWrapper, Map<String, String> headMap) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                log.info("current thread getHeaders: {}", Thread.currentThread().getName());
                long contentLength = headers.getContentLength();
                HttpHeaders newHeaders = new HttpHeaders();
                newHeaders.putAll(headers);
                Map<String, String> map = requestTemporaryWrapper.getMap();
                if (CollectionUtil.isNotEmpty(map)) {
                    newHeaders.setAll(map);
                }
                if (CollectionUtil.isNotEmpty(headMap)) {
                    newHeaders.setAll(headMap);
                }
                if (contentLength > 0) {
                    newHeaders.setContentLength(contentLength);
                } else {
                    newHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                if (CollectionUtil.isNotEmpty(headMap) && StringUtil.isNotEmpty(headMap.get(TRACE_ID))) {
                    MDC.put(TRACE_ID, headMap.get(TRACE_ID));
                }
                return newHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }

    public boolean checkParameter(String originalBody, String noVerify) {
        return (!(VERIFY_VALUE.equals(noVerify))) && StringUtil.isNotEmpty(originalBody);
    }

    public boolean skipCheckParameter(String url) {
        for (String skipCheckTokenPath : gatewayProperty.getCheckSkipParmeterPaths()) {
            PathMatcher matcher = new AntPathMatcher();
            if (matcher.match(skipCheckTokenPath, url)) {
                return true;
            }
        }
        return false;
    }

    public boolean skipCheckToken(String url) {
        for (String skipCheckTokenPath : gatewayProperty.getCheckTokenPaths()) {
            PathMatcher matcher = new AntPathMatcher();
            if (matcher.match(skipCheckTokenPath, url)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkNeedUserId(String url) {
        for (String userIdPath : gatewayProperty.getUserIdPaths()) {
            PathMatcher matcher = new AntPathMatcher();
            if (matcher.match(userIdPath, url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
