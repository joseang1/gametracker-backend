package com.josedev.gametracker.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.josedev.gametracker.auth.dtos.AuthDTOResponse;
import com.josedev.gametracker.auth.dtos.LoginDTORequest;
import com.josedev.gametracker.auth.dtos.RegisterDTORequest;
import com.josedev.gametracker.security.TokenService;
import com.josedev.gametracker.user.Role;
import com.josedev.gametracker.user.UserEntity;
import com.josedev.gametracker.user.UserRepository;
import com.josedev.gametracker.user.dtos.UserDTOResponse;
import com.josedev.gametracker.user.exceptions.UserAlreadyExistsException;
import com.josedev.gametracker.user.exceptions.UserNotFoundException;
import com.josedev.gametracker.user.mappers.UserMapper;

@Service
public class AuthServiceImpl implements InterfaceAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public AuthDTOResponse register(RegisterDTORequest dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.username()))
            throw new UserAlreadyExistsException("Username '" + dto.username() + "' is already taken");

        if (userRepository.existsByEmailIgnoreCase(dto.email()))
            throw new UserAlreadyExistsException("Email is already registered");

        UserEntity user = new UserEntity(dto.username(), dto.email().toLowerCase(),
                passwordEncoder.encode(dto.password()), Role.USER);
        UserEntity saved = userRepository.save(user);

        return new AuthDTOResponse(tokenService.generateToken(saved), UserMapper.toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthDTOResponse login(LoginDTORequest dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));

        UserEntity user = findUser(dto.username());
        return new AuthDTOResponse(tokenService.generateToken(user), UserMapper.toDTO(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTOResponse getCurrentUser(String username) {
        return UserMapper.toDTO(findUser(username));
    }

    private UserEntity findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User '" + username + "' not found"));
    }

}
