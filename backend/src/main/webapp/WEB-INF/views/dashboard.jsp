<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Dashboard - TechZone</title>
</head>
<body style="font-family: Arial; padding: 20px;">
    <h1>Bienvenido, ${sessionScope.usuario}</h1>
    <p>Tu rol actual es: <strong>${sessionScope.rol}</strong></p>

    <!-- Navegación condicionada por roles usando JSTL -->
    <c:choose>
        <c:when test="${sessionScope.rol == 'vendedor'}">
            <div style="background: #e9ecef; padding: 10px; margin-bottom: 10px;">
                <h3>Panel de Opciones de Vendedor</h3>
                <a href="${pageContext.request.contextPath}/vendedor/panel">Ir al Panel Protegido de Vendedor</a>
            </div>
        </c:when>
        <c:otherwise>
            <p>Acceso general de cliente habilitado.</p>
        </c:otherwise>
    </c:choose>

    <br>
    <a href="${pageContext.request.contextPath}/logout" style="color: red;">Cerrar Sesión</a>
</body>
</html>