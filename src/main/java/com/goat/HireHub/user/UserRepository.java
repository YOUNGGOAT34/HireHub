package com.goat.HireHub.user;

import com.goat.HireHub.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
      Optional<User> findByEmailIgnoreCase(String email);
      Boolean existsByEmailIgnoreCase(String email);
      List<UserResponse> findByFirstNameContainingIgnoreCase(String firstName);
}
