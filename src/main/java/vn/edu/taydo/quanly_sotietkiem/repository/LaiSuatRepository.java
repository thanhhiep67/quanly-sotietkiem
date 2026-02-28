package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;

public interface LaiSuatRepository extends MongoRepository<LaiSuat, String> {
    LaiSuat findTopByLoaiStkIdOrderByNgayApDungDesc(String loaiStkId);
}
