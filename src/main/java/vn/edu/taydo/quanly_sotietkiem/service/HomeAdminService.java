package vn.edu.taydo.quanly_sotietkiem.service;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.bson.Document;

import org.springframework.stereotype.Service;
import vn.edu.taydo.quanly_sotietkiem.config.JwtUtil;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoTK;
import vn.edu.taydo.quanly_sotietkiem.model.LoaiSoThongKeMap;
import vn.edu.taydo.quanly_sotietkiem.model.NhanVien;
import vn.edu.taydo.quanly_sotietkiem.repository.LoaiSoTKRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.NhanVienRepository;
import vn.edu.taydo.quanly_sotietkiem.repository.SoTietKiemRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeAdminService {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    public String getTenNhanVien(HttpServletRequest request) {
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) return null;

        Claims claims = JwtUtil.extractClaims(token);

        // claims.userId chứa id của NhanVien (vd: NVxxx), tìm và trả tên
        String userId = claims.get("userId", String.class);
        if (userId == null) return null;

        return nhanVienRepository.findById(userId)
                .map(NhanVien::getHoten)
                .orElse(null);
    }

    @Autowired
    SoTietKiemRepository soTietKiemRepository;
    @Autowired
    LoaiSoTKRepository loaiSoTKRepository;

    public List<Map<String,Object>> thongKeLoaiSo() {
        List<LoaiSoThongKeMap> counts = soTietKiemRepository.countByLoaiSoGrouped();
        Map<String, Long> countMap = counts.stream()
                .collect(Collectors.toMap(LoaiSoThongKeMap::getId, LoaiSoThongKeMap::getCount));

        List<LoaiSoTK> allLoaiSo = loaiSoTKRepository.findAll();
        long tongSo = soTietKiemRepository.count();

        List<Map<String,Object>> result = new ArrayList<>();

        for (LoaiSoTK loai : allLoaiSo) {
            long count = countMap.getOrDefault(loai.getId(), 0L);
            double pct = tongSo > 0 ? (count * 100.0 / tongSo) : 0;

            Map<String,Object> item = new HashMap<>();
            item.put("tenLoai", loai.getTenLoai());
            item.put("kyHanThang", loai.getKyHanThang());
            item.put("count", count);
            item.put("pct", pct);
            result.add(item);
        }

        return result;
    }



}
