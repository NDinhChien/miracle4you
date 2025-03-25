package mfy.server.domain.project.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mfy.server.domain.project.dto.ProjectRequestDto.ProjectCreateRequestDto;
import mfy.server.domain.project.dto.ProjectRequestDto.ProjectUpdateRequestDto;
import mfy.server.domain.project.entity.type.Category;
import mfy.server.domain.project.entity.type.Language;
import mfy.server.domain.project.entity.type.Stage;
import mfy.server.domain.user.entity.User;
import mfy.server.global.util.EnumUtil;

@Getter
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "entityCache")
@Entity
@DynamicUpdate
@Table(name = "projects", indexes = {
        @Index(name = "project_category_idx", columnList = "category"),
        @Index(name = "project_srcLang_idx", columnList = "srcLang"),
        @Index(name = "project_desLang_idx", columnList = "desLang"),
        @Index(name = "project_stage_idx", columnList = "stage"),
        @Index(name = "project_isRecruiting_idx", columnList = "isRecruiting"),
        @Index(name = "project_views_idx", columnList = "views"),
        @Index(name = "project_publishedAt_idx", columnList = "publishedAt"),
        @Index(name = "project_fulltext_idx", columnList = "fulltext"),
})
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @JsonIgnore
    @Type(PostgreSQLTSVectorType.class)
    @Column(columnDefinition = "tsvector", nullable = true)
    private String fulltext;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false, unique = true)
    private String srcUrl;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Language srcLang;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Language desLang;

    @Column(nullable = false)
    private Integer views;

    @Column(nullable = false)
    private Integer likes;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "admin_id", insertable = false, updatable = false)
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;

    @Column
    private LocalDateTime deadline;

    @Column(nullable = false)
    private Boolean isRecruiting;

    @Column
    private LocalDateTime publishedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by_id")
    private User publishedBy;

    @Column(name = "published_by_id", insertable = false, updatable = false)
    private Long publishedById;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Translator> translators;

    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Applicant> applicants;

    @Builder
    public Project(String title, String srcUrl, Language srcLang, Language desLang, User admin, Category category,
            String description) {
        this.title = title;
        this.srcUrl = srcUrl;
        this.srcLang = srcLang;
        this.desLang = desLang;
        this.admin = admin;
        this.translators = new HashSet<Translator>();
        this.translators.add(new Translator(admin, this));

        this.category = category;
        this.description = description;
        this.fulltext = calcFullText(this.title, this.description);
    }

    public Project(ProjectCreateRequestDto requestDto, User admin) {
        this.title = requestDto.getTitle();
        this.srcUrl = requestDto.getSrcUrl();
        this.srcLang = EnumUtil.fromString(Language.class, requestDto.getSrcLang());
        this.desLang = EnumUtil.fromString(Language.class, requestDto.getDesLang());
        this.admin = admin;
        this.translators = new HashSet<Translator>();
        this.translators.add(new Translator(admin, this));

        this.category = EnumUtil.fromString(Category.class, requestDto.getCategory());
        this.description = requestDto.getDescription();
        this.fulltext = calcFullText(this.title, this.description);
    }

    @PrePersist
    public void prePersist() {
        if (this.category == null) {
            this.category = Category.OTHER;
        }
        if (this.views == null) {
            this.views = 0;
        }
        if (this.likes == null) {
            this.likes = 0;
        }
        if (this.stage == null) {
            this.stage = Stage.PROPOSING;
        }
        if (this.isRecruiting == null) {
            this.isRecruiting = false;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }

    public Project updateProfile(ProjectUpdateRequestDto requestDto) {
        String title = requestDto.getTitle();
        String description = requestDto.getDescription();
        String category = requestDto.getCategory();
        String srcLang = requestDto.getSrcLang();
        String desLang = requestDto.getDesLang();
        String stage = requestDto.getStage();
        Boolean isRecruiting = requestDto.getIsRecruiting();

        if (StringUtils.hasText(title)) {
            this.title = title;
        }
        if (StringUtils.hasText(description)) {
            this.description = description;
        }
        if (StringUtils.hasText(category)) {
            this.category = EnumUtil.fromString(Category.class, category);
        }
        if (StringUtils.hasText(srcLang)) {
            this.srcLang = EnumUtil.fromString(Language.class, srcLang);
        }
        if (StringUtils.hasText(desLang)) {
            this.desLang = EnumUtil.fromString(Language.class, desLang);
        }
        if (StringUtils.hasText(stage)) {
            this.stage = EnumUtil.fromString(Stage.class, stage);
        }
        if (isRecruiting != null) {
            this.isRecruiting = isRecruiting;
        }

        return this;
    }

    public Project addTranslators(User... users) {
        for (User user : users) {
            this.translators.add(new Translator(user, this));
        }
        return this;
    }

    public static String calcFullText(String... strs) {
        StringBuilder result = new StringBuilder();
        for (String str : strs) {
            String sanitized = str == null ? "" : str.replaceAll("[^a-zA-Z0-9\\s]", "");
            result.append(sanitized + " ");
        }
        return result.toString().trim();

    }
}
