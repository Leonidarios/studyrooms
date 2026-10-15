package gr.hua.dit.studyrooms.core.port.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import gr.hua.dit.studyrooms.core.port.SmsNotificationPort;
import gr.hua.dit.studyrooms.core.port.impl.dto.SendSmsRequest;
import gr.hua.dit.studyrooms.core.port.impl.dto.SendSmsResult;

/**
 * Default implementation of {@link SmsNotificationPort}. It uses the NOC external service.
 */
@Service
public class SmsNotificationPortImpl implements SmsNotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsNotificationPortImpl.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final boolean enabled;

    public SmsNotificationPortImpl(final RestTemplate restTemplate,
                                   @Value("${app.noc.base-url}") final String baseUrl,
                                   @Value("${app.noc.sms-enabled}") final boolean enabled) {
        if (restTemplate == null) throw new NullPointerException();
        if (baseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.enabled = enabled;
    }

    @Override
    public boolean sendSms(final String e164, final String content) {
        if (e164 == null) throw new NullPointerException();
        if (e164.isBlank()) throw new IllegalArgumentException();
        if (content == null) throw new NullPointerException();
        if (content.isBlank()) throw new IllegalArgumentException();

        if (!this.enabled) {
            LOGGER.info("SMS notifications are disabled (app.noc.sms-enabled=false). Skipping SMS to {}", e164);
            return false;
        }

        // Headers
        // --------------------------------------------------

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Payload
        // --------------------------------------------------

        final SendSmsRequest body = new SendSmsRequest(e164, content);
        final HttpEntity<SendSmsRequest> entity = new HttpEntity<>(body, httpHeaders);
        final String url = this.baseUrl + "/api/v1/sms";

        try {
            final ResponseEntity<SendSmsResult> response = this.restTemplate.postForEntity(url, entity, SendSmsResult.class);
            final SendSmsResult result = response.getBody();
            final boolean sent = result != null && result.sent();
            LOGGER.info("SMS request to NOC for {} completed (sent={})", e164, sent);
            return sent;
        } catch (RestClientException e) {
            LOGGER.warn("SMS to {} could not be sent, NOC unavailable: {}", e164, e.getMessage());
            return false;
        }
    }
}
