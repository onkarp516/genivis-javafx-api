package in.truethics.ethics.ethicsapiv10.controller.masters;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.PrincipleGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PrinciplesGroupController {
    @Autowired
    PrincipleGroupService service;

    @PostMapping(path = "/create_subgroup")
    public ResponseEntity<?> createSubgroup(HttpServletRequest request) {
        return ResponseEntity.ok(service.createSubgroup(request));
    }

    /*  get list outletwise*/
    @GetMapping(path = "/get_under_list")
    public Object getAllAccountGroups(HttpServletRequest request) {
        return service.getAllAccountGroups(request).toString();
    }

    /* Create Associates Groups 'N' number of levels */
    @PostMapping(path = "/create_associate_groups")
    public ResponseEntity<?> createAssocitesgroup(HttpServletRequest request) {
        return ResponseEntity.ok(service.createAssocitesgroup(request));
    }
    /* Validate Associates Groups */
    @PostMapping(path = "/validate_associate_groups")
    public ResponseEntity<?> validateAssocitesgroup(HttpServletRequest request) {
        return ResponseEntity.ok(service.validateAssocitesgroup(request));
    }
    /* Validate Associates Groups for update  */
    @PostMapping(path = "/validate_associate_groups_update")
    public ResponseEntity<?> validateAssocitesgroupUpdate(HttpServletRequest request) {
        return ResponseEntity.ok(service.validateAssocitesgroupUpdate(request));
    }

    /* Edit Associates Groups of 'N' number of levels */
    @PostMapping(path = "/edit_associate_groups")
    public ResponseEntity<?> editAssocitesgroup(HttpServletRequest request) {
        return ResponseEntity.ok(service.editAssocitesgroup(request));
    }

    /* get all associates groups */
    @GetMapping(path = "/get_associate_groups")
    public Object getAssocitesgroup(HttpServletRequest request) {
        return service.getAssocitesgroup(request).toString();
    }
    /* DT associates groups */
   /* @PostMapping(path = "/DTAssociateGroups")
    public Object DTAssociateGroups(@RequestBody Map<String, String> request, HttpServletRequest req) {
        return service.DTAssociateGroups(request, req);
    }
*/
    /* Get Associates Groups of 'N' number of levels */
    @PostMapping(path = "/get_associate_group")
    public Object get_associate_group(HttpServletRequest request) {
        return service.get_associate_group(request);
    }

    @PostMapping(path = "/delete_ledger_group")
    public Object ledgergroupDelete(HttpServletRequest request) {
        JsonObject object = service.ledgergroupDelete(request);
        return object.toString();
    }

    @PostMapping(path = "/search_associate_groups")
    public Object searchAssocitesgroupAPI(HttpServletRequest request) {
        return service.searchAssocitesgroup(request).toString();
    }
}
