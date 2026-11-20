package ru.vsu.durak;

/**
 * Масть карты.
 */
public enum Suit {
    SPADES("♠"),
    HEARTS("♥"),
    DIAMONDS("♦"),
    CLUBS("♣");

    private final String symbol;

    Suit(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Символ масти для вывода.
     *
     * @return символ масти для вывода
     */
    public String getSymbol() {
        return symbol;
    }
}

