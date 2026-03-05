package vn.edu.taydo.quanly_sotietkiem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "khach_hang")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KhachHang {
    @Id
    private String id;
    private String taikhoan_id;
    private String hoten;
    private String gioiTinh;
    private String sdt;
    private String cccd;
    private String diaChi;
    private String email;
    private LocalDate ngaySinh;
    private LocalDateTime createdAt;
}
