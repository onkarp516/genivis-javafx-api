package in.truethics.ethics.ethicsapiv10.model.tranx.sales;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.PackingMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tranx_cs_prod_details_tbl")
public class TranxCounterSalesDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "tranx_counter_sale_id")
    @JsonManagedReference
    private TranxCounterSales counterSale;

    @ManyToOne
    @JoinColumn(name = "packing_master_id")
    @JsonManagedReference
    private PackingMaster packingMaster;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<TranxCounterSalesProdSrNo> tranxCounterSalesProdSrNos;

    private Double qtyHigh;
    private Double rateHigh;
    private Double qtyMedium;
    private Double rateMedium;
    private Double qtyLow;
    private Double rateLow;
    private Double baseAmtHigh;
    private Double baseAmtLow;
    private Double baseAmtMedium;
    private Double totalPrice;
    private Double disAmt;
    private Double disPer;
    private Double disAmtCal;
    private Double disPerCal;
    //private Long qty;
    private Boolean status;
    private LocalDate billDate;
}
