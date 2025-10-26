package com.fooddelivery.user.service;

import com.fooddelivery.user.dto.AuthResponseDTO;
import com.fooddelivery.user.dto.LoginRequestDTO;
import com.fooddelivery.user.dto.RegisterRequestDTO;
import com.fooddelivery.user.dto.UserResponseDTO;
import com.fooddelivery.user.entity.User;
import com.fooddelivery.user.enums.UserStatus;
import com.fooddelivery.user.exception.UserAlreadyExistsException;
import com.fooddelivery.user.exception.UserNotFoundException;
import com.fooddelivery.user.repository.UserRepository;
import com.fooddelivery.user.security.JwtTokenProvider;
import com.fooddelivery.user.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private static final long MILLIS_TO_SECONDS = 1000L;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Registering new user with email: {}", request.email());
        
        String encodedPassword = passwordEncoder.encode(request.password());
        
        User savedUser;
        try {
            savedUser = createUser(request, encodedPassword);
            log.info("User registered successfully with id: {}", savedUser.getId());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Registration failed - duplicate email or phone: {}", request.email());
            
            if (userRepository.existsByEmail(request.email())) {
                throw new UserAlreadyExistsException("Email already in use");
            }
            if (userRepository.existsByPhone(request.phone())) {
                throw new UserAlreadyExistsException("Phone number already in use");
            }
            throw new UserAlreadyExistsException("Registration failed - duplicate data");
        }
        
        String token = tokenProvider.generateToken(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole().name()
        );
        
        UserResponseDTO userResponse = userMapper.toResponse(savedUser);
        
        return new AuthResponseDTO(token, tokenProvider.getExpirationMs() / MILLIS_TO_SECONDS, userResponse);
    }
    
    @Transactional
    private User createUser(RegisterRequestDTO request, String encodedPassword) {
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(encodedPassword);
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        
        return userRepository.save(user);
    }
    
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("User login attempt: {}", request.email());
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = findUserById(userPrincipal.getId());
        
        UserResponseDTO userResponse = userMapper.toResponse(user);
        
        log.info("User logged in successfully: {}", request.email());
        
        return new AuthResponseDTO(token, tokenProvider.getExpirationMs() / MILLIS_TO_SECONDS, userResponse);
    }
    
    @Transactional(readOnly = true)
    private User findUserById(Long userId) {
        return userRepository.findActiveById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}

