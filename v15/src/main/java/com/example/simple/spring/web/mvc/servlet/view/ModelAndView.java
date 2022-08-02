package com.example.simple.spring.web.mvc.servlet.view;

import com.example.simple.spring.web.mvc.bind.support.SessionStatus;
import com.example.simple.spring.web.mvc.bind.support.SimpleSessionStatus;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;

public class ModelAndView {

    public static final String MODEL_AND_VIEW = ModelAndView.class.getName();

    public static final String MODEL = ModelAndView.class.getName() + ".MODEL";

    public static final String SESSION_STATUS = ModelAndView.class.getName() + ".SESSION_STATUS";

    public static final String VIEW = ModelAndView.class.getName() + ".VIEW";

    public static final String VIEW_NAME = ModelAndView.class.getName() + ".VIEW_NAME";

    private HttpServletRequest request;

    private View view;

    private String viewName;

    private ModelMap model;

    private SessionStatus sessionStatus;

    private ModelAndView() {
    }

    public static ModelAndView get(HttpServletRequest request) {
        final Object mav = request.getAttribute(MODEL_AND_VIEW);
        if (mav != null) {
            return (ModelAndView) mav;
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.request = request;
        modelAndView.sessionStatus = getSessionStatus(request);
        modelAndView.model = getModel(request);

        request.setAttribute(MODEL_AND_VIEW, modelAndView);
        return modelAndView;
    }

    public static boolean hasView(HttpServletRequest request) {
        return request.getAttribute(VIEW) != null || request.getAttribute(VIEW_NAME) != null;
    }

    public static View getView(HttpServletRequest request) {
        final Object view = request.getAttribute(VIEW);
        if (view != null) {
            return (View) view;
        }

        return null;
    }

    public static void setView(HttpServletRequest request, View view) {
        request.setAttribute(VIEW, view);
    }

    public static String getViewName(HttpServletRequest request) {
        final Object view = request.getAttribute(VIEW_NAME);
        if (view != null) {
            return (String) view;
        }

        return null;
    }

    public static void setViewName(HttpServletRequest request, String viewName) {
        request.setAttribute(VIEW_NAME, viewName);
    }

    public static SessionStatus getSessionStatus(HttpServletRequest request) {
        final Object object = request.getAttribute(SESSION_STATUS);
        if (object != null) {
            return (SessionStatus) object;
        }

        SessionStatus sessionStatus = new SimpleSessionStatus();
        request.setAttribute(SESSION_STATUS, sessionStatus);
        return sessionStatus;
    }

    public static ModelMap getModel(HttpServletRequest request) {
        final Object model = request.getAttribute(MODEL);
        if (model != null) {
            return (ModelMap) model;
        }

        ModelMap modelMap = new ModelMap();
        request.setAttribute(MODEL, modelMap);
        return modelMap;
    }

    public static ModelMap addObject(HttpServletRequest request, String attributeName, Object attributeValue) {
        final ModelMap model = getModel(request);
        model.addAttribute(attributeName, attributeValue);
        return model;
    }

    public static ModelMap addObject(HttpServletRequest request, Object attributeValue) {
        final ModelMap model = getModel(request);
        model.addAttribute(attributeValue);
        return model;
    }

    public static void addAttribute(HttpServletRequest request, String name, Object value) {
        final ModelMap model = getModel(request);
        model.addAttribute(name, value);
    }

    public static boolean containsAttribute(HttpServletRequest request, String modelName) {
        final ModelMap model = getModel(request);
        return model.containsAttribute(modelName);
    }

    public static void clear(HttpServletRequest request) {
        setView(request, null);
        setViewName(request, null);
    }

    public View getView() {
        return view;
    }

    public ModelAndView setView(View view) {
        this.view = view;
        setView(request, view);
        return this;
    }

    public String getViewName() {
        return viewName;
    }

    public ModelAndView setViewName(String viewName) {
        this.viewName = viewName;
        setViewName(request, viewName);
        return this;
    }

    public ModelMap getModel() {
        return getModel(request);
    }

    public boolean hasView() {
        return view != null || viewName != null;
    }

    public ModelMap addObject(String attributeName, Object attributeValue) {
        getModel().addAttribute(attributeName, attributeValue);
        return model;
    }

    public ModelMap addObject(Object attributeValue) {
        getModel().addAttribute(attributeValue);
        return model;
    }

    public void addAttribute(String name, Object value) {
        getModel().addAttribute(name, value);
    }

    public boolean containsAttribute(String modelName) {
        return getModel().containsAttribute(modelName);
    }
}
