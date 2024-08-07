package com.example.demo.src.global.oauth.controller;

import com.example.demo.src.global.jwt.JWTUtil;
import com.example.demo.src.refresh.domain.RefreshToken;
import com.example.demo.src.user.domain.User;
import com.example.demo.src.user.model.Role;
import com.example.demo.src.user.service.UserSignUpAndFindService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class RefreshTokenController {

    private final JWTUtil jwtUtil;

    private final UserSignUpAndFindService userSignUpAndFindService;

    public RefreshTokenController(JWTUtil jwtUtil, UserSignUpAndFindService userSignUpAndFindService) {
        this.jwtUtil = jwtUtil;
        this.userSignUpAndFindService = userSignUpAndFindService;
    }

    @PostMapping("/reissue") // 리프레시토큰 재발급
    public ResponseEntity<?> reIssue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = null;
        // 쿠키들 가져와서 여기서 refresh토큰 값 얻어내기
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        // 만약 리프레시 토큰이 없다면 ?
        if (refresh == null) {
            // 예외처리
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        // 만료여부 체크
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            return new ResponseEntity<>("access token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 리프레시 토큰인지 확인
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // db에 있는지 확인
        if (!userSignUpAndFindService.isExistByRefresh(refresh)) {

            return new ResponseEntity<>("invalid refresh token - not exist", HttpStatus.BAD_REQUEST);
        }


        String userUUId = jwtUtil.getUserUUId(refresh);
        Role role = jwtUtil.getRole(refresh);

        // jwt만들기
        String newAccess = jwtUtil.createJwt("access", userUUId, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", userUUId, role, 86400000L);

        // 기존 토큰 삭제 후 새로운 refresh토큰 저장
        userSignUpAndFindService.deleteByRefresh(refresh);
        addRefreshAddDB(userUUId, newRefresh, 86400000L);


        // response에 넣기
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));


        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void addRefreshAddDB(String userProvideId, String newRefresh, Long expireMs) {
        Date date = new Date(System.currentTimeMillis() + expireMs);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserProvideId(userProvideId);
        refreshToken.setRefreshToken(newRefresh);
        refreshToken.setExpiration(date.toString());

        if (!userSignUpAndFindService.isExistByProvideId(userProvideId)) {
            // 존재 X -> 새로운 사람
            User user = new User();
            user.setUserProvideId(userProvideId);
            user.setRefreshToken(newRefresh);
            user.setExpiration(date.toString());

            userSignUpAndFindService.save(user);
        } else {
            // 존재 O -> 기존유저
            User user = userSignUpAndFindService.findByProvideId(userProvideId);
            user.setRefreshToken(newRefresh);
            user.setExpiration(date.toString());
            userSignUpAndFindService.updateRefreshToken(newRefresh, date.toString(), userProvideId);

        }
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
//        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
