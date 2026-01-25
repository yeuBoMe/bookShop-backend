package com.fullstack.bookShop.security;

import com.fullstack.bookShop.service.interfaces.UserService;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        com.fullstack.bookShop.model.User userGetByEmail = userService.getUserByEmail(username);

        return new User(
                userGetByEmail.getEmail(),
                userGetByEmail.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority(
                                AuthorityConstant.ROLE_PREFIX + userGetByEmail.getRole().getName()
                        )
                )
        );
    }
}
