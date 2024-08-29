package in.truethics.ethics.ethicsapiv10.dto.masterdto;

import lombok.Data;

import java.time.LocalDate;

@Data

public class HsnDTO {

    private Long id;
    private String hsnNumber;
    private String description;
    private String type;
}
