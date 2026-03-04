package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.BaoCaoNgay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BaoCaoNgayRepository extends MongoRepository<BaoCaoNgay, String> {
    List<BaoCaoNgay> findByNgayBetween(LocalDate start, LocalDate end);
    Optional<BaoCaoNgay> findByNgay(LocalDate ngay);
}

