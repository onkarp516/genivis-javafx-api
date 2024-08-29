package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class GroupController {
    @Autowired
    private GroupService groupService;


    @PostMapping(path = "/create_group")
    public ResponseEntity<?> addGroup(HttpServletRequest request) {
        return ResponseEntity.ok(groupService.addGroup(request));
    }

    /* update group by id*/
    @PostMapping(path = "/update_group")
    public Object updateGroup(HttpServletRequest request) {
        JsonObject result = groupService.updateGroup(request);
        return result.toString();
    }

    /* Get all groups of Outlets */
    @GetMapping(path = "/get_outlet_groups")
    public Object getAllOutletGroups(HttpServletRequest request) {
        JsonObject result = groupService.getAllOutletGroups(request);
        return result.toString();
    }

    /* get Group by Id */
    @PostMapping(path = "/get_groups_by_id")
    public Object getGroupById(HttpServletRequest request) {
        JsonObject result = groupService.getGroupById(request);
        return result.toString();
    }


    /**** Remove Multiple Group ****/
    @PostMapping(path = "/remove-multiple-groups")
    public Object removeMultipleGroups(HttpServletRequest request) {
        JsonObject result = groupService.removeMultipleGroups(request);
        return result.toString();
    }

}
