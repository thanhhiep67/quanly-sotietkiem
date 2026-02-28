package vn.edu.taydo.quanly_sotietkiem.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.repository.KhachHangRepository;
import vn.edu.taydo.quanly_sotietkiem.service.HomeService;
@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        String tenKhachHang = homeService.getTenKhachHang(request);


        if (tenKhachHang != null) {
            model.addAttribute("tenKhachHang", tenKhachHang);
        } else {
            model.addAttribute("errorMessage", "Không tìm thấy khách hàng hoặc chưa đăng nhập!");
        }

        return "qlstk/client/index";
    }
}

