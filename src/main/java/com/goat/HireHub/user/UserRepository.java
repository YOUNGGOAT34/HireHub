package com.goat.HireHub.user;
import com.goat.HireHub.user.dto.UserSummary;
import com.goat.HireHub.security.AuthenticatedUser;
import com.goat.HireHub.user.dto.UserResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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
    @Query(
            value= """
                     SELECT new com.goat.HireHub.user.dto.UserResponse(u.id,firstName,lastName,username)
                     FROM User u
                     WHERE u.id= :userId
                    """
    )
    Optional<UserResponse> getUserById(@Param("userId")Long userId);
    @Modifying(clearAutomatically = true)
    @Query(
            value = """
                      DELETE FROM users
                      WHERE user_id= :userId
                    """,
            nativeQuery = true
    )
    void deleteUserById(@Param("userId")Long userId);

    @Modifying
    @Query(value= """
               UPDATE users\s
               SET first_name= COALESCE( :firstName,first_name),
                    last_name= COALESCE( :lastName,last_name),
                    email= COALESCE( :email,email),
                    username= COALESCE( :username,username)
               WHERE user_id= :userId
               AND(
                   first_name IS DISTINCT FROM COALESCE( :firstName,first_name)
                   OR last_name IS DISTINCT FROM COALESCE( :lastName,last_name)
                   OR email IS DISTINCT FROM COALESCE( :email,email)
                   OR username IS DISTINCT FROM COALESCE( :username,username)
               )
              \s
           \s""",
            nativeQuery = true
    )
    void patchUser(@Param("userId")Long userId,
                   @Param("firstName")String firstName,
                   @Param("lastName")String lastName,
                   @Param("email")String email,
                   @Param("username")String username
                   );

    @Modifying
    @Query(
            value= """
                     UPDATE users
                     SET first_name= :firstName,
                         last_name= :lastName,
                         email= :email,
                         username= :username
                     WHERE user_id= :userId
                     AND(
                         last_name IS DISTINCT FROM :lastName
                         OR first_name IS DISTINCT FROM :firstName
                         OR email IS DISTINCT FROM :email
                         OR username IS DISTINCT FROM :username
                     )
                    """,
            nativeQuery = true
    )
    void updateUser(@Param("userId")Long userId,
                    @Param("firstName")String firstName,
                    @Param("lastName")String lastName,
                    @Param("email")String email,
                    @Param("username")String username
    );

    @Modifying
    @Query(
            value= """
                      INSERT INTO users(user_id,email,first_name,last_name,username,password_hash)
                      VALUES(nextval('users_seq'), :email, :firstName, :lastName, :username, :passwordHash)
                    """,
            nativeQuery = true
    )
    void createAccount(@Param("email")String email,
                       @Param("firstName")String firstName,
                       @Param("lastName")String lastName,
                       @Param("username")String username,
                       @Param("passwordHash")String passwordHash
                       );

}
