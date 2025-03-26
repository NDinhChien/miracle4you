package mfy.server.domain.message.repository;

import mfy.server.domain.message.dto.MessageResponseDto.IProjectMessage;
import mfy.server.domain.message.dto.MessageResponseDto.ProjectMessageDto;
import mfy.server.domain.message.entity.ProjectMessage;
import mfy.server.domain.project.entity.Project;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMessageRepository extends JpaRepository<ProjectMessage, Long> {

    Page<ProjectMessageDto> findAllByProject(Project project, Pageable pageable);

    List<ProjectMessageDto> findByProjectInAndCreatedAtGreaterThan(List<Project> projects, Instant createdAt);

    long countByProject(Project project);
}
