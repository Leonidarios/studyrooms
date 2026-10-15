package gr.hua.dit.studyrooms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestApiClientConfig {

    @Bean
    public RestTemplate restTemplate(@Value("${app.noc.connect-timeout-ms}") final int connectTimeoutMs,
                                     @Value("${app.noc.read-timeout-ms}") final int readTimeoutMs) {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
