package com.finance.tracker.service;

import com.finance.tracker.dao.UserDao;
import com.finance.tracker.dto.LoginAndRegisterResponseDTO;
import com.finance.tracker.dto.LoginRequestDTO;
import com.finance.tracker.dto.RegisterResuestDTO;
import com.finance.tracker.exception.InvalidCredentialsException;
import com.finance.tracker.exception.UserAlreadyExistsException;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.utils.JWTUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.finance.tracker.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserDao userDao;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    JWTUtil jwtUtil;

    @InjectMocks
    AuthService authService;

    @Test
    void registerNewUserTest() {
        RegisterResuestDTO registerResuestDTO = new RegisterResuestDTO("Alice", "alice@test.com", "Alice123");
        Mockito.when(userDao.findByEmail(registerResuestDTO.email())).thenReturn(Optional.empty());
        Mockito.when(jwtUtil.generateToken(registerResuestDTO.email())).thenReturn("token");

        assertEquals("token", authService.register(registerResuestDTO).token());
        verify(userDao).save(any(User.class));
    }

    @Test
    void registerExistingUserTest() {
        RegisterResuestDTO registerResuestDTO = new RegisterResuestDTO("Alice", "alice@test.com", "Alice123");
        Mockito.when(userDao.findByEmail(registerResuestDTO.email())).thenReturn(Optional.of(new User()));
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerResuestDTO));
        verify(userDao, never()).save(any());
    }

    @Test
    void loginNewUserTest() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("alice@test.com", "Alice123");
        Mockito.when(userDao.findByEmail(loginRequestDTO.email())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(loginRequestDTO));
    }

    @Test
    void loginUserWithIncorrectCredentialsTest() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("alice@test.com", "Alice123");
        User mockUser = new User();
        mockUser.setPassword("password");
        Mockito.when(userDao.findByEmail(loginRequestDTO.email())).thenReturn(Optional.of(mockUser));
        Mockito.when(passwordEncoder.matches(loginRequestDTO.password(), "password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequestDTO));
    }

    @Test
    void loginUserWithCorrectCredentialsTest() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("alice@test.com", "Alice123");
        User mockUser = new User();
        mockUser.setPassword("password");
        Mockito.when(userDao.findByEmail(loginRequestDTO.email())).thenReturn(Optional.of(mockUser));
        Mockito.when(passwordEncoder.matches(loginRequestDTO.password(), "password")).thenReturn(true);
        Mockito.when(jwtUtil.generateToken(loginRequestDTO.email())).thenReturn("token");

        assertEquals("token", authService.login(loginRequestDTO).token());
    }
}