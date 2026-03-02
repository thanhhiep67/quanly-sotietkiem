package vn.edu.taydo.quanly_sotietkiem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "loai_sotk")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoaiSoTK {
    @Id
    private String id;

    private String tenLoai;
    private Integer kyHanThang;   // đổi sang Integer
    private boolean choGuiThem;
    private boolean choRutMotPhan;
    private Date createdAt;
}

