package in.truethics.ethics.ethicsapiv10.service.master_service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.common.FindCatlog;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.master.Branch;
import in.truethics.ethics.ethicsapiv10.model.master.Outlet;
import in.truethics.ethics.ethicsapiv10.model.master.PackingMaster;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.ledgerdetails_repository.LedgerTransactionPostingsRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.PackingMasterRepository;
import in.truethics.ethics.ethicsapiv10.util.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class PackingMasterService {
    @Autowired
    private PackingMasterRepository repository;
    @Autowired
    JwtTokenUtil jwtRequestFilter;
    @Autowired
    LedgerTransactionPostingsRepository ledgerTransactionPostingsRepository;

    @Autowired
    private FindCatlog findCatlog;
    @Autowired
    private ProductRepository productRepository;
    private static final Logger packingLogger = LogManager.getLogger(PackingMasterService.class);

    public JsonObject createPackaging(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        if (validatePackage(request.getParameter("packing_name").trim(), users.getOutlet(), branch, 0L)) {
            jsonObject.addProperty("message", request.getParameter("packing_name").trim()+ " already created");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            PackingMaster mPackageMaster = null;
            PackingMaster packingMaster = new PackingMaster();
            packingMaster.setPackName(request.getParameter("packing_name").trim());
            packingMaster.setBranch(branch);
            packingMaster.setOutlet(users.getOutlet());
            packingMaster.setStatus(true);
            packingMaster.setCreatedBy(users.getId());
            try {
                mPackageMaster = repository.save(packingMaster);
            } catch (Exception e) {
                e.printStackTrace();
                packingLogger.error("createPackaging -> failed to createPackaging " + e);
                jsonObject.addProperty("message", "error");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            jsonObject.addProperty("message", "Package created successfully");
            jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            jsonObject.addProperty("responseObject", mPackageMaster.getId());
        }
        return jsonObject;
    }

    private boolean validatePackage(String package_name, Outlet outlet, Branch branch, Long id) {
        Boolean flag = false;
        PackingMaster packageMaster = null;
        if (branch != null) {
            if (id != 0)
                packageMaster = repository.findByOutletIdAndBranchIdAndPackNameIgnoreCaseAndStatusAndIdNot(
                        outlet.getId(), branch.getId(), package_name, true, id);
            else
                packageMaster = repository.findByOutletIdAndBranchIdAndPackNameIgnoreCaseAndStatus(outlet.getId(), branch.getId(), package_name, true);
        } else {
            if (id != 0)
                packageMaster = repository.findByOutletIdAndPackNameIgnoreCaseAndStatusAndIdNotAndBranchIsNull(
                        outlet.getId(), package_name, true, id);
            else
                packageMaster = repository.findByOutletIdAndPackNameIgnoreCaseAndStatusAndBranchIsNull(outlet.getId(), package_name, true);
        }
        if (packageMaster != null) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    public JsonObject getPackagings(HttpServletRequest request) {
        JsonArray array = new JsonArray();
        JsonObject res = new JsonObject();
        List<PackingMaster> list = new ArrayList<>();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        if (users.getBranch() != null) {
            list = repository.findByOutletIdAndBranchIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), true);
        } else {
            list = repository.findByOutletIdAndStatusAndBranchIsNull(users.getOutlet().getId(), true);
        }
        for (PackingMaster mPack : list) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", mPack.getId());
            jsonObject.addProperty("name", mPack.getPackName());
            array.add(jsonObject);
        }
        if (list != null && list.size() > 0) {
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("list", array);
        } else {
            res.addProperty("message", "empty list");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("list", array);
        }
        return res;
    }

    public JsonObject updatePackaging(HttpServletRequest request) {
        JsonObject jsonObject = new JsonObject();
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        Branch branch = null;
        PackingMaster packingMaster = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
        if (users.getBranch() != null) branch = users.getBranch();
        if (validatePackage(request.getParameter("packing_name").trim(), users.getOutlet(), branch, packingMaster.getId())) {
            jsonObject.addProperty("message", request.getParameter("packing_name").trim()+ " already created");
            jsonObject.addProperty("responseStatus", HttpStatus.CONFLICT.value());
        } else {
            try {
                packingMaster.setPackName(request.getParameter("packing_name").trim());
                packingMaster.setUpdatedBy(users.getId());
                repository.save(packingMaster);
                jsonObject.addProperty("message", "Package updated successfully");
                jsonObject.addProperty("responseStatus", HttpStatus.OK.value());
            } catch (Exception e) {
                e.printStackTrace();
                packingLogger.error("updatePackaging -> failed to updatePackaging" + e);
                jsonObject.addProperty("message", "error");
                jsonObject.addProperty("responseStatus", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
        return jsonObject;
    }

    public JsonObject updgetPackagingById(HttpServletRequest request) {
        JsonObject mObject = new JsonObject();
        JsonObject res = new JsonObject();
        PackingMaster mPack = null;
        try {
            Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
            mPack = repository.findByIdAndStatus(Long.parseLong(request.getParameter("id")), true);
            mObject.addProperty("id", mPack.getId());
            mObject.addProperty("name", mPack.getPackName());
            res.addProperty("message", "success");
            res.addProperty("responseStatus", HttpStatus.OK.value());
            res.add("data", mObject);
        } catch (Exception e) {
            e.printStackTrace();
            packingLogger.error("updgetPackagingById-> failed to updgetPackagingById" + e);
        }
        return res;
    }

    public JsonObject removeMultiplePackages(HttpServletRequest request) {
        Users users = jwtRequestFilter.getUserDataFromToken(request.getHeader("Authorization").substring(7));
        JsonParser parser = new JsonParser();
        JsonArray usedArray = new JsonArray();
        JsonArray removedArray = new JsonArray();
        JsonObject finalObject = new JsonObject();
        Branch branch = null;
        if (users.getBranch() != null) {
            branch = users.getBranch();
        }
        String removePackageList = request.getParameter("removepackageslist");
        JsonElement removePackageElement = parser.parse(removePackageList);
        JsonArray removeDeptJson = removePackageElement.getAsJsonArray();
        PackingMaster packages = null;
        List<Product> products = new ArrayList<>();
        if (removeDeptJson.size() > 0) {
            for (JsonElement mList : removeDeptJson) {
                Long object = mList.getAsLong();
                JsonObject removePackage = new JsonObject();
                JsonObject usedPackages = new JsonObject();
                if (object != 0) {
                    if (branch != null) {
                        packages = repository.findByOutletIdAndBranchIdAndIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIdAndPackingMasterIdAndStatus(users.getOutlet().getId(), users.getBranch().getId(), object, true);
                    } else {
                        packages = repository.findByOutletIdAndBranchIsNullAndIdAndStatus(users.getOutlet().getId(), object, true);
                        products = productRepository.findByOutletIdAndBranchIsNullAndPackingMasterIdAndStatus(users.getOutlet().getId(), object, true);
                    }
                    if (products != null && products.size() > 0) {
                        usedPackages.addProperty("message", packages.getPackName() + " is used in product ,First delete product");
                        usedPackages.addProperty("name", packages.getPackName());
                        usedArray.add(usedPackages);
                    } else {
                        if (packages != null) packages.setStatus(false);
                        try {
                            repository.save(packages);
                            removePackage.addProperty("message", packages.getPackName() + " Deleted Successfully");
                            removePackage.addProperty("name", packages.getPackName());
                            removedArray.add(removePackage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Exception:" + e.getMessage());
                            e.getMessage();
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        finalObject.add("removedArray", removedArray);
        finalObject.add("usedArray", usedArray);
        finalObject.addProperty("responseStatus", HttpStatus.OK.value());
        return finalObject;
    }
}
