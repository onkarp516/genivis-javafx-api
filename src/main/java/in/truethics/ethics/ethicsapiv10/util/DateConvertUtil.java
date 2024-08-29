package in.truethics.ethics.ethicsapiv10.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class DateConvertUtil {
    public static LocalDate convertDateToLocalDate(Date date) {
        return LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(date));
    }

    public static LocalDate convertStringToLocalDate(String date) {
        return LocalDate.parse(date);
//        return LocalDate.parse(new SimpleDateFormat("YYYY-MM-dd").format(date));
    }

    public static Date convertStringToDate(String date) {
        Date dt = new Date();
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String str[] = sdf1.format(dt).split(" ");
            String time = str[1];
            date = date +" "+ time;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            dt = sdf.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return dt;
    }
}
