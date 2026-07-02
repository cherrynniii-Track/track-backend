package com.track.track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 통합 테스트
 */
@Testcontainers
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    // HTTP 요청 보내는 객체
    @Autowired
    private MockMvc mockMvc;

    // Java 객체를 JSON 문자열로 변환해주는 객체
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 회원가입 성공 테스트
     */
    @Test
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {
        Map<String, String> request = Map.of(
                "email", "test@test.com",
                "password", "1234",
                "nickname", "테스트"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스트"));
    }

    /**
     * 같은 이메일로 회원가입하면 409가 반환되는지 테스트
     */
    @Test
    @DisplayName("중복 이메일 회원가입 실패")
    void signup_duplicateEmail_fail() throws Exception {
        Map<String, String> request = Map.of(
                "email", "duplicate@test.com",
                "password", "1234",
                "nickname", "테스트"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    /**
     * 로그인 성공 테스트
     */
    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        Map<String, String> signupRequest = Map.of(
                "email", "login@test.com",
                "password", "1234",
                "nickname", "테스트"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = Map.of(
                "email", "login@test.com",
                "password", "1234"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    /**
     * 비밀번호가 틀리면 로그인 실패하는지 테스트
     */
    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_wrongPassword_fail() throws Exception {
        Map<String, String> signupRequest = Map.of(
                "email", "wrong@test.com",
                "password", "1234",
                "nickname", "테스트"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = Map.of(
                "email", "wrong@test.com",
                "password", "wrong-password"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}