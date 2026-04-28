package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.AuthRequest;
import com.oism.capitaltech.dto.AuthResponse;
import com.oism.capitaltech.dto.RegisterRequest;
import com.oism.capitaltech.dto.UserResponse;
import com.oism.capitaltech.entity.User;
import com.oism.capitaltech.repository.UserRepository;
import com.oism.capitaltech.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvestmentService investmentService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       JwtService jwtService,
                       UserService userService,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       InvestmentService investmentService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.investmentService = investmentService;
    }

    public UserResponse register(RegisterRequest request) {
        return userService.register(request);
    }

    public AuthResponse login(AuthRequest request) {
        // Verifica se o email existe antes de tentar autenticar
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuário não encontrado. Faça seu cadastro."));

        // Email existe — verifica a senha
        if (!passwordEncoder.matches(request.senha(), user.getSenha())) {
            throw new WrongPasswordException("Senha incorreta.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(userDetails);

        // Catch-up accrual: calculate any missing daily interest since last login
        try {
            investmentService.processAccrualForUser(user.getId());
        } catch (Exception e) {
            log.warn("Catch-up accrual failed for userId={}: {}", user.getId(), e.getMessage());
        }

        return new AuthResponse(token, UserResponse.fromEntity(user));
    }

    // Exceções específicas para diferenciar os casos no handler
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) { super(message); }
    }

    public static class WrongPasswordException extends RuntimeException {
        public WrongPasswordException(String message) { super(message); }
    }
}
