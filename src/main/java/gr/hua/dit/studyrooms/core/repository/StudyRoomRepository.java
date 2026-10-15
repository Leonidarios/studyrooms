package gr.hua.dit.studyrooms.core.repository;

import gr.hua.dit.studyrooms.core.model.StudyRoom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link StudyRoom} entity.
 */
@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    List<StudyRoom> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(final String name);

    boolean existsByNameIgnoreCaseAndIdNot(final String name, final Long id);
}
