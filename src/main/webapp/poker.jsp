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
    Integer actionCount = (Integer) session.getAttribute("actionCount");
    if (actionCount == null) actionCount = 0;
%>
<html>
<head>
    <title>Texas Hold'em Poker</title>
    <style>
        body {
            font-family: "Helvetica Neue", Arial, "Hiragino Kaku Gothic ProN", "Hiragino Sans", Meiryo, sans-serif;
            background-color: #ffffff;
            color: #333;
            margin: 0;
            padding: 20px;
        }

        h1 {
            color: #ecf0f1;
            text-align: center;
            padding-bottom: 10px;
        }

        ul {
            list-style-type: none;
            padding: 0;
        }

        li {
            background: #f9f9f9;
            margin: 8px 0;
            padding: 10px;
            border-radius: 4px;
            border: 1px solid #eee;
            color: #333;
        }

        /* Legacy form styles */

        input[type="text"] {
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            width: 200px;
        }

        input[type="submit"] {
            background-color: #3498db;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 4px;
            cursor: pointer;
            font-weight: bold;
        }

        input[type="submit"]:hover {
            background-color: #2980b9;
        }

        /* 削除ボタン用のスタイル（赤色） */
        form[action="delete"] input[type="submit"] {
            background-color: #e74c3c;
            padding: 5px 10px;
            font-size: 0.9em;
        }
        form[action="delete"] input[type="submit"]:hover {
            background-color: #c0392b;
        }

        a {
            color: #3498db;
            text-decoration: none;
            display: inline-block;
            margin-top: 10px;
        }

        a:hover {
            text-decoration: underline;
        }

        /* Game Container */
        .game-container {
            max-width: 900px;
            min-height: 600px; /* ログエリア確保のため高さをある程度確保 */
            margin: 0 auto;
            background-color: #34495e;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0,0,0,0.5);
            position: relative;
            color: white;
        }

        .back-link {
            float: right;
            font-weight: bold;
        }

        .error-message {
            color: #e74c3c;
            background-color: #fadbd8;
            padding: 10px;
            border: 1px solid #e74c3c;
            border-radius: 4px;
            margin-bottom: 15px;
        }

        /* アクションカウンター */
        .action-counter {
            position: absolute;
            top: 20px;
            left: 20px;
            color: #ecf0f1;
            font-weight: bold;
            font-size: 1.2em;
            z-index: 10;
        }

        /* ポーカーテーブルのデザイン */
        .poker-table {
            background-color: #27ae60;
            border: 15px solid #1e8449;
            border-radius: 100px; /* 楕円形っぽく */
            padding: 40px;
            margin: 20px 20px 20px 260px; /* 左側にログエリア分のスペースを空ける */
            position: relative;
            text-align: center;
        }

        /* Player Area */
        .player-area {
            position: relative;
            margin: 15px 0;
        }

        .player-label {
            font-weight: bold;
            background: rgba(0,0,0,0.5);
            padding: 5px 15px;
            border-radius: 15px;
            display: inline-block;
            margin: 0 5px;
        }

        .player-info-top {
            margin-bottom: 10px;
        }

        .player-info-bottom {
            margin-top: 10px;
        }

        .dealer-button {
            position: absolute;
            top: -10px;
            right: -10px;
            width: 24px;
            height: 24px;
            background: #fff;
            border: 2px solid #333;
            border-radius: 50%;
            color: #333;
            font-weight: bold;
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10;
            font-size: 14px;
        }

        /* カードのデザイン */
        .card {
            display: inline-block;
            background-color: white;
            color: black;
            border-radius: 5px;
            margin: 3px;
            width: 45px;
            height: 65px;
            line-height: 65px;
            font-size: 20px;
            font-weight: bold;
            box-shadow: 1px 1px 3px rgba(0,0,0,0.3);
            vertical-align: middle;
        }
        .card.red { color: #e74c3c; }
        .card.hidden { background: #bdc3c7; color: #bdc3c7; }
        .card.winner {
            box-shadow: 0 0 15px #f1c40f;
            border: 2px solid #f1c40f;
            transform: scale(1.1);
            z-index: 10;
            position: relative;
        }

        .card-placeholder {
            display: inline-block;
            border: 2px dashed rgba(255, 255, 255, 0.3);
            border-radius: 5px;
            margin: 3px;
            width: 41px; /* 45px - 4px border */
            height: 61px; /* 65px - 4px border */
            vertical-align: middle;
            background-color: rgba(255, 255, 255, 0.05);
        }

        /* コミュニティカードエリア */
        .pot-display {
            font-size: 1.5em;
            font-weight: bold;
            margin-bottom: 5px;
            color: #f1c40f;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
        }

        .community-cards {
            background-color: rgba(0,0,0,0.2);
            padding: 15px 30px;
            border-radius: 50px;
            display: inline-flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            gap: 5px;
            min-height: 70px;
            min-width: 250px;
        }

        .cards-row {
            display: flex;
            gap: 5px;
            justify-content: center;
        }

        .waiting-text {
            color: #ddd;
            font-style: italic;
        }
        
        /* コントロールエリア */
        .controls {
            background: rgba(255,255,255,0.1);
            padding: 10px;
            border-radius: 6px;
            /* テーブルの下、中央に配置 */
            position: relative;
            margin: 0 auto;
            width: fit-content;
            display: flex;
            justify-content: center;
            position: relative;
        }
        .controls.disabled {
            pointer-events: none;
            opacity: 0.6;
        }

        /* Buttons */
        .btn {
            padding: 6px 12px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            margin: 0 5px;
            color: white;
            font-weight: bold;
            font-size: 14px;
            transition: background 0.2s;
            display: inline-block;
        }
        .btn:hover { opacity: 0.9; }

        .btn-fold { background: #e74c3c; }
        .btn-check { background: #ecf0f1; color: #333; }
        .btn-bet { background: #f1c40f; color: #333; }
        .btn-next { background: #2ecc71; }

        .btn-reset {
            font-size: 0.8em;
            background: none;
            border: none;
            color: #bdc3c7;
            cursor: pointer;
            text-decoration: underline;
            margin-top: 5px;
            padding: 5px;
        }

        /* Bet Menu */
        .bet-menu {
            display: none;
            position: absolute;
            bottom: 110%;
            left: 50%;
            transform: translateX(-50%); /* 中央揃え */
            background: rgba(44, 62, 80, 0.95);
            padding: 20px;
            border-radius: 12px;
            text-align: center;
            z-index: 100;
            min-width: 260px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            box-shadow: 0 10px 25px rgba(0,0,0,0.5);
            backdrop-filter: blur(10px);
        }
        .bet-menu.show {
            display: block;
        }
        .bet-menu-content {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }
        .bet-label { color: white; font-weight: bold; }
        .bet-input {
            padding: 8px;
            width: 100%;
            box-sizing: border-box;
            text-align: center;
            border-radius: 6px;
            border: 1px solid #7f8c8d;
            background: rgba(255, 255, 255, 0.1);
            color: white;
            font-size: 1.2em;
            font-weight: bold;
            outline: none;
            transition: border-color 0.3s;
        }
        .bet-input:focus {
            border-color: #3498db;
        }
        .bet-slider { width: 100%; cursor: pointer; margin: 10px 0; }
        .bet-presets { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
        .btn-preset {
            cursor: pointer;
            padding: 8px;
            background: #ecf0f1;
            border: none;
            border-radius: 6px;
            color: #2c3e50;
            font-size: 0.9em;
            font-weight: bold;
            transition: all 0.2s ease;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        .btn-preset:hover {
            background: #bdc3c7;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        .btn-allin { background: #9b59b6; color: white; }
        .btn-allin:hover { background: #8e44ad; }
        .btn-confirm {
            padding: 12px;
            background: #f1c40f;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-weight: bold;
            margin-top: 10px;
            width: 100%;
            color: #2c3e50;
            font-size: 1em;
            transition: all 0.2s;
        }
        .btn-confirm:hover {
            background: #f39c12;
            transform: scale(1.02);
        }
        .btn-cancel {
            padding: 10px;
            background: transparent;
            color: #bdc3c7;
            border: 1px solid #7f8c8d;
            border-radius: 6px;
            cursor: pointer;
            width: 100%;
            margin-top: 5px;
            transition: all 0.2s;
        }
        .btn-cancel:hover {
            background: rgba(255, 255, 255, 0.1);
            color: white;
            border-color: white;
        }

        /* Logs */
        .log-area {
            position: absolute;
            top: 80px;
            left: 20px;
            bottom: 20px;
            width: 220px;
            background: rgba(0,0,0,0.3);
            padding: 10px;
            border-radius: 5px;
            overflow-y: auto;
            font-family: 'Consolas', 'Monaco', monospace;
            font-size: 0.9em;
            color: #ecf0f1;
            text-align: left;
            box-sizing: border-box;
        }

        /* アクション吹き出し */
        .action-bubble {
            position: absolute;
            background: white;
            color: #333;
            padding: 5px 12px;
            border-radius: 15px;
            font-size: 0.9em;
            font-weight: bold;
            z-index: 50;
            box-shadow: 2px 2px 5px rgba(0,0,0,0.3);
            white-space: nowrap;
        }

        /* CPU用（カードの左） */
        .bubble-cpu {
            top: 60%;
            right: 50%;
            margin-right: 80px;
            transform: translateY(-50%);
        }

        /* Player用（カードの右） */
        .bubble-player {
            top: 40%;
            left: 50%;
            margin-left: 80px;
            transform: translateY(-50%);
        }

        /* 結果画面用 */
        .result-message {
            font-size: 2.5em;
            margin: 20px 0;
            text-align: center;
        }

        .result-win {
            color: #2ecc71;
        }

        .result-lose {
            color: #e74c3c;
        }
    </style>
    <script>
        function toggleBetMenu() {
            var menu = document.getElementById("bet-menu");
            // CSSクラスで初期非表示になっているため、style.displayがblockでなければ表示するロジックにする
            if (menu.style.display === "block") {
                menu.style.display = "none";
            } else {
                menu.style.display = "block";
                validateBetAmount();
            }
        }

        function setBet(amount) {
            document.getElementById('bet-input').value = amount;
            document.getElementById('bet-slider').value = amount;
            validateBetAmount();
        }

        function syncInput(val) {
            document.getElementById('bet-input').value = val;
            validateBetAmount();
        }

        function syncSlider(val) {
            document.getElementById('bet-slider').value = val;
            validateBetAmount();
        }

        function validateBetAmount() {
            var input = document.getElementById('bet-input');
            var confirmBtn = document.querySelector('.btn-confirm');
            
            if (!input || !confirmBtn) return;

            var val = parseInt(input.value);
            var min = parseInt(input.getAttribute('min'));
            var max = parseInt(input.getAttribute('max'));
            
            var isValid = !isNaN(val) && val >= min && val <= max;

            if (isValid) {
                input.style.borderColor = "";
                input.style.backgroundColor = "";
                confirmBtn.disabled = false;
                confirmBtn.style.opacity = "1";
                confirmBtn.style.cursor = "pointer";
            } else {
                input.style.borderColor = "#e74c3c";
                input.style.backgroundColor = "#fadbd8";
                confirmBtn.disabled = true;
                confirmBtn.style.opacity = "0.6";
                confirmBtn.style.cursor = "not-allowed";
            }
        }

        function autoSubmitForm(formId, delay) {
            setTimeout(function() {
                var form = document.getElementById(formId);
                if (form) {
                    form.submit();
                }
            }, delay);
        }
    </script>
</head>
<body>
    <div class="game-container">
        <h1>Texas Hold'em</h1>
        <a href="result.jsp" class="back-link">戻る</a>
        <div class="action-counter"><%= actionCount %>回</div>

        <!-- Game Log (左側に配置) -->
        <div class="log-area">
            <% for(String log : game.getLogs()) { %>
                <div><%= log %></div>
            <% } %>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error-message">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <%-- CPUターン自動リロード --%>
        <% if (isCpuTurn) { %>
            <script>
                setTimeout(() => window.location.reload(), 1500);
            </script>
        <% } %>
        
        <div class="poker-table">
            <!-- CPU Area -->
            <div class="player-area">
                <% String cpuAction = cpu.consumeLastAction();
                   if (cpuAction != null) { %>
                    <div class="action-bubble bubble-cpu"><%= cpuAction %></div>
                <% } %>
                <% if (game.getDealerButtonPosition() == 1) { %>
                    <div class="dealer-button">D</div>
                <% } %>
                <div class="player-info-top">
                    <span class="player-label">CPU</span>
                    <span class="player-label">Chips: <%= cpu.getChips() %></span>
                </div>
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
                        <span class="card hidden">?</span>
                        <span class="card hidden">?</span>
                    <% } %>
                </div>
            </div>

            <!-- Community Cards & Pot -->
            <div class="community-cards">
                <div class="pot-display">POT: <%= game.getPot() %></div>
                <div class="cards-row">
                    <% 
                    List<Card> communityCards = game.getCommunityCards();
                    for (int i = 0; i < 5; i++) {
                        if (i < communityCards.size()) {
                            Card c = communityCards.get(i);
                            String colorClass = (c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS) ? "red" : "";
                            String winnerClass = "";
                            if (game.getState() == State.SHOWDOWN) {
                                if (cpu.isWinner() && cpu.getBestHand() != null && cpu.getBestHand().contains(c)) winnerClass = "winner";
                                if (human.isWinner() && human.getBestHand() != null && human.getBestHand().contains(c)) winnerClass = "winner";
                            }
                    %>
                        <span class="card <%= colorClass %> <%= winnerClass %>"><%= c %></span>
                    <%  } else { %>
                        <span class="card-placeholder"></span>
                    <%  } 
                    } %>
                </div>
            </div>

            <!-- Player Area -->
            <div class="player-area">
                <% String humanAction = human.consumeLastAction();
                   if (humanAction != null) { %>
                    <div class="action-bubble bubble-player"><%= humanAction %></div>
                <% } %>
                <% if (game.getDealerButtonPosition() == 0) { %>
                    <div class="dealer-button">D</div>
                <% } %>
                <div>
                    <% for(Card c : human.getHoleCards()) { 
                        String colorClass = (c.getSuit() == Card.Suit.HEARTS || c.getSuit() == Card.Suit.DIAMONDS) ? "red" : "";
                        String winnerClass = (human.isWinner() && human.getBestHand() != null && human.getBestHand().contains(c)) ? "winner" : "";
                    %>
                        <span class="card <%= colorClass %> <%= winnerClass %>"><%= c %></span>
                    <% } %>
                </div>
                <div class="player-info-bottom">
                    <span class="player-label"><%= human.getName() %></span>
                    <span class="player-label">Chips: <%= human.getChips() %></span>
                </div>
            </div>
        </div>

        <!-- Controls (テーブルの下に配置) -->
        <div class="controls <%= isCpuTurn ? "disabled" : "" %>">
            <form action="poker" method="post">
                <% if (game.getState() == State.SHOWDOWN) { %>
                    <button type="submit" name="action" value="next" class="btn btn-next">Next Round</button>
                <% } else { %>
                    <!-- メインアクションボタン -->
                    <div id="main-actions">
                        <button type="submit" name="action" value="fold" class="btn btn-fold">Fold</button>
                        <button type="submit" name="action" value="call" class="btn btn-check">Check / Call</button>
                        <button type="button" onclick="toggleBetMenu()" class="btn btn-bet">Bet / Raise ...</button>
                    </div>

                <%
                    // ベット額の計算ロジックをGameクラスに移譲
                    BettingOptions opts = game.getBettingOptions(human);
                    int minAmount = opts.getMinAmount();
                    int maxAmount = opts.getMaxAmount();
                    int halfPotAmount = opts.getHalfPotAmount();
                    int potAmount = opts.getPotAmount();
                %>

                <!-- ベットメニュー（ポップアップ風に表示） -->
                <div id="bet-menu" class="bet-menu">
                    <div class="bet-menu-content">
                        <div class="bet-label">Bet Amount</div>
                        <input type="number" name="amount" id="bet-input" class="bet-input" 
                               value="<%= minAmount %>" 
                               min="<%= minAmount %>" 
                               max="<%= maxAmount %>" 
                               oninput="syncSlider(this.value)">
                        <input type="range" id="bet-slider" class="bet-slider" 
                               min="<%= minAmount %>" 
                               max="<%= maxAmount %>" 
                               value="<%= minAmount %>" 
                               oninput="syncInput(this.value)">
                        
                        <div class="bet-presets">
                            <button type="button" onclick="setBet(<%= minAmount %>)" class="btn-preset">Min</button>
                            <button type="button" onclick="setBet(<%= halfPotAmount %>)" class="btn-preset">1/2 Pot</button>
                            <button type="button" onclick="setBet(<%= potAmount %>)" class="btn-preset">Pot</button>
                            <button type="button" onclick="setBet(<%= maxAmount %>)" class="btn-preset btn-allin">All In</button>
                        </div>
                        
                        <button type="submit" name="action" value="bet" class="btn-confirm">Confirm</button>
                        <button type="button" onclick="toggleBetMenu()" class="btn-cancel">Cancel</button>
                    </div>
                <% } %>
            </form>
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
        <% if (isAllIn && isGameActive && !isCpuTurn) { %>
            autoSubmitForm("autoAdvanceForm", 1000);
        <% } %>
    </script>
</body>
</html>
