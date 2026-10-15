package gr.hua.dit.studyrooms.core.service.model;

import gr.hua.dit.studyrooms.core.model.PersonType;
import gr.hua.dit.studyrooms.core.service.PersonBusinessLogicService;

/**
 * General view of {@link gr.hua.dit.studyrooms.core.model.Person} entity.
 *
 * @see gr.hua.dit.studyrooms.core.model.Person
 * @see PersonBusinessLogicService
 */
public record PersonView(
    long id,
    String huaId,
    String firstName,
    String lastName,
    String mobilePhoneNumber,
    String emailAddress,
    PersonType type
) {

    public String fullName() {
        return this.firstName + " " + this.lastName;
    }
}
