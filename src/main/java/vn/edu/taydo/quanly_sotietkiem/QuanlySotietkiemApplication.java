package vn.edu.taydo.quanly_sotietkiem;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.taydo.quanly_sotietkiem.service.BaoCaoService;


@SpringBootApplication

@EnableScheduling
public class QuanlySotietkiemApplication {


	@Autowired
	BaoCaoService baoCaoService;

	public static void main(String[] args) {
		SpringApplication.run(QuanlySotietkiemApplication.class, args);
	}
	@PostConstruct
	public void runBaoCaoBu() {
		baoCaoService.taoBaoCaoBu();
	}

}
