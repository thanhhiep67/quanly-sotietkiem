package vn.edu.taydo.quanly_sotietkiem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "giao_dich")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GiaoDich {
    @Id
    private String id;

    private String soTkId;
    private String khachHangId;   // thêm trường này
    private String loaiGiaoDich;  // RUT / GUI
    private Date ngayGiaoDich;
    private double soTien;
    private double tienLaiPhatSinh;
    private double tongTienNhan;
    private String ghiChu;
    private String trangThai; // PENDING, COMPLETED, FAILED
    private Date createdAt;
}

