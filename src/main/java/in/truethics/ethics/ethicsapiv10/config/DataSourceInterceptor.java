package in.truethics.ethics.ethicsapiv10.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataSourceInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

//        if(request.getParameter(""))
        String branch = request.getHeader("branch");

        BranchContextHolder.clearBranchContext();
		if(branch != null && !branch.isEmpty()) {
            System.out.println("DB changing to >>>>>"+branch);
            BranchContextHolder.setBranchContext(branch.toLowerCase());
        }
		else BranchContextHolder.setBranchContext("gvmh001");

        return super.preHandle(request, response, handler);
    }


}
