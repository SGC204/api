package com.ysl.yslapigateway;

import com.ysl.yslapiclientsdk.utils.SignUtils;
import com.ysl.yslapicommon.model.entity.InterfaceInfo;
import com.ysl.yslapicommon.model.entity.User;
import com.ysl.yslapicommon.service.InnerInterfaceInfoService;
import com.ysl.yslapicommon.service.InnerUserInterfaceInfoService;
import com.ysl.yslapicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 全局过滤
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;


    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    private static final String INTERFACE_HOST = "http://localhost:8123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求唯一标识" + path);
        log.info("请求路径" + method);
        log.info("请求方法" + request.getMethod());
        log.info("请求参数" + request.getQueryParams());
        log.info("请求来源地址" + request.getRemoteAddress());
        String sourceAddress = Objects.requireNonNull(request.getLocalAddress()).getHostString();
        log.info("请求来源地址" + sourceAddress);
        ServerHttpResponse response = exchange.getResponse();
        // 2.访问控制 - 白名单
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            return handleNoAuth(response);
        }
        // 3.用户鉴权
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String body = headers.getFirst("body");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error", e);
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        // 随机数不能为空或大于10000
        if (nonce == null || Long.parseLong(nonce) > 10000L ) {
            return handleNoAuth(response);
        }
        // 时间和当前时间不能超过5分钟
        long currentTime = System.currentTimeMillis() / 1000;
        final long FIVE_MINUTES = 60 * 5L;
        if (timestamp == null || (currentTime - Long.parseLong(timestamp)) > FIVE_MINUTES) {
            return handleNoAuth(response);
        }
        // 校验签名
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.getsign(body, secretKey);
        if (sign == null || !sign.equals(serverSign)) {
            return handleNoAuth(response);
        };
        //4. 请求的模拟接口是否存在
        InterfaceInfo invokeInterfaceInfo = null;
        try {
            invokeInterfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error", e);
        }
        if (invokeInterfaceInfo == null) {
            return handleNoAuth(response);
        }
        Mono<Void> filter = chain.filter(exchange);
        //todo 是否还有调用次数
        //5. 请求转发，调用模拟接口，响应日志
        //return chain.filter(exchange)
        return handleResponse(exchange, chain, invokeInterfaceInfo.getId(), invokeUser.getId());
    }

    /**
     * 处理响应
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
            try {
                ServerHttpResponse response = exchange.getResponse();
                DataBufferFactory bufferFactory = response.bufferFactory();
                HttpStatusCode statusCode = response.getStatusCode();
                if (statusCode == HttpStatus.OK) {
                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
                        //等调用完转发的接口后执行
                        @Override
                        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                            log.info("body instance of Flux: {}", (body instanceof Flux));
                            if (body instanceof Flux) {
                                Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                                //往返回值里写数据
                                return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                    //6. 调用成功后,次数+1 invokeCount
                                        try {
                                            innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);
                                    String data = new String(content, StandardCharsets.UTF_8);
                                    //打印日志
                                    log.info("响应结果：" + data);
                                    return bufferFactory.wrap(content);
                                }));
                            } else {
                                //8. 调用失败，返回一个规范的错误码
                                log.error("<-- {} 响应code异常", getStatusCode());
                            }
                            return super.writeWith(body);
                        }
                    };
                    return chain.filter(exchange.mutate().response(decoratedResponse).build());
                }
                //降级处理返回数据
                return chain.filter(exchange);
            } catch (Exception e) {
                log.error("网关处理响应异常 \n" + e);
                return chain.filter(exchange);
            }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}

