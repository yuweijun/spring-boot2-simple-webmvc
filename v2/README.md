# controller call stack

    home:11, HomeController (com.example.simple.spring.v1.controller)
    invoke0:-1, NativeMethodAccessorImpl (jdk.internal.reflect)
    invoke:62, NativeMethodAccessorImpl (jdk.internal.reflect)
    invoke:43, DelegatingMethodAccessorImpl (jdk.internal.reflect)
    invoke:566, Method (java.lang.reflect)
    doInvoke:205, InvocableHandlerMethod (org.springframework.web.method.support)
    invokeForRequest:150, InvocableHandlerMethod (org.springframework.web.method.support)
    invokeAndHandle:117, ServletInvocableHandlerMethod (org.springframework.web.servlet.mvc.method.annotation)
    invokeHandlerMethod:895, RequestMappingHandlerAdapter (org.springframework.web.servlet.mvc.method.annotation)
    handleInternal:808, RequestMappingHandlerAdapter (org.springframework.web.servlet.mvc.method.annotation)
    handle:87, AbstractHandlerMethodAdapter (org.springframework.web.servlet.mvc.method)
    doDispatch:1067, DispatcherServlet (org.springframework.web.servlet)
    doService:963, DispatcherServlet (org.springframework.web.servlet)
    processRequest:1006, FrameworkServlet (org.springframework.web.servlet)
    doGet:898, FrameworkServlet (org.springframework.web.servlet)
    service:655, HttpServlet (javax.servlet.http)
    service:883, FrameworkServlet (org.springframework.web.servlet)
    service:764, HttpServlet (javax.servlet.http)
    internalDoFilter:227, ApplicationFilterChain (org.apache.catalina.core)
    doFilter:162, ApplicationFilterChain (org.apache.catalina.core)
    doFilter:53, WsFilter (org.apache.tomcat.websocket.server)
    internalDoFilter:189, ApplicationFilterChain (org.apache.catalina.core)
    doFilter:162, ApplicationFilterChain (org.apache.catalina.core)
    doFilterInternal:100, RequestContextFilter (org.springframework.web.filter)
    doFilter:117, OncePerRequestFilter (org.springframework.web.filter)
    internalDoFilter:189, ApplicationFilterChain (org.apache.catalina.core)
    doFilter:162, ApplicationFilterChain (org.apache.catalina.core)
    doFilterInternal:93, FormContentFilter (org.springframework.web.filter)
    doFilter:117, OncePerRequestFilter (org.springframework.web.filter)
    internalDoFilter:189, ApplicationFilterChain (org.apache.catalina.core)
    doFilter:162, ApplicationFilterChain (org.apache.catalina.core)
    doFilterInternal:96, WebMvcMetricsFilter (org.springframework.boot.actuate.metrics.web.servlet)
    doFilter:117, OncePerRequestFilter (org.springframework.web.filter)
    internalDoFilter:189, ApplicationFilterChain (org.apache.catalina.core)
    doFilter:162, ApplicationFilterChain (org.apache.catalina.core)
    doFilterInternal:201, CharacterEncodingFilter (org.springframework.web.filter)
    doFilter:117, OncePerRequestFilter (org.springframework.web.filter)
    internalDoFilter:189, ApplicationFilterChain (org.apache.catalina.core)
    doFilter:162, ApplicationFilterChain (org.apache.catalina.core)
    invoke:197, StandardWrapperValve (org.apache.catalina.core)
    invoke:97, StandardContextValve (org.apache.catalina.core)
    invoke:541, AuthenticatorBase (org.apache.catalina.authenticator)
    invoke:135, StandardHostValve (org.apache.catalina.core)
    invoke:92, ErrorReportValve (org.apache.catalina.valves)
    invoke:78, StandardEngineValve (org.apache.catalina.core)
    service:360, CoyoteAdapter (org.apache.catalina.connector)
    service:399, Http11Processor (org.apache.coyote.http11)
    process:65, AbstractProcessorLight (org.apache.coyote)
    process:890, AbstractProtocol$ConnectionHandler (org.apache.coyote)
    doRun:1787, NioEndpoint$SocketProcessor (org.apache.tomcat.util.net)
    run:49, SocketProcessorBase (org.apache.tomcat.util.net)
    runWorker:1191, ThreadPoolExecutor (org.apache.tomcat.util.threads)
    run:659, ThreadPoolExecutor$Worker (org.apache.tomcat.util.threads)
    run:61, TaskThread$WrappingRunnable (org.apache.tomcat.util.threads)
    run:829, Thread (java.lang)