package in.truethics.ethics.ethicsapiv10.model.master;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "city_tbl")
public class City {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 50)
        private String name;

        @Column(columnDefinition = "mediumint", name = "state_id")
        private Long stateId;
        @Column(name = "state_code")
        private String stateCode;

        @Column(columnDefinition = "mediumint", name = "country_id")
        private Long countryId;

        @Column(length = 2, columnDefinition = "char", name = "country_code")
        private String countryCode;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JsonIgnoreProperties(value = {"city","hibernateLazyInitializer"})
//    @JoinColumn(name = "state_id", nullable = false)
//    private State state;





}
