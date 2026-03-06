package vn.edu.taydo.quanly_sotietkiem.repository;

import org.bson.Document;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoThongKeMap;
import vn.edu.taydo.quanly_sotietkiem.model.SoTietKiem;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface SoTietKiemRepository extends MongoRepository<SoTietKiem, String> {

    List<SoTietKiem> findByKhachHangId(String khachHangId);

    SoTietKiem findByMaSo(String soTkId);

    long countByKhachHangId(String khachHangId);

    long countByTrangThai(String mo);
    @Aggregation(pipeline = {
            "{ $match: { trangThai: 'MO' } }",
            "{ $group: { _id: null, total: { $sum: \"$soDuHienTai\" } } }"
    })
    Double sumSoDuHienTaiDangHoatDong();

    @Aggregation(pipeline = {
            "{ $group: { _id: \"$loaiSoId\", count: { $sum: 1 } } }"
    })
    List<LoaiSoThongKeMap> countByLoaiSoGrouped();


    long countByNgayMoSo(LocalDate ngay);


    long countByNgayDongSo(LocalDate ngay);

    long countByNgayDongSoBetween(Date startOfDay, Date endOfDay);

    long countByNgayMoSoBetween(Date startOfDay, Date endOfDay);



}
