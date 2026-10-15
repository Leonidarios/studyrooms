package gr.hua.dit.studyrooms.web.rest.model;

import gr.hua.dit.studyrooms.web.rest.ClientAuthResource;

/**
 * @see ClientAuthResource
 */
public record ClientTokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}
