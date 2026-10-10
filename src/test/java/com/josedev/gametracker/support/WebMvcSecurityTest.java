package com.josedev.gametracker.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.josedev.gametracker.config.JwtConfig;
import com.josedev.gametracker.config.SecurityConfig;
import com.josedev.gametracker.security.JpaUserDetailsService;
import com.josedev.gametracker.security.UserStatusJwtConverter;
import com.josedev.gametracker.user.UserRepository;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ SecurityConfig.class, JwtConfig.class, UserStatusJwtConverter.class })
@MockitoBean(types = { UserRepository.class, JpaUserDetailsService.class })
public @interface WebMvcSecurityTest {

}
