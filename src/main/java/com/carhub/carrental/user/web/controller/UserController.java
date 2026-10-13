package com.carhub.carrental.user.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import com.carhub.carrental.user.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

}
