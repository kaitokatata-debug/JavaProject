package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RankingDao {

    // データを運ぶための簡易的な内部クラス（DTO）
    public static class RankingRecord {
        public String name;
        public int score;

        public RankingRecord(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    /**
     * データベースからランキング情報を取得します。
     * @return ランキングレコードのリスト
     */
    public List<RankingRecord> getAllRankings() {
        List<RankingRecord> list = new ArrayList<>();
        
        // DB接続ロジックをここに集約
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT u.name, COALESCE(MAX(r.score), 0) as score FROM users u LEFT JOIN rankings r ON u.id = r.user_id GROUP BY u.id, u.name ORDER BY score DESC")) {
            
            while (rs.next()) {
                list.add(new RankingRecord(rs.getString("name"), rs.getInt("score")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}