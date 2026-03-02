package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;

import java.util.List;

public interface SoTietKiemRepository extends MongoRepository<SoTietKiem, String> {

    List<SoTietKiem> findByKhachHangId(String khachHangId);

    SoTietKiem findByMaSo(String soTkId);

    long countByKhachHangId(String khachHangId);
}
