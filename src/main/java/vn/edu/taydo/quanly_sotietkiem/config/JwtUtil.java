package vn.edu.taydo.quanly_sotietkiem.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private static final Key SECRET_KEY =
            Keys.hmacShaKeyFor("VerySecretKeyVerySecretKeyVerySecretKey".getBytes());
    private static final long EXPIRATION_TIME = 4 * 60 * 60 * 1000; // 4h

    // Tạo token với claims
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Kiểm tra token hợp lệ
    public static Claims validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Lấy claims từ token (ví dụ userId, role)
    public static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public static String getUserIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    try {
                        Claims claims = validateToken(cookie.getValue());
                        return claims.get("userId", String.class);
                    } catch (ExpiredJwtException e) {
                        throw new RuntimeException("Token đã hết hạn");
                    } catch (JwtException e) {
                        throw new RuntimeException("Token không hợp lệ");
                    }
                }
            }
        }
        return null;
    }

    public static Map<String, Object> getClaimsFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    try {
                        Claims claims = validateToken(cookie.getValue());
                        return claims; // trả về toàn bộ claims dưới dạng Map
                    } catch (ExpiredJwtException e) {
                        throw new RuntimeException("Token đã hết hạn");
                    } catch (JwtException e) {
                        throw new RuntimeException("Token không hợp lệ");
                    }
                }
            }
        }
        return null;
    }


}
