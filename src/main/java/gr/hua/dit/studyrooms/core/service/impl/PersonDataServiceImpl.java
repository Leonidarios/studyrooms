package gr.hua.dit.studyrooms.core.service.impl;

import gr.hua.dit.studyrooms.core.model.Person;
import gr.hua.dit.studyrooms.core.repository.PersonRepository;
import gr.hua.dit.studyrooms.core.service.PersonDataService;
import gr.hua.dit.studyrooms.core.service.mapper.PersonMapper;
import gr.hua.dit.studyrooms.core.service.model.PersonView;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link PersonDataService}.
 */
@Service
public class PersonDataServiceImpl implements PersonDataService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonDataServiceImpl(final PersonRepository personRepository,
                                 final PersonMapper personMapper) {
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    @Override
    public List<PersonView> getAllPeople() {
        final List<Person> personList = this.personRepository.findAll();
        final List<PersonView> personViewList = personList
            .stream()
            .map(this.personMapper::convertPersonToPersonView)
            .toList();
        return personViewList;
    }

    @Override
    public Optional<PersonView> getPersonByHuaId(final String huaId) {
        if (huaId == null || huaId.isBlank()) return Optional.empty();
        return this.personRepository.findByHuaIdIgnoreCase(huaId.strip())
            .map(this.personMapper::convertPersonToPersonView);
    }
}
