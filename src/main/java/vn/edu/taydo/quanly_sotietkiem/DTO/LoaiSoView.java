package vn.edu.taydo.quanly_sotietkiem.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoaiSoView {
    private String id;
    private String tenLoai;
    private int kyHanThang;
    private double laiSuatNam;

    public LoaiSoView(LoaiSoTK loai, LaiSuat laiSuat) {
        this.id = loai.getId();
        this.tenLoai = loai.getTenLoai();
        this.kyHanThang = loai.getKyHanThang();
        this.laiSuatNam = laiSuat != null ? laiSuat.getLaiSuatNam() : 0.0;
    }

    // getter
}
