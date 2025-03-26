package mfy.server.domain.user.entity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private Instant birthday;

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
    private Instant lastUpdateNickname;

    @Column(nullable = false)
    private Instant lastUpdateProfile;

    @Column
    private Instant lastSendEmail;

    @Column
    private Instant lastLoginSuccessAt;

    @Column
    private Instant lastLoginFailureAt;

    @Column
    private Instant lastOnline;

    @Column(nullable = false)
    private Integer currentLoginFailureCount;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

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
            joinedAt = Instant.now();
        }
        if (score == null) {
            score = 0;
        }
        if (nickname != null && lastUpdateNickname == null) {
            lastUpdateNickname = Instant.now();
        }
        if (lastUpdateProfile == null) {
            lastUpdateProfile = Instant.now();
        }
        if (currentLoginFailureCount == null) {
            currentLoginFailureCount = 0;
        }
        if (tokenSecret == null) {
            updateTokenSecret();
        }
        if (lastOnline == null) {
            lastOnline = Instant.now();
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
        this.lastLoginSuccessAt = Instant.now();
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
            if (lastLoginFailureAt == null
                    || this.lastLoginFailureAt.plus(AuthService.RESET_LOGIN_AFTER_MINUS, ChronoUnit.MINUTES)
                            .isBefore(Instant.now())) {
                return true;
            }
        }
        return canLogin;
    }

    public User updateLoginFailure() {
        this.currentLoginFailureCount += 1;
        this.lastLoginFailureAt = Instant.now();
        return this;
    }

    public boolean canUpdateNickname() {
        return lastUpdateNickname == null || lastUpdateNickname
                .isBefore(Instant.now().minus(UserService.UPDATE_NICKNAME_DISTANCE_DAYS, ChronoUnit.DAYS));
    }

    public User updateNickname(String nickname) {
        if (!canUpdateNickname())
            return this;
        this.nickname = nickname;
        this.lastUpdateNickname = Instant.now();
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
        Gender gender = dto.getGender();
        String bio = dto.getBio();
        Instant birthday = dto.getBirthday();

        if (StringUtils.hasText(diocese)) {
            this.diocese = diocese;
        }
        if (StringUtils.hasText(parish)) {
            this.parish = parish;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (StringUtils.hasText(fullName)) {
            this.fullName = fullName;
        }
        if (StringUtils.hasText(bio)) {
            this.bio = bio;
        }
        if (birthday != null) {
            this.birthday = birthday;
        }
        this.lastUpdateProfile = Instant.now();

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
        this.lastSendEmail = Instant.now();
        return this;
    }

    public Boolean canSendEmail() {
        return lastSendEmail == null
                || lastSendEmail.plus(AuthService.EMAIL_SEND_DISTANCE_MINUS, ChronoUnit.MINUTES)
                        .isBefore(Instant.now());
    }

    public User updateLastOnline() {
        this.lastOnline = Instant.now();
        return this;
    }

}
