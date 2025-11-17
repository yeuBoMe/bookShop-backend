package com.fullstack.bookShop.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomDefaultOauth2UserService extends DefaultOAuth2UserService {

    // Hàm được Spring gọi tự động sau khi nhận được access_token từ Google.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // dùng access_token để gọi Google UserInfo API → lấy email, name, picture.
        OAuth2User oauth2User = super.loadUser(userRequest);

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                oauth2User.getAttributes(), // full data trả về từ gg
                "email" // key name định danh
        );
    }
}

