package com.example.springsecurity.config;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Request;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");

        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);

        WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
        webExpressionVoter.setExpressionHandler(handler);

        List<AccessDecisionVoter<? extends Object>> voters = Arrays.asList(webExpressionVoter);
        return new AffirmativeBased(voters);
    }

    // 스프링 시큐리티 5.7 이상부터는 아래와 같은 방법 사용
    // 정적 리소스에 대한 요청 전부 허용 -> 필터를 등록시키지 않으므로 성능상 장점
    // WebsecurityConfig에서 ignore를 적용시키는 것보다 HttpSecurity의 Config을 수정하는 것을 권장
    // 하지만 HttpSecurity는 SecurityFilterChain을 돌기 때문에 정적리소스 예외시는 리소스 낭비되므로 정적리소스에 한해서는 WebSecurity에서 처리
    // 참고 : https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()
                .mvcMatchers("/", "/info", "/account/**", "/sign-up").permitAll()
                .mvcMatchers("/admin").hasRole("ADMIN")
                .mvcMatchers("/user").hasRole("USER")
                .anyRequest().authenticated()
        ;
        http.formLogin()
                .loginPage("/login").permitAll()
        ;
        http.httpBasic();
//        http.csrf().disable();
        http.logout()
                .logoutUrl("/logout") // logout 요청할 URL
                .logoutSuccessUrl("/") // logout 처리 후 이동할 URL
        ;

        // 세션 관리하는 법 설정
        http.sessionManagement()
                .sessionFixation()
                .changeSessionId()
                .maximumSessions(1)// 동시성을 제어하기 위해 최대 1개의 세션을 사용하도록 설정
                .maxSessionsPreventsLogin(true) // 추가 로그인 방지
        ;

        // 세성 생성 정책 설정
        // RESTful 한 개발을 위해서 채택해야하는 전략이지만 웹기반으로는 적절하지 않음
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        return http.build();
    }

//    @Bean
//    public InMemoryUserDetailsManager userDetailsManager() {
//        UserDetails user = User.withUsername("user")
//                .password("{noop}pass123")
//                .roles("USER")
//                .build();
//
//        UserDetails admin = User.withUsername("admin")
//                .password("{noop}pass123")
//                .roles("ADMIN")
//                .build();
//
//        List<UserDetails> userDetails = List.of(user, admin);
//        return new InMemoryUserDetailsManager(userDetails);
//    }
}
