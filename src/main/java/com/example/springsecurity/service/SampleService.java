package com.example.springsecurity.service;

import com.example.springsecurity.account.Account;
import com.example.springsecurity.account.AccountContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class SampleService {
    public void dashboard() {
        /*
        기존에는 SecurityContextHolder에 접근해서 Account를 가져 왔다면
        이제는 ThreadLocal에 Account를 추가하여 AccountContext에서 꺼내올 수 있다.
         */
        Account account = AccountContext.getAccount();
        System.out.println("===========");
        System.out.println(account.getUsername());
    }
}
