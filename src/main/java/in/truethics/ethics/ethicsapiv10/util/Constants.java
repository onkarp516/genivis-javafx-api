package in.truethics.ethics.ethicsapiv10.util;

import java.text.DecimalFormat;

public class Constants {
    public static String CONSUMER_SALES="consumer_sales";
    public static DecimalFormat decimalFormat = new DecimalFormat("0.#");
    static String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public  static Double CAL_DR_CLOSING(Double opening, Double credit, Double debit) {
        return opening + debit - credit;
    }
    public static  Double CAL_CR_CLOSING(Double opening, Double credit, Double debit) {
        return opening - debit + credit;
    }
    public static String num_hash(int num) {
        if (num < 26) return Character.toString(alpha.charAt(num - 1));
        else {
            int q = Math.floorDiv(num, 26);
            int r = num % 26;
            if (r == 0) {
                if (q == 1) {
                    return Character.toString(alpha.charAt((26 + r - 1) % 26));
                } else return num_hash(q - 1) + alpha.charAt((26 + r - 1) % 26);
            } else return num_hash(q) + alpha.charAt((26 + r - 1) % 26);
        }
    }
}
