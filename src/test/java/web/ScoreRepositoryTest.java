package web;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScoreRepositoryTest {

    // インメモリDBを使用（テスト実行中のみメモリ上に存在）
    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    private ScoreRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        // テスト用DB接続設定でリポジトリを初期化
        repository = new ScoreRepository(DB_URL, DB_USER, DB_PASS);

        // テスト用テーブルの作成
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), score INT DEFAULT 0)");
            stmt.execute("CREATE TABLE rankings (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, game_name VARCHAR(255), score INT)");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // テーブルの削除（クリーンアップ）
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE users");
            stmt.execute("DROP TABLE rankings");
        }
    }

    @Test
    void testSaveScore_NewUser() throws Exception {
        // 新規ユーザーのスコア保存
        repository.saveScore("NewUser", 100, null, "TestGame");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // usersテーブルの確認
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE name = ?")) {
                ps.setString(1, "NewUser");
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next(), "User should be created");
                assertEquals(100, rs.getInt("score"), "Score should be 100");
            }

            // rankingsテーブルの確認
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM rankings WHERE game_name = ?")) {
                ps.setString(1, "TestGame");
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next(), "Ranking should be created");
                assertEquals(100, rs.getInt("score"), "Ranking score should be 100");
            }
        }
    }

    @Test
    void testSaveScore_UpdateHighScore() throws Exception {
        // 事前にユーザーを作成（スコア50）
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO users (name, score) VALUES ('ExistingUser', 50)");
        }

        // より高いスコアで保存
        repository.saveScore("ExistingUser", 200, null, "TestGame");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // スコアが更新されているか確認
            try (PreparedStatement ps = conn.prepareStatement("SELECT score FROM users WHERE name = ?")) {
                ps.setString(1, "ExistingUser");
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next());
                assertEquals(200, rs.getInt("score"), "Score should be updated to 200");
            }
        }
    }

    @Test
    void testSaveScore_DoNotUpdateLowScore() throws Exception {
        // 事前にユーザーを作成（スコア100）
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO users (name, score) VALUES ('ProUser', 100)");
        }

        // 低いスコアで保存
        repository.saveScore("ProUser", 50, null, "TestGame");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // スコアが更新されていないことを確認
            try (PreparedStatement ps = conn.prepareStatement("SELECT score FROM users WHERE name = ?")) {
                ps.setString(1, "ProUser");
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next());
                assertEquals(100, rs.getInt("score"), "Score should remain 100");
            }
            
            // ランキングには今回のスコアが記録されるべき
            try (PreparedStatement ps = conn.prepareStatement("SELECT score FROM rankings WHERE game_name = ?")) {
                ps.setString(1, "TestGame");
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next());
                assertEquals(50, rs.getInt("score"), "Ranking should record the new score regardless");
            }
        }
    }
}