package io.blog.devlog.global.oauth2.service;

import io.blog.devlog.domain.user.model.Role;
import io.blog.devlog.domain.user.model.User;
import io.blog.devlog.domain.user.service.UserService;
import io.blog.devlog.global.login.dto.PrincipalDetails;
import io.blog.devlog.global.oauth2.OAuthAttributes;
import io.blog.devlog.global.oauth2.SocialType;
import io.blog.devlog.global.oauth2.userinfo.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

// OAuth2 Client를 통해 로그인을 진행할 경우, 호출되는 서비스
// 라이브러리를 통해 인증 과정을 끝내고 AccessToken을 받은 시점에서 해당 서비스가 실행된다.
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2User의 loadUser() 호출");
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // nameAttributeKey
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        Map<String, Object> userAttributes = oAuth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuthAttributes extractAttributes = OAuthAttributes.of(getSocialType(provider), userNameAttributeName, userAttributes);
        OAuth2UserInfo oauth2UserInfo = extractAttributes.getOauth2UserInfo();
        String providerId = oauth2UserInfo.getId();
        String username = oauth2UserInfo.getNickname();
        String email = oauth2UserInfo.getEmail();
        User user = null;
        Optional<User> userEntity = userService.getUserByEmail(email);
        if (userEntity.isEmpty()) {
            user = User.builder()
                    .username(username)
                    .email(email)
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.USER)
                    .password(providerId)
                    .certificate(true) // 소셜 로그인은 인증이 필요 없음
                    .build();
            user.updateProfile(oauth2UserInfo.getImageUrl());
            userService.saveUser(user);
        }
        else {
            user = userEntity.get();
        }
        log.info("CustomOAuth2UserService Username : {}", user.getUsername());
        log.info("userAttributes : {}", oauth2UserInfo);
        log.info("nameAttributesKey : {}", extractAttributes.getNameAttributeKey());

        return new PrincipalDetails(
                user,
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                userAttributes,
                extractAttributes.getNameAttributeKey()
        );
//        return new CustomOAuth2User(
//                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
//                userAttributes,
//                extractAttributes.getNameAttributeKey(),
//                user.getUsername(),
//                user.getRole()
//        );
//        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), userNameAttributeName);
    }

    private SocialType getSocialType(String registrationId) {
//        if(NAVER.equals(registrationId)) {
//            return SocialType.NAVER;
//        }
//        if(KAKAO.equals(registrationId)) {
//            return SocialType.KAKAO;
//        }
        return SocialType.GOOGLE;
    }
}
