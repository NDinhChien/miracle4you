package mfy.server.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import mfy.server.domain.project.entity.Applicant;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}