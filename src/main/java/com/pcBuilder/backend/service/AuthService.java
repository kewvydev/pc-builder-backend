package com.pcBuilder.backend.service;

import com.pcBuilder.backend.dto.AuthResponse;
import com.pcBuilder.backend.dto.LoginRequest;
import com.pcBuilder.backend.dto.SignUpRequest;
import com.pcBuilder.backend.exception.AuthenticationException;
import com.pcBuilder.backend.exception.ValidationException;
import com.pcBuilder.backend.model.user.AppUser;
import com.pcBuilder.backend.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service layer for simple signup/login workflows (development only, no hashing).
 */
@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AppUserRepository appUserRepository;

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AuthResponse signUp(SignUpRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Las contraseñas no coinciden");
        }

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException("El email ya está registrado");
        }

        AppUser user = AppUser.builder()
                .email(email)
                .password(request.getPassword())
                .nickname(request.getNickname())
                .isActive(Boolean.TRUE)
                .build();

        AppUser saved = appUserRepository.save(user);
        log.info("Usuario creado con id {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthenticationException("Credenciales inválidas"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new AuthenticationException("Usuario inactivo");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new AuthenticationException("Credenciales inválidas");
        }

        return toResponse(user);
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ValidationException("El email no puede estar vacío");
        }
        return email.trim().toLowerCase();
    }

    private AuthResponse toResponse(AppUser user) {
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .active(Boolean.TRUE.equals(user.getIsActive()))
                .admin(Boolean.TRUE.equals(user.getIsAdmin()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}

