package com.example.spring.boot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HelloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @RequestMapping("/index")
    public String index(@ModelAttribute("map") Map<String, Integer> map, String time, Model model) {
        LOGGER.info("@ModelAttribute(map) is {}", map);
        LOGGER.info("index");
        LOGGER.info("time is : {}", time);
        model.addAttribute("hello", "yu");
        return "index";
    }

    @RequestMapping("/test")
    public String testNullView(Model model) {
        LOGGER.info("testNullView");
        model.addAttribute("hello", "test");
        return null;
    }

    @RequestMapping("/json")
    @ResponseBody
    public User user() {
        User user = new User();
        user.setId(1);
        user.setName("test");
        return user;
    }

    @ModelAttribute
    public Map<String, Integer> method1() {
        LOGGER.info("method1");
        Map<String, Integer> map = new HashMap<>();
        map.put("test", 1);
        return map;
    }

    public static class User {

        private int id;

        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
        }
    }
}
