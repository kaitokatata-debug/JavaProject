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
    </style>
</head>
<body>
    <h1>ブロック崩しゲーム</h1>
    
    <div class="game-container">
        <canvas id="myCanvas" width="480" height="320"></canvas>
        <p class="controls">操作方法: 左右の矢印キー（← →）でパドルを移動</p>
        <a href="result.jsp">結果画面に戻る</a>
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
        
        // ブロックの設定
        var brickRowCount = 3;
        var brickColumnCount = 5;
        var brickWidth = 75;
        var brickHeight = 20;
        var brickPadding = 10;
        var brickOffsetTop = 30;
        var brickOffsetLeft = 30;
        var score = 0;

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
            if(e.key == "Right" || e.key == "ArrowRight") {
                rightPressed = true;
            }
            else if(e.key == "Left" || e.key == "ArrowLeft") {
                leftPressed = true;
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
                            dx *= 1.05;
                            dy *= 1.05;

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
            ctx.rect(paddleX, canvas.height-paddleHeight, paddleWidth, paddleHeight);
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
            ctx.fillText("Score: "+score, 8, 20);
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
                dy = -dy;
            } else if(y + dy > canvas.height-ballRadius) {
                if(x > paddleX && x < paddleX + paddleWidth) {
                    dy = -dy;
                    // 高さも変える（上方向に伸びる演出）
                    paddleHeight = basePaddleHeight + 10;
                }
                else {
                    // ゲームオーバーメッセージを描画して停止
                    ctx.font = "24px Arial";
                    ctx.fillStyle = "red";
                    ctx.textAlign = "center";
                    ctx.fillText("GAME OVER", canvas.width/2, canvas.height/2);
                    
                    // 1秒後に画面遷移させる
                    setTimeout(function() {
                        document.location.href = "saveScore?score=" + score;
                    }, 1000);
                    return;
                }
            }

            if(rightPressed && paddleX < canvas.width-paddleWidth) {
                paddleX += 7;
            }
            else if(leftPressed && paddleX > 0) {
                paddleX -= 7;
            }

            x += dx;
            y += dy;
            requestAnimationFrame(draw);
        }

        draw();
    </script>
</body>
</html>