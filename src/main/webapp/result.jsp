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
    <h1>結果画面</h1>
    <p>こんにちは、<%= name == null ? "ゲスト" : name %> さん！</p>
    <a href="index.jsp">戻る</a>
    <br>
    <a href="list">一覧を見る</a>
    <br>
    <a href="game.jsp">ブロック崩しで遊ぶ</a>
    <br>
    <a href="logout">ログアウト</a>
</body>
</html>