package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.KhachHang;

import java.util.Optional;

public interface KhachHangRepository extends MongoRepository<KhachHang, String> {
    Optional<KhachHang> findByCccd(String cccd);



}
