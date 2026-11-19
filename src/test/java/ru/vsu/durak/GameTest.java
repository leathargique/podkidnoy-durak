package ru.vsu.durak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GameTest {

    @Test
    void invalidPlayerCountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Game(1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Game(6, 1));
    }

    @Test
    void dealGivesSixCardsAndLeavesRestInDeck() {
        for (int n = 2; n <= 5; n++) {
            Game game = new Game(n, 10);
            for (int i = 0; i < n; i++) {
                assertEquals(6, game.getPlayer(i).handSize());
            }
            assertEquals(36 - 6 * n, game.getDeckSize());
        }
    }

    @Test
    void trumpSuitMatchesTrumpCard() {
        Game game = new Game(2, 4);
        assertEquals(game.getTrumpCard().getSuit(), game.getTrumpSuit());
    }

    @Test
    void firstAttackerHasLowestTrumpIfAnyoneHasTrump() {
        for (long seed = 0; seed < 50; seed++) {
            Game game = new Game(3, seed);
            int lowest = 100;
            int owner = 0;
            for (int i = 0; i < 3; i++) {
                for (Card card : game.getPlayer(i).getHand()) {
                    if (card.getSuit() == game.getTrumpSuit() && card.getRank().ordinal() < lowest) {
                        lowest = card.getRank().ordinal();
                        owner = i;
                    }
                }
            }
            assertEquals(owner, game.getAttacker());
        }
    }

    @Test
    void sameSeedGivesSameGame() {
        Game a = new Game(3, 99);
        Game b = new Game(3, 99);
        RandomBot.playToEnd(a, new Random(1), 100000);
        RandomBot.playToEnd(b, new Random(1), 100000);
        assertEquals(a.getEventLog().toString(), b.getEventLog().toString());
        assertEquals(a.getLoser(), b.getLoser());
    }

    @Test
    void differentSeedsGiveDifferentDeals() {
        Game a = new Game(2, 1);
        Game b = new Game(2, 2);
        assertFalse(a.getPlayer(0).getHand().equals(b.getPlayer(0).getHand()));
    }

    @Test
    void onlyAttackerStartsTurn() {
        Game game = new Game(3, 7);
        int other = (game.getAttacker() + 2) % 3;
        if (other == game.getDefender()) {
            other = (game.getAttacker() + 1) % 3;
        }
        Card card = game.getPlayer(other).getHand().get(0);
        final int thrower = other;
        assertThrows(IllegalMoveException.class, () -> game.attack(thrower, card));
    }

    @Test
    void defenderCannotAttack() {
        Game game = new Game(2, 7);
        int d = game.getDefender();
        Card card = game.getPlayer(d).getHand().get(0);
        assertThrows(IllegalMoveException.class, () -> game.attack(d, card));
    }

    @Test
    void cannotPlayCardFromOtherHand() {
        Game game = new Game(2, 7);
        Card foreign = game.getPlayer(game.getDefender()).getHand().get(0);
        int a = game.getAttacker();
        assertThrows(IllegalMoveException.class, () -> game.attack(a, foreign));
    }

    @Test
    void finishTurnOnEmptyTableIsRejected() {
        Game game = new Game(2, 7);
        assertThrows(IllegalMoveException.class, game::finishTurn);
    }

    @Test
    void takeWithoutAttackIsRejected() {
        Game game = new Game(2, 7);
        assertThrows(IllegalMoveException.class, game::take);
    }

    @Test
    void throwInIsLimitedByDefenderHandAndSix() {
        Game game = new Game(2, 3);
        int a = game.getAttacker();
        int limit = game.getAttackLimit();
        assertEquals(Math.min(6, game.getPlayer(game.getDefender()).handSize()), limit);
        List<Card> candidates = game.getAttackCandidates(a);
        while (!candidates.isEmpty()) {
            game.attack(a, candidates.get(0));
            candidates = game.getAttackCandidates(a);
        }
        assertTrue(game.getTable().size() <= limit);
        for (Card card : game.getPlayer(a).getHand()) {
            if (game.getTable().size() == limit) {
                assertFalse(game.canAttack(a, card));
            }
        }
    }

    @Test
    void throwInMustMatchRankOnTable() {
        Game game = new Game(2, 3);
        int a = game.getAttacker();
        Card first = game.getPlayer(a).getHand().get(0);
        game.attack(a, first);
        for (Card card : game.getAttackCandidates(a)) {
            assertEquals(first.getRank(), card.getRank());
        }
    }

    @Test
    void takeMovesTableToDefenderAndSkipsHisDraw() {
        Game game = new Game(2, 1);
        int a = game.getAttacker();
        int d = game.getDefender();
        game.attack(a, game.getPlayer(a).getHand().get(0));
        game.take();
        assertTrue(game.isTakeDeclared());
        game.finishTurn();

        assertEquals(7, game.getPlayer(d).handSize());
        assertEquals(6, game.getPlayer(a).handSize());
        assertEquals(36 - 12 - 1, game.getDeckSize());
        // после взятия защитник пропускает ход: атакует тот же игрок
        assertEquals(a, game.getAttacker());
        assertEquals(d, game.getDefender());
    }

    @Test
    void beatingAllCardsSendsThemToDiscardAndSwapsRoles() {
        for (long seed = 0; seed < 200; seed++) {
            Game game = new Game(2, seed);
            int a = game.getAttacker();
            int d = game.getDefender();
            Card attackCard = game.getPlayer(a).getHand().get(0);
            game.attack(a, attackCard);
            List<Card> options = game.getDefenseCandidates(attackCard);
            if (options.isEmpty()) {
                continue;
            }
            game.defend(attackCard, options.get(0));
            game.finishTurn();

            assertEquals(2, game.getDiscardSize());
            assertEquals(6, game.getPlayer(a).handSize());
            assertEquals(6, game.getPlayer(d).handSize());
            assertEquals(d, game.getAttacker());
            assertEquals(a, game.getDefender());
            return;
        }
        throw new AssertionError("не нашлось раздачи, где можно отбиться");
    }

    @Test
    void cannotFinishWhileUnbeatenCardsRemainAndDefenderDidNotTake() {
        Game game = new Game(2, 1);
        int a = game.getAttacker();
        game.attack(a, game.getPlayer(a).getHand().get(0));
        assertThrows(IllegalMoveException.class, game::finishTurn);
    }

    @Test
    void defenseWithWeakCardIsRejected() {
        for (long seed = 0; seed < 200; seed++) {
            Game game = new Game(2, seed);
            int a = game.getAttacker();
            Card attackCard = game.getPlayer(a).getHand().get(0);
            game.attack(a, attackCard);
            for (Card card : game.getPlayer(game.getDefender()).getHand()) {
                if (!card.beats(attackCard, game.getTrumpSuit())) {
                    assertThrows(IllegalMoveException.class, () -> game.defend(attackCard, card));
                    return;
                }
            }
        }
    }

    @Test
    void randomGamesAlwaysFinishAndKeep36Cards() {
        for (int n = 2; n <= 5; n++) {
            for (long seed = 0; seed < 100; seed++) {
                Game game = new Game(n, seed);
                RandomBot.playToEnd(game, new Random(seed), 100000);
                assertTrue(game.isFinished());
                int total = game.getDeckSize() + game.getDiscardSize();
                for (int i = 0; i < n; i++) {
                    total += game.getPlayer(i).handSize();
                }
                assertEquals(36, total);
                assertEquals(0, game.getDeckSize());
                if (game.getLoser() >= 0) {
                    assertTrue(game.getPlayer(game.getLoser()).handSize() > 0);
                }
            }
        }
    }

    @Test
    void actionsAfterGameOverAreRejected() {
        Game game = new Game(2, 5);
        RandomBot.playToEnd(game, new Random(5), 100000);
        assertTrue(game.isFinished());
        assertThrows(IllegalMoveException.class, game::take);
        assertThrows(IllegalMoveException.class, game::finishTurn);
    }

    @Test
    void lastEventOfFinishedGameIsGameOver() {
        Game game = new Game(2, 5);
        RandomBot.playToEnd(game, new Random(5), 100000);
        List<GameEvent> log = game.getEventLog();
        assertEquals(EventType.GAME_OVER, log.get(log.size() - 1).getType());
    }
}
