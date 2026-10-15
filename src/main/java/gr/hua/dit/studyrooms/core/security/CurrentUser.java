package gr.hua.dit.studyrooms.core.security;

import gr.hua.dit.studyrooms.core.model.PersonType;

/**
 * @see CurrentUserProvider
 */
public record CurrentUser(long id, String emailAddress, PersonType type) {}
