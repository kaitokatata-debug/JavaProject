package web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ScoreRepository {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    public ScoreRepository() {
        this("jdbc:h2:./mydb", "sa", "");
    }

    public ScoreRepository(String dbUrl, String dbUser, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    public void saveScore(String name, int score, Integer userId, String gameName) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            // userIdがない場合、名前から取得
            if (userId == null) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE name = ?")) {
                    ps.setString(1, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("id");
                        }
                    }
                }
            }

            // ハイスコアの場合のみusersテーブルを更新
            try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET score = ? WHERE name = ? AND score < ?")) {
                updateStmt.setInt(1, score);
                updateStmt.setString(2, name);
                updateStmt.setInt(3, score);
                int rows = updateStmt.executeUpdate();

                // 更新されず、かつユーザーが存在しない場合は新規作成
                if (rows == 0 && userId == null) {
                    try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (name, score) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, name);
                        insertStmt.setInt(2, score);
                        insertStmt.executeUpdate();
                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                userId = generatedKeys.getInt(1);
                            }
                        }
                    }
                }
            }

            // ランキングテーブルに保存
            if (userId != null) {
                try (PreparedStatement rankStmt = conn.prepareStatement("INSERT INTO rankings (user_id, game_name, score) VALUES (?, ?, ?)")) {
                    rankStmt.setInt(1, userId);
                    rankStmt.setString(2, gameName);
                    rankStmt.setInt(3, score);
                    rankStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}