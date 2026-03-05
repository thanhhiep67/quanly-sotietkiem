package vn.edu.taydo.quanly_sotietkiem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Document(collection = "so_tiet_kiem")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SoTietKiem {

    @Id
    private String maSo;

    private String khachHangId;
    private String loaiSoId;
    private Date ngayMoSo;
    private Date ngayDaoHan;
    private double soTienBanDau;
    private double soDuHienTai;
    private double laiSuatApDung;
    private String yeuCauMoSoId;
    private String trangThai;
    private LocalDate ngayDongSo;// MO / DONG / DAO_HAN

    // getter/setter
}

