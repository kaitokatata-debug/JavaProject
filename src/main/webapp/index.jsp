<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <title>入力フォーム</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h1>Hello from JSP!</h1>
    <p>Current time: <%= new java.util.Date() %></p>

    <%-- エラーメッセージがあれば赤字で表示 --%>
    <% if (request.getAttribute("errorMessage") != null) { %>
        <p class="error"><%= request.getAttribute("errorMessage") %></p>
    <% } %>

    <form action="submit" method="post">
        <label>お名前: <input type="text" name="username"></label>
        <input type="submit" value="送信">
    </form>

    <br>
    <a href="list">登録済みリストを見る</a>
</body>
</html>