package mfy.server.domain.message.service.type;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import mfy.server.domain.user.entity.User;

@Getter
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
public class OnlineUser {

    private Long id;

    private String nickname;

    private String fullName;

    private String avatar;

    private String email;

    private Boolean isOnline;

    private LocalDateTime lastOnline;

    public OnlineUser(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.fullName = user.getFullName();
        this.avatar = user.getAvatar();
        this.email = user.getEmail();
        this.isOnline = true;
        this.lastOnline = LocalDateTime.now();
    }

    public OnlineUser updateIsOnline() {
        this.isOnline = true;
        return this;
    }

    public OnlineUser updateLastOnline() {
        this.isOnline = false;
        this.lastOnline = LocalDateTime.now();
        return this;
    }
}