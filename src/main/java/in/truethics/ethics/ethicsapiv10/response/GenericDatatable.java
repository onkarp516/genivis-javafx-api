package in.truethics.ethics.ethicsapiv10.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericDatatable<T> {
//    private List<T> rows;
//    private Integer totalRows;


    private List<T> data;
    private long total;//total rows
    private int page; // page number
    private int per_page; // page size/ limit
    private int total_pages; // total pages
}
