package vn.edu.taydo.quanly_sotietkiem.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.taydo.quanly_sotietkiem.model.NhanVien;
import vn.edu.taydo.quanly_sotietkiem.repository.NhanVienRepository;

@Service
public class AdminHomeService {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    public String getTenNhanVien(String userId) {
        return nhanVienRepository.findById(userId)
                .map(NhanVien::getHoten)
                .orElse(null);
    }

}
