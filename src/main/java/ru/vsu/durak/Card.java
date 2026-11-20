package ru.vsu.durak;

public final class Card {
    private final Suit suit;
    private final Rank rank;

    /**
     * Создаёт объект.
     *
     * @param suit масть
     * @param rank достоинство
     */
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Масть карты.
     *
     * @return масть карты
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Достоинство карты.
     *
     * @return достоинство карты
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Проверяет, бьёт ли эта карта другую карту.
     *
     * @param other карта, которую нужно побить
     * @param trump козырь
     * @return true, если эта карта бьёт другую, false иначе
     */
    public boolean beats(Card other, Suit trump) {
        if (suit == other.suit) {
            return rank.ordinal() > other.rank.ordinal();
        }
        return suit == trump;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Card)) {
            return false;
        }
        Card other = (Card) obj;
        return suit == other.suit && rank == other.rank;
    }

    @Override 
    public int hashCode() {
        return suit.hashCode() * 100 + rank.hashCode();
    }

    @Override 
    public String toString() {
        return rank.getLabel() + suit.getSymbol();
    }
}
