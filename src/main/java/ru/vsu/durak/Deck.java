package ru.vsu.durak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {
    private final List<Card> cards = new ArrayList<>();
    private Card trumpCard;
    private Random random;

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Card temp = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, temp);
        }
    }

    public void fixTrump() {
        trumpCard = cards.get(cards.size() - 1);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Колода пуста");
        }
        return cards.remove(0);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public Card getTrumpCard() {
        return trumpCard;
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
}