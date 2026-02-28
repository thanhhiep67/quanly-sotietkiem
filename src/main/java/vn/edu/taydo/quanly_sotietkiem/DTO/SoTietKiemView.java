package vn.edu.taydo.quanly_sotietkiem.DTO;

import lombok.Getter;
import lombok.Setter;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;

@Getter
@Setter
public class SoTietKiemView {

    private SoTietKiem so;
    private LoaiSoTK loai;

    public SoTietKiemView(SoTietKiem so, LoaiSoTK loai) {
        this.so = so;
        this.loai = loai;
    }
}