<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Catálogo - TechZone</title>
</head>
<body style="font-family: Arial; padding: 30px;">
    <h1>Catálogo de Productos - TechZone</h1>
    <p>Bienvenido, <b>${sessionScope.usuario}</b> (<c:out value="${sessionScope.rol}" default="cliente"/>)</p>
    <hr>
    
    <h3>Productos Disponibles:</h3>
    <ul>
        <%-- Ejemplo dinámico con JSTL para evitar duplicidad de código HTML --%>
        <c:forEach var="producto" items="${productos}">
            <li>${producto.nombre} - $${producto.precio}</li>
        </c:forEach>
    </ul>
    
    <br>
    <a href="${pageContext.request.contextPath}/logout">Cerrar Sesión</a>
</body>
</html>