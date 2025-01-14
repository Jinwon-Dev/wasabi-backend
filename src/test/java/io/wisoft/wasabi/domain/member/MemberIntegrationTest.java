package io.wisoft.wasabi.domain.member;

import autoparams.AutoSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wisoft.wasabi.domain.member.application.MemberRepository;
import io.wisoft.wasabi.domain.member.web.dto.UpdateMemberInfoRequest;
import io.wisoft.wasabi.domain.member.persistence.Member;
import io.wisoft.wasabi.domain.member.persistence.Part;
import io.wisoft.wasabi.global.config.common.Const;
import io.wisoft.wasabi.global.config.common.jwt.JwtTokenProvider;
import io.wisoft.wasabi.setting.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MemberIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("회원 개인 정보 수정")
    class UpdateInfo {

        @ParameterizedTest
        @AutoSource
        @DisplayName("요청시 정상적으로 수정되어야 한다.")
        void update_info(final Member member) throws Exception {

            // given
            final Member savedMember = memberRepository.save(member);

            final String accessToken = jwtTokenProvider.createAccessToken(savedMember.getId(), member.getName(), member.getRole(), true);

            final var request = new UpdateMemberInfoRequest(
                    "name",
                    "phoneNumber",
                    "referenceUrl",
                    Part.BACKEND,
                    "organization",
                    "motto"
            );

            final String json = objectMapper.writeValueAsString(request);

            // when
            final var result = mockMvc.perform(patch("/members")
                    .contentType(APPLICATION_JSON)
                    .header(Const.AUTH_HEADER, Const.TOKEN_TYPE + " " + accessToken)
                    .content(json));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.memberId").exists());
        }

        @Test
        @DisplayName("요청시 로그인 상태여야 한다.")
        void update_info_fail() throws Exception {

            // given
            final var request = new UpdateMemberInfoRequest(
                    "name",
                    "phoneNumber",
                    "referenceUrl",
                    Part.BACKEND,
                    "organization",
                    "motto"
            );

            final String json = objectMapper.writeValueAsString(request);

            // when
            final var result = mockMvc.perform(patch("/members")
                    .contentType(APPLICATION_JSON)
                    .content(json));

            // then
            result.andExpect(status().isUnauthorized());

        }
    }

    @Nested
    @DisplayName("개인 정보 조회")
    class ReadMemberInfo {

        @ParameterizedTest
        @AutoSource
        @DisplayName("토큰이 유효하여, 개인 정보 조회에 성공한다.")
        void read_member_info_success(final Member member) throws Exception {

            //given
            memberRepository.save(member);

            final String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getName(), member.getRole(), true);

            //when
            final var result = mockMvc.perform(get("/members")
                    .contentType(APPLICATION_JSON)
                    .header(Const.AUTH_HEADER, Const.TOKEN_TYPE + " " + accessToken));

            //then
            result.andExpect(status().isOk());
        }
    }
}
