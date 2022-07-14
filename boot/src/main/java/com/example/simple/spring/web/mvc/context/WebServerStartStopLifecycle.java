package com.example.simple.spring.web.mvc.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.SmartLifecycle;

class WebServerStartStopLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerStartStopLifecycle.class);

    private final SimpleServletWebServerApplicationContext applicationContext;

    private final WebServer webServer;

    private volatile boolean running;

    WebServerStartStopLifecycle(SimpleServletWebServerApplicationContext applicationContext, WebServer webServer) {
        this.applicationContext = applicationContext;
        this.webServer = webServer;
    }

    @Override
    public void start() {
        this.webServer.start();
        this.running = true;
        LOGGER.info("this.applicationContext.publishEvent(new ServletWebServerInitializedEvent)");
    }

    @Override
    public void stop() {
        this.webServer.stop();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }

}
