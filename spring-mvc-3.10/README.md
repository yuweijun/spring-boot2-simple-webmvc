# spring-boot2-simple-webmvc

Spring web MVC 3.1.0.RELEASE test

# jstl error

    SEVERE: Servlet.service() for servlet [jsp] in context with path [] threw exception [The absolute uri: [http://java.sun.com/jsp/jstl/core] cannot be resolved in either web.xml or the jar files deployed with this application] with root cause
    org.apache.jasper.JasperException: The absolute uri: [http://java.sun.com/jsp/jstl/core] cannot be resolved in either web.xml or the jar files deployed with this application
    at org.apache.jasper.compiler.DefaultErrorHandler.jspError(DefaultErrorHandler.java:55)

## fix jstl issue

1. add dependencies
```groovy
    implementation group: 'javax.servlet', name: 'jstl', version: '1.2'
    implementation group: 'org.apache.taglibs', name: 'taglibs-standard-impl', version: '1.2.5'
```
2. download https://github.com/javaee/jstl-api/blob/master/impl/src/main/resources/META-INF/c.tld and add c.tld to WEB-INF
3. web.xml add config
```xml
   <jsp-config>
       <taglib>
       <taglib-uri>http://java.sun.com/jsp/jstl/core</taglib-uri>
       <taglib-location>/WEB-INF/c.tld</taglib-location>
       </taglib>
   </jsp-config>
``` 

# java.lang.ArrayIndexOutOfBoundsException: Index 17152 out of bounds for length 77 when server start using JDK8 or JDK11

    ArrayOutOfBoundsException on Bean creation while using Java 8 constructs
    https://stackoverflow.com/questions/30729125/arrayoutofboundsexception-on-bean-creation-while-using-java-8-constructs

## fix spring3 issue

    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6


