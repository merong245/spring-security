package com.example.springsecurity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SampleController {

    // handlerMapper가 적절한 컨트롤러를 찾아줌
    // handlerAdaptor가 해당 컨트롤러에 동작을 위임함
    // mapping url
    @GetMapping("/")
    public String index(Model model){
        // model에 message라는 속성에 Hello Spring Security라는 값을 담아줌
        model.addAttribute("message","Hello Spring Security");
        return "index"; // view Resolver가 해석
    }
}
