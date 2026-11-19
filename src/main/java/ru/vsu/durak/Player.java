package ru.vsu.durak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    private final int index;
    private final List<Card> hand = new ArrayList<>();

    Player(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public int handSize() {
        return hand.size();
    }

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