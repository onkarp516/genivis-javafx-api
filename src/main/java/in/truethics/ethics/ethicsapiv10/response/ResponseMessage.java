package in.truethics.ethics.ethicsapiv10.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage {
    private int responseStatus;
    private Object responseObject;
    private static ResponseMessage instance = null;
    private String message = null;
    private String data;
    private Object response;
    private String userRole="";

    public static ResponseMessage getInstance() {

        if (instance == null) {
            instance = new ResponseMessage();
        }
        return instance;
    }
}
