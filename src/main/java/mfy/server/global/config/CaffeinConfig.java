package mfy.server.global.config;

import java.time.Duration;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;

@EnableCaching
@Configuration
public class CaffeinConfig {
    @Bean
    Caffeine<Object, Object> caffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(1024)
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofMinutes(5));
    }

    @Bean("caffeinCacheManager")
    CacheManager caffeinCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager("Users", "Projects", "SystemMessages",
                "GlobalMessages", "PrivateMessages", "ProjectMessages", "Notifications");
        caffeineCacheManager.setCaffeine(caffeine());
        return caffeineCacheManager;
    }

    @Bean("usersCache")
    Cache usersCache(CacheManager cacheManager) {
        return cacheManager.getCache("Users");
    }

    @Bean("projectsCache")
    Cache projectsCache(CacheManager cacheManager) {
        return cacheManager.getCache("Projects");
    }

    @Bean("notificationsCache")
    Cache notificationsCache(CacheManager cacheManager) {
        return cacheManager.getCache("Notifications");
    }

    @Bean("systemMessagesCache")
    Cache systemMessagesCache(CacheManager cacheManager) {
        return cacheManager.getCache("SystemMessages");
    }
}
