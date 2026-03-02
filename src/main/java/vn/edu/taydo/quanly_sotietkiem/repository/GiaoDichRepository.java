package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.GiaoDich;

import java.util.List;

public interface GiaoDichRepository extends MongoRepository<GiaoDich, String> {

    List<GiaoDich> findTop5ByKhachHangIdOrderByCreatedAtDesc(String khachHangId);
}
