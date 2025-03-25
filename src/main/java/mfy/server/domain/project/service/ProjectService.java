package mfy.server.domain.project.service;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.project.dto.ProjectRequestDto.ProjectCreateRequestDto;
import mfy.server.domain.project.dto.ProjectRequestDto.ProjectSearchRequestDto;
import mfy.server.domain.project.dto.ProjectRequestDto.ProjectUpdateRequestDto;
import mfy.server.domain.project.dto.ProjectResponseDto.ProjectBasicDto;
import mfy.server.domain.project.dto.ProjectResponseDto.ProjectPublicDto;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.entity.type.Category;
import mfy.server.domain.project.repository.ProjectRepository;
import mfy.server.domain.user.entity.User;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import mfy.server.global.util.EnumUtil;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private static final int PAGE_SIZE = 10;
    private final ProjectRepository projectRepository;
    private final TranslatorService translatorService;

    @PersistenceContext
    private EntityManager entityManager;

    public Project getProject(Long projectId) {
        return validateProject(projectId);
    }

    @Transactional
    public Project createProject(ProjectCreateRequestDto requestDto, User admin) {
        if (projectRepository.existsBySrcUrl(requestDto.getSrcUrl())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.PROJECT_URL_EXISTS);
        }
        try {
            Project project = new Project(requestDto, admin);
            return projectRepository.save(project);
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create project");
        }
    }

    public ProjectBasicDto getProjectBasicInfo(Long id) {
        return projectRepository.findById(id, ProjectBasicDto.class).orElseThrow(() -> {
            return new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.PROJECT_NOT_FOUND);
        });
    }

    @Transactional
    public void joinProject(User user, Long projectId) {
        Project project = validateProject(projectId);
        translatorService.addTranslator(project, user);
    }

    private Project validateProject(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> {
            return new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.PROJECT_NOT_FOUND);
        });
    }

    public Project updateProject(User user, Long projectId, ProjectUpdateRequestDto requestDto) {
        Project project = validateProject(projectId);
        if (project.getAdminId() != user.getId()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ErrorMessage.NOT_PROJECT_OWNER);
        }
        project = project.updateProfile(requestDto);
        return projectRepository.save(project);
    }

    @Cacheable(value = "Projects", cacheManager = "caffeinCacheManager")
    public List<ProjectPublicDto> searchProjects(ProjectSearchRequestDto requestDto) {
        String sortBy = requestDto.getSortBy() == "latest" ? "createdAt" : "views";
        Integer page = requestDto.getPage();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, sortBy));
        String keywords = requestDto.getKeywords();
        Category category = EnumUtil.fromString(Category.class, requestDto.getCategory());

        if (keywords == null) {
            if (category == null) {
                return projectRepository.findAllBy(pageable, ProjectPublicDto.class);
            } else {
                return projectRepository.findAllByCategory(category, pageable, ProjectPublicDto.class);
            }
        } else {
            List<Project> projects;
            if (category == null) {
                projects = projectRepository.search(keywords, pageable);
            } else {
                projects = projectRepository.search(keywords, category.ordinal(), pageable);
            }
            return projects.stream().map(ProjectPublicDto::fromEntity).toList();
        }
    }

}
