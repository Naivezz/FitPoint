package com.naivez.fithub.unit.security;

import com.naivez.fithub.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String USERNAME = "user1@gmail.com";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400000L);

        userDetails = User.builder()
                .username(USERNAME)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(userDetails);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(USERNAME, extractedUsername);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidUsername_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = User.builder()
                .username("different@gmail.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        boolean isValid = jwtUtil.validateToken(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1L);
        String token = jwtUtil.generateToken(userDetails);

        Thread.sleep(10);

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtUtil.validateToken(token, userDetails);
        });
    }

//    @Test
//    void extractExpiration_ShouldReturnFutureDate() {
//        String token = jwtUtil.generateToken(userDetails);
//
//        Date expiration = jwtUtil.extractExpiration(token);
//
//        assertTrue(expiration.after(new Date()));
//    }

//    @Test
//    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
//        String token = jwtUtil.generateToken(userDetails);
//
//        boolean isExpired = ReflectionTestUtils.invokeMethod(jwtUtil, "isTokenExpired", token);
//
//        assertFalse(isExpired);
//    }
}