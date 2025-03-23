package mfy.server.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.repository.UserRepository;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            return new UsernameNotFoundException(ErrorMessage.USER_NOT_FOUND);
        });
        return new UserDetailsImpl(user);
    }

}