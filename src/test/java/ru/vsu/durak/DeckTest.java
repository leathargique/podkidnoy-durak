package ru.vsu.durak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DeckTest {

    @Test
    void deckHas36UniqueCards() {
        Deck deck = new Deck();
        assertEquals(36, deck.size());
        assertEquals(36, new HashSet<>(deck.getCards()).size());
    }

    @Test
    void sameSeedGivesSameOrder() {
        Deck a = new Deck();
        Deck b = new Deck();
        a.shuffle(new Random(5));
        b.shuffle(new Random(5));
        assertEquals(a.getCards(), b.getCards());
    }

    @Test
    void shuffleKeepsAllCards() {
        Deck deck = new Deck();
        deck.shuffle(new Random(1));
        Set<Card> set = new HashSet<>(deck.getCards());
        assertEquals(36, set.size());
    }

    @Test
    void trumpIsBottomCardAndDrawnLast() {
        Deck deck = new Deck();
        deck.shuffle(new Random(3));
        deck.fixTrump();
        Card trump = deck.getTrumpCard();
        Card last = null;
        while (!deck.isEmpty()) {
            last = deck.draw();
        }
        assertEquals(trump, last);
    }

    @Test
    void drawFromEmptyDeckThrows() {
        Deck deck = new Deck();
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertThrows(IllegalStateException.class, deck::draw);
    }
}