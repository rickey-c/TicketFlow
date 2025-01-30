package com.ticketflow.filter;

import com.alibaba.fastjson.JSON;
import com.ticketflow.common.ApiResponse;
import com.ticketflow.service.ChannelDataService;
import com.ticketflow.utils.RsaTool;
import com.ticketflow.utils.StringUtil;
import com.ticketflow.vo.GetChannelDataVo;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.BiFunction;

import static com.ticketflow.constant.GatewayConstant.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

/**
 * @Description:
 * @Author: rickey-c
 * @Date: 2025/1/30 20:18
 */
@Slf4j
@Component
public class ResponseValidationFilter implements GlobalFilter, Ordered {

    @Value("${aes.vector:default}")
    private String aesVector;

    @Autowired
    private ChannelDataService channelDataService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange.mutate().response(decorate(exchange)).build());
    }

    private ServerHttpResponse decorate(ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.add(HttpHeaders.CONTENT_TYPE,
                        originalResponseContentType);

                ClientResponse clientResponse = ClientResponse
                        .create(Objects.requireNonNull(exchange.getResponse().getStatusCode()))
                        .headers(headers -> headers.putAll(httpHeaders))
                        .body(Flux.from(body)).build();

                Mono<String> modifiedBody = clientResponse
                        .bodyToMono(String.class)
                        .flatMap(originalBody -> modifyResponseBody().apply(exchange, originalBody));

                BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody,
                        String.class);
                CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(
                        exchange, exchange.getResponse().getHeaders());
                return bodyInserter.insert(outputMessage, new BodyInserterContext())
                        .then(Mono.defer(() -> {
                            Flux<DataBuffer> messageBody = outputMessage.getBody();
                            HttpHeaders headers = getDelegate().getHeaders();
                            if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
                                messageBody = messageBody.doOnNext(data -> headers
                                        .setContentLength(data.readableByteCount()));
                            }
                            return getDelegate().writeWith(messageBody);
                        }));
            }

            private BiFunction<ServerWebExchange, String, Mono<String>> modifyResponseBody() {
                return (serverWebExchange, responseBody) -> {
                    String modifyResponseBody = checkResponseBody(serverWebExchange, responseBody);
                    return Mono.just(modifyResponseBody);
                };
            }

            @Override
            public Mono<Void> writeAndFlushWith(
                    Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }
        };
    }

    private String checkResponseBody(final ServerWebExchange serverWebExchange, final String responseBody) {
        String modifyResponseBody = responseBody;
        ServerHttpRequest request = serverWebExchange.getRequest();
        String noVerify = request.getHeaders().getFirst(NO_VERIFY);
        String encrypt = request.getHeaders().getFirst(ENCRYPT);
        if ((!VERIFY_VALUE.equals(noVerify)) && V2.equals(encrypt) && StringUtil.isNotEmpty(responseBody)) {
            ApiResponse apiResponse = JSON.parseObject(responseBody, ApiResponse.class);
            Object data = apiResponse.getData();
            if (data != null) {
                String code = request.getHeaders().getFirst(CODE);
                GetChannelDataVo channelDataVo = channelDataService.getChannelDataByCode(code);
                String rsaEncrypt = RsaTool.encrypt(JSON.toJSONString(data), channelDataVo.getDataPublicKey());
                apiResponse.setData(rsaEncrypt);
                modifyResponseBody = JSON.toJSONString(apiResponse);
            }
        }
        return modifyResponseBody;
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
