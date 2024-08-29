package in.truethics.ethics.ethicsapiv10.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomArrayUtilities {
    public static List<Long> getTwoArrayMergeUnique(List<Long> arrayOne,List<Long> arrayTwo){
        Set<Long> set = new HashSet<Long>();
        set.addAll(arrayOne);
        set.addAll(arrayTwo);
        List<Long> setList = new ArrayList<>();
        setList.addAll(set);
        return setList;
    }
}
