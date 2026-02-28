package vn.edu.taydo.quanly_sotietkiem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.taydo.quanly_sotietkiem.model.TaiKhoan;

import java.util.Optional;

public interface TaiKhoanRepository extends MongoRepository<TaiKhoan, String> {
    Optional<TaiKhoan> findById(String id);
    }
