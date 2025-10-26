package com.fooddelivery.user.service;

import com.fooddelivery.common.security.SecurityUtils;
import com.fooddelivery.user.dto.*;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.entity.UserAddress;
import com.fooddelivery.user.enums.UserRole;
import com.fooddelivery.user.enums.UserStatus;
import com.fooddelivery.user.exception.UserAlreadyExistsException;
import com.fooddelivery.user.exception.UserNotFoundException;
import com.fooddelivery.user.mapper.UserMapper;
import com.fooddelivery.user.repository.UserRepository;
import com.fooddelivery.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private UserResponseDTO userResponse;
    private AuthResponseDTO authResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setPhone("+1234567890");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);

        registerRequest = new RegisterRequestDTO(
            "test@example.com",
            "password123",
            "+1234567890",
            "John",
            "Doe",
            UserRole.CUSTOMER
        );

        loginRequest = new LoginRequestDTO("test@example.com", "password123");

        userResponse = new UserResponseDTO(
            1L,
            "test@example.com",
            "+1234567890",
            "John",
            "Doe",
            UserRole.CUSTOMER,
            UserStatus.ACTIVE,
            List.of()
        );

        authResponse = new AuthResponseDTO(
            "access-token",
            "refresh-token",
            "Bearer",
            86400L,
            userResponse
        );
    }

    @Test
    void register_Success() {
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateToken(anyLong(), anyString(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(anyLong())).thenReturn("refresh-token");
        when(tokenProvider.getTokenExpiration()).thenReturn(86400L);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        AuthResponseDTO result = userService.register(registerRequest);

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_EmailAlreadyExists() {
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequest));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.generateToken(anyLong(), anyString(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(anyLong())).thenReturn("refresh-token");
        when(tokenProvider.getTokenExpiration()).thenReturn(86400L);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        AuthResponseDTO result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.login(loginRequest));
    }

    @Test
    void login_WrongPassword() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.login(loginRequest));
    }

    @Test
    void getProfile_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(userResponse);

            UserResponseDTO result = userService.getProfile();

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("test@example.com", result.email());
        }
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequestDTO updateRequest = new UpdateProfileRequestDTO(
            "+9876543210",
            "Jane",
            "Smith"
        );

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            UserResponseDTO result = userService.updateProfile(updateRequest);

            assertNotNull(result);
            verify(userRepository).save(testUser);
            assertEquals("+9876543210", testUser.getPhone());
            assertEquals("Jane", testUser.getFirstName());
            assertEquals("Smith", testUser.getLastName());
        }
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);

        UserResponseDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void deleteUser_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            userService.deleteUser();

            assertNotNull(testUser.getDeletedAt());
            assertEquals(UserStatus.DELETED, testUser.getStatus());
            verify(userRepository).save(testUser);
        }
    }
}

