package mfy.server.global.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.user.entity.User;
import mfy.server.global.auth.TokenProvider;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import mfy.server.global.util.ResponseUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    private List<String> excludedPaths = Arrays.asList("/api/v1/auth/login", "/api/v1/auth/google/callback",
            "^/swagger-ui.*$", "^/v3/api-docs.*$", "^/ws.*$");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return excludedPaths.stream().anyMatch(excludedPath -> path.matches(excludedPath));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = tokenProvider.getAccessTokenFromRequest(request);
        if (!StringUtils.hasText(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        User user;
        try {
            user = tokenProvider.validateToken(accessToken, "access");
        } catch (Exception e) {
            ResponseUtil.fail(response, HttpStatus.UNAUTHORIZED, e.getMessage());
            return;
        }

        setAuthentication(user);
        filterChain.doFilter(request, response);
    }

    public void setAuthentication(User user) {
        Authentication authentication = createAuthentication(user);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(User user) {
        UserDetails userDetails = new UserDetailsImpl(user);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}