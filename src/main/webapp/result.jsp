<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    request.setCharacterEncoding("UTF-8");
    String name = request.getParameter("username");
%>
<html>
<body>
    <h1>結果画面</h1>
    <p>こんにちは、<%= name == null ? "ゲスト" : name %> さん！</p>
    <a href="index.jsp">戻る</a>
    <br>
    <a href="list">一覧を見る</a>
</body>
</html>