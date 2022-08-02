package com.example.simple.spring.web.mvc.controller;

import com.example.simple.spring.web.mvc.bind.annotation.RequestMapping;
import com.example.simple.spring.web.mvc.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/rest")
public class AnnotationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationController.class);

    @RequestMapping("/annotation")
    public String annotation() {
        LOGGER.info("annotation method invoked");
        return "annotation controller";
    }

    @RequestMapping("/void")
    public void diov() {
        LOGGER.info("diov method invoked");
    }

}
