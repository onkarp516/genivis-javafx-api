package in.truethics.ethics.ethicsapiv10.controller.masters;


import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.service.master_service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
public class BranchController {
    @Autowired
    private BranchService branchService;

    @PostMapping(path = "/create_branch")
    public ResponseEntity<?> createBranch(HttpServletRequest request) {
        return ResponseEntity.ok(branchService.createBranch(request));
    }

    @PostMapping(path = "/update_branch")
    public Object updateBranch(HttpServletRequest request) {
        JsonObject newObject = branchService.updateBranch(request);
        return newObject.toString();
    }

    /* get Branches of Super admin */
    @GetMapping(path = "/get_branches_super_admin")
    public Object getAllBranches(HttpServletRequest request) {
        JsonObject res = branchService.getAllBranches(request);
        return res.toString();
    }

    @GetMapping(path = "/get_branches_by_company")
    public Object getBranchesCompany(HttpServletRequest request) {
        JsonObject res = branchService.getBranchesCompany(request);
        return res.toString();
    }

    @PostMapping(path = "/get_branches_by_selection_of_company")
    public Object getBranchbySelectionCompany(HttpServletRequest request) {
        JsonObject res = branchService.getBranchesBySelectionCompany(request);
        return res.toString();
    }


    /* get branch by id */
    @PostMapping(path = "/get_branch_by_id")
    public Object getBranchById(HttpServletRequest request) {
        JsonObject jsonObject = branchService.getBranchById(request);
        return jsonObject.toString();
    }

    /***** duplicate Branch *****/
    @PostMapping(path = "/validate_branch")
    public Object duplicateBranch(HttpServletRequest request) {
        JsonObject jsonObject = branchService.duplicateBranch(request);
        return jsonObject.toString();
    }
    @PostMapping(path = "/validate_branch_update")
    public Object duplicateBranchUpdate(HttpServletRequest request) {
        JsonObject jsonObject = branchService.duplicateBranchUpdate(request);
        return jsonObject.toString();
    }
    /***** validation of duplicate Branch Admin *****/
    @PostMapping(path = "/validate_branch_admin")
    public Object duplicateBranchAdmin(HttpServletRequest request) {
        JsonObject jsonObject = branchService.duplicateBranchAdmin(request);
        return jsonObject.toString();
    }
}
