package mfy.server.domain.project.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.entity.Translator;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.project.entity.type.TranslatorBase;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslatorRepository extends JpaRepository<Translator, Long> {

    boolean existsByProjectAndUser(Project project, User user);

    List<TranslatorBase> findByUser(User user);

}
