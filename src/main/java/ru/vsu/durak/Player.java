package ru.vsu.durak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Игрок: номер и карты на руках. Изменять руку можно только через {@link Game}.
 */
public class Player {
    private final int index;
    private final List<Card> hand = new ArrayList<>();

    Player(int index) {
        this.index = index;
    }

    /**
     * Номер игрока (с нуля).
     *
     * @return номер игрока (с нуля)
     */
    public int getIndex() {
        return index;
    }

    /**
     * Карты на руках (только для чтения).
     *
     * @return карты на руках (только для чтения)
     */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    /**
     * Количество карт на руках.
     *
     * @return количество карт на руках
     */
    public int handSize() {
        return hand.size();
    }

    /**
     * Создаёт объект.
     *
     * @param card карта
     * @return true, если карта есть на руках
     */
    public boolean hasCard(Card card) {
        return hand.contains(card);
    }

    void addCard(Card card) {
        hand.add(card);
    }

    void removeCard(Card card) {
        hand.remove(card);
    }
}
