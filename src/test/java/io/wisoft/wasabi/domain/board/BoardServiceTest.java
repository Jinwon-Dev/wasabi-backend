package io.wisoft.wasabi.domain.board;

import autoparams.AutoSource;
import autoparams.customization.Customization;
import io.wisoft.wasabi.customization.NotSaveBoardCustomization;
import io.wisoft.wasabi.domain.board.dto.WriteBoardRequest;
import io.wisoft.wasabi.domain.board.dto.WriteBoardResponse;
import io.wisoft.wasabi.domain.member.Member;
import io.wisoft.wasabi.domain.member.MemberRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @InjectMocks
    private BoardServiceImpl boardServiceImpl;

    @Spy
    private BoardMapper boardMapper;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BoardRepository boardRepository;

    @Nested
    @DisplayName("게시글 작성")
    class WriteBoard {

        @DisplayName("요청시 정상적으로 저장되어야 한다.")
        @ParameterizedTest
        @AutoSource
        void write_board(final Member member) {

            // given
            given(memberRepository.findById(any())).willReturn(Optional.of(member));

            final WriteBoardRequest request = new WriteBoardRequest(
                    "title",
                    "content",
                    new String[]{"tags"},
                    new String[]{"imageUrls"});

            final Board board = boardMapper.writeBoardRequestToEntity(request, member);
            given(boardRepository.save(any())).willReturn(board);

            // when
            final WriteBoardResponse response = boardServiceImpl.writeBoard(request, 1L);

            // then
            assertEquals("title", response.title());
            assertNotNull(response);
        }
    }

    @Nested
    @DisplayName("게시글 조회")
    class ReadBoard {

        @DisplayName("요청이 성공적으로 수행되어, 조회수가 1 증가해야 한다.")
        @ParameterizedTest
        @AutoSource
        void read_board_success(final Member member) throws Exception {

            //given
            final WriteBoardRequest request = new WriteBoardRequest(
                    "title",
                    "content",
                    new String[]{"tags"},
                    new String[]{"imageUrls"});

            final Board board = boardMapper.writeBoardRequestToEntity(request, member);
            given(boardRepository.findById(any())).willReturn(Optional.of(board));

            //when
            final var response = boardServiceImpl.readBoard(board.getId());

            //then
            Assertions.assertThat(response.views()).isEqualTo(1L);
        }

        @DisplayName("작성한 게시글 목록 조회 요청시 자신이 작성한 게시글 목록이 최신순으로 조회된다.")
        @ParameterizedTest
        @AutoSource
        @Customization(NotSaveBoardCustomization.class)
        void read_my_Boards(
                final Long memberId,
                final List<Board> boardList
                ) {

            // given
            final Slice<Board> boards = new SliceImpl<>(boardList);
            given(boardRepository.findAllMyBoards(any(), any())).willReturn(boards);

            // when
            final var pageable = PageRequest.of(0, 3);
            final var myBoards = boardServiceImpl.getMyBoards(memberId, pageable);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(myBoards).isNotEmpty();
                softly.assertThat(myBoards.getSize()).isEqualTo(3);
            });
        }
    }
}
