package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.ExceptionHandler;
import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RequestParam;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseStatus;
import com.example.simple.spring.web.mvc.controller.dto.UserDTO;
import com.example.simple.spring.web.mvc.http.HttpStatus;
import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RequestParamController {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private List<HandlerInterceptor> interceptors;

    @RequestMapping("/requestParam")
    @ResponseBody
    public Map<String, String> requestParam(Map<String, String> map) {
        logger.info("requestParam method invoked");
        for (HandlerInterceptor interceptor : interceptors) {
            map.put(interceptor.getClass().getSimpleName(), interceptor.getClass().getCanonicalName());
        }
        return map;
    }

    // http://localhost:8080/requestStringParam?id=1&username=test
    @RequestMapping("/requestStringParam")
    @ResponseBody
    public UserDTO requestStringParam(@RequestParam String username) {
        logger.info("requestStringParam method invoked with username : " + username);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername(username);
        return userDTO;
    }

    @RequestMapping("/requestParamNamed")
    @ResponseBody
    public UserDTO requestParamNamed(@RequestParam("username") String name) {
        logger.info("requestParamNamed method invoked with username : " + name);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername(name);
        return userDTO;
    }

    // http://localhost:8080/requestIntegerParam?id=1&username=test
    // http://localhost:8080/requestIntegerParam?id=id&username=test will throw exception and caught by method #handleTypeMismatchException
    @RequestMapping("/requestIntegerParam")
    @ResponseBody
    public UserDTO requestIntegerParam(@RequestParam int id) {
        logger.info("DataBinder is ExtendedServletRequestDataBinder which cast string id to integer value");
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("id = " + id);
        userDTO.setId(id);
        return userDTO;
    }

    @ExceptionHandler({TypeMismatchException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleTypeMismatchException(HttpServletRequest request, HttpServletResponse response, Object... providedArgs) throws IOException {
        logger.error("TypeMismatchException handler");
        Map<String, String> map = new HashMap<>();
        map.put("uri", request.getRequestURI());
        map.put("error", TypeMismatchException.class.getName());
        return map;
    }
}
