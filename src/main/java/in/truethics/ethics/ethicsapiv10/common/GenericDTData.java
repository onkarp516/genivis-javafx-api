package in.truethics.ethics.ethicsapiv10.common;

import lombok.Data;

import java.util.List;

@Data
public class    GenericDTData<T> {
    private List<T> rows;
    private Integer totalRows; //total rows
}
