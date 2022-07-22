package com.example.simple.spring.web.mvc.config;

import com.example.simple.spring.web.mvc.servlet.DispatcherServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import javax.servlet.Servlet;

@Configuration
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@Import({BeanPostProcessorsRegistrar.class})
public class ErrorMvcAutoConfiguration {

    @Bean
    public ServerProperties serverProperties() {
        final ServerProperties serverProperties = new ServerProperties();
        serverProperties.setPort(8080);
        return serverProperties;
    }

    @Bean
    public ErrorProperties errorProperties() {
        final ErrorProperties error = serverProperties().getError();
        error.setPath("/error");
        error.setIncludeException(true);
        error.setIncludeBindingErrors(ErrorProperties.IncludeAttribute.ALWAYS);
        error.setIncludeMessage(ErrorProperties.IncludeAttribute.ALWAYS);
        error.setIncludeStacktrace(ErrorProperties.IncludeAttribute.ALWAYS);
        return error;
    }

    @Bean
    public ErrorPageCustomizer errorPageCustomizer() {
        return new ErrorPageCustomizer(serverProperties());
    }

    static class ErrorPageCustomizer implements ErrorPageRegistrar, Ordered {

        private final ServerProperties properties;

        protected ErrorPageCustomizer(ServerProperties properties) {
            this.properties = properties;
        }

        @Override
        public void registerErrorPages(ErrorPageRegistry errorPageRegistry) {
            ErrorPage errorPage = new ErrorPage(this.properties.getError().getPath());
            errorPageRegistry.addErrorPages(errorPage);
        }

        @Override
        public int getOrder() {
            return 0;
        }

    }
}
