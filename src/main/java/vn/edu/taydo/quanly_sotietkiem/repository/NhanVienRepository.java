package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.NhanVien;

import java.util.Optional;

public interface NhanVienRepository extends MongoRepository<NhanVien, String> {
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);
}

