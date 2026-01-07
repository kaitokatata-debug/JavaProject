<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="poker.*" %>
<%@ page import="java.util.List" %>
<%
    TexasHoldemGame game = (TexasHoldemGame) session.getAttribute("pokerGame");
    String stageName = String.valueOf(session.getAttribute("pokerStage"));
    if (game == null) {
        response.sendRedirect("poker");
        return;
    }
    Player human = game.getPlayers().get(0);
    Player cpu = game.getPlayers().get(1);
%>
<html>
<head>
    <title>Texas Hold'em Poker</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .poker-table { background-color: #2c3e50; color: white; padding: 20px; border-radius: 10px; text-align: center; }
        .card { display: inline-block; background: white; color: black; padding: 10px; margin: 5px; border-radius: 5px; font-size: 1.5em; width: 40px; height: 60px; line-height: 60px; border: 1px solid #999; }
        .card.red { color: red; }
        .community-cards { margin: 20px 0; background-color: #34495e; padding: 10px; border-radius: 5px; min-height: 80px; }
        .controls { margin-top: 20px; }
        .log-area { background: #f4f4f9; color: #333; padding: 10px; height: 150px; overflow-y: scroll; text-align: left; border: 1px solid #ccc; margin-top: 20px; font-family: monospace; }
        .pot-display { font-size: 1.2em; color: #f1c40f; font-weight: bold; margin: 10px; }
    </style>
</head>
<body>
    <div class="game-container">
        <h1>Texas Hold'em</h1>
        <a href="result.jsp" style="float:right; color: #3498db;">戻る</a>
        
        <div class="poker-table">
            <!-- CPU Area -->
            <div>
                <h3>CPU (Chips: <%= cpu.getChips() %>)</h3>
                <div>
                    <% if ("SHOWDOWN".equals(stageName)) { 
                        for(Card c : cpu.getHoleCards()) { 
                            String colorClass = (c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS) ? "red" : "";
                    %>
                        <span class="card <%= colorClass %>"><%= c %></span>
                    <%  } 
                       } else { %>
                        <span class="card" style="background:#ccc; color:#ccc;">?</span>
                        <span class="card" style="background:#ccc; color:#ccc;">?</span>
                    <% } %>
                </div>
            </div>

            <!-- Community Cards & Pot -->
            <div class="pot-display">POT: <%= game.getPot() %></div>
            <div class="community-cards">
                <% for(Card c : game.getCommunityCards()) { 
                    String colorClass = (c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS) ? "red" : "";
                %>
                    <span class="card <%= colorClass %>"><%= c %></span>
                <% } %>
                <% if(game.getCommunityCards().isEmpty()) { %>
                    <span style="color:#aaa;">Waiting for Flop...</span>
                <% } %>
            </div>

            <!-- Player Area -->
            <div>
                <h3><%= human.getName() %> (Chips: <%= human.getChips() %>)</h3>
                <div>
                    <% for(Card c : human.getHoleCards()) { 
                        String colorClass = (c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS) ? "red" : "";
                    %>
                        <span class="card <%= colorClass %>"><%= c %></span>
                    <% } %>
                </div>
            </div>

            <!-- Controls -->
            <div class="controls">
                <form action="poker" method="post">
                    <% if ("SHOWDOWN".equals(stageName)) { %>
                        <button type="submit" name="action" value="next" style="padding:10px 20px; background:#2ecc71; color:white; border:none; border-radius:5px; cursor:pointer;">Next Round</button>
                    <% } else { %>
                        <button type="submit" name="action" value="call" style="padding:10px 20px; cursor:pointer;">Check / Call</button>
                        <button type="submit" name="action" value="fold" style="padding:10px 20px; background:#e74c3c; color:white; border:none; border-radius:5px; cursor:pointer;">Fold</button>
                        
                        <span style="margin-left: 20px;">
                            <input type="number" name="amount" value="100" min="10" max="<%= human.getChips() %>" style="width:80px; padding:5px;">
                            <button type="submit" name="action" value="bet" style="padding:10px 20px; background:#f1c40f; border:none; border-radius:5px; cursor:pointer;">Bet / Raise</button>
                        </span>
                    <% } %>
                    <div style="margin-top:10px;">
                        <button type="submit" name="action" value="reset" style="font-size:0.8em; background:none; border:none; color:#aaa; cursor:pointer; text-decoration:underline;">Reset Game</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Game Log -->
        <div class="log-area">
            <% for(String log : game.getLogs()) { %>
                <div><%= log %></div>
            <% } %>
        </div>
    </div>
</body>
</html>
