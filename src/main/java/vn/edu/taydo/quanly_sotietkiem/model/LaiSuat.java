package vn.edu.taydo.quanly_sotietkiem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "lai_suat")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LaiSuat {
    @Id
    private String id;

    private String loaiStkId;
    private double laiSuatNam;
    private Date ngayApDung;
    private Date createdAt;

    // getter/setter
}

