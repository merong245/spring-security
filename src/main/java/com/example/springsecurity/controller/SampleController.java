package com.example.springsecurity.controller;

import com.example.springsecurity.account.AccountContext;
import com.example.springsecurity.account.AccountRepository;
import com.example.springsecurity.account.UserAccount;
import com.example.springsecurity.common.SecurityLogger;
import com.example.springsecurity.service.SampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.concurrent.Callable;

@Controller
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;
    private final AccountRepository accountRepository;

    // handlerMapper가 적절한 컨트롤러를 찾아줌
    // handlerAdaptor가 해당 컨트롤러에 동작을 위임함
    // mapping url
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserAccount userAccount){
        if(userAccount == null) {
            // model에 message라는 속성에 Hello Spring Security라는 값을 담아줌
            model.addAttribute("message", "Hello Spring Security");
        }
        else{
            model.addAttribute("message","Hello " + userAccount.getUsername());
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
        sampleService.dashboard();
        return "dashboard";
    }

    @GetMapping("/admin")
    public String admin(Model model, Principal principal){
        model.addAttribute("message","Hello " + principal.getName());
        return "admin";
    }

    @GetMapping("/user")
    public String user(Model model, Principal principal){
        model.addAttribute("message","Hello User" + principal.getName());
        return "user";
    }

    @GetMapping("/async-handler")
    @ResponseBody
    public Callable<String> asyncHandler(){
        SecurityLogger.log("asyncHandler Test");

        // 별도의 쓰레드
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                SecurityLogger.log("callable Test");
                return "Async Handler";
            }
        };
    }
    @GetMapping("/async-service")
    @ResponseBody
    public String asyncService(){
        SecurityLogger.log("before AsyncService");
        sampleService.asyncService();
        SecurityLogger.log("after asyncService");
        return "AsyncService";
    }

}
