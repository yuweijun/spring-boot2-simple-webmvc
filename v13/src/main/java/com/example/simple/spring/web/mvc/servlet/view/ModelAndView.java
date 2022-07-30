package com.example.simple.spring.web.mvc.servlet.view;

import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class ModelAndView {

    public static final String MODEL_AND_VIEW = ModelAndView.class.getName();

    private Object view;

    private ModelMap model;

    private boolean cleared = false;

    public ModelAndView() {
    }

    public ModelAndView(String viewName) {
        this.view = viewName;
    }

    public ModelAndView(View view) {
        this.view = view;
    }

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.view = viewName;
        if (model != null) {
            getModelMap().addAllAttributes(model);
        }
    }

    public ModelAndView(View view, Map<String, ?> model) {
        this.view = view;
        if (model != null) {
            getModelMap().addAllAttributes(model);
        }
    }

    public ModelAndView(String viewName, String modelName, Object modelObject) {
        this.view = viewName;
        addObject(modelName, modelObject);
    }

    public ModelAndView(View view, String modelName, Object modelObject) {
        this.view = view;
        addObject(modelName, modelObject);
    }

    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }

    public void setViewName(String viewName) {
        this.view = viewName;
    }

    public View getView() {
        return (this.view instanceof View ? (View) this.view : null);
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean hasView() {
        return (this.view != null);
    }

    public boolean isReference() {
        return (this.view instanceof String);
    }

    public Map<String, Object> getModelInternal() {
        return this.model;
    }

    public ModelMap getModelMap() {
        if (this.model == null) {
            this.model = new ModelMap();
        }
        return this.model;
    }

    public Map<String, Object> getModel() {
        return getModelMap();
    }

    public ModelAndView addObject(String attributeName, Object attributeValue) {
        getModelMap().addAttribute(attributeName, attributeValue);
        return this;
    }

    public ModelAndView addObject(Object attributeValue) {
        getModelMap().addAttribute(attributeValue);
        return this;
    }

    public ModelAndView addAllObjects(Map<String, ?> modelMap) {
        getModelMap().addAllAttributes(modelMap);
        return this;
    }

    public void clear() {
        this.view = null;
        this.model = null;
        this.cleared = true;
    }

    public boolean isEmpty() {
        return (this.view == null && CollectionUtils.isEmpty(this.model));
    }

    public boolean wasCleared() {
        return (this.cleared && isEmpty());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ModelAndView: ");
        if (isReference()) {
            sb.append("reference to view with name '").append(this.view).append("'");
        } else {
            sb.append("materialized View is [").append(this.view).append(']');
        }
        sb.append("; model is ").append(this.model);
        return sb.toString();
    }

    public void put(HttpServletRequest request) {
        ModelAndView.put(request, this);
    }

    public static void put(HttpServletRequest request, ModelAndView modelAndView) {
        request.setAttribute(ModelAndView.class.getName(), modelAndView);
    }

    public static void clear(HttpServletRequest request) {
        put(request, null);
    }

    public static ModelAndView get(HttpServletRequest request) {
        return (ModelAndView) request.getAttribute(ModelAndView.class.getName());
    }

    public static void addAttribute(HttpServletRequest request, String name, Object value) {
        ModelAndView modelAndView = get(request);
        if (modelAndView == null) {
            modelAndView = new ModelAndView();
            put(request, modelAndView);
        }

        modelAndView.getModelMap().addAttribute(name, value);
    }

    public static boolean containsAttribute(HttpServletRequest request, String modelName) {
        ModelAndView modelAndView = get(request);
        if (modelAndView != null) {
            return modelAndView.getModelMap().containsAttribute(modelName);
        }
        return false;
    }
}
