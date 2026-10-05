package com.finance.tracker.service;

import com.finance.tracker.dao.UserDao;
import com.finance.tracker.dto.LoginRequestDTO;
import com.finance.tracker.dto.LoginAndRegisterResponseDTO;
import com.finance.tracker.dto.RegisterResuestDTO;
import com.finance.tracker.exception.InvalidCredentialsException;
import com.finance.tracker.exception.UserAlreadyExistsException;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.model.User;
import com.finance.tracker.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {
    @Autowired
    UserDao userDao;

    @Autowired
    JWTUtil jwtUtil;

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoginAndRegisterResponseDTO register(RegisterResuestDTO registerResuestDTO) {
        if (userDao.findByEmail(registerResuestDTO.email()).isPresent()) {
            throw new UserAlreadyExistsException("This user already exists.");
        }
        String encryptedRegisterPassword = bCryptPasswordEncoder.encode(registerResuestDTO.password());
        User newUser = new User();
        newUser.setUserName(registerResuestDTO.userName());
        newUser.setEmail(registerResuestDTO.email());
        newUser.setPassword(encryptedRegisterPassword);
        userDao.save(newUser);

        return new LoginAndRegisterResponseDTO(jwtUtil.generateToken(registerResuestDTO.email()));
    }

    public LoginAndRegisterResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User existingUser = userDao.findByEmail(loginRequestDTO.email())
                .orElseThrow(() -> new UserNotFoundException("No user found with this credentials."));

        if (!bCryptPasswordEncoder.matches(loginRequestDTO.password(), existingUser.getPassword())) {
            throw new InvalidCredentialsException("Credentials Invalid!");
        }

        return new LoginAndRegisterResponseDTO(jwtUtil.generateToken(loginRequestDTO.email()));
    }
}
