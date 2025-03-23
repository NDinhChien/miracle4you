package mfy.server.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import mfy.server.global.auth.TokenProvider;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@OpenAPIDefinition(info = @Info(title = "APIs", version = "v1.0"), security = {
                @SecurityRequirement(name = "bearerToken"), @SecurityRequirement(name = "access-cookie"),
                @SecurityRequirement(name = "refresh-cookie") }, servers = { @Server(url = "/") })
@SecuritySchemes({
                @SecurityScheme(name = "bearerToken", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT"),
                @SecurityScheme(name = "accessCookie", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE, paramName = TokenProvider.AUTHORIZATION_HEADER),
})
@Configuration
public class SwaggerConfig {
}
