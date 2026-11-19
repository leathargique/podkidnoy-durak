package ru.vsu.durak;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CardTest {
    private final Suit trump = Suit.HEARTS;

    @Test
    void higherRankOfSameSuitBeats() {
        Card seven = new Card(Suit.SPADES, Rank.SEVEN);
        Card nine = new Card(Suit.SPADES, Rank.NINE);
        assertTrue(nine.beats(seven, trump));
        assertFalse(seven.beats(nine, trump));
    }

    @Test
    void trumpBeatsNonTrumpOfAnyRank() {
        Card smallTrump = new Card(Suit.HEARTS, Rank.SIX);
        Card ace = new Card(Suit.SPADES, Rank.ACE);
        assertTrue(smallTrump.beats(ace, trump));
        assertFalse(ace.beats(smallTrump, trump));
    }

    @Test
    void differentNonTrumpSuitsDoNotBeat() {
        Card spadeAce = new Card(Suit.SPADES, Rank.ACE);
        Card clubSix = new Card(Suit.CLUBS, Rank.SIX);
        assertFalse(spadeAce.beats(clubSix, trump));
    }

    @Test
    void equalCardsDoNotBeatEachOther() {
        Card a = new Card(Suit.CLUBS, Rank.KING);
        Card b = new Card(Suit.CLUBS, Rank.KING);
        assertFalse(a.beats(b, trump));
    }
}