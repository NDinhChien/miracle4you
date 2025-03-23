package mfy.server.global.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class UserInfo {
    private final String type;
    private final String email;
    private final String role;
}