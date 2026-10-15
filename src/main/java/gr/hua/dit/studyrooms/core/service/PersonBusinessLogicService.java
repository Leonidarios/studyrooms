package gr.hua.dit.studyrooms.core.service;

import gr.hua.dit.studyrooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.studyrooms.core.service.model.CreatePersonResult;

/**
 * Service for managing {@link gr.hua.dit.studyrooms.core.model.Person}.
 */
public interface PersonBusinessLogicService {

    CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify);

    default CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest) {
        return this.createPerson(createPersonRequest, true);
    }
}
