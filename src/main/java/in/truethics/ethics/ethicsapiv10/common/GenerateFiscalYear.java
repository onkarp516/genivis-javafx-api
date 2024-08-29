package in.truethics.ethics.ethicsapiv10.common;

import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.FiscalYearRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
@Data
public class GenerateFiscalYear {
    @Autowired
    private FiscalYearRepository fiscalYearRepository;

    public FiscalYear getFiscalYear(LocalDate curDate) {
        FiscalYear fiscalYear = null;
        FiscalYear lastRecord = fiscalYearRepository.findTopByOrderByIdDesc();
        fiscalYear = fiscalYearRepository.findFiscalYear(curDate,lastRecord.getDateStart(),lastRecord.getDateEnd());
        return fiscalYear;
    }

    //generating Start years from Start Date
    public String getStartYear() {
        String startYear = null;
        startYear = fiscalYearRepository.getStartYear();
        return startYear;
    }

    //generating Last years from Start Date
    public String getEndYear() {
        String endYear = null;
        endYear = fiscalYearRepository.getLastYear();
        return endYear;
    }

    public void generateFiscalYear() {

    }
}
