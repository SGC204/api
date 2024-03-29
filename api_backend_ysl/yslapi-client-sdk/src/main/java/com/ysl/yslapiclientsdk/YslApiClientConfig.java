package com.ysl.yslapiclientsdk;

import com.ysl.yslapiclientsdk.client.YslApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("yslapi.client")
@Data
@ComponentScan
public class YslApiClientConfig {
    private String accessKey;
    private String secretKey;

    @Bean
    public YslApiClient yslApiClient() {
        return new YslApiClient(accessKey, secretKey);
    }
}
