<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%
    // ListServletから渡されたリストを取得
    // 要素は String[] { name, score, crown }
    List<String[]> users = (List<String[]>) request.getAttribute("users");
%>
<html>
<head>
    <title>ユーザー一覧</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        table {
            width: 80%;
            margin: 20px auto;
            border-collapse: collapse;
        }
        th, td {
            padding: 10px;
            border: 1px solid #ccc;
            text-align: center;
        }
        th {
            background-color: #0095DD;
            color: white;
        }
        .delete-btn {
            background-color: #ff4444;
            color: white;
            border: none;
            padding: 5px 10px;
            cursor: pointer;
            border-radius: 4px;
        }
    </style>
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
    <div class="game-container" style="text-align: center;">
        <h1>ユーザー一覧</h1>
        
        <table>
            <tr>
                <th>名前</th>
                <th>スコア</th>
                <th>操作</th>
            </tr>
            <% if (users != null) {
                for (String[] user : users) { %>
            <tr>
                <!-- 王冠(user[2])と名前(user[0])を表示 -->
                <td style="text-align: left; padding-left: 20px;"><%= user[2] %> <%= user[0] %></td>
                <td><%= user[1] %></td>
                <td>
                    <form action="delete" method="post" style="margin:0;">
                        <input type="hidden" name="username" value="<%= user[0] %>">
                        <input type="submit" value="削除" class="delete-btn" onclick="return confirm('本当に削除しますか？');">
                    </form>
                </td>
            </tr>
            <%  }
               } %>
        </table>

        <a href="result.jsp">トップに戻る</a>
    </div>
</body>
</html>