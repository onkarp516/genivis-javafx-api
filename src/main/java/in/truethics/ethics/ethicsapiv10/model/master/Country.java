package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "country_tbl")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String name;
    private String phonecode;
    private String currency;
    @Column(name = "currency_symbol")
    private String currencySymbol;
}
