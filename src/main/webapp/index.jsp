<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <title>入力フォーム</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
        <%-- 画面右上に名前を表示するコード --%>
<div style="position: absolute; top: 10px; right: 10px; padding: 5px; background-color: #f9f9f9; border: 1px solid #ccc; border-radius: 4px; z-index: 1000;">
    <% 
        // セッションから "username" を取得
        String loginName = (String) session.getAttribute("username");
        if (loginName != null && !loginName.isEmpty()) {
    %>
        <%-- 名前を表示 --%>
        <span style="font-weight: bold;">User: <%= loginName %></span>
    <% 
        } else {
    %>
        <%-- 未ログイン時の表示（必要であれば） --%>
        <span>Guest</span>
    <% 
        }
    %>
</div>
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