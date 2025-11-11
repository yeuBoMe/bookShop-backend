package com.fullstack.bookShop.config;

import com.fullstack.bookShop.service.UserService;
import lombok.RequiredArgsConstructor;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.fullstack.bookShop.domain.entity.User userGetByEmail = this.userService.getUserByEmail(username);

        if (userGetByEmail == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        }

        return new User(
                userGetByEmail.getEmail(),
                userGetByEmail.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
