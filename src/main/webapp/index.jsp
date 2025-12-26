<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<body>
    <h1>Hello from JSP!</h1>
    <p>Current time: <%= new java.util.Date() %></p>

    <%-- エラーメッセージがあれば赤字で表示 --%>
    <% if (request.getAttribute("errorMessage") != null) { %>
        <p style="color: red;"><%= request.getAttribute("errorMessage") %></p>
    <% } %>

    <form action="submit" method="post">
        <label>お名前: <input type="text" name="username"></label>
        <input type="submit" value="送信">
    </form>

    <br>
    <a href="list">登録済みリストを見る</a>
</body>
</html>