package io.wisoft.wasabi.domain.board.web;

import autoparams.AutoSource;
import autoparams.customization.Customization;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wisoft.wasabi.customization.NotSaveBoardCustomization;
import io.wisoft.wasabi.customization.composite.BoardCompositeCustomizer;
import io.wisoft.wasabi.domain.auth.exception.TokenNotExistException;
import io.wisoft.wasabi.domain.board.application.BoardMapper;
import io.wisoft.wasabi.domain.board.persistence.Board;
import io.wisoft.wasabi.domain.board.web.dto.*;
import io.wisoft.wasabi.domain.like.web.LikeService;
import io.wisoft.wasabi.domain.like.web.dto.RegisterLikeRequest;
import io.wisoft.wasabi.domain.member.persistence.Member;
import io.wisoft.wasabi.domain.member.persistence.Role;
import io.wisoft.wasabi.domain.tag.persistence.Tag;
import io.wisoft.wasabi.global.config.common.Const;
import io.wisoft.wasabi.global.config.web.resolver.AnyoneResolver;
import io.wisoft.wasabi.global.config.web.resolver.MemberIdResolver;
import io.wisoft.wasabi.global.config.common.jwt.JwtTokenProvider;
import io.wisoft.wasabi.global.config.web.interceptor.AdminInterceptor;
import io.wisoft.wasabi.global.config.web.response.ResponseAspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.wisoft.wasabi.domain.board.BoardListToSliceMapper.createBoardList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private BoardImageService boardImageService;

    @MockBean
    private LikeService likeService;

    @MockBean
    private MemberIdResolver memberIdResolver;

    @MockBean
    private AdminInterceptor adminInterceptor;

    @SpyBean
    private JwtTokenProvider jwtTokenProvider;

    @SpyBean
    private AnyoneResolver anyoneResolver;

    @Spy
    private ObjectMapper objectMapper;

    @SpyBean
    private ResponseAspect responseAspect;

    @Nested
    @DisplayName("게시글 작성")
    class WriteBoard {

        @Test
        @DisplayName("요청시 정상적으로 저장되어야 한다.")
        void write_board() throws Exception {

            // given
            final String accessToken = jwtTokenProvider.createAccessToken(1L, "writer", Role.GENERAL, true);

            final var request = new WriteBoardRequest(
                    "title",
                    "content",
                    "tag",
                    new String[]{"imageUrls"},
                    new ArrayList<>());

            final var response = new WriteBoardResponse(1L);

            given(boardService.writeBoard(any(), any())).willReturn(response);

            // when
            final var perform = mockMvc.perform(
                    post("/boards")
                            .contentType(APPLICATION_JSON)
                            .header(Const.AUTH_HEADER, Const.TOKEN_TYPE + " " + accessToken)
                            .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isCreated());
        }
    }


    @Nested
    @DisplayName("게시글 조회")
    class ReadBoard {

        @ParameterizedTest
        @AutoSource
        @DisplayName("요청이 성공적으로 수행되어, 댓글이 없는 게시글 조회에 성공한다.")
        void read_board_success(final ReadBoardResponse.Writer writer) throws Exception {

            //given
            final UUID sessionId = UUID.randomUUID();
            final MockHttpSession session = new MockHttpSession(null, sessionId.toString());

            final Long boardId = 1L;

            final var response = new ReadBoardResponse(
                    1L,
                    "title",
                    "content",
                    writer,
                    LocalDateTime.now(),
                    0,
                    1,
                    false,
                    null,
                    null
            );

            given(boardService.readBoard(any(), any(), anyBoolean())).willReturn(response);

            //when
            final var result = mockMvc.perform(
                    get("/boards/{boardId}", boardId)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON)
                            .session(session));

            //then
            result.andExpect(status().isOk());
        }

        @ParameterizedTest
        @AutoSource
        @DisplayName("요청이 성공적으로 수행되어, 댓글이 있는 게시글 조회에 성공한다.")
        void read_board_success(final ReadBoardResponse.Writer writer, final List<ReadBoardResponse.Comment> comments) throws Exception {

            //given
            final UUID sessionId = UUID.randomUUID();
            final MockHttpSession session = new MockHttpSession(null, sessionId.toString());

            final Long boardId = 1L;

            final var response = new ReadBoardResponse(
                    1L,
                    "title",
                    "content",
                    writer,
                    LocalDateTime.now(),
                    0,
                    1,
                    false,
                    null,
                    comments
            );

            given(boardService.readBoard(any(), any(), anyBoolean())).willReturn(response);

            //when
            final var result = mockMvc.perform(
                    get("/boards/{boardId}", boardId)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON)
                            .session(session));

            //then
            result.andExpect(status().isOk());
        }

        @ParameterizedTest
        @AutoSource
        @DisplayName("게시글 조회수 순 정렬 후 조회시, 조회수 가장 많은 게시글이 먼저 조회된다.")
        @Customization(NotSaveBoardCustomization.class)
        void read_boards_order_by_views(
                final Board board1,
                final Board board2) throws Exception {

            //given
            board2.increaseView();
            final var boardList = createBoardList(board2, board1);

            given(boardService.getBoardList(any(), any(), any())).willReturn(boardList);

            //when
            final var result = mockMvc.perform(
                    get("/boards?sortBy=views")
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON));

            //then
            result.andExpect(status().isOk());
        }

        @DisplayName("게시글 조회수 순 정렬 후 조회시, 최신순으로 게시글이 먼저 조회된다.")
        @ParameterizedTest
        @AutoSource
        @Customization(NotSaveBoardCustomization.class)
        void read_boards_order_by_created_at(final Board board1,
                                             final Board board2) throws Exception {
            //given
            final var boardList = createBoardList(board2, board1);

            given(boardService.getBoardList(any(), any(), any())).willReturn(boardList);

            //when
            final var result = mockMvc.perform(
                    get("/boards?sortBy=latest")
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON)
            );

            //then
            result.andExpect(status().isOk());
        }

        @DisplayName("게시글 좋아요 순 정렬 후 조회시, 좋아요가 가장 많은 게시글이 먼저 조회된다.")
        @ParameterizedTest
        @AutoSource
        @Customization(NotSaveBoardCustomization.class)
        void read_boards_order_by_likes(
                final Board board1,
                final Board board2,
                final Member member) throws Exception {
            //given
            likeService.registerLike(member.getId(), new RegisterLikeRequest(board1.getId()));

            final var boardList = createBoardList(board2, board1);

            given(boardService.getBoardList(any(), any(), any())).willReturn(boardList);

            //when
            final var result = mockMvc.perform(
                    get("/boards?sortBy=likes")
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON)
            );

            //then
            result.andExpect(status().isOk());
        }

        @DisplayName("작성한 게시글 목록 조회 요청시 자신이 작성한 게시글 목록이 반환된다.")
        @ParameterizedTest
        @AutoSource
        void read_my_boards(
                final List<MyBoardsResponse> boardsResponses
        ) throws Exception {

            // given
            given(boardService.getMyBoards(any(), any())).willReturn(new SliceImpl<>(boardsResponses));

            final String accessToken = jwtTokenProvider.createAccessToken(1L, "writer", Role.GENERAL, true);

            // when
            final var result = mockMvc.perform(
                    get("/boards/my-board")
                            .param("page", String.valueOf(0))
                            .param("size", String.valueOf(3))
                            .contentType(APPLICATION_JSON)
                            .header(Const.AUTH_HEADER, Const.TOKEN_TYPE + " " + accessToken)
            );

            // then
            result.andExpect(status().isOk());
        }


        @DisplayName("로그인 하지 않은 사용자가 좋아요한 게시글 목록 조회 요청시 예외가 발생한다.")
        @ParameterizedTest
        @AutoSource
        void read_my_like_boards_fail(final TokenNotExistException exception) throws Exception {

            // given
            given(boardService.getMyLikeBoards(any(), any())).willThrow(exception);

            // when
            final var result = mockMvc.perform(
                    get("/boards/my-like")
                            .contentType(APPLICATION_JSON));

            // then
            result.andExpect(status().isUnauthorized());
        }

        @DisplayName("좋아요한 게시글 목록 조회 요청시 자신이 좋아요를 누른 게시글 목록이 반환된다.")
        @ParameterizedTest
        @AutoSource
        @Customization(NotSaveBoardCustomization.class)
        void read_my_like_boards(final List<Board> boards) throws Exception {

            // given
            final String accessToken = jwtTokenProvider.createAccessToken(1L, "writer", Role.GENERAL, true);

            final var response = BoardMapper.entityToMyLikeBoardsResponse(new SliceImpl<>(boards));

            given(boardService.getMyLikeBoards(any(), any())).willReturn(response);

            // when
            final var result = mockMvc.perform(
                    get("/boards/my-like")
                            .contentType(APPLICATION_JSON)
                            .header(Const.AUTH_HEADER, Const.TOKEN_TYPE + " " + accessToken));

            // then
            result.andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("게시글 태그 검색")
    class SearchBoard {
        @DisplayName("게시글 태그 검색 목록 조회시, 해당 태그가 포함된 게시글만 최신순으로 조회된다.")
        @ParameterizedTest
        @AutoSource
        @Customization(BoardCompositeCustomizer.class)
        void search_boards_by_tag(final Tag tag, final List<SortBoardResponse> boards) throws Exception {

            // given
            final String accessToken = jwtTokenProvider.createAccessToken(1L, "writer", Role.GENERAL, true);

            given(boardService.getBoardList(any(), any(), any())).willReturn(new SliceImpl<>(boards));

            // when
            final var result = mockMvc.perform(
                    get("/boards?sortBy=latest&keyword=")
                            .param("page", String.valueOf(0))
                            .param("size", String.valueOf(3))
                            .param("keyword", tag.getName())
                            .contentType(APPLICATION_JSON)
                            .header(Const.AUTH_HEADER, Const.TOKEN_TYPE + " " + accessToken));

            // then
            result.andExpect(status().isOk());
        }
    }
}