package poker;

import java.util.ArrayList;
import java.util.List;

/**
 * ゲームのログを管理するクラス。
 */
public class Logger {
    private final List<String> logs = new ArrayList<>();

    /**
     * ログメッセージを追加し、標準出力にも表示します。
     * @param message ログメッセージ
     */
    public void log(String message) {
        logs.add(message);
        System.out.println(message);
    }

    /**
     * 現在保持しているログのリストを取得します。
     * @return ログリスト
     */
    public List<String> getLogs() {
        return logs;
    }

    /**
     * ログをクリアします。
     */
    public void clear() {
        logs.clear();
    }
}