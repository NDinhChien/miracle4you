package mfy.server.domain.user.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import mfy.server.domain.project.entity.Applicant;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.entity.Translator;
import mfy.server.domain.user.dto.UserRequestDto.UpdateRequestDto;
import mfy.server.domain.user.entity.type.Gender;
import mfy.server.domain.user.entity.type.Role;
import mfy.server.domain.user.service.AuthService;
import mfy.server.domain.user.service.UserService;
import mfy.server.global.util.CommonUtil;
import mfy.server.global.util.EnumUtil;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;

@Getter
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "entityCache")
@DynamicUpdate
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column
    private String googleId;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column
    private String avatar;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String tokenSecret;

    @Column(nullable = false)
    private Boolean isVerified;

    @Column(nullable = false)
    private Boolean isBanned;

    @Column(nullable = false)
    private Integer score;

    @Column
    private String fullName;

    @Column
    private LocalDateTime birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column
    private String bio;

    @Column
    private String diocese;

    @Column
    private String parish;

    @Column(nullable = false)
    private LocalDateTime lastUpdateNickname;

    @Column(nullable = false)
    private LocalDateTime lastUpdateProfile;

    @Column
    private LocalDateTime lastSendEmail;

    @Column
    private LocalDateTime lastLoginSuccessAt;

    @Column
    private LocalDateTime lastLoginFailureAt;

    @Column
    private LocalDateTime lastOnline;

    @Column(nullable = false)
    private Integer currentLoginFailureCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Applicant> applicants;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Translator> translators;

    @JsonIgnore
    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "entityCache")
    private List<Project> projects;

    @JsonIgnore
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatItem> chatItems;

    @PrePersist
    public void prePersist() {
        if (isVerified == null) {
            isVerified = false;
        }
        if (isBanned == null) {
            isBanned = false;
        }
        if (role == null) {
            role = Role.TRANSLATOR;
        }
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (score == null) {
            score = 0;
        }
        if (nickname != null && lastUpdateNickname == null) {
            lastUpdateNickname = LocalDateTime.now();
        }
        if (lastUpdateProfile == null) {
            lastUpdateProfile = LocalDateTime.now();
        }
        if (currentLoginFailureCount == null) {
            currentLoginFailureCount = 0;
        }
        if (tokenSecret == null) {
            updateTokenSecret();
        }
        if (lastOnline == null) {
            lastOnline = LocalDateTime.now();
        }
    }

    @Builder
    public User(String nickname, String email, String password, Role role, String googleId, String avatar) {
        this.nickname = nickname;
        this.email = email;
        this.password = CommonUtil.encodePassword(password);
        this.avatar = avatar;
        this.role = role;
        this.googleId = googleId;
    }

    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = CommonUtil.encodePassword(password);
    }

    public User updateGoogleId(String googleId) {
        if (this.googleId != null)
            return this;
        this.googleId = googleId;
        return this;
    }

    public User updateLogout() {
        updateTokenSecret();
        return this;
    }

    public User updateLoginSucces() {
        updateTokenSecret();
        this.currentLoginFailureCount = 0;
        this.lastLoginFailureAt = null;
        this.lastLoginSuccessAt = LocalDateTime.now();
        return this;
    }

    public User updatePassword(String password) {
        updateTokenSecret();
        this.currentLoginFailureCount = 0;
        this.password = CommonUtil.encodePassword(password);
        return this;
    }

    public User updateAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public boolean canDoLogin() {
        var canLogin = currentLoginFailureCount <= AuthService.MAX_LOGIN_ATTEMPT;
        if (canLogin == false) {
            if (lastLoginFailureAt == null || this.lastLoginFailureAt.plusMinutes(AuthService.RESET_LOGIN_AFTER_MINUS)
                    .isBefore(LocalDateTime.now())) {
                return true;
            }
        }
        return canLogin;
    }

    public User updateLoginFailure() {
        this.currentLoginFailureCount += 1;
        this.lastLoginFailureAt = LocalDateTime.now();
        return this;
    }

    public boolean canUpdateNickname() {
        return lastUpdateNickname == null || lastUpdateNickname
                .isBefore(LocalDateTime.now().minusDays(UserService.UPDATE_NICKNAME_DISTANCE_DAYS));
    }

    public User updateNickname(String nickname) {
        if (!canUpdateNickname())
            return this;
        this.nickname = nickname;
        this.lastUpdateNickname = LocalDateTime.now();
        return this;
    }

    public User updateRole(Role role) {
        this.role = role;
        return this;
    }

    public User updateProfile(UpdateRequestDto dto) {
        String fullName = dto.getFullName();
        String diocese = dto.getDiocese();
        String parish = dto.getParish();
        String gender = dto.getGender();
        String bio = dto.getBio();
        String birthday = dto.getBirthday();

        if (StringUtils.hasText(diocese)) {
            this.diocese = diocese;
        }
        if (StringUtils.hasText(parish)) {
            this.parish = parish;
        }
        if (StringUtils.hasText(gender)) {
            this.gender = EnumUtil.fromString(Gender.class, gender);
        }
        if (StringUtils.hasText(fullName)) {
            this.fullName = fullName;
        }
        if (StringUtils.hasText(bio)) {
            this.bio = bio;
        }
        if (StringUtils.hasText(birthday)) {
            this.birthday = CommonUtil.parseDateString(birthday + " 00:00:00");
        }
        this.lastUpdateProfile = LocalDateTime.now();

        return this;
    }

    private User updateTokenSecret() {
        this.tokenSecret = CommonUtil.generateSecureString(25);
        return this;
    }

    public User updateIsVerified() {
        this.isVerified = true;
        return this;
    }

    public User updateLastSendEmail() {
        this.lastSendEmail = LocalDateTime.now();
        return this;
    }

    public Boolean canSendEmail() {
        return lastSendEmail == null
                || lastSendEmail.plusMinutes(AuthService.EMAIL_SEND_DISTANCE_MINUS).isBefore(LocalDateTime.now());
    }

    public User updateLastOnline() {
        this.lastOnline = LocalDateTime.now();
        return this;
    }

}
