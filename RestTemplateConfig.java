package cn.knet.businesstask;

import cn.knet.domain.filter.LoggingClientHttpRequestInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class RestTemplateConfig {

    private static final int HTTP_CLIENT_RETRY_COUNT = 3;

    private static final int MAXIMUM_TOTAL_CONNECTION = 10;

    private static final int MAXIMUM_CONNECTION_PER_ROUTE = 5;

    private static final int CONNECTION_VALIDATE_AFTER_INACTIVITY_MS = 10 * 1000;

    /**
     * @param connectionTimeoutMs milliseconds/毫秒
     * @param readTimeoutMs       milliseconds/毫秒
     * @return
     */
    public static RestTemplate createRestTemplate(int connectionTimeoutMs, int readTimeoutMs, ObjectMapper objectMapper) {

        HttpClientBuilder clientBuilder = HttpClients.custom();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        // 整个连接池最大连接数
        connectionManager.setMaxTotal(MAXIMUM_TOTAL_CONNECTION);
        // 设置每个路由的最大并发连接数,默认为2.
        connectionManager.setDefaultMaxPerRoute(MAXIMUM_CONNECTION_PER_ROUTE);
        // 官方推荐使用检查永久链接的可用性,而不推荐每次请求的时候才去检查 (milliseconds 毫秒)
        connectionManager.setValidateAfterInactivity(CONNECTION_VALIDATE_AFTER_INACTIVITY_MS);

        clientBuilder.setConnectionManager(connectionManager);
        //设置重连操作次数，这里设置了3次
        clientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(HTTP_CLIENT_RETRY_COUNT, true, new ArrayList<>()) {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper) context.getAttribute("http.request");
                HttpRequest original = httpRequestWrapper.getOriginal();
                log.info("Retry request, execution count:{}, exception:{}, request URL:{}", executionCount, exception.getMessage(), original);
                return super.retryRequest(exception, executionCount, context);
            }
        });
//
//        //使用httpClient创建一个ClientHttpRequestFactory的实现
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(clientBuilder.build());
        httpRequestFactory.setConnectTimeout(connectionTimeoutMs);
        httpRequestFactory.setConnectionRequestTimeout(readTimeoutMs);
        httpRequestFactory.setReadTimeout(readTimeoutMs);

        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        // 添加自定义拦截器
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
        interceptors.add(new LoggingClientHttpRequestInterceptor());
        restTemplate.setInterceptors(interceptors);
        //提供对传出/传入流的缓冲,可以让响应body多次读取(如果不配置,拦截器读取了Response流,再响应数据时会返回body=null)
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(httpRequestFactory));



        //防止响应中文乱码
        restTemplate.getMessageConverters().stream().filter(StringHttpMessageConverter.class::isInstance).map(StringHttpMessageConverter.class::cast).forEach(a -> {
            a.setWriteAcceptCharset(false);
            a.setDefaultCharset(StandardCharsets.UTF_8);
        });

        return restTemplate;
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = RestTemplateConfig.createRestTemplate(180000, 180000, new ObjectMapper());
        //配置自定义的interceptor拦截器
        //使用restTemplate远程调用防止400和401导致报错而获取不到正确反馈信息
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        return restTemplate;
    }
}
