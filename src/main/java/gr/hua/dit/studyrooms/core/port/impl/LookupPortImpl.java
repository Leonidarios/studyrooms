package gr.hua.dit.studyrooms.core.port.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import gr.hua.dit.studyrooms.core.exception.ExternalServiceException;
import gr.hua.dit.studyrooms.core.model.PersonType;
import gr.hua.dit.studyrooms.core.port.LookupPort;
import gr.hua.dit.studyrooms.core.port.impl.dto.LookupResult;

/**
 * Default implementation of {@link LookupPort}. It uses the NOC external service.
 */
@Service
public class LookupPortImpl implements LookupPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(LookupPortImpl.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LookupPortImpl(final RestTemplate restTemplate,
                          @Value("${app.noc.base-url}") final String baseUrl) {
        if (restTemplate == null) throw new NullPointerException();
        if (baseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<PersonType> lookup(final String huaId) {
        if (huaId == null) throw new NullPointerException();
        if (huaId.isBlank()) throw new IllegalArgumentException();

        // HTTP Request
        // --------------------------------------------------

        final String url = this.baseUrl + "/api/v1/lookups/{huaId}";

        final ResponseEntity<LookupResult> response;
        try {
            response = this.restTemplate.getForEntity(url, LookupResult.class, huaId);
        } catch (RestClientException e) {
            LOGGER.error("NOC lookup failed for {}: {}", huaId, e.getMessage());
            throw new ExternalServiceException("University registry service (NOC) is unavailable. Please try again later.", e);
        }

        final LookupResult lookupResult = response.getBody();
        if (lookupResult == null) {
            throw new ExternalServiceException("University registry service (NOC) returned an empty response.", null);
        }
        if (!lookupResult.exists()) {
            return Optional.empty();
        }
        return Optional.ofNullable(lookupResult.type());
    }
}
