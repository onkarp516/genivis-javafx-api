package in.truethics.ethics.ethicsapiv10.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DataSourceRouting extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return BranchContextHolder.getBranchContext();
    }

    public void initDatasource() {
        Map<Object, Object> dataSourceMap = new HashMap<>();
//		dataSourceMap.put("gp", gpDataSource);
        try {
            String fileName = System.getProperty("dbfile_properties");
//            String fileName="/Users/shrikantande/Opethic/Genivis/GVFXRemote/genivis_pharma_api/dbs.json";
//            String fileName = System.getenv("dbfile_properties");
            if (fileName == null) {
                fileName = System.getProperty("user.dir");
                fileName = fileName + File.separator + "dbs.json";
                System.out.println("Dbs.json Path:"+fileName);
            }
               /* fileName="/home/ubuntu/dbs/dbs.json";
                fileName="E:\\GV\\genivis_pharma_api\\dbs.json";
                fileName="/home/ubuntu/genivis/dbs.json";*/


//			File file= ResourceUtils.getFile("classpath:dbs.json");
            File file = ResourceUtils.getFile(fileName);
            String jsonData = new String(Files.readAllBytes(file.toPath()));
            JsonArray jsonArray = new JsonParser().parse(jsonData).getAsJsonArray();
//			System.out.println("JSONARRAY LENGTH =: "+jsonArray.size());

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                DriverManagerDataSource dataSource = new DriverManagerDataSource();
                dataSource.setUrl(jsonObject.get("url").getAsString());
                dataSource.setUsername(jsonObject.get("username").getAsString());
                dataSource.setPassword(jsonObject.get("password").getAsString());
                dataSourceMap.put(jsonObject.get("code").getAsString().toLowerCase(), dataSource);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setTargetDataSources(dataSourceMap);
        this.setDefaultTargetDataSource(dataSourceMap.get("gvmh001"));
//        this.setDefaultTargetDataSource(dataSourceMap.get("gvmh003"));
    }
}
