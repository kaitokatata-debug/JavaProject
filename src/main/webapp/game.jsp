<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String sessionName = (String) session.getAttribute("username");
    if (sessionName == null) sessionName = "Guest";
%>
<html>
<head>
    <title>ブロック崩し</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        /* ゲーム画面専用のスタイル */
        canvas { 
            background: #eee; 
            display: block; 
            margin: 0 auto; 
            border: 2px solid #333; 
            border-radius: 4px;
        }
        .game-container { 
            text-align: center; 
            margin-top: 20px; 
        }
        .controls {
            margin-top: 10px;
            font-size: 0.9em;
            color: #666;
        }
        /* モーダルウィンドウのスタイル */
        .modal {
            display: none; /* 初期状態は非表示 */
            position: fixed;
            z-index: 2000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5); /* 半透明の黒背景 */
            align-items: center;
            justify-content: center;
        }
        .modal-content {
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
            min-width: 300px;
        }
        .modal-btn { margin: 10px; padding: 10px 20px; cursor: pointer; border: none; border-radius: 4px; color: white; font-weight: bold; }
        .btn-retry { background-color: #3498db; }
        .btn-exit { background-color: #e74c3c; }
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
    <h1>ブロック崩しゲーム</h1>
    
    <div class="game-container">
        <canvas id="myCanvas" width="480" height="400"></canvas>
        
        <p class="controls">操作方法: 左右の矢印キー（← →）で移動、スペースキーで上下入れ替え</p>
        <a href="result.jsp">結果画面に戻る</a>
    </div>

    <!-- ゲームオーバー時のモーダル -->
    <div id="gameOverModal" class="modal">
        <div class="modal-content">
            <h2 style="color: #e74c3c;">GAME OVER</h2>
            <p id="modalScoreText" style="font-size: 1.2em; margin: 15px 0;"></p>
            <p>コンティニューしますか？</p>
            <button class="modal-btn btn-retry" onclick="onContinue()">リトライ (継続)</button>
            <button class="modal-btn btn-exit" onclick="onExit()">終了 (保存)</button>
        </div>
    </div>

    <script>
        // セッションからユーザー名を取得
        const username = "<%= sessionName %>";

        // ゲームの変数を設定
        var canvas = document.getElementById("myCanvas");
        var ctx = canvas.getContext("2d");
        var ballRadius = 10;
        var x = canvas.width/2;
        var y = canvas.height-30;
        var dx = 2;
        var dy = -2;
        var basePaddleHeight = 10;
        var paddleHeight = basePaddleHeight;
        var basePaddleWidth = 75;
        var paddleWidth = basePaddleWidth;
        var paddleX = (canvas.width-paddleWidth)/2;
        var rightPressed = false;
        var leftPressed = false;
        var paddleSpeed = 5; // パドルの移動速度（7から5に下げて調整）
        var isPaddleBottom = true; // パドルの位置（true:下, false:上）
        
        // ブロックの設定
        var brickRowCount = 3;
        var brickColumnCount = 5;
        var brickWidth = 75;
        var brickHeight = 20;
        var brickPadding = 10;
        var brickOffsetTop = (canvas.height - (brickRowCount * (brickHeight + brickPadding))) / 2;
        var brickOffsetLeft = 30;
        
        // コンティニュー用に保存されたスコアがあれば取得
        var savedScore = sessionStorage.getItem("continueScore");
        var score = savedScore ? parseInt(savedScore) : 0;
        sessionStorage.removeItem("continueScore"); // 一度読み込んだら削除
        var isGameStarted = false;

        var bricks = [];
        for(var c=0; c<brickColumnCount; c++) {
            bricks[c] = [];
            for(var r=0; r<brickRowCount; r++) {
                bricks[c][r] = { x: 0, y: 0, status: 1 };
            }
        }

        // キーボード操作の監視
        document.addEventListener("keydown", keyDownHandler, false);
        document.addEventListener("keyup", keyUpHandler, false);

        function keyDownHandler(e) {
            if (!isGameStarted) {
                isGameStarted = true;
                draw();
            }
            if(e.key == "Right" || e.key == "ArrowRight") {
                rightPressed = true;
            }
            else if(e.key == "Left" || e.key == "ArrowLeft") {
                leftPressed = true;
            }
            else if(e.code == "Space") {
                isPaddleBottom = !isPaddleBottom;
            }
        }

        function keyUpHandler(e) {
            if(e.key == "Right" || e.key == "ArrowRight") {
                rightPressed = false;
            }
            else if(e.key == "Left" || e.key == "ArrowLeft") {
                leftPressed = false;
            }
        }

        function handleGameOver() {
            ctx.font = "24px Arial";
            ctx.fillStyle = "red";
            ctx.textAlign = "center";
            ctx.fillText("GAME OVER", canvas.width/2, canvas.height/2);
            
            // モーダルを表示
            document.getElementById("modalScoreText").innerText = "Score: " + score;
            document.getElementById("gameOverModal").style.display = "flex";
        }

        function onContinue() {
            sessionStorage.setItem("continueScore", score);
            // 裏側でスコア保存を実行してからリロードする
            fetch("saveScore?score=" + score)
                .then(function() {
                    document.location.reload();
                })
                .catch(function() {
                    document.location.reload();
                });
        }

        function onExit() {
            document.location.href = "saveScore?score=" + score;
        }

        // 当たり判定
        function collisionDetection() {
            for(var c=0; c<brickColumnCount; c++) {
                for(var r=0; r<brickRowCount; r++) {
                    var b = bricks[c][r];
                    if(b.status == 1) {
                        if(x > b.x && x < b.x+brickWidth && y > b.y && y < b.y+brickHeight) {
                            dy = -dy;
                            b.status = 0;
                            score++;
                            // ブロックを壊すたびに速度を10%アップ（加速）
                            dx *= 1.1;
                            dy *= 1.1;

                            if(score == brickRowCount*brickColumnCount) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        function drawBall() {
            ctx.beginPath();
            ctx.arc(x, y, ballRadius, 0, Math.PI*2);
            ctx.fillStyle = "#0095DD";
            ctx.fill();
            ctx.closePath();
        }
        function drawPaddle() {
            ctx.beginPath();
            // 膨らんだ分（差分）の半分をオフセットとして計算し、中心から膨らむようにする
            var heightDiff = paddleHeight - basePaddleHeight;
            var offset = heightDiff / 2;
            var pY = isPaddleBottom ? canvas.height-paddleHeight + offset : -offset;
            ctx.rect(paddleX, pY, paddleWidth, paddleHeight);
            ctx.fillStyle = "#0095DD";
            ctx.fill();
            ctx.closePath();
        }
        function drawBricks() {
            for(var c=0; c<brickColumnCount; c++) {
                for(var r=0; r<brickRowCount; r++) {
                    if(bricks[c][r].status == 1) {
                        var brickX = (c*(brickWidth+brickPadding))+brickOffsetLeft;
                        var brickY = (r*(brickHeight+brickPadding))+brickOffsetTop;
                        bricks[c][r].x = brickX;
                        bricks[c][r].y = brickY;
                        ctx.beginPath();
                        ctx.rect(brickX, brickY, brickWidth, brickHeight);
                        ctx.fillStyle = "#0095DD";
                        ctx.fill();
                        ctx.closePath();
                    }
                }
            }
        }
        function drawScore() {
            ctx.font = "16px Arial";
            ctx.fillStyle = "#0095DD";
            ctx.textAlign = "left";
            ctx.fillText("Score: "+score, 8, 25);
        }

        function draw() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            
            // パドルの高さも徐々に戻す
            if(paddleHeight > basePaddleHeight) {
                paddleHeight -= 1;
                if(paddleHeight < basePaddleHeight) paddleHeight = basePaddleHeight;
            }

            drawBricks();
            drawBall();
            drawPaddle();
            drawScore();
            
            if(collisionDetection()) {
                // ゴージャスなクリア演出
                ctx.fillStyle = "rgba(255, 255, 255, 0.9)"; // 背景を白っぽく
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                
                ctx.font = "bold 36px Arial";
                ctx.fillStyle = "#FFD700"; // ゴールド
                ctx.shadowColor = "black"; // 黒い影
                ctx.shadowBlur = 10;       // 影をぼかす
                ctx.textAlign = "center";
                ctx.fillText("CONGRATULATIONS!", canvas.width/2, canvas.height/2);
                
                ctx.shadowBlur = 0;
                ctx.fillStyle = "#333";
                ctx.font = "20px Arial";
                ctx.fillText("Score: " + score, canvas.width/2, canvas.height/2 + 40);

                setTimeout(function() {
                    document.location.href = "saveScore?score=" + score;
                }, 2000); // 2秒間余韻に浸らせる
                return;
            }

            if(x + dx > canvas.width-ballRadius || x + dx < ballRadius) {
                dx = -dx;
            }
            if(y + dy < ballRadius) {
                if(!isPaddleBottom && x > paddleX && x < paddleX + paddleWidth) {
                    dy = -dy;
                    dx = (x - (paddleX + paddleWidth/2)) * 0.15;
                    paddleHeight = basePaddleHeight + 10;
                } else {
                    // ゲームオーバー
                    handleGameOver();
                    return;
                }
            } else if(y + dy > canvas.height-ballRadius) {
                if(isPaddleBottom && x > paddleX && x < paddleX + paddleWidth) {
                    dy = -dy;
                    
                    // パドルの当たった位置によってボールの角度（ベクトル）を変える
                    // 中心から離れるほど横方向の速度をつける（0.15は変化の強さ）
                    dx = (x - (paddleX + paddleWidth/2)) * 0.15;

                    // 高さも変える（上方向に伸びる演出）
                    paddleHeight = basePaddleHeight + 10;
                }
                else {
                    // ゲームオーバーメッセージを描画して停止
                    handleGameOver();
                    return;
                }
            }

            if(rightPressed && paddleX < canvas.width-paddleWidth) {
                paddleX += paddleSpeed;
            }
            else if(leftPressed && paddleX > 0) {
                paddleX -= paddleSpeed;
            }

            x += dx;
            y += dy;
            requestAnimationFrame(draw);
        }

        function initDraw() {
            drawBricks();
            drawBall();
            drawPaddle();
            drawScore();
            ctx.font = "20px Arial";
            ctx.fillStyle = "#333";
            ctx.textAlign = "center";
            ctx.fillText("キーを押してスタート", canvas.width/2, canvas.height/2);
        }
        initDraw();
    </script>
</body>
</html>