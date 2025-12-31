<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String name = (String) session.getAttribute("username");
%>
<html>
<head>
    <title>結果画面</title>
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
    <h1>結果画面</h1>
    <p>こんにちは、<%= name == null ? "ゲスト" : name %> さん！</p>
    <br>
    <a href="list">一覧を見る</a>
    <br>
    <a href="game.jsp">ブロック崩しで遊ぶ</a>

    <br>
    <a href="logout">ログアウト</a>
</body>
</html>