package vn.edu.taydo.quanly_sotietkiem.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "yeu_cau_mo_so")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class YeuCauMoSo {
    @Id
    private String id;

    private String khachHangId;
    private String loaiSoId;
    private double soTienGuiBanDau;
    private String trangThai; // CHO / DA_DUYET / TU_CHOI
    private String nhanVienId;
    private Date ngayXuLy;
    private String lyDoTuChoi;
    private Double laiSuatApDung;
    private String soTkId;
    private String soTkid;
    private Date createdAt;



}


