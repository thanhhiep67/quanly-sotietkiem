package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import vn.edu.taydo.quanly_sotietkiem.model.GiaoDich;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface GiaoDichRepository extends MongoRepository<GiaoDich, String> {

    List<GiaoDich> findTop5ByKhachHangIdOrderByCreatedAtDesc(String khachHangId);


    @Aggregation(pipeline = {
            "{ $match: { loaiGiaoDich: ?0, ngayGiaoDich: { $gte: ?1, $lt: ?2 } } }",
            "{ $group: { _id: null, total: { $sum: \"$soTien\" } } }",
            "{ $project: { _id: 0, total: 1 } }"
    })
    Double sumSoTienByLoaiAndNgay(String loai, Date start, Date end);

    List<GiaoDich> findByLoaiGiaoDichAndNgayGiaoDichBetween(
            String loai, Date start, Date end);

    List<GiaoDich> findByKhachHangIdOrderByNgayGiaoDichDesc(String id);

    List<GiaoDich> findByKhachHangIdOrderByCreatedAtDesc(String khachHangId);
}
