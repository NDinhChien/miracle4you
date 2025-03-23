package mfy.server.domain.project.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.entity.Translator;
import mfy.server.domain.project.repository.TranslatorRepository;
import mfy.server.domain.user.entity.User;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;

@Slf4j
@RequiredArgsConstructor
@Service
public class TranslatorService {

    private final TranslatorRepository translatorRepository;

    public List<Long> getProjectIds(User user) {
        return translatorRepository.findByUser(user).stream().map(t -> t.getProjectId()).toList();
    }

    public List<Project> getProjects(User user) {
        return translatorRepository.findByUser(user).stream().map(t -> t.getProject()).toList();
    }

    public boolean isMember(Project project, User user) {
        return translatorRepository.existsByProjectAndUser(project, user);
    }

    public void addTranslator(Project project, User user) {
        boolean isMember = isMember(project, user);
        if (isMember) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.ALREADY_JOINED_PROJECT);
        }
        Translator translator = new Translator(user, project);
        translatorRepository.save(translator);
    }
}
