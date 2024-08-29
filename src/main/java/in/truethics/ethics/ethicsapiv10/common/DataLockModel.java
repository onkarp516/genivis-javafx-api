package in.truethics.ethics.ethicsapiv10.common;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class DataLockModel {
    private static boolean isCreated = false;

    private static DataLockModel instance = null;
    private static Map<String, Object> dataLockMap = null;//new ConcurrentHashMap<>();
    private DataLockModel(){
        dataLockMap = new ConcurrentHashMap<>();
    }

    public static DataLockModel getInstance(){
        if(instance == null){
            synchronized (DataLockModel.class){
                if(instance == null){
                    instance = new DataLockModel();
                }
            }
        }
        return instance;
    }


    public void addObject(String key, Object obj){
        dataLockMap.put(key, obj);
    }

    public void removeObject(String key){
        dataLockMap.remove(key);
    }

    public boolean isPresent(String key){
        return dataLockMap.containsKey(key);
    }

    public void removeAll(){
        dataLockMap.clear();
    }
}
