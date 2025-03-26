package mfy.server.domain.user.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import mfy.server.domain.user.dto.UserResponseDto.IUserBasicDto;
import mfy.server.domain.user.dto.UserResponseDto.IUserPublicDto;
import mfy.server.domain.user.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    <T> Optional<T> findByEmail(String email, Class<T> type);

    Optional<User> findById(Long id);

    Optional<IUserBasicDto> findUserBasicById(Long id);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByNickname(String username);

    boolean existsByEmail(String email);

    @Query("SELECT MAX(CAST(SUBSTRING(u.nickname, 4) AS int)) FROM User u WHERE u.nickname LIKE 'User%'")
    Integer findMaxNicknameSequence();

    Optional<IUserPublicDto> findUserById(Long id);

    @Query("""
                SELECT u.id as id, u.nickname as nickname, u.avatar as avatar, u.score as score, u.fullName as fullName, u.joinedAt as joinedAt FROM User u
                WHERE u.nickname ILIKE %:name% OR u.fullName ILIKE %:name%
            """)
    Page<IUserBasicDto> searchUsers(String name, Pageable pageable);

    Page<IUserBasicDto> findAllBy(Pageable pageable);

    @Query(value = """
            SELECT u.id as id, u.nickname as nickname, u.avatar as avatar, u.score as score, u.joinedAt as joinedAt, u.fullName as fullName
            FROM User u WHERE u.id IN :userIds
            """)
    List<IUserBasicDto> findInIds(Set<Long> userIds);

    @Modifying
    @Query("UPDATE User u SET u.lastOnline = :lastOnline WHERE u.id = :id")
    void updateLastOnline(Long id, Instant lastOnline);
}
