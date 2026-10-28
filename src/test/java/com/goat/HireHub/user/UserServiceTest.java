package com.goat.HireHub.user;

import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.*;
import com.goat.HireHub.user.dto.UserPatchRequest;
import com.goat.HireHub.user.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

        @Mock UserRepository userRepository;
        @Mock PasswordEncoder passwordEncoder;
        @Mock SecurityUtils securityUtils;

        @InjectMocks UserService userService;

        @Test
        void registerUser_whenEmailTaken_throwsConflictAndSavesNothing() {
            when(userRepository.existsByEmailIgnoreCase("kim@x.com")).thenReturn(true);

            assertThrows(ConflictException.class,
                    () -> userService.registerUser(new UserRequest("Lucas","Kim@X.com ","me","goat","hello")));

            verify(userRepository,never()).save(any());

        }

        @Test
        void registerUser_normalizesEmailAndHashesPassword() {
            when(userRepository.existsByEmailIgnoreCase("kim@x.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("HASHED");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            userService.registerUser(registerRequest("  Kim@X.com "));
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();
            assertEquals("kim@x.com", saved.getEmail());
            assertEquals("HASHED", saved.getPassword());
        }

        // ---------- patch ----------

        @Test
        void patchUser_toEmailOwnedBySomeoneElse_throwsConflict() {
            User me = userWithEmail("me@x.com");
            when(securityUtils.getCurrentUser()).thenReturn(me);
            when(userRepository.existsByEmailIgnoreCase("taken@x.com")).thenReturn(true);

            assertThrows(ConflictException.class,
                    () -> userService.patchUser(patchRequestWithEmail("taken@x.com")));

            assertEquals("me@x.com", me.getEmail());       // unchanged
        }

        @Test
        void patchUser_sameEmailInDifferentCase_doesNotCountAsTaken() {
            User me = userWithEmail("me@x.com");
            when(securityUtils.getCurrentUser()).thenReturn(me);

            userService.patchUser(patchRequestWithEmail(" ME@x.com "));

            verify(userRepository, never()).existsByEmailIgnoreCase(any());
        }



        @Test
        void deleteUserById_deletesTheCurrentUserOnly() {
            User me = userWithEmail("me@x.com");
            when(securityUtils.getCurrentUser()).thenReturn(me);

            userService.deleteUserById();
            verify(userRepository).delete(me);
        }


        private User userWithEmail(String email) {
            User u = new User();
            u.setEmail(email);
            u.setFirstName("Kim");
            u.setLastName("Otieno");
            return u;
        }

        private UserRequest registerRequest(String email) {
            return new UserRequest("Lucas",email,"me","goat","hello");
        }

        private UserPatchRequest patchRequestWithEmail(String email) {
            return new UserPatchRequest(null,null,email,null,null);
        }
    }


