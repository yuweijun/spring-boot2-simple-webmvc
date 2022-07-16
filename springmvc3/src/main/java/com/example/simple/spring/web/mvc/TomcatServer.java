package com.example.simple.spring.web.mvc;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.ProtocolHandler;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import java.io.File;

public class TomcatServer {

    private static final Logger LOGGER = Logger.getLogger(TomcatServer.class);

    public static void main(String[] args) throws ServletException, LifecycleException {

        Tomcat tomcat = new Tomcat();

        // springmvc3/build/tomcat.8080/work/Tomcat/localhost/ROOT/org/apache/jsp
        tomcat.setBaseDir("springmvc3/build/tomcat.8080");
        tomcat.setPort(8080);

        final String webappDirLocation = "springmvc3/src/main/webapp";
        String path = new File(webappDirLocation).getAbsolutePath();
        LOGGER.info("webapp path : " + path);

        StandardContext ctx = (StandardContext) tomcat.addWebapp("/", path);
        WebResourceRoot resources = new StandardRoot(ctx);
        ctx.setResources(resources);

        Connector connector = tomcat.getConnector();
        Object connectionTimeout = connector.getProperty("connectionTimeout");
        LOGGER.info("connectionTimeout : " + connectionTimeout);

        connector.setProperty("connectionTimeout", "3000");
        LOGGER.info("connectionTimeout : " + connector.getProperty("connectionTimeout"));

        ProtocolHandler protocolHandler = connector.getProtocolHandler();
        LOGGER.info("protocolHandler : " + protocolHandler);

        tomcat.start();
        tomcat.getServer().await();
    }

}
