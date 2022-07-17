package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestBody;
import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RequestMethod;
import com.example.simple.spring.web.mvc.bind.annotation.ResponseBody;
import com.example.simple.spring.web.mvc.controller.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
  
@Controller
public class RequestBodyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestBodyController.class);

    /**
     * curl -H 'Content-Type: application/json' -d '{"id":1,"username":"test"}' http://localhost:8080/requestBody
     */
    @RequestMapping(value = "/requestBody", method = RequestMethod.POST)
    @ResponseBody
    public UserDTO requestBody(@RequestBody UserDTO userDTO) {
        LOGGER.info("requestBody method invoked for userDTO : {}", userDTO);
        userDTO.setId(2);
        userDTO.setUsername("username2");
        return userDTO;
    }

}
