package vn.edu.taydo.quanly_sotietkiem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "nhan_vien")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NhanVien {
    @Id
    private String id;
    private String taikhoan_id;
    private String hoten;
    private String maNhanVien;
    private String gioiTinh;
    private String sdt;
    private String diaChi;
    private String ngaySinh;
    private LocalDateTime createdAt;
}
