package in.truethics.ethics.ethicsapiv10.controller.masters;
import in.truethics.ethics.ethicsapiv10.service.master_service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

    @RestController
    public class CityController {
        @Autowired
        private CityService cityService;

        //    fetch all cities as list from state id for frontend
        @PostMapping(path = "/getCitiesByState")
        public Object getCitiesByState(HttpServletRequest request) {
            return cityService.getCitiesByState(Long.parseLong(request.getParameter("stateId")));
        }
    }

