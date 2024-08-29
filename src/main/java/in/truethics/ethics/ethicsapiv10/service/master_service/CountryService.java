package in.truethics.ethics.ethicsapiv10.service.master_service;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.truethics.ethics.ethicsapiv10.model.master.Country;
import in.truethics.ethics.ethicsapiv10.model.user.Users;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CountryRepository;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class CountryService {
    @Autowired
    private CountryRepository repository;
    @Autowired
    RestTemplate restTemplate;
    @Value("${spring.serversource.url}")
    private String serverUrl;

    public JsonObject getCountry(HttpServletRequest request) {
        List<Country> list  = repository.findAll();
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for(Country mcountry:list){
            JsonObject res = new JsonObject();
            res.addProperty("id",mcountry.getId());
            res.addProperty("countryName",mcountry.getName());
            jsonArray.add(res);
        }
        jsonObject.addProperty("message","success");
        jsonObject.addProperty("responseStatu", HttpStatus.OK.value());
        jsonObject.add("responseObject",jsonArray);

        /*HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", request.getHeader("Authorization"));

        LinkedMultiValueMap body = new LinkedMultiValueMap();
        body.add("name", request.getParameter("name"));
        body.add("phonecode", request.getParameter("phonecode"));
        body.add("currency", request.getParameter("currency"));
        body.add("currency_symbol", request.getParameter("currency_symbol"));
        HttpEntity entity = new HttpEntity<>(body,headers);

        String response = restTemplate.exchange(
                serverUrl + "/saveCountry", HttpMethod.POST, entity, String.class).getBody();
        System.out.println("API Response => " + response);*/

        /*HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("branch", "gvmh001");
        headers.add("Authorization", request.getHeader("Authorization"));

        LinkedMultiValueMap body = new LinkedMultiValueMap();
        body.add("usercode", "gvmh001");
        HttpEntity entity = new HttpEntity<>(body,headers);

        String response = restTemplate.exchange(
                serverUrl + "/getUserToken", HttpMethod.POST, entity, String.class).getBody();
        System.out.println("API Response => " + response);*/


        /*HttpHeaders gvHdr = new HttpHeaders();
        gvHdr.setContentType(MediaType.MULTIPART_FORM_DATA);
        gvHdr.add("branch", "gvmh001");

        LinkedMultiValueMap gvBody = new LinkedMultiValueMap();
        gvBody.add("usercode", "gvmh001");
        HttpEntity gvEntity = new HttpEntity<>(gvBody,gvHdr);

        Object gvData = restTemplate.exchange(
                serverUrl + "/getUserToken", HttpMethod.POST, gvEntity, String.class).getBody();
        System.out.println("getUserToken API Response => " + gvData.toString());
        JsonObject jsonObject1 = new JsonParser().parse(gvData.toString()).getAsJsonObject();
        System.out.println("jsonObject1 data "+jsonObject1.get("response").getAsString());
        JsonObject cadmin = jsonObject1.get("responseObject").getAsJsonObject();
        System.out.println("cadmin "+cadmin.toString());

        Long outletId = 0l;
        Long branchId = null;

        if(!cadmin.get("outlet").isJsonNull())
            outletId = cadmin.get("outlet").getAsJsonObject().get("id").getAsLong();
        System.out.println("outletId "+outletId);

        if(!cadmin.get("branch").isJsonNull())
            branchId = cadmin.get("branch").getAsJsonObject().get("id").getAsLong();
        System.out.println("branchId "+branchId);*/

        return jsonObject;
    }

    public Object getIndiaCountry(HttpServletRequest request) {
        Country country = repository.findByName("India");
        return country;

    }

    public Object saveCountry(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        Country country = new Country();
        country.setName(request.getParameter("name"));
        country.setPhonecode(request.getParameter("phonecode"));
        country.setCurrency(request.getParameter("currency"));
        country.setCurrencySymbol(request.getParameter("currency_symbol"));

        repository.save(country);

        response.addProperty("message", "Country Saved");
        response.addProperty("responseStatus",HttpStatus.OK.value());
        return response.toString();
    }
}
