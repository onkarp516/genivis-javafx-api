package in.truethics.ethics.ethicsapiv10.common;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class GenerateCoreLogic {

    /**
     * @param openingBal
     * @param dr
     * @param cr
     * @return
     */
    public Double generateCrLogic(Double openingBal, Double dr, Double cr) {
        Double closingBal = openingBal - dr + cr;
        return closingBal;
    }

    /**
     * @param openingBal
     * @param dr
     * @param cr
     * @return
     */
    public Double generateDrLogic(Double openingBal, Double dr, Double cr) {
        Double closingBal = openingBal + dr - cr;
        return closingBal;
    }
}
