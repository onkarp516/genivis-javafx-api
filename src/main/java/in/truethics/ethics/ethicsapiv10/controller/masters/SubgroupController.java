package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.SubgroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SubgroupController {
    @Autowired
    private SubgroupService subgroupService;


    @PostMapping(path = "/create_sub_group")
    public ResponseEntity<?> addSubgroup(HttpServletRequest request) {
        return ResponseEntity.ok(subgroupService.addSubgroup(request));
    }

    /* update Subgroup by id*/
    @PostMapping(path = "/update_sub_group")
    public Object updateSubgroup(HttpServletRequest request) {
        JsonObject result = subgroupService.updateSubgroup(request);
        return result.toString();
    }

    /* get SubGroup by Id */
    @PostMapping(path = "/get_sub_group_by_id")
    public Object getSubGroupById(HttpServletRequest request) {
        JsonObject result = subgroupService.getSubGroupById(request);
        return result.toString();
    }

    /* get all subgroups of group */
    @PostMapping(path = "/get_all_subgroups")
    public Object getAllSubGroups(HttpServletRequest request) {
        JsonObject result = subgroupService.getAllSubGroups(request);
        return result.toString();
    }

    /* Get all subgroups of Outlets */
    @GetMapping(path = "/get_outlet_subgroups")
    public Object getAllOutletSubGroups(HttpServletRequest request) {
        JsonObject result = subgroupService.getAllOutletSubGroups(request);
        return result.toString();
    }
    /**** Remove Multiple SubGroup ****/
    @PostMapping(path = "/remove-multiple-subgroups")
    public Object removeMultipleSubGroups(HttpServletRequest request) {
        JsonObject result =subgroupService.removeMultipleSubGroups(request);
        return result.toString();
    }

}
