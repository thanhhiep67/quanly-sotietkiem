package vn.edu.taydo.quanly_sotietkiem.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "tai_khoan")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaiKhoan {
    @Id
    private String id;

    private String password;  // mật khẩu đã mã hoá

    private String role;          // KHACH_HANG | ADMIN
    private LocalDateTime createdAt;
}
