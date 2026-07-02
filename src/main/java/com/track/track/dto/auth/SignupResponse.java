package com.track.track.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {
    private Long memberId;
    private String email;
    private String nickname;
}
