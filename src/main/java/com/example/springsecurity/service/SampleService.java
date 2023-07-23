package com.example.springsecurity.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SampleService {
    public void dashboard() {
        /*
        기존에는 SecurityContextHolder에 접근해서 Account를 가져 왔다면
        이제는 ThreadLocal에 Account를 추가하여 AccountContext에서 꺼내올 수 있다.
         */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println("===========");
        System.out.println(userDetails.getUsername());
    }
}
