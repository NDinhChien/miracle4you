package mfy.server.domain.project.dto;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import mfy.server.domain.project.entity.Applicant;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.entity.Translator;
import mfy.server.domain.project.entity.type.Category;
import mfy.server.domain.project.entity.type.Language;
import mfy.server.domain.project.entity.type.Stage;

public class ProjectResponseDto {

    public static record TranslatorBaseDto(Long userId, Long projectId, Instant createdAt) {
        public static TranslatorBaseDto fromEntity(Translator translator) {
            return new TranslatorBaseDto(
                    translator.getUserId(),
                    translator.getProjectId(),
                    translator.getCreatedAt());
        }

        public static TranslatorBaseDto fromEntity(Applicant applicant) {
            return new TranslatorBaseDto(
                    applicant.getUserId(),
                    applicant.getProjectId(),
                    applicant.getCreatedAt());
        }

    }

    public static record ProjectBasicDto(
            Long id,
            String title,
            String srcUrl,
            Language srcLang,
            Language desLang,
            Category category) {

        public static ProjectBasicDto fromEntity(Project project) {
            return new ProjectBasicDto(
                    project.getId(),
                    project.getTitle(),
                    project.getSrcUrl(),
                    project.getSrcLang(),
                    project.getDesLang(),
                    project.getCategory());
        }
    }

    public static record ProjectPublicDto(
            Long id,
            String title,
            String srcUrl,
            Language srcLang,
            Language desLang,
            Category category,
            Stage stage,
            Integer views,
            Integer likes,
            Instant createdAt,
            Instant publishedAt) {

        public static ProjectPublicDto fromEntity(Project project) {
            return new ProjectPublicDto(
                    project.getId(),
                    project.getTitle(),
                    project.getSrcUrl(),
                    project.getSrcLang(),
                    project.getDesLang(),
                    project.getCategory(),
                    project.getStage(),
                    project.getViews(),
                    project.getLikes(),
                    project.getCreatedAt(),
                    project.getPublishedAt());
        }
    }

    public static record ProjectDto(
            Long id,
            String title,
            String srcUrl,
            Language srcLang,
            Language desLang,
            Category category,
            Stage stage,
            Integer views,
            Integer likes,
            Instant createdAt,
            Instant publishedAt,
            Boolean isRecruiting,
            String description,
            Long adminId,
            Instant deadline,
            Long publishedById,
            List<TranslatorBaseDto> translators,
            List<TranslatorBaseDto> applicants) {
        public static ProjectDto fromEntity(Project project) {
            return new ProjectDto(
                    project.getId(),
                    project.getTitle(),
                    project.getSrcUrl(),
                    project.getSrcLang(),
                    project.getDesLang(),
                    project.getCategory(),
                    project.getStage(),
                    project.getViews(),
                    project.getLikes(),
                    project.getCreatedAt(),
                    project.getPublishedAt(),
                    project.getIsRecruiting(),
                    project.getDescription(),
                    project.getAdminId(),
                    project.getDeadline(),
                    project.getPublishedById(),
                    project.getTranslators() != null ? project.getTranslators().stream()
                            .map(TranslatorBaseDto::fromEntity)
                            .collect(Collectors.toList())
                            : null,
                    project.getApplicants() != null ? project.getApplicants().stream()
                            .map(TranslatorBaseDto::fromEntity)
                            .collect(Collectors.toList())
                            : null);
        }
    }

    public static interface ITranslatorBaseDto {
        Long getUserId();

        Long getProjectId();

        Instant getCreatedAt();
    }

    public static interface IProjectBasicDto {
        Long getId();

        String getTitle();

        Category getCategory();

        String getSrcUrl();

        Language getSrcLang();

        Language getDesLang();

    }

    public static interface IProjectPublicDto extends IProjectBasicDto {

        String getDescription();

        Integer getViews();

        Integer getLikes();

        Long getAdminId();

        Stage getStage();

        Instant getDeadline();

        Boolean getIsRecruiting();

        Boolean getIsDeleted();

        Instant getCreatedAt();

        Instant getPublishedAt();

        Long getPublishedById();

    }

    public static interface IProjectDto extends IProjectPublicDto {

        List<ITranslatorBaseDto> getTranslators();

        List<ITranslatorBaseDto> getApplicants();

    }
}
