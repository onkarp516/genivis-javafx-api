package in.truethics.ethics.ethicsapiv10.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class TranxCodeUtility {
    private static AtomicInteger autoInteger = new AtomicInteger(0);

    private TranxCodeUtility() {

    }

    public static Integer getNextNum() {
        if (autoInteger.get() == 0) {
            synchronized (TranxCodeUtility.class) {
                return autoInteger.incrementAndGet();

            }
        } else {
            if (autoInteger.get() == 999) {
                autoInteger.set(0);
            }
            return autoInteger.incrementAndGet();
        }
    }

    public static String generateTxnId(String txnDesc) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        String txnId = txnDesc + sdf.format(new Date());
        Integer seqId = getNextNum();
//        System.out.println(txnId+String.format("%03d", seqId));
        return txnId+String.format("%03d",seqId);
    }
}
