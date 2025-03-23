package mfy.server.domain.project.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import mfy.server.domain.project.dto.ProjectResponseDto.IProjectBasicDto;
import mfy.server.domain.project.dto.ProjectResponseDto.IProjectDto;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.entity.type.Category;
import mfy.server.domain.user.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsBySrcUrl(String srcUrl);

    <T> List<T> findAllByCategory(Category category, Pageable pageable, Class<T> type);

    List<IProjectDto> findByAdmin(User admin);

    <T> List<T> findAllBy(Pageable pageable, Class<T> type);

    <T> Optional<T> findById(Long id, Class<T> type);

    @Query(value = """
                SELECT * FROM projects p WHERE p.fulltext @@ plainto_tsquery(:keywords)
            """, nativeQuery = true)
    List<Project> search(String keywords, Pageable pageable);

    @Query(value = """
                SELECT * FROM projects p WHERE (p.fulltext @@ plainto_tsquery(:keywords)) AND (p.category = :category)
            """, nativeQuery = true)
    List<Project> search(String keywords, Integer category, Pageable pageable);

    @Query(value = """
            SELECT p.id AS id, p.title AS title, p.srcUrl AS srcUrl, p.srcLang AS srcLang, p.desLang AS desLang, p.category AS category
            FROM Project p WHERE p.id IN :projectIds
            """)
    List<IProjectBasicDto> findInIds(Set<Long> projectIds);
}
