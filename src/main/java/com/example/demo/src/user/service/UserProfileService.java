package com.example.demo.src.user.service;

import com.example.demo.src.user.dao.UserMapper;
import com.example.demo.src.user.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.src.user.dto.UserResponseDto.*;

/*
* # 유저의 마이프로필 관련 서비스 -> 사실상 닉네임변경만 가능
*   - 마이프로필 조회 (딱 들어가면 나올 페이지)
*       - 내 리스트
*       - 내 리뷰
*       - 닉네임
*   - 마이 프로필 수정
*       - 닉네임
*   -
*
* */
@Service
@AllArgsConstructor
public class UserProfileService {

    private final UserMapper userMapper;
    private final UserSignUpAndFindService userSignUpAndFindService;

//    private final List<list> list;
//    private final List<Review> review;

    public userProfileResponseDto getMyProfile(String userUUId) {
        User user = userSignUpAndFindService.findByUUID(userUUId);
        return userProfileResponseDto.builder()
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
//                .numOfReview(review.size())
//                .numOfList(list.size())
                .build();
    }

}
