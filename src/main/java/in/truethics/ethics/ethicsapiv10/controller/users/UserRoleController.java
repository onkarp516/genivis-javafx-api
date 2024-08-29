package in.truethics.ethics.ethicsapiv10.controller.users;

import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.user_service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;


    @PostMapping(path="create_user_role")
    public Object createUserRole(HttpServletRequest request) {
        JsonObject jsonObject = userRoleService.createUserRole(request);
        return jsonObject.toString();
    }

    @PostMapping(path = "/get_role_permissions_by_id")
    public Object getRolePermissions(HttpServletRequest request) {
        JsonObject res = userRoleService.getRolePermissionById(request);
        return res.toString();
    }

    @GetMapping(path = "/get_role_permission_list")
    public Object getAllRolePermissionList() {
        JsonObject res = userRoleService.getAllRolePermissionList();
        return res.toString();
    }

    @PostMapping(path = "/get_role_by_id")
    public Object getRoleById(HttpServletRequest request) {
        JsonObject res = userRoleService.getRoleById(request);
        return res.toString();
    }



    @PostMapping(path = "/update_role")
    public ResponseEntity<?> updateRole(HttpServletRequest request) {
        return ResponseEntity.ok(userRoleService.updateRole(request));
    }
    /* validate of Users for  create page duplication */
    @PostMapping(path = "/validate_user_role")
    public Object validateUserRole(HttpServletRequest request) {
        JsonObject jsonObject = userRoleService.validateUserRole(request);
        return jsonObject.toString();
    }

    /* validate of Users for  edit page duplication */
    @PostMapping(path = "/validate_user_role_update")
    public Object validateUserRoleUpdate(HttpServletRequest request) {
        JsonObject jsonObject = userRoleService.validateUserRoleUpdate(request);
        return jsonObject.toString();
    }

//    @PostMapping(path= "/delete_role")
//    public Object roleDelete(HttpServletRequest request){
//        JsonObject object =userRoleService.roleDelete(request);
//        return object.toString();
//    }
}
