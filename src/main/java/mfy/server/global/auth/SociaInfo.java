package mfy.server.global.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SociaInfo {
    private final String id;
    private final String email;
    private final String avatar;
}
