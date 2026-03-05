package vn.edu.taydo.quanly_sotietkiem.model;

public class LaiSuatView {
    private LoaiSoTK loaiSo;
    private LaiSuat laiSuat;

    public LaiSuatView(LoaiSoTK loaiSo, LaiSuat laiSuat) {
        this.loaiSo = loaiSo;
        this.laiSuat = laiSuat;
    }

    public LoaiSoTK getLoaiSo() { return loaiSo; }
    public LaiSuat getLaiSuat() { return laiSuat; }
}

