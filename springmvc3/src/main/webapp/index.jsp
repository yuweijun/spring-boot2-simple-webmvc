<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
    <body>
        <h2>
          <a href="/index.jsp?name=test"><c:out value="${param.name}" default="index.jsp?name=test" escapeXml="false"/></a>
        </h2>
    </body>
</html>