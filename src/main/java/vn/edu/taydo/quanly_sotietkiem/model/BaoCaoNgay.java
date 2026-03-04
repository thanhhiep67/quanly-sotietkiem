package vn.edu.taydo.quanly_sotietkiem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "bao_cao_ngay")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaoCaoNgay {
    @Id
    private String id;
    private LocalDate ngay;
    private double tongThu;
    private double tongChi;
    private double chenhLech;
    private long soMo;
    private long soDong;
    private long chenhLechSo;

    // getters & setters
}

