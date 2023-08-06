package io.wisoft.wasabi.domain.board;

import io.wisoft.wasabi.domain.board.dto.MyBoardsResponse;
import io.wisoft.wasabi.domain.board.dto.SortBoardResponse;
import io.wisoft.wasabi.domain.board.dto.WriteBoardRequest;
import io.wisoft.wasabi.domain.board.dto.WriteBoardResponse;
import io.wisoft.wasabi.global.config.common.annotation.MemberId;
import io.wisoft.wasabi.global.config.web.response.CommonResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(final BoardService boardService) {
        this.boardService = boardService;
    }


    @PostMapping
    public ResponseEntity<CommonResponse> writeBoard(@RequestBody @Valid final WriteBoardRequest request,
                                                     @MemberId @Valid @NotNull final Long memberId) {

        final WriteBoardResponse response = boardService.writeBoard(request, memberId);
        return ResponseEntity.status(CREATED).body(CommonResponse.newInstance(response));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<CommonResponse> readBoard(@PathVariable final Long boardId) {

        final var response = boardService.readBoard(boardId);
        return ResponseEntity.ok(CommonResponse.newInstance(response));
    }

    @GetMapping
    public ResponseEntity<CommonResponse> sortedBoards(
            @RequestParam(name = "sortBy", defaultValue = "default") final String sortBy,
            @Valid @RequestParam(name = "page", defaultValue = "0") final int page,
            @Valid @RequestParam(name = "size", defaultValue = "2") final int size) {

        final Slice<SortBoardResponse> sortedBoards = boardService.getSortedBoards(sortBy, page, size);

        return ResponseEntity.ok(CommonResponse.newInstance(sortedBoards));
    }

    @GetMapping("/my-board")
    public ResponseEntity<CommonResponse> myBoards(@MemberId final Long memberId, final Pageable pageable) {
        final Slice<MyBoardsResponse> response = boardService.getMyBoards(memberId, pageable);
        return ResponseEntity.ok(CommonResponse.newInstance(response));
    }
}
