<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<html>
<head>
    <title>ユーザー一覧</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h1>登録ユーザー一覧</h1>
    <ul>
    <%
        // サーブレットから渡された "users" というデータ（リスト）を受け取る
        List<String> users = (List<String>) request.getAttribute("users");
        
        // データが存在する場合のみループ処理を行う
        if (users != null) {
            for (String userName : users) {
    %>
                <li>
                    <%-- ユーザー名を表示 --%>
                    <%= userName %>
                    
                    <%-- 削除ボタンのフォーム --%>
                    <%-- style="display:inline;" でボタンを名前の横に並べる --%>
                    <form action="delete" method="post" style="display:inline;">
                        <%-- どのユーザーを削除するかをサーバーに伝えるための隠しデータ --%>
                        <input type="hidden" name="username" value="<%= userName %>">
                        <%-- 削除ボタン。押すと確認ダイアログが出る --%>
                        <input type="submit" value="削除" onclick="return confirm('本当に削除しますか？');">
                    </form>
                </li>
    <%
            }
        }
    %>
    </ul>
    <a href="index.jsp">トップへ戻る</a>
</body>
</html>