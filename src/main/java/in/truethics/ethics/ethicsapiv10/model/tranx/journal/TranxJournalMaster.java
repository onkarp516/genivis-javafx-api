package in.truethics.ethics.ethicsapiv10.model.tranx.journal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tranx_journal_master_tbl")
public class TranxJournalMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonManagedReference
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    @JsonManagedReference
    private Outlet outlet;

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    @JsonManagedReference
    private FiscalYear fiscalYear;

    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TranxJournalDetails> tranxJournalDetails;


    @Column(name = "journal_no")
    private String journalNo;
    @Column(name = "journal_sr_no")
    private double journalSrNo;
    @Column(name = "transcation_date")
        private Date transcationDate;
    @Column(name = "total_amt")
    private double totalAmt;
    private boolean status;

    private String narrations;
    @Column(name = "financial_year")
    private String financialYear;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "tranx_code")
    private String tranxCode;//Transaction unique code of each transaction performed


}
