package in.truethics.ethics.ethicsapiv10.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import in.truethics.ethics.ethicsapiv10.model.barcode.ProductBatchNo;
import in.truethics.ethics.ethicsapiv10.model.inventory.Product;
import in.truethics.ethics.ethicsapiv10.model.inventory.ProductUnitPacking;
import in.truethics.ethics.ethicsapiv10.model.master.FiscalYear;
import in.truethics.ethics.ethicsapiv10.model.master.LevelA;
import in.truethics.ethics.ethicsapiv10.model.master.LevelB;
import in.truethics.ethics.ethicsapiv10.model.master.LevelC;
import in.truethics.ethics.ethicsapiv10.repository.barcode_repository.ProductBatchNoRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductRepository;
import in.truethics.ethics.ethicsapiv10.repository.inventory_repository.ProductUnitRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.LevelARepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.LevelBRepository;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.LevelCRepository;
import in.truethics.ethics.ethicsapiv10.util.DateConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ProductData {

    @Autowired
    private ProductUnitRepository productUnitRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductBatchNoRepository productBatchNoRepository;
    @Autowired
    private LevelARepository levelARepository;
    @Autowired
    private LevelBRepository levelBRepository;
    @Autowired
    private LevelCRepository levelCRepository;

    @Autowired
    private InventoryCommonPostings inventoryCommonPostings;
    @Autowired
    private GenerateFiscalYear generateFiscalYear;


    public JsonArray getProductByBFPUCommon(LocalDate invoiceDate, List<Object[]> productIds) {
        JsonArray productArray = new JsonArray();
        for (int i = 0; i < productIds.size(); i++) {
            JsonObject response = new JsonObject();
            Object obj[] = productIds.get(i);
            Product mProduct = productRepository.findByIdAndStatus(Long.parseLong(obj[0].toString()), true);
            List<ProductUnitPacking> brandsArray = productUnitRepository.findByUniqueProductIdAndStatus(Long.parseLong(obj[0].toString()), true);
            JsonArray brandsJsonArray = new JsonArray();
            /***Brands Master ****/
            for (ProductUnitPacking bId : brandsArray) {
                List<ProductUnitPacking> brandsMasters = productUnitRepository.findByUniqueBrandsList(Long.parseLong(obj[0].toString()), true);
                for (ProductUnitPacking mBrands : brandsMasters) {
                    JsonObject brandsObjects = new JsonObject();
                    Long brandId = null;
                    List<ProductUnitPacking> groupArray = new ArrayList<>();
                    if (mBrands.getBrand() != null) {
                        brandsObjects.addProperty("label", mBrands.getBrand().getBrandName());
                        brandsObjects.addProperty("value", mBrands.getBrand().getId());
                        brandsObjects.addProperty("isDisable", "false");
                        brandId = mBrands.getBrand().getId();
                    } else {
                        brandsObjects.addProperty("label", "");
                        brandsObjects.addProperty("value", "");
                        brandsObjects.addProperty("isDisable", "false");
                    }
                    groupArray = productUnitRepository.findByUniqueGroupListwithBrands(
                            Long.parseLong(obj[0].toString()), brandId, true);
                    JsonArray groupJsonArray = new JsonArray();
                    for (ProductUnitPacking mGroup : groupArray) {
                        JsonObject groupJsonObject = new JsonObject();
                        Long groupId = null;
                        List<ProductUnitPacking> categoryArray = new ArrayList<>();
                        if (mGroup.getGroup() == null) {
                            groupJsonObject.addProperty("label", "");
                            groupJsonObject.addProperty("value", "");
                            groupJsonObject.addProperty("isDisable", "false");
                        } else {
                            groupJsonObject.addProperty("label", mGroup.getGroup().getGroupName());
                            groupJsonObject.addProperty("value", mGroup.getGroup().getId());
                            groupJsonObject.addProperty("isDisable", "false");
                            groupId = mGroup.getGroup().getId();
                        }
                        categoryArray = productUnitRepository.findByUniqueCategoryListBrands(
                                Long.parseLong(obj[0].toString()), brandId, groupId, true);
                        JsonArray categoryJsonArray = new JsonArray();
                        for (ProductUnitPacking mCategory : categoryArray) {
                            JsonObject categoryJsonObject = new JsonObject();
                            Long categoryId = null;
                            List<ProductUnitPacking> subCategoryArray = new ArrayList<>();
                            if (mCategory.getCategory() == null) {
                                categoryJsonObject.addProperty("label", "");
                                categoryJsonObject.addProperty("value", "");
                                categoryJsonObject.addProperty("isDisable", "false");
                            } else {
                                categoryJsonObject.addProperty("label", mCategory.getCategory().getCategoryName());
                                categoryJsonObject.addProperty("value", mCategory.getCategory().getId());
                                categoryJsonObject.addProperty("isDisable", "false");
                                categoryId = mCategory.getCategory().getId();
                            }
                            subCategoryArray = productUnitRepository.
                                    findByUniqueSubcategoryListBrands(Long.parseLong(obj[0].toString()), brandId,
                                            groupId, categoryId, true);
                            JsonArray subCategoryJsonArray = new JsonArray();
                            for (ProductUnitPacking subCategory : subCategoryArray) {
                                JsonObject subCategoryJsonObject = new JsonObject();
                                Long subCateId = null;
                                List<ProductUnitPacking> packageArray = new ArrayList<>();
                                if (subCategory.getSubcategory() != null) {
                                    subCategoryJsonObject.addProperty("label", subCategory.getSubcategory().getSubcategoryName());
                                    subCategoryJsonObject.addProperty("value", subCategory.getSubcategory().getId());
                                    subCategoryJsonObject.addProperty("isDisable", "false");
                                    subCateId = subCategory.getSubcategory().getId();
                                } else {
                                    subCategoryJsonObject.addProperty("label", "");
                                    subCategoryJsonObject.addProperty("value", "");
                                    subCategoryJsonObject.addProperty("isDisable", "false");
                                }
                                JsonArray packageJsonArray = new JsonArray();
                                packageArray = productUnitRepository.
                                        findByUniquePackageListBrands(Long.parseLong(obj[0].toString()), brandId,
                                                groupId, categoryId, subCateId, true);
                                for (ProductUnitPacking packages : packageArray) {
                                    JsonObject packageJsonObject = new JsonObject();
                                    Long packageId = null;
                                    if (packages.getPackingMaster() != null) {
                                        packageId = packages.getPackingMaster().getId();
                                        packageJsonObject.addProperty("label", packages.getPackingMaster().getPackName());
                                        packageJsonObject.addProperty("value", packages.getPackingMaster().getId());
                                        packageJsonObject.addProperty("isDisable", "false");
                                    } else {
                                        packageJsonObject.addProperty("label", "");
                                        packageJsonObject.addProperty("value", "");
                                        packageJsonObject.addProperty("isDisable", "false");
                                    }
                                    JsonArray unitsJsonArray = new JsonArray();
                                    List<ProductUnitPacking> unitsArray = productUnitRepository.
                                            findByUniqueUnitsBrands(Long.parseLong(obj[0].toString()), brandId,
                                                    groupId, categoryId, subCateId, packageId, true);
                                    for (ProductUnitPacking units : unitsArray) {
                                        JsonObject unitsJsonObjects = new JsonObject();
                                        Long unitId = units.getUnits().getId();
                                        unitsJsonObjects.addProperty("label", units.getUnits().getUnitName());
                                        unitsJsonObjects.addProperty("value", units.getUnits().getId());
                                        unitsJsonObjects.addProperty("isDisable", "false");
                                        unitsJsonObjects.addProperty("isNegativeStocks", units.getIsNegativeStocks());
                                        unitsJsonObjects.addProperty("unit_id", units.getUnits().getId());
                                        unitsJsonObjects.addProperty("details_id", units.getId());
                                        unitsJsonObjects.addProperty("unit_name", units.getUnits().getUnitName());
                                        unitsJsonObjects.addProperty("unit_conv", units.getUnitConversion());
                                        unitsJsonObjects.addProperty("unit_marg", units.getUnitConvMargn());
                                        unitsJsonObjects.addProperty("mrp", units.getMrp());
                                        unitsJsonObjects.addProperty("purchase_rate", units.getPurchaseRate());
                                        unitsJsonObjects.addProperty("rateA", units.getMinRateA() != null ? units.getMinRateA() : 0);
                                        unitsJsonObjects.addProperty("rateB", units.getMinRateB() != null ? units.getMinRateB() : 0);
                                        unitsJsonObjects.addProperty("rateC", units.getMinRateC() != null ? units.getMinRateC() : 0);
                                        unitsJsonObjects.addProperty("minMargin", units.getMinMargin());
                                        unitsJsonObjects.addProperty("maxDiscount", units.getMaxDiscount());
                                        unitsJsonObjects.addProperty("minDiscount", units.getMinDiscount());
                                        unitsJsonObjects.addProperty("igst", units.getTaxMaster().getIgst());
                                        unitsJsonObjects.addProperty("cgst", units.getTaxMaster().getCgst());
                                        unitsJsonObjects.addProperty("sgst", units.getTaxMaster().getSgst());
                                        unitsJsonObjects.addProperty("hsnId", units.getProductHsn() != null ? units.getProductHsn().getId() : null);
                                        unitsJsonObjects.addProperty("taxMasterId", units.getTaxMaster() != null ? units.getTaxMaster().getId() : null);
                                        unitsJsonObjects.addProperty("applicableDate", units.getTaxApplicableDate() != null ? units.getTaxApplicableDate().toString() : "");
                                        unitsJsonObjects.addProperty("brandId", units.getBrand() != null ? units.getBrand().getId() : null);
                                        unitsJsonObjects.addProperty("groupId", units.getGroup() != null ? units.getGroup().getId() : null);
                                        unitsJsonObjects.addProperty("categoryId", units.getCategory() != null ? units.getCategory().getId() : null);
                                        unitsJsonObjects.addProperty("subcategoryId", units.getSubcategory() != null ? units.getSubcategory().getId() : null);
                                        unitsJsonObjects.addProperty("packageId", units.getPackingMaster() != null ?
                                                units.getPackingMaster().getId() : null);

                                        /***** Batch Number against Product *****/
                                        if (mProduct.getIsBatchNumber()) {
                                            JsonArray batchArray = new JsonArray();
                                            List<ProductBatchNo> batchNos = productBatchNoRepository.findByUniqueProductIdAndStatus(mProduct.getId(),
                                                    brandId, groupId, categoryId, subCateId, packageId, unitId, true);
                                            if (batchNos != null && batchNos.size() > 0) {
                                                for (ProductBatchNo mBatch : batchNos) {
                                                    JsonObject batchObject = new JsonObject();
                                                    batchObject.addProperty("batch_no", mBatch.getBatchNo());
                                                    batchObject.addProperty("qty", mBatch.getQnty());
                                                    batchObject.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                                                    batchObject.addProperty("purchase_rate", mBatch.getPurchaseRate());
                                                    batchObject.addProperty("min_rate_a", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0);
                                                    batchObject.addProperty("min_rate_b", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0);
                                                    batchObject.addProperty("min_rate_c", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0);
                                                    batchObject.addProperty("max_discount", mBatch.getMaxDiscount());
                                                    batchObject.addProperty("min_discount", mBatch.getMinDiscount());
                                                    batchObject.addProperty("manufacturing_date", mBatch.getManufacturingDate().toString());
                                                    batchObject.addProperty("mrp", mBatch.getMrp());
                                                    batchObject.addProperty("min_margin", mBatch.getMinMargin());
                                                    batchObject.addProperty("id", mBatch.getId());
                                                    if (mBatch.getExpiryDate() != null) {
                                                        if (invoiceDate.isAfter(mBatch.getExpiryDate())) {
                                                            batchObject.addProperty("is_expired", true);
                                                        } else {
                                                            batchObject.addProperty("is_expired", false);
                                                        }
                                                    } else {
                                                        batchObject.addProperty("is_expired", false);
                                                    }

                                                    batchArray.add(batchObject);
                                                }
                                            }
                                            unitsJsonObjects.add("batchOpt", batchArray);
                                        }
                                        unitsJsonArray.add(unitsJsonObjects);
                                    }
                                    packageJsonObject.add("unitOpt", unitsJsonArray);
                                    packageJsonArray.add(packageJsonObject);
                                }
                                subCategoryJsonObject.add("packageOpt", packageJsonArray);
                                subCategoryJsonArray.add(subCategoryJsonObject);
                            }
                            categoryJsonObject.add("subCategoryOpt", subCategoryJsonArray);
                            categoryJsonArray.add(categoryJsonObject);
                        }
                        groupJsonObject.add("categoryOpt", categoryJsonArray);
                        groupJsonArray.add(groupJsonObject);
                    }
                    brandsObjects.add("groupOpt", groupJsonArray);
                    brandsJsonArray.add(brandsObjects);
                }
            }
            response.addProperty("product_id", obj[0].toString());
            response.addProperty("value", obj[0].toString());
            response.add("brandsOpt", brandsJsonArray);
            response.addProperty("isBrand", mProduct.getIsBrand());
            response.addProperty("isGroup", mProduct.getIsGroup());
            response.addProperty("isCategory", mProduct.getIsCategory());
            response.addProperty("isSubcategory", mProduct.getIsSubCategory());
            response.addProperty("isPackage", mProduct.getIsPackage());


            productArray.add(response);
        }
        return productArray;
    }


    public JsonArray getProductByBFPUCommon_Return(LocalDate purReturnDate, List<Object[]> productIds) {
        JsonArray productArray = new JsonArray();
        for (int i = 0; i < productIds.size(); i++) {
            JsonObject response = new JsonObject();
            Object obj[] = productIds.get(i);
            Product mProduct = productRepository.findByIdAndStatus(Long.parseLong(obj[0].toString()), true);
            List<ProductUnitPacking> brandsArray = productUnitRepository.findByUniqueProductIdAndStatus(Long.parseLong(obj[0].toString()), true);
            JsonArray brandsJsonArray = new JsonArray();
            /***Brands Master ****/
            for (ProductUnitPacking bId : brandsArray) {
                List<ProductUnitPacking> brandsMasters = productUnitRepository.findByUniqueBrandsList(Long.parseLong(obj[0].toString()), true);
                for (ProductUnitPacking mBrands : brandsMasters) {
                    JsonObject brandsObjects = new JsonObject();
                    Long brandId = null;
                    List<ProductUnitPacking> groupArray = new ArrayList<>();
                    if (mBrands.getBrand() != null) {
                        brandsObjects.addProperty("label", mBrands.getBrand().getBrandName());
                        brandsObjects.addProperty("value", mBrands.getBrand().getId());
                        brandsObjects.addProperty("isDisable", "false");
                        brandId = mBrands.getBrand().getId();
                    } else {
                        brandsObjects.addProperty("label", "");
                        brandsObjects.addProperty("value", "");
                        brandsObjects.addProperty("isDisable", "false");
                    }
                    groupArray = productUnitRepository.findByUniqueGroupListwithBrands(
                            Long.parseLong(obj[0].toString()), brandId, true);
                    JsonArray groupJsonArray = new JsonArray();
                    for (ProductUnitPacking mGroup : groupArray) {
                        JsonObject groupJsonObject = new JsonObject();
                        Long groupId = null;
                        List<ProductUnitPacking> categoryArray = new ArrayList<>();
                        if (mGroup.getGroup() == null) {
                            groupJsonObject.addProperty("label", "");
                            groupJsonObject.addProperty("value", "");
                            groupJsonObject.addProperty("isDisable", "false");
                        } else {
                            groupJsonObject.addProperty("label", mGroup.getGroup().getGroupName());
                            groupJsonObject.addProperty("value", mGroup.getGroup().getId());
                            groupJsonObject.addProperty("isDisable", "false");
                            groupId = mGroup.getGroup().getId();
                        }
                        categoryArray = productUnitRepository.findByUniqueCategoryListBrands(
                                Long.parseLong(obj[0].toString()), brandId, groupId, true);
                        JsonArray categoryJsonArray = new JsonArray();
                        for (ProductUnitPacking mCategory : categoryArray) {
                            JsonObject categoryJsonObject = new JsonObject();
                            Long categoryId = null;
                            List<ProductUnitPacking> subCategoryArray = new ArrayList<>();
                            if (mCategory.getCategory() == null) {
                                categoryJsonObject.addProperty("label", "");
                                categoryJsonObject.addProperty("value", "");
                                categoryJsonObject.addProperty("isDisable", "false");
                            } else {
                                categoryJsonObject.addProperty("label", mCategory.getCategory().getCategoryName());
                                categoryJsonObject.addProperty("value", mCategory.getCategory().getId());
                                categoryJsonObject.addProperty("isDisable", "false");
                                categoryId = mCategory.getCategory().getId();
                            }
                            subCategoryArray = productUnitRepository.
                                    findByUniqueSubcategoryListBrands(Long.parseLong(obj[0].toString()), brandId,
                                            groupId, categoryId, true);
                            JsonArray subCategoryJsonArray = new JsonArray();
                            for (ProductUnitPacking subCategory : subCategoryArray) {
                                JsonObject subCategoryJsonObject = new JsonObject();
                                Long subCateId = null;
                                List<ProductUnitPacking> packageArray = new ArrayList<>();
                                if (subCategory.getSubcategory() != null) {
                                    subCategoryJsonObject.addProperty("label", subCategory.getSubcategory().getSubcategoryName());
                                    subCategoryJsonObject.addProperty("value", subCategory.getSubcategory().getId());
                                    subCategoryJsonObject.addProperty("isDisable", "false");
                                    subCateId = subCategory.getSubcategory().getId();
                                } else {
                                    subCategoryJsonObject.addProperty("label", "");
                                    subCategoryJsonObject.addProperty("value", "");
                                    subCategoryJsonObject.addProperty("isDisable", "false");
                                }
                                JsonArray packageJsonArray = new JsonArray();
                                packageArray = productUnitRepository.
                                        findByUniquePackageListBrands(Long.parseLong(obj[0].toString()), brandId,
                                                groupId, categoryId, subCateId, true);
                                for (ProductUnitPacking packages : packageArray) {
                                    JsonObject packageJsonObject = new JsonObject();
                                    Long packageId = null;
                                    if (packages.getPackingMaster() != null) {
                                        packageId = packages.getPackingMaster().getId();
                                        packageJsonObject.addProperty("label", packages.getPackingMaster().getPackName());
                                        packageJsonObject.addProperty("value", packages.getPackingMaster().getId());
                                        packageJsonObject.addProperty("isDisable", "false");
                                    } else {
                                        packageJsonObject.addProperty("label", "");
                                        packageJsonObject.addProperty("value", "");
                                        packageJsonObject.addProperty("isDisable", "false");
                                    }
                                    JsonArray unitsJsonArray = new JsonArray();
                                    List<ProductUnitPacking> unitsArray = productUnitRepository.
                                            findByUniqueUnitsBrands(Long.parseLong(obj[0].toString()), brandId,
                                                    groupId, categoryId, subCateId, packageId, true);
                                    for (ProductUnitPacking units : unitsArray) {
                                        JsonObject unitsJsonObjects = new JsonObject();
                                        Long unitId = units.getUnits().getId();
                                        unitsJsonObjects.addProperty("label", units.getUnits().getUnitName());
                                        unitsJsonObjects.addProperty("value", units.getUnits().getId());
                                        unitsJsonObjects.addProperty("isDisable", "false");
                                        unitsJsonObjects.addProperty("isNegativeStocks", units.getIsNegativeStocks());
                                        unitsJsonObjects.addProperty("unit_id", units.getUnits().getId());
                                        unitsJsonObjects.addProperty("details_id", units.getId());
                                        unitsJsonObjects.addProperty("unit_name", units.getUnits().getUnitName());
                                        unitsJsonObjects.addProperty("unit_conv", units.getUnitConversion());
                                        unitsJsonObjects.addProperty("unit_marg", units.getUnitConvMargn());
                                        unitsJsonObjects.addProperty("mrp", units.getMrp());
                                        unitsJsonObjects.addProperty("purchase_rate", units.getPurchaseRate());
                                        unitsJsonObjects.addProperty("rateA", units.getMinRateA() != null ? units.getMinRateA() : 0);
                                        unitsJsonObjects.addProperty("rateB", units.getMinRateB() != null ? units.getMinRateB() : 0);
                                        unitsJsonObjects.addProperty("rateC", units.getMinRateC() != null ? units.getMinRateC() : 0);
                                        unitsJsonObjects.addProperty("minMargin", units.getMinMargin());
                                        unitsJsonObjects.addProperty("maxDiscount", units.getMaxDiscount());
                                        unitsJsonObjects.addProperty("minDiscount", units.getMinDiscount());
                                        unitsJsonObjects.addProperty("igst", units.getTaxMaster().getIgst());
                                        unitsJsonObjects.addProperty("cgst", units.getTaxMaster().getCgst());
                                        unitsJsonObjects.addProperty("sgst", units.getTaxMaster().getSgst());
                                        unitsJsonObjects.addProperty("hsnId", units.getProductHsn() != null ? units.getProductHsn().getId() : null);
                                        unitsJsonObjects.addProperty("taxMasterId", units.getTaxMaster() != null ? units.getTaxMaster().getId() : null);
                                        unitsJsonObjects.addProperty("applicableDate", units.getTaxApplicableDate() != null ? units.getTaxApplicableDate().toString() : "");
                                        unitsJsonObjects.addProperty("brandId", units.getBrand() != null ? units.getBrand().getId() : null);
                                        unitsJsonObjects.addProperty("groupId", units.getGroup() != null ? units.getGroup().getId() : null);
                                        unitsJsonObjects.addProperty("categoryId", units.getCategory() != null ? units.getCategory().getId() : null);
                                        unitsJsonObjects.addProperty("subcategoryId", units.getSubcategory() != null ? units.getSubcategory().getId() : null);
                                        unitsJsonObjects.addProperty("packageId", units.getPackingMaster() != null ?
                                                units.getPackingMaster().getId() : null);

                                        /***** Batch Number against Product *****/
                                        if (mProduct.getIsBatchNumber()) {
                                            JsonArray batchArray = new JsonArray();
                                            List<ProductBatchNo> batchNos = productBatchNoRepository.findByUniqueProductIdAndStatus(mProduct.getId(),
                                                    brandId, groupId, categoryId, subCateId, packageId, unitId, true);
                                            if (batchNos != null && batchNos.size() > 0) {
                                                for (ProductBatchNo mBatch : batchNos) {
                                                    JsonObject batchObject = new JsonObject();
                                                    batchObject.addProperty("batch_no", mBatch.getBatchNo());
                                                    batchObject.addProperty("qty", mBatch.getQnty());
                                                    batchObject.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                                                    batchObject.addProperty("purchase_rate", mBatch.getPurchaseRate());
                                                    batchObject.addProperty("min_rate_a", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0);
                                                    batchObject.addProperty("min_rate_b", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0);
                                                    batchObject.addProperty("min_rate_c", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0);
                                                    batchObject.addProperty("max_discount", mBatch.getMaxDiscount());
                                                    batchObject.addProperty("min_discount", mBatch.getMinDiscount());
                                                    batchObject.addProperty("manufacturing_date", mBatch.getManufacturingDate().toString());
                                                    batchObject.addProperty("mrp", mBatch.getMrp());
                                                    batchObject.addProperty("min_margin", mBatch.getMinMargin());
                                                    batchObject.addProperty("id", mBatch.getId());

                                                    if (purReturnDate.isAfter(mBatch.getExpiryDate())) {
                                                        batchObject.addProperty("is_expired", true);
                                                    } else {
                                                        batchObject.addProperty("is_expired", false);
                                                    }

                                                    batchArray.add(batchObject);
                                                }
                                            }
                                            unitsJsonObjects.add("batchOpt", batchArray);
                                        }
                                        unitsJsonArray.add(unitsJsonObjects);
                                    }
                                    packageJsonObject.add("unitOpt", unitsJsonArray);
                                    packageJsonArray.add(packageJsonObject);
                                }
                                subCategoryJsonObject.add("packageOpt", packageJsonArray);
                                subCategoryJsonArray.add(subCategoryJsonObject);
                            }
                            categoryJsonObject.add("subCategoryOpt", subCategoryJsonArray);
                            categoryJsonArray.add(categoryJsonObject);
                        }
                        groupJsonObject.add("categoryOpt", categoryJsonArray);
                        groupJsonArray.add(groupJsonObject);
                    }
                    brandsObjects.add("groupOpt", groupJsonArray);
                    brandsJsonArray.add(brandsObjects);
                }
            }
            response.addProperty("product_id", obj[0].toString());
            response.addProperty("value", obj[0].toString());
            response.add("brandsOpt", brandsJsonArray);
            response.addProperty("isBrand", false);
            response.addProperty("isGroup", false);
            response.addProperty("isCategory", false);
            response.addProperty("isSubcategory", false);
            response.addProperty("isPackage", false);
            productArray.add(response);
        }
        return productArray;
    }

    /**** PK Visit *****/
    public JsonArray getProductByBFPUCommonNew(Date invoiceDate, List<Object[]> productIds) {
        JsonArray productArray = new JsonArray();
        try {
            for (int i = 0; i < productIds.size(); i++) {
                JsonObject response = new JsonObject();
                Object obj[] = productIds.get(i);
                Product mProduct = productRepository.findByIdAndStatus(Long.parseLong(obj[0].toString()), true);
                JsonArray mLevelArray = new JsonArray();
                List<Long> levelaArray = productUnitRepository.findLevelAIdDistinct(mProduct.getId());
                for (Long mLeveA : levelaArray) {
                    JsonObject levelaJsonObject = new JsonObject();
                    LevelA levelA = null;
                    Long levelAId = null;
                    if (mLeveA != null) {
                        levelA = levelARepository.findByIdAndStatus(mLeveA, true);
                        if (levelA != null) {
                            levelAId = levelA.getId();
                            levelaJsonObject.addProperty("value", levelA.getId());
                            levelaJsonObject.addProperty("label", levelA.getLevelName());
                        }
                    } else {
                        levelaJsonObject.addProperty("value", "");
                        levelaJsonObject.addProperty("label", "");
                    }
                    JsonArray levelBArray = new JsonArray();
                    List<Long> levelBunits = productUnitRepository.findByProductsLevelB(mProduct.getId(), mLeveA);
                    for (Long mLeveB : levelBunits) {
                        JsonObject levelbJsonObject = new JsonObject();
                        Long levelBId = null;
                        LevelB levelB = null;
                        if (mLeveB != null) {
                            levelB = levelBRepository.findByIdAndStatus(mLeveB, true);
                            if (levelB != null) {
                                levelBId = levelB.getId();
                                levelbJsonObject.addProperty("value", levelB.getId());
                                levelbJsonObject.addProperty("label", levelB.getLevelName());
                            }
                        } else {
                            levelbJsonObject.addProperty("value", "");
                            levelbJsonObject.addProperty("label", "");
                        }
                        JsonArray levelCArray = new JsonArray();
                        List<Long> levelCunits = productUnitRepository.findByProductsLevelC(
                                mProduct.getId(), mLeveA, mLeveB);
                        for (Long mLeveC : levelCunits) {
                            JsonObject levelcJsonObject = new JsonObject();
                            LevelC levelC = null;
                            Long levelCId = null;
                            if (mLeveC != null) {
                                levelC = levelCRepository.findByIdAndStatus(mLeveC, true);
                                if (levelC != null) {
                                    levelCId = levelC.getId();
                                    levelcJsonObject.addProperty("value", levelC.getId());
                                    levelcJsonObject.addProperty("label", levelC.getLevelName());
                                }
                            } else {
                                levelcJsonObject.addProperty("value", "");
                                levelcJsonObject.addProperty("label", "");
                            }
                            List<Object[]> unitList = productUnitRepository.
                                    findUniqueUnitsByProductId(mProduct.getId(), mLeveA,
                                            mLeveB, mLeveC);
                            Long fiscalId = null;
                            LocalDate currentDate = LocalDate.now();
                            FiscalYear fiscalYear = generateFiscalYear.getFiscalYear(currentDate);
                            if (fiscalYear != null) fiscalId = fiscalYear.getId();
                            JsonArray unitArray = new JsonArray();
                            for (int j = 0; j < unitList.size(); j++) {
                                Object[] objects = unitList.get(j);
                                Long unitId = Long.parseLong(objects[0].toString());
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("value", Long.parseLong(objects[0].toString()));
                                jsonObject.addProperty("unitId", Long.parseLong(objects[0].toString()));
                                jsonObject.addProperty("label", objects[1].toString());
                                jsonObject.addProperty("unitName", objects[1].toString());
                                jsonObject.addProperty("unitCode", objects[2].toString());
                                jsonObject.addProperty("unitConversion", objects[3].toString());
                                jsonObject.addProperty("product_name", mProduct.getProductName());
                                /***** Batch Number against Product *****/
                                if (mProduct.getIsBatchNumber()) {
                                    JsonArray batchArray = new JsonArray();
                                    List<ProductBatchNo> batchNos = productBatchNoRepository.findByUniqueBatchProductIdAndStatus(mProduct.getId(),
                                            levelAId, levelBId, levelCId, unitId, true);
                                    if (batchNos != null && batchNos.size() > 0) {
                                        for (ProductBatchNo mBatch : batchNos) {
                                            Double closing = inventoryCommonPostings.getClosingStockProductFilters(
                                                    mBatch.getBranch() != null ? mBatch.getBranch().getId() : null,
                                                    mBatch.getOutlet().getId(), mProduct.getId(), levelAId, levelBId,
                                                    levelCId, unitId, mBatch.getId(), fiscalId);

                                            JsonObject batchObject = new JsonObject();
                                            batchObject.addProperty("batch_no", mBatch.getBatchNo() != null ? mBatch.getBatchNo() : "");
                                            batchObject.addProperty("b_no", mBatch.getBatchNo() != null ? mBatch.getBatchNo() : "");
                                            batchObject.addProperty("b_details_id", mBatch.getId());
                                            batchObject.addProperty("qty", mBatch.getQnty());
                                            batchObject.addProperty("expiry_date", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                                            batchObject.addProperty("b_expiry", mBatch.getExpiryDate() != null ? mBatch.getExpiryDate().toString() : "");
                                            batchObject.addProperty("pur_date", mBatch.getPurchaseDate() != null ? mBatch.getPurchaseDate().toString() : "");
                                            batchObject.addProperty("purchase_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.00);
                                            batchObject.addProperty("b_purchase_rate", mBatch.getPurchaseRate() != null ? mBatch.getPurchaseRate() : 0.00);
                                            batchObject.addProperty("min_rate_a", mBatch.getMinRateA() != null ? mBatch.getMinRateA() : 0.00);
                                            batchObject.addProperty("min_rate_b", mBatch.getMinRateB() != null ? mBatch.getMinRateB() : 0.00);
                                            batchObject.addProperty("min_rate_c", mBatch.getMinRateC() != null ? mBatch.getMinRateC() : 0.00);
                                            batchObject.addProperty("free_qty", mBatch.getFreeQty());
                                            batchObject.addProperty("closing_stock", closing!=null ?closing:0.00);
                                            batchObject.addProperty("costing_with_tax", mBatch.getCostingWithTax() != null ?
                                                    mBatch.getCostingWithTax() : 0.00);
                                            batchObject.addProperty("manufacturing_date", mBatch.getManufacturingDate() != null ? mBatch.getManufacturingDate().toString() : "");
                                            batchObject.addProperty("mrp", mBatch.getMrp() != null ? mBatch.getMrp() : 0.00);
                                            batchObject.addProperty("b_rate", mBatch.getMrp() != null ? mBatch.getMrp() : 0.00);
                                            batchObject.addProperty("min_margin", mBatch.getMinMargin() != null ? mBatch.getMinMargin() : 0.00);
                                            batchObject.addProperty("id", mBatch.getId());
                                            batchObject.addProperty("net_rate", mBatch.getCosting() != null ? mBatch.getCosting() : 0.00);
                                            batchObject.addProperty("costing", mBatch.getCosting() != null ? mBatch.getCosting() : 0.00);
                                            batchObject.addProperty("sale_rate",mBatch.getSalesRate() != null ? mBatch.getSalesRate() : 0.00);
                                            batchObject.addProperty("b_sale_rate", mBatch.getSalesRate() != null ? mBatch.getSalesRate() : 0.00);
                                            if(closing>0)
                                            {
                                                batchObject.addProperty("isBatchInTranx",true);
                                            }else{
                                                batchObject.addProperty("isBatchInTranx",false);
                                            }
                                            double salesRateWithTax = mBatch.getSalesRate();
                                            if (mBatch.getProduct().getTaxMaster() != null)
                                                salesRateWithTax = mBatch.getSalesRate() +
                                                        ((mBatch.getSalesRate() * mBatch.getProduct().getTaxMaster().getIgst()) / 100);
                                            batchObject.addProperty("sales_rate_with_tax", salesRateWithTax);
                                            if (mBatch.getExpiryDate() != null) {
                                                LocalDate invDate =  DateConvertUtil.convertDateToLocalDate(invoiceDate);
                                                if (invDate.isAfter(mBatch.getExpiryDate())) {
                                                    batchObject.addProperty("is_expired", true);
                                                } else {
                                                    batchObject.addProperty("is_expired", false);
                                                }
                                            } else {
                                                batchObject.addProperty("is_expired", false);
                                            }
                                            batchArray.add(batchObject);
                                        }
                                    }
                                    jsonObject.add("batchOpt", batchArray);
                                }

                                unitArray.add(jsonObject);
                            }
                            levelcJsonObject.add("unitOpts", unitArray);
                            levelCArray.add(levelcJsonObject);
                        }
                        levelbJsonObject.add("levelCOpts", levelCArray);
                        levelBArray.add(levelbJsonObject);
                    }
                    levelaJsonObject.add("levelBOpts", levelBArray);
                    mLevelArray.add(levelaJsonObject);
                }

                response.addProperty("product_id", obj[0].toString());
                response.addProperty("value", obj[0].toString());
                response.add("levelAOpt", mLevelArray);
                productArray.add(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception " + e.getMessage());
        }
        return productArray;
    }

}
