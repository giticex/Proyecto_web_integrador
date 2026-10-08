<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Login - TechZone</title>
</head>
<body>
    <div style="margin: 50px auto; width: 300px; font-family: Arial;">
        <h2>Iniciar Sesión</h2>
        <form action="${pageContext.request.contextPath}/login" method="post">
            <div style="margin-bottom: 10px;">
                <label>Usuario:</label><br>
                <input type="text" name="username" required style="width: 100%; padding: 5px;" />
            </div>
            <div style="margin-bottom: 10px;">
                <label>Contraseña:</label><br>
                <input type="password" name="password" required style="width: 100%; padding: 5px;" />
            </div>
            <button type="submit" style="width: 100%; padding: 8px; background: #007bff; color: white; border: none;">Ingresar</button>
        </form>
    </div>
</body>
</html>