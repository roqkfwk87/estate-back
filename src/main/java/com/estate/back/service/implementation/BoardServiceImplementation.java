package com.estate.back.service.implementation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.estate.back.dto.request.board.PostBoardRequestDto;
import com.estate.back.dto.request.board.PostCommentRequestDto;
import com.estate.back.dto.request.board.PutBoardRequestDto;
import com.estate.back.dto.response.ResponseDto;
import com.estate.back.dto.response.board.GetBoardListResponseDto;
import com.estate.back.dto.response.board.GetBoardResponseDto;
import com.estate.back.dto.response.board.GetSearchBoardResponseDto;
import com.estate.back.entity.BoardEntity;
import com.estate.back.repository.BoardRepository;
import com.estate.back.repository.UserRepository;
import com.estate.back.service.BoardService;

import lombok.RequiredArgsConstructor;

@Service
// private으로 정의한 변수를 선언해줌
@RequiredArgsConstructor
public class BoardServiceImplementation implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<ResponseDto> postBoard(PostBoardRequestDto dto, String userId) {

        try {

            boolean isExistUser = userRepository.existsByUserId(userId);
            if (!isExistUser) return ResponseDto.authenticationFailed();

            // board 데이터를 삽입하기 위해서 boardentity를 선언해 줘야 함
            BoardEntity boardEntity = new BoardEntity(dto, userId);
            boardRepository.save(boardEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return ResponseDto.success();

    }

    @Override
    public ResponseEntity<? super GetBoardListResponseDto> getBoardList() {

        try {

            List<BoardEntity> boardEntities = boardRepository.findByOrderByReceptionNumberDesc();
            return GetBoardListResponseDto.success(boardEntities);
            
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }


    }

    @Override
    public ResponseEntity<? super GetSearchBoardResponseDto> getSearchBoardList(String searchWord) {
        try {

            List<BoardEntity> boardEntities = boardRepository.findByTitleContainsOrderByReceptionNumberDesc(searchWord);
            return GetSearchBoardResponseDto.success(boardEntities);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    }

    @Override
    public ResponseEntity<? super GetBoardResponseDto> getBoard(int receptionNumber) {

        try {

            BoardEntity boardEntity = boardRepository.findByReceptionNumber(receptionNumber);
            if (boardEntity == null) return ResponseDto.noExistBoard();

            boardEntity.increaseViewCount();
            boardRepository.save(boardEntity);

            return GetBoardResponseDto.success(boardEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    
    }

    @Override
    public ResponseEntity<ResponseDto> increaseViewCount(int receptionNumber) {

        try {

            BoardEntity boardEntity = boardRepository.findByReceptionNumber(receptionNumber);
            if (boardEntity == null) return ResponseDto.noExistBoard();

            boardEntity.increaseViewCount();
            boardRepository.save(boardEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    
        return ResponseDto.success();

    }

    @Override
    public ResponseEntity<ResponseDto> postComment(PostCommentRequestDto dto, int receptionNumber) {

        try {

            BoardEntity boardEntity = boardRepository.findByReceptionNumber(receptionNumber);
            if (boardEntity == null) return ResponseDto.noExistBoard();

            boolean status = boardEntity.getStatus();
            if (status) return ResponseDto.writtenComment();

            String comment = dto.getComment();
            boardEntity.setStatus(true);
            boardEntity.setComment(comment);

            boardRepository.save(boardEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return ResponseDto.success();
    }

    @Override
    public ResponseEntity<ResponseDto> deleteBoard(int receptionNumber, String userId) {

        try {

            BoardEntity boardEntity = boardRepository.findByReceptionNumber(receptionNumber);
            if (boardEntity == null) return ResponseDto.noExistBoard();

            String writerId = boardEntity.getWriterId();
            boolean isWriter = userId.equals(writerId);
            if (!isWriter) return ResponseDto.authorizationFailed();

            boardRepository.delete(boardEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return ResponseDto.success();
    }

    @Override
    public ResponseEntity<ResponseDto> putBoard(PutBoardRequestDto dto, int receptionNumber, String userId) {

        try {

            BoardEntity boardEntity = boardRepository.findByReceptionNumber(receptionNumber);
            if (boardEntity == null) return ResponseDto.noExistBoard();

            String writerId = boardEntity.getWriterId();
            boolean isWriter = userId.equals(writerId);
            if (!isWriter) return ResponseDto.authorizationFailed();

            boolean status = boardEntity.getStatus();
            if (status) return ResponseDto.writtenComment();

            boardEntity.update(dto);
            boardRepository.save(boardEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return ResponseDto.success();
    }
}
