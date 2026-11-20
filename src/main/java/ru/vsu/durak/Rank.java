package ru.vsu.durak;

/**
 * Достоинство карты. Порядок констант — от младшей к старшей.
 */
public enum Rank {
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    JACK("J"),
    QUEEN("Q"),
    KING("K"),
    ACE("A");

    private final String label;

    Rank(String label) {
        this.label = label;
    }

    /**
     * Короткая подпись достоинства.
     *
     * @return короткая подпись достоинства
     */
    public String getLabel() {
        return label;
    }
}