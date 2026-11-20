package ru.vsu.durak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {
    private final List<Card> cards = new ArrayList<>();
    private Card trumpCard;

    /**
     * Создаёт полную несмешанную колоду из 36 карт.
     */
    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    /**
     * Тасует колоду (алгоритм Фишера — Йетса) с помощью переданного генератора.
     *
     * @param random единственный генератор случайных чисел партии
     */
    public void shuffle(Random random) {
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Card temp = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, temp);
        }
    }

    /**
     * Запоминает нижнюю карту колоды как козырную. Вызывается один раз после тасования.
     */
    public void fixTrump() {
        trumpCard = cards.get(cards.size() - 1);
    }

    /**
     * Берёт верхнюю карту.
     *
     * @return верхняя карта
     * @throws IllegalStateException если колода пуста
     */
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Колода пуста");
        }
        return cards.remove(0);
    }

    /**
     * Количество карт в колоде.
     *
     * @return количество карт в колоде
     */
    public int size() {
        return cards.size();
    }

    /**
     * True, если карт не осталось.
     *
     * @return true, если карт не осталось
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Козырная карта (нижняя карта колоды в начале партии).
     *
     * @return козырная карта (нижняя карта колоды в начале партии)
     */
    public Card getTrumpCard() {
        return trumpCard;
    }

    /**
     * Карты колоды сверху вниз (только для чтения).
     *
     * @return карты колоды сверху вниз (только для чтения)
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
}