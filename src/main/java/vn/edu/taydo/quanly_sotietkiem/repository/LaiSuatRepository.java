package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.LaiSuat;

import java.util.List;

public interface LaiSuatRepository extends MongoRepository<LaiSuat, String> {
    LaiSuat findTopByLoaiStkIdOrderByNgayApDungDesc(String loaiStkId);

    List<LaiSuat> findByLoaiStkIdOrderByNgayApDungDesc(String id);
}
