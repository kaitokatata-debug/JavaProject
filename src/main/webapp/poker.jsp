<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="poker.*" %>
<%@ page import="playingcards.Card" %>
<%@ page import="java.util.List" %>
<%
    TexasHoldemGame game = (TexasHoldemGame) session.getAttribute("pokerGame");
    if (game == null) {
        response.sendRedirect("poker");
        return;
    }
    Player human = game.getPlayers().get(0);
    Player cpu = game.getPlayers().get(1);
    Boolean isCpuTurn = (Boolean) session.getAttribute("isCpuTurn");
    if (isCpuTurn == null) isCpuTurn = false;
%>
<html>
<head>
    <title>Texas Hold'em Poker</title>
    <link rel="stylesheet" href="css/style.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
    <div class="game-container">
        <h1>Texas Hold'em</h1>
        <a href="result.jsp" style="float:right; color: #3498db;">戻る</a>

        <% if (request.getAttribute("error") != null) { %>
            <div style="color: #e74c3c; background-color: #fadbd8; padding: 10px; border: 1px solid #e74c3c; border-radius: 4px; margin-bottom: 15px;">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>
        
        <%-- CPU思考中の表示と自動遷移スクリプト --%>
        <% if (isCpuTurn) { %>
            <div style="position: absolute; top: 40%; left: 50%; transform: translate(-50%, -50%); background: rgba(0,0,0,0.8); color: white; padding: 15px 30px; border-radius: 50px; z-index: 200; font-weight: bold; font-size: 1.2em; border: 2px solid #fff;">
                Thinking...
            </div>
            <form id="cpuTurnForm" action="poker" method="post">
                <input type="hidden" name="action" value="cpu_turn">
            </form>
            <script>
                setTimeout(function() {
                    document.getElementById("cpuTurnForm").submit();
                }, 1500); // 1.5秒後にCPUのアクションを実行
            </script>
        <% } %>

        <div class="poker-table">
            <!-- CPU Area -->
            <div style="position: relative;">
                <% String cpuAction = cpu.consumeLastAction();
                   if (cpuAction != null) { %>
                    <div class="action-bubble bubble-cpu"><%= cpuAction %></div>
                <% } %>
                <div class="player-label">CPU</div>
                <div>
                    <% if (game.getState() == State.SHOWDOWN) { 
                        for(Card c : cpu.getHoleCards()) { 
                            String colorClass = (c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS) ? "red" : "";
                            // 勝者かつ、そのカードが役に使われている場合に強調
                            String winnerClass = (cpu.isWinner() && cpu.getBestHand() != null && cpu.getBestHand().contains(c)) ? "winner" : "";
                    %>
                        <span class="card <%= colorClass %> <%= winnerClass %>"><%= c %></span>
                    <%  } 
                       } else { %>
                        <span class="card" style="background:#ccc; color:#ccc;">?</span>
                        <span class="card" style="background:#ccc; color:#ccc;">?</span>
                    <% } %>
                </div>
                <div class="player-label" style="margin-top: 5px;">Chips: <%= cpu.getChips() %></div>
            </div>

            <!-- Community Cards & Pot -->
            <div class="pot-display">POT: <%= game.getPot() %></div>
            <div class="community-cards">
                <% for(Card c : game.getCommunityCards()) { 
                    String colorClass = (c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS) ? "red" : "";
                    String winnerClass = "";
                    if (game.getState() == State.SHOWDOWN) {
                        if (cpu.isWinner() && cpu.getBestHand() != null && cpu.getBestHand().contains(c)) winnerClass = "winner";
                        if (human.isWinner() && human.getBestHand() != null && human.getBestHand().contains(c)) winnerClass = "winner";
                    }
                %>
                    <span class="card <%= colorClass %> <%= winnerClass %>"><%= c %></span>
                <% } %>
                <% if(game.getCommunityCards().isEmpty()) { %>
                    <span style="color:#aaa;">Waiting for Flop...</span>
                <% } %>
            </div>

            <!-- Player Area -->
            <div style="position: relative;">
                <% String humanAction = human.consumeLastAction();
                   if (humanAction != null) { %>
                    <div class="action-bubble bubble-player"><%= humanAction %></div>
                <% } %>
                <div class="player-label"><%= human.getName() %></div>
                <div>
                    <% for(Card c : human.getHoleCards()) { 
                        String colorClass = (c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS) ? "red" : "";
                        String winnerClass = (human.isWinner() && human.getBestHand() != null && human.getBestHand().contains(c)) ? "winner" : "";
                    %>
                        <span class="card <%= colorClass %> <%= winnerClass %>"><%= c %></span>
                    <% } %>
                </div>
                <div class="player-label" style="margin-top: 5px;">Chips: <%= human.getChips() %></div>
            </div>

            <!-- Controls (Tableの外に出すか、中に入れるか。ここではテーブルの下部に配置し右寄せにする) -->
            <div class="controls" style="background: rgba(255,255,255,0.1); padding: 15px; border-radius: 10px; margin-top: 30px; position: relative; <%= isCpuTurn ? "pointer-events: none; opacity: 0.6;" : "" %>">
                <form action="poker" method="post">
                    <% if (game.getState() == State.SHOWDOWN) { %>
                        <button type="submit" name="action" value="next" style="padding:10px 20px; background:#2ecc71; color:white; border:none; border-radius:5px; cursor:pointer;">Next Round</button>
                    <% } else { %>
                        <!-- メインアクションボタン -->
                        <div id="main-actions">
                            <button type="submit" name="action" value="fold" style="padding:10px 20px; background:#e74c3c; color:white; border:none; border-radius:5px; cursor:pointer; margin: 0 5px;">Fold</button>
                            <button type="submit" name="action" value="call" style="padding:10px 20px; cursor:pointer; margin: 0 5px;">Check / Call</button>
                            <button type="button" onclick="toggleBetMenu()" style="padding:10px 20px; background:#f1c40f; border:none; border-radius:5px; cursor:pointer; margin: 0 5px;">Bet / Raise ...</button>
                        </div>

                        <%
                            int currentHighest = game.getCurrentHighestBet();
                            int myBet = human.getCurrentBet();
                            int callAmt = currentHighest - myBet;
                            // ベットがない場合は10、ある場合はコール額+1（最小レイズ）
                            int minAmount = (currentHighest == 0) ? 10 : (callAmt + 1);
                            int maxAmount = human.getChips();

                            // --- ポットベット計算ロジック ---
                            int totalPot = game.getPot();
                            for(Player p : game.getPlayers()) {
                                totalPot += p.getCurrentBet();
                            }

                            int halfPotAmount;
                            int potAmount;

                            if (currentHighest == 0) {
                                halfPotAmount = Math.max(minAmount, totalPot / 2);
                                potAmount = Math.max(minAmount, totalPot);
                            } else {
                                int potAfterCall = totalPot + callAmt;
                                halfPotAmount = callAmt + (potAfterCall / 2);
                                potAmount = callAmt + potAfterCall;
                            }

                            halfPotAmount = Math.min(Math.max(halfPotAmount, minAmount), maxAmount);
                            potAmount = Math.min(Math.max(potAmount, minAmount), maxAmount);
                        %>

                        <!-- ベットメニュー（ポップアップ風に表示） -->
                        <div id="bet-menu" style="display: none; position: absolute; bottom: 100%; right: 0; background: rgba(0,0,0,0.85); padding: 15px; border-radius: 8px; text-align: center; z-index: 100; min-width: 200px; border: 1px solid #555;">
                            <div style="display: flex; flex-direction: column; gap: 8px;">
                                <div style="color: white; font-weight: bold; margin-bottom: 5px;">Bet Amount</div>
                                <input type="number" name="amount" id="bet-input" value="<%= minAmount %>" min="<%= minAmount %>" max="<%= maxAmount %>" oninput="syncSlider(this.value)" style="padding:8px; width: 100%; box-sizing: border-box; text-align: center;">
                                <input type="range" id="bet-slider" min="<%= minAmount %>" max="<%= maxAmount %>" value="<%= minAmount %>" oninput="syncInput(this.value)" style="width: 100%; cursor: pointer;">
                                
                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 5px;">
                                    <button type="button" onclick="setBet(<%= minAmount %>)" style="cursor:pointer; padding: 5px;">Min</button>
                                    <button type="button" onclick="setBet(<%= halfPotAmount %>)" style="cursor:pointer; padding: 5px;">1/2 Pot</button>
                                    <button type="button" onclick="setBet(<%= potAmount %>)" style="cursor:pointer; padding: 5px;">Pot</button>
                                    <button type="button" onclick="setBet(<%= maxAmount %>)" style="cursor:pointer; padding: 5px; background:#8e44ad; color:white; border:none; border-radius:5px;">All In</button>
                                </div>
                                
                                <button type="submit" name="action" value="bet" style="padding:10px; background:#f1c40f; border:none; border-radius:5px; cursor:pointer; font-weight: bold; margin-top: 5px;">Confirm</button>
                                <button type="button" onclick="toggleBetMenu()" style="padding:5px; background:#7f8c8d; color:white; border:none; border-radius:5px; cursor:pointer;">Cancel</button>
                            </div>
                        </div>
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

    <%-- オールイン時の自動進行用スクリプト --%>
    <% 
       boolean isAllIn = (human.getChips() == 0 || cpu.getChips() == 0);
       boolean isGameActive = game.getState() != State.SHOWDOWN;
       // CPUターン中はオールイン進行処理を待機させる
       if (isAllIn && isGameActive && !isCpuTurn) {
    %>
    <form id="autoAdvanceForm" action="poker" method="post">
        <input type="hidden" name="action" value="auto_advance">
    </form>
    <% } %>

    <script>
        function toggleBetMenu() {
            var menu = document.getElementById("bet-menu");
            if (menu.style.display === "none") {
                menu.style.display = "block";
            } else {
                menu.style.display = "none";
            }
        }
        function setBet(amount) {
            document.getElementById('bet-input').value = amount;
            document.getElementById('bet-slider').value = amount;
        }
        function syncInput(val) {
            document.getElementById('bet-input').value = val;
        }
        function syncSlider(val) {
            document.getElementById('bet-slider').value = val;
        }

        <% if (isAllIn && isGameActive && !isCpuTurn) { %>
        setTimeout(function() {
            document.getElementById("autoAdvanceForm").submit();
        }, 1000); // 0.5秒後に次のステップへ
        <% } %>
    </script>
</body>
</html>
