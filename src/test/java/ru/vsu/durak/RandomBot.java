package ru.vsu.durak;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBot {
        public static int playToEnd(Game game, Random botRandom, int maxSteps) {
        int steps = 0;
        while (!game.isFinished() && steps < maxSteps) {
            steps++;
            if (game.getTable().isEmpty()) {
                List<Card> cards = game.getAttackCandidates(game.getAttacker());
                game.attack(game.getAttacker(), cards.get(botRandom.nextInt(cards.size())));
            } else if (!game.isTakeDeclared() && !game.getUnbeatenCards().isEmpty()) {
                Card target = game.getUnbeatenCards().get(0);
                List<Card> options = game.getDefenseCandidates(target);
                if (options.isEmpty() || botRandom.nextInt(5) == 0) {
                    game.take();
                } else {
                    game.defend(target, options.get(botRandom.nextInt(options.size())));
                }
            } else {
                tryThrowOrFinish(game, botRandom);
            }
        }
        return steps;
    }

    private static void tryThrowOrFinish(Game game, Random botRandom) {
        List<Integer> throwers = new ArrayList<>();
        List<Card> cards = new ArrayList<>();
        for (int p = 0; p < game.getPlayerCount(); p++) {
            for (Card card : game.getAttackCandidates(p)) {
                throwers.add(p);
                cards.add(card);
            }
        }
        if (!cards.isEmpty() && botRandom.nextInt(2) == 0) {
            int i = botRandom.nextInt(cards.size());
            game.attack(throwers.get(i), cards.get(i));
        } else {
            game.finishTurn();
        }
    }
}
