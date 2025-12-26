<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<html>
<body>
    <h1>登録ユーザー一覧</h1>
    <ul>
    <%
        List<String> users = (List<String>) request.getAttribute("users");
        if (users != null) {
            for (String userName : users) {
    %>
                <li><%= userName %></li>
    <%
            }
        }
    %>
    </ul>
    <a href="index.jsp">トップへ戻る</a>
</body>
</html>