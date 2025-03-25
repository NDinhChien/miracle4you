package mfy.server.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mfy.server.domain.project.entity.type.Category;
import mfy.server.domain.project.entity.type.Language;
import mfy.server.domain.project.entity.type.Stage;
import mfy.server.global.validator.ValueOfEnum;

public class ProjectRequestDto {

    @Getter
    public static class ProjectCreateRequestDto {

        @Schema(example = "Biblical Series: The Gospels")
        @NotBlank
        private String title;

        @Schema(example = "https://youtu.be/8jb-UxGAJ8M?si=V74XDh3yIWNvMU9n")
        @NotBlank
        private String srcUrl;

        @ValueOfEnum(enumClass = Language.class)
        @Schema(example = "eng")
        @NotBlank
        private String srcLang;

        @ValueOfEnum(enumClass = Language.class)
        @Schema(example = "vie")
        @NotBlank
        private String desLang;

        @Schema(example = "Dr. Peterson’s third biblical series, “The Gospels” is part of his lifelong effort to rescue society from the meaning crisis that seeks to devour our culture.")
        private String description;

        @ValueOfEnum(enumClass = Category.class)
        @Schema(example = "WISDOM")
        private String category;

    }

    @Getter
    public static class JoinProjectRequestDto {

        private Long id;

    }

    @Getter
    public static class ProjectUpdateRequestDto {

        private String title;

        private String description;

        @ValueOfEnum(enumClass = Category.class)
        private String category;

        @ValueOfEnum(enumClass = Stage.class)
        private String stage;

        @ValueOfEnum(enumClass = Language.class)
        private String desLang;

        @ValueOfEnum(enumClass = Language.class)
        private String srcLang;

        private Boolean isRecruiting;
    }

    @Getter
    @AllArgsConstructor
    public static class ProjectSearchRequestDto {

        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Only contain nummeric or alphabet characters")
        private String keywords;

        @ValueOfEnum(enumClass = Category.class)
        private String category;

        @Min(1)
        @NotNull
        private Integer page;

        @NotNull
        @Pattern(regexp = "^(latest|mostPopular)$", flags = Pattern.Flag.CASE_INSENSITIVE)
        private String sortBy;
    }
}
