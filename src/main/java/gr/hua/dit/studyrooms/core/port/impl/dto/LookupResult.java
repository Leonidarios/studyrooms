package gr.hua.dit.studyrooms.core.port.impl.dto;

import gr.hua.dit.studyrooms.core.model.PersonType;

/**
 * LookupResult DTO.
 */
public record LookupResult(
    String raw,
    boolean exists,
    String huaId,
    PersonType type
) {}
