package io.wisoft.wasabi.domain.board;

import autoparams.AutoSource;
import autoparams.customization.Customization;
import io.wisoft.wasabi.customization.composite.BoardCompositeCustomizer;
import io.wisoft.wasabi.domain.like.Like;
import io.wisoft.wasabi.domain.member.Member;
import io.wisoft.wasabi.domain.tag.Tag;
import io.wisoft.wasabi.setting.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(QueryDslTestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class BoardQueryRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardQueryRepository boardQueryRepository;

    @Nested
    @DisplayName("게시글 목록 정렬 조회")
    class ReadBoard {

        private final Pageable pageable = PageRequest.of(0, 3);

        @DisplayName("게시글 목록 조회시, 조회수 많은 순으로 졍렬 후 조회에 성공한다.")
        @ParameterizedTest
        @AutoSource
        @Customization(BoardCompositeCustomizer.class)
        void read_boards_order_by_views(final Member member,
                                        final List<Board> boards) {

            // given
            em.persist(member);
            boardRepository.saveAll(boards);

            final var expected = boards.get(0);
            expected.increaseView();

            // when
            final var result = boardQueryRepository.boardList(pageable, BoardSortType.VIEWS, null);

            // then
            final var mostViewedBoard = result.getContent().get(0);
            assertThat(mostViewedBoard.id()).isEqualTo(expected.getId());
        }

        @DisplayName("게시글 목록 조회시, 최신 순으로 졍렬 후 조회에 성공한다.")
        @ParameterizedTest
        @AutoSource
        @Customization(BoardCompositeCustomizer.class)
        void read_boards_order_by_create_at(final Member member,
                                            final List<Board> boards) {

            // given
            em.persist(member);
            boardRepository.saveAll(boards);

            final var expected = boards.get(boards.size() - 1);
            em.persist(expected);

            // when
            final var result = boardQueryRepository.boardList(pageable, BoardSortType.LATEST, null);

            // then
            final var latestBoard = result.getContent().get(0);
            assertThat(latestBoard.id()).isEqualTo(expected.getId());
        }

        @DisplayName("게시글 목록 조회시, 좋아요 많은 순으로 졍렬 후 조회에 성공한다.")
        @ParameterizedTest
        @AutoSource
        @Customization(BoardCompositeCustomizer.class)
        void read_boards_order_by_likes(final Member member,
                                        final List<Board> boards) {

            // given
            em.persist(member);
            boardRepository.saveAll(boards);
            em.persist(new Like(member, boards.get(0)));

            final var expected = boards.get(0);
            em.persist(expected);

            // when
            final var result = boardQueryRepository.boardList(pageable, BoardSortType.VIEWS, null);

            // then
            final var mostLikedBoard = result.getContent().get(0);
            assertThat(mostLikedBoard.id()).isEqualTo(expected.getId());
        }
    }

    @Nested
    @DisplayName("게시글 태그 검색")
    class SearchBoard {

        private final Pageable pageable = PageRequest.of(0, 3);

        @DisplayName("게시글 태그 검색 목록 조회 시, 해당 태그가 포함된 게시글만 조회된다.")
        @ParameterizedTest
        @AutoSource
        @Customization(BoardCompositeCustomizer.class)
        void search_boards_by_keyword(final Member member,
                                      final Tag tag,
                                      final List<Board> boards) {

            // given
            em.persist(member);
            boardRepository.saveAll(boards);
            em.persist(tag);

            final var expected = boards.get(0);
            expected.setTag(tag);
            em.persist(expected);

            final String keyword = tag.getName();

            // when
            final var result = boardQueryRepository.boardList(pageable, BoardSortType.VIEWS, keyword);

            // then
            final var searchKeywordBoard = result.getContent().get(0);
            assertThat(searchKeywordBoard.id()).isEqualTo(expected.getId());
        }
    }
}