package vn.edu.taydo.quanly_sotietkiem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@SpringBootApplication

@Controller
public class QuanlySotietkiemApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuanlySotietkiemApplication.class, args);
	}

	@GetMapping("/hello")
	public String hello() {
		return "Hello, welcome to the Savings Management Application!";
	}


}
