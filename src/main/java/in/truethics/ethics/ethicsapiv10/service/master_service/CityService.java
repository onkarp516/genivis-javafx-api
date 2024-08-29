package in.truethics.ethics.ethicsapiv10.service.master_service;

import in.truethics.ethics.ethicsapiv10.model.master.City;
import in.truethics.ethics.ethicsapiv10.repository.master_repository.CityRepository;
import in.truethics.ethics.ethicsapiv10.response.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

        public Object getCitiesByState(Long stateId) {
            ResponseMessage responseMessage = new ResponseMessage();
            try {
                List<City> cityList = cityRepository.findAllByStateId(stateId);
                if (cityList.size() > 0) {
                    responseMessage.setResponseObject(cityList);
                    responseMessage.setResponseStatus(HttpStatus.OK.value());
                } else {
                    responseMessage.setMessage("No record found");
                    responseMessage.setResponseStatus(HttpStatus.NOT_FOUND.value());
                }
            } catch (Exception e) {
                e.printStackTrace();
                responseMessage.setMessage("Exception occurred");
                responseMessage.setResponseStatus(HttpStatus.BAD_REQUEST.value());
            }
            return responseMessage;
        }
    }


