package io.wisoft.wasabi.domain.auth.dto;

import io.wisoft.wasabi.domain.auth.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateMemberRequest(
        @NotBlank(message = "Email을 입력해주세요.") @Email String email,
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{4,20}",
                message = "비밀번호는 영문과 숫자가 포함된 4자 ~ 20자의 비밀번호여야 합니다.")
        @NotBlank(message = "password를 입력하세요.")
        String password,
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{4,20}",
                message = "비밀번호는 영문과 숫자가 포함된 4자 ~ 20자의 비밀번호여야 합니다.") @NotBlank(message = "password를 입력하세요.")
        String checkPassword,
        @NotBlank String name,
        @NotBlank String phoneNumber,
        @NotNull Role role
) {
    public CreateMemberRequest {
        if (!password.equals(checkPassword)) {
            throw new IllegalArgumentException("비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCheckPassword() {
        return checkPassword;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public Role getRole() {
        return role;
    }
}