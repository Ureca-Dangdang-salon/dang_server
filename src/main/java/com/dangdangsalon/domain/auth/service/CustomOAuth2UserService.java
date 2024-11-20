package com.dangdangsalon.domain.auth.service;

import static com.dangdangsalon.domain.auth.Social.*;

import com.dangdangsalon.domain.auth.Social;
import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.auth.dto.GoogleResponse;
import com.dangdangsalon.domain.auth.dto.NaverResponse;
import com.dangdangsalon.domain.auth.dto.OAuth2Response;
import com.dangdangsalon.domain.auth.dto.UserDto;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User: " + oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;

        if (registrationId.equals(NAVER.getName())) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals(GOOGLE.getName())) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        // 리소스 서버에서 발급 받은 정보로 사용자를 특정할 수 있는 아이디
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        User existData = userRepository.findByUsername(username);

        if (existData != null) {
            UserDto userDto = UserDto.fromEntity(existData);

            return new CustomOAuth2User(userDto);
        } else {
            User user = User.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .imageKey(oAuth2Response.getProfileImage())
                    .role(Role.ROLE_PENDING)
                    .build();

            userRepository.save(user);

            UserDto userDto = UserDto.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().toString())
                    .image(user.getImageKey())
                    .district(null)
                    .build();

            return new CustomOAuth2User(userDto);
        }
    }
}
