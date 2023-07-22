package com.example.springsecurity.controller;

import com.example.springsecurity.account.AccountContext;
import com.example.springsecurity.account.AccountRepository;
import com.example.springsecurity.service.SampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;
    private final AccountRepository accountRepository;

    // handlerMapper가 적절한 컨트롤러를 찾아줌
    // handlerAdaptor가 해당 컨트롤러에 동작을 위임함
    // mapping url
    @GetMapping("/")
    public String index(Model model, Principal principal){
        if(principal == null) {
            // model에 message라는 속성에 Hello Spring Security라는 값을 담아줌
            model.addAttribute("message", "Hello Spring Security");
        }
        else{
            model.addAttribute("message","Hello " + principal.getName());
        }
        return "index"; // view Resolver가 해석
    }
    @GetMapping("/info")
    public String info(Model model){
        model.addAttribute("message","Info");
        return "info";
    }

    @GetMapping("/dashboard")
    public String info(Model model, Principal principal){
        model.addAttribute("message","Hello " + principal.getName());
        AccountContext.setAccount(accountRepository.findByUsername(principal.getName()));
        sampleService.dashboard();
        return "dashboard";
    }

    @GetMapping("/admin")
    public String admin(Model model, Principal principal){
        model.addAttribute("message","Hello " + principal.getName());
        return "admin";
    }



}
