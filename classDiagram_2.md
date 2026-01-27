```mermaid
classDiagram
    class TexasHoldemGame {
        -Dealer dealer
        -RuleEngine rules
        -Table table
    }

    %% 1. カード操作の専門家
    class Dealer {
        -Deck deck
        +shuffle()
        +dealHoleCards(List~Player~)
        +dealFlop(Board)
    }

    %% 2. 状態の保持者（構造化）
    class Table {
        -PlayerManager players
        -Board board
    }

    %% 「場」の概念
    class Board {
        -CommunityCards cards
        -Pot pot
    }

    %% 3. ルールの判定者
    class RuleEngine {
        +resolveShowdown(Table)
        +getBettingOptions(Table, Player)
    }

    TexasHoldemGame --> Dealer
    TexasHoldemGame --> RuleEngine
    TexasHoldemGame --> Table
    
    Table --> Board
    Dealer ..> Table : 操作
    RuleEngine ..> Table : 参照
