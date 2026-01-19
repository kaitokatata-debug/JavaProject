package poker;

/**
 * 人間が操作するプレイヤーを表すクラス。
 */
public class HumanPlayer extends Player {

    public HumanPlayer(String name, int chips) {
        super(name, chips);
    }

    @Override
    public boolean isBot() {
        return false;
    }
}