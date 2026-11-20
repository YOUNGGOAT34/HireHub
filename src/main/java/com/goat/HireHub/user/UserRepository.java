package com.goat.HireHub.user;
import com.goat.HireHub.user.dto.UserSummary;
import com.goat.HireHub.security.AuthenticatedUser;
import com.goat.HireHub.user.dto.UserResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
      Optional<UserSummary> findByEmailIgnoreCase(String email);
      Boolean existsByEmailIgnoreCase(String email);
      @Query(
              """
                      SELECT new com.goat.HireHub.security.AuthenticatedUser(
                          u.id,
                          u.email,
                          u.passwordHash
                      )
                      FROM User u
                      WHERE UPPER(u.email)=UPPER( :email)
                      """
      )
      Optional<AuthenticatedUser> findAuthenticatedUserByEmail(@Param("email")String email);

    @Query(
            value = """
                      SELECT new com.goat.HireHub.user.dto.UserResponse(u.id,firstName,lastName,username)
                      FROM User u
                    """
    )
    Slice<UserResponse> findAllUsers(Pageable pageable);

}
