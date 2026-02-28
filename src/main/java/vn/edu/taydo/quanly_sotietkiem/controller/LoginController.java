package vn.edu.taydo.quanly_sotietkiem.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.taydo.quanly_sotietkiem.service.LoginService;

@Controller
public class LoginController {

    @Autowired
    LoginService loginService;

    @GetMapping("/login")
    public String login() {
        return "qlstk/client/login";
    }
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        return loginService.login(username, password, response);
    }
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");       // phải giống lúc tạo
        cookie.setMaxAge(0);       // 0 = xóa ngay
        response.addCookie(cookie); // THIẾU DÒNG NÀY

        return "redirect:/login";
    }




}

