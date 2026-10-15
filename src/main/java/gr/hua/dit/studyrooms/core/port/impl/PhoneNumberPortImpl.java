package gr.hua.dit.studyrooms.core.port.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import gr.hua.dit.studyrooms.core.exception.ExternalServiceException;
import gr.hua.dit.studyrooms.core.port.PhoneNumberPort;
import gr.hua.dit.studyrooms.core.port.impl.dto.PhoneNumberValidationResult;

/**
 * Default implementation of {@link PhoneNumberPort}. It uses the NOC external service.
 */
@Service
public class PhoneNumberPortImpl implements PhoneNumberPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberPortImpl.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PhoneNumberPortImpl(final RestTemplate restTemplate,
                               @Value("${app.noc.base-url}") final String baseUrl) {
        if (restTemplate == null) throw new NullPointerException();
        if (baseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public PhoneNumberValidationResult validate(final String rawPhoneNumber) {
        if (rawPhoneNumber == null) throw new NullPointerException();
        if (rawPhoneNumber.isBlank()) throw new IllegalArgumentException();

        // HTTP Request
        // --------------------------------------------------

        final String url = this.baseUrl + "/api/v1/phone-numbers/{phoneNumber}/validations";

        final ResponseEntity<PhoneNumberValidationResult> response;
        try {
            response = this.restTemplate.getForEntity(url, PhoneNumberValidationResult.class, rawPhoneNumber);
        } catch (RestClientException e) {
            LOGGER.error("NOC phone validation failed for {}: {}", rawPhoneNumber, e.getMessage());
            throw new ExternalServiceException("Phone validation service (NOC) is unavailable. Please try again later.", e);
        }

        final PhoneNumberValidationResult result = response.getBody();
        if (result == null) {
            throw new ExternalServiceException("Phone validation service (NOC) returned an empty response.", null);
        }
        return result;
    }
}
