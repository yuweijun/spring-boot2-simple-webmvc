package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.ExceptionHandler;
import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RequestParam;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.controller.dto.UserDTO;
import com.example.simple.spring.web.mvc.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RequestParamController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParamController.class);

    @Autowired
    private List<HandlerInterceptor> interceptors;

    @RequestMapping("/requestParam")
    @ResponseBody
    public Map<String, String> requestParam(Map<String, String> map) {
        LOGGER.info("requestParam method invoked");
        for (HandlerInterceptor interceptor : interceptors) {
            map.put(interceptor.getClass().getSimpleName(), interceptor.getClass().getCanonicalName());
        }
        return map;
    }

    // http://localhost:8080/requestStringParam?id=1&username=test
    @RequestMapping("/requestStringParam")
    @ResponseBody
    public UserDTO requestStringParam(@RequestParam String username) {
        LOGGER.info("requestStringParam method invoked with username : {}", username);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername(username);
        return userDTO;
    }

    @RequestMapping("/requestParamNamed")
    @ResponseBody
    public UserDTO requestParamNamed(@RequestParam("username") String name) {
        LOGGER.info("requestParamNamed method invoked with username : {}", name);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setUsername(name);
        return userDTO;
    }

    // http://localhost:8080/requestIntegerParam?id=1&username=test
    @RequestMapping("/requestIntegerParam")
    @ResponseBody
    public UserDTO requestIntegerParam(@RequestParam int id) {
        LOGGER.info("DataBinder is ExtendedServletRequestDataBinder which cast string id to integer value");
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("id = " + id);
        userDTO.setId(id);
        return userDTO;
    }

    @ExceptionHandler({TypeMismatchException.class})
    public Map<String, String> handleTypeMismatchException(HttpServletRequest request, HttpServletResponse response, Object... providedArgs) {
        LOGGER.error("TypeMismatchException handler");
        Map<String, String> map = new HashMap<>();
        map.put("uri", request.getRequestURI());
        map.put("error", TypeMismatchException.class.getName());
        return map;
    }
}