package com.example.miniproject.service;

import com.example.miniproject.config.jwt.JwtUtil;
import com.example.miniproject.dto.LoginRequestDto;
import com.example.miniproject.dto.SignupRequestDto;
import com.example.miniproject.dto.UserIdRequestDto;
import com.example.miniproject.entity.RefreshToken;
import com.example.miniproject.entity.User;
import com.example.miniproject.repository.TokenRepository;
import com.example.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String userId = loginRequestDto.getUserId();
        String password = loginRequestDto.getPassword();

        User user = userRepository.findByUserId(userId).orElseThrow(
                () -> new IllegalArgumentException("넌 오류야야야ㅑ"));// 예외처리 해주기

        System.out.println(password);
        System.out.println(user.getPassword());

//        if (!passwordEncoder.matches(password,user.getPassword())) {
//            throw new IllegalStateException("난 오류야야야야");
//        }

        String accessToken = jwtUtil.createAccessToken(user.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(user.getUserId());

        tokenRepository.save(new RefreshToken(refreshToken));

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);
        response.addHeader(JwtUtil.REFRESHTOKEN_HEADER, refreshToken);
    }

    //아이디 중복확인
    @Transactional
    public void checkId(UserIdRequestDto userIdRequestDto){
        String userId = userIdRequestDto.getUserId();

        if(userRepository.existsByUserId(userId)){
            throw new IllegalStateException("이미 아이디가 존재합니다.");
        }
    }
    //회원가입
    @Transactional
    public void signup(SignupRequestDto signupRequestDto){
        String userId = signupRequestDto.getUserId();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());
        String nickname = signupRequestDto.getNickname();
        String name = signupRequestDto.getName();

        Optional<User> found = userRepository.findByUserId(userId);
        if(userRepository.existsByUserId(userId)){
            throw new IllegalStateException("아이디 중복확인을 해주세요.");
        }
        User user = new User(userId,passwordEncoder.encode(password),nickname,name);
        userRepository.save(user);
    }
}
