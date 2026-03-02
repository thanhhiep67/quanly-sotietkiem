package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.YeuCauMoSo;

import java.util.List;

public interface  YeuCauMoSoRepository extends MongoRepository<YeuCauMoSo, String> {

    List<YeuCauMoSo> findByKhachHangId(String khachHangId);
    List<YeuCauMoSo> findByTrangThaiOrderByCreatedAtDesc(String trangThai);

}
