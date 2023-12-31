package com.example.springsecurity.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @Transactional
    @WithAnonymousUser
    @DisplayName("익명 유저의 인덱스 페이지 정상 방문")
    public void index_anonymous() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithUser
    @Transactional
    @DisplayName("로그인된 상태의 유저의 인덱스 페이지 정상 방문")
    public void index_user() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());

    }


    @Test
    @Transactional
    @WithAnonymousUser
    @DisplayName("익명 유저의 관리자 페이지 방문 실패 - 미인증")
    public void admin_page_with_anonymous() throws Exception {
        mockMvc.perform(get("/admin"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUser
    @Transactional
    @DisplayName("로그인된 상태의 유저의 관리자 페이지 방문 실패 - 권한 거부 ")
    public void admin_page_with_user() throws Exception {
        mockMvc.perform(get("/admin"))
                .andDo(print())
                .andExpect(status().isForbidden());

    }


    @Test
    @WithAdmin
    @Transactional
    @DisplayName("로그인된 상태의 관리자의 관리자 페이지 방문 성공")
    public void admin_page_with_admin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @Transactional
    @DisplayName("올바른 계정으로 로그인")
    public void login_success() throws Exception {
        String username = "junhyeok";
        String pwd = "pwd";
        Account user = createUser(username, pwd);
        System.out.println(user);

        mockMvc.perform(formLogin().user(user.getUsername()).password(pwd))
                .andDo(print())
                .andExpect(authenticated());
    }

    @Test
    @Transactional
    @DisplayName("다른 계정으로 로그인")
    public void login_fail() throws Exception {
        String username = "junhyeok";
        String pwd = "pwd";
        Account user = createUser(username, pwd);

        mockMvc.perform(formLogin().user(user.getUsername()+"123").password(pwd))
                .andDo(print())
                .andExpect(unauthenticated());
    }

    private Account createUser(String username, String pwd) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(pwd);
        account.setRole("USER");

        return accountService.createAccount(account);
    }

}