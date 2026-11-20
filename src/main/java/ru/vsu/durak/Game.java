package ru.vsu.durak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Механика подкидного дурака (2–5 игроков, колода 36 карт).
 *
 * <p>Класс хранит состояние партии и проверяет ходы. Он ничего не печатает:
 * каждый ход возвращает список событий, а состояние читается через геттеры.
 * Все случайности берутся из одного генератора {@link Random} с заданным зерном.</p>
 *
 * <p>Ход игры: атакующий кладёт карту ({@link #attack}), защитник отбивает
 * ({@link #defend}) или берёт ({@link #take}), остальные подкидывают карты того же
 * достоинства, что уже лежат на столе. Когда все закончили, вызывается
 * {@link #finishTurn()}: стол уходит в отбой или к защитнику, игроки добирают карты.</p>
 */
public class Game {
    /** Сколько карт должно быть у игрока после добора. */
    public static final int HAND_SIZE = 6;
    /** Максимум атакующих карт за один ход. */
    public static final int MAX_ATTACK_CARDS = 6;

    private final int playerCount;
    private final long seed;
    private final Random random;
    private final Deck deck;
    private final Suit trumpSuit;
    private final List<Player> players = new ArrayList<>();
    private final List<Card> discard = new ArrayList<>();
    private final List<TablePair> table = new ArrayList<>();
    private final List<GameEvent> log = new ArrayList<>();
    private final boolean[] outPlayers;

    private int attacker;
    private int defender;
    private boolean takeDeclared;
    private int attackLimit;
    private boolean finished;
    private int loser = -1;

    /**
     * Создаёт партию: тасует колоду, определяет козырь, раздаёт по 6 карт
     * и выбирает первого атакующего (владелец младшего козыря, иначе игрок 0).
     * События начала партии доступны через {@link #getEventLog()}.
     *
     * @param playerCount число игроков от 2 до 5
     * @param seed        зерно генератора: одно зерно — одна и та же партия
     * @throws IllegalArgumentException если игроков меньше 2 или больше 5
     */
    public Game(int playerCount, long seed) {
        if (playerCount < 2 || playerCount > 5) {
            throw new IllegalArgumentException("Игроков должно быть от 2 до 5");
        }
        this.playerCount = playerCount;
        this.seed = seed;
        this.random = new Random(seed);
        this.deck = new Deck();
        this.outPlayers = new boolean[playerCount];

        deck.shuffle(random);
        deck.fixTrump();
        trumpSuit = deck.getTrumpCard().getSuit();
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player(i));
        }

        List<GameEvent> start = new ArrayList<>();
        emit(start, new GameEvent(EventType.TRUMP_REVEALED, -1, deck.getTrumpCard(), null, 0));
        for (int round = 0; round < HAND_SIZE; round++) {
            for (Player p : players) {
                p.addCard(deck.draw());
            }
        }
        for (Player p : players) {
            emit(start, new GameEvent(EventType.DEAL, p.getIndex(), null, null, HAND_SIZE));
        }
        startTurn(start, findFirstAttacker());
    }

    // ------------------------------------------------------------------
    // Ходы
    // ------------------------------------------------------------------

    /**
     * Игрок кладёт карту на стол: первую карту хода — атакующий,
     * остальные (подкидывание) — любой игрок, кроме защитника.
     *
     * @param player номер игрока
     * @param card   карта из его руки
     * @return события, произошедшие в результате хода
     * @throws IllegalMoveException если ход запрещён правилами
     */
    public List<GameEvent> attack(int player, Card card) {
        String problem = attackProblem(player, card);
        if (problem != null) {
            throw new IllegalMoveException(problem);
        }
        players.get(player).removeCard(card);
        table.add(new TablePair(card));
        List<GameEvent> out = new ArrayList<>();
        emit(out, new GameEvent(EventType.ATTACK, player, card, null, 0));
        return out;
    }

    /**
     * Защитник бьёт одну из неотбитых карт на столе.
     *
     * @param attackCard  карта на столе, которую бьют
     * @param defenseCard карта из руки защитника
     * @return события хода
     * @throws IllegalMoveException если ход запрещён правилами
     */
    public List<GameEvent> defend(Card attackCard, Card defenseCard) {
        String problem = defendProblem(attackCard, defenseCard);
        if (problem != null) {
            throw new IllegalMoveException(problem);
        }
        for (TablePair pair : table) {
            if (!pair.isBeaten() && pair.getAttack().equals(attackCard)) {
                pair.setDefense(defenseCard);
                break;
            }
        }
        players.get(defender).removeCard(defenseCard);
        List<GameEvent> out = new ArrayList<>();
        emit(out, new GameEvent(EventType.DEFEND, defender, defenseCard, attackCard, 0));
        return out;
    }

    /**
     * Защитник объявляет, что берёт карты. После этого остальные игроки
     * могут ещё подкинуть карты (в пределах лимита), затем вызывается {@link #finishTurn()}.
     *
     * @return события хода
     * @throws IllegalMoveException если брать нечего или защитник уже взял
     */
    public List<GameEvent> take() {
        String problem = takeProblem();
        if (problem != null) {
            throw new IllegalMoveException(problem);
        }
        takeDeclared = true;
        List<GameEvent> out = new ArrayList<>();
        emit(out, new GameEvent(EventType.TAKE_DECLARED, defender, null, null, 0));
        return out;
    }

    /**
     * Завершает ход, когда подкидывать больше никто не хочет.
     * Если защитник брал — он забирает все карты со стола, иначе стол уходит в отбой
     * (для этого все карты должны быть отбиты). Затем игроки добирают карты
     * (атакующий, подкидывавшие по кругу, защитник последним), проверяется конец
     * партии и определяются роли следующего хода.
     *
     * @return события, произошедшие при завершении хода
     * @throws IllegalMoveException если стол пуст или есть неотбитые карты, а защитник не брал
     */
    public List<GameEvent> finishTurn() {
        String problem = finishProblem();
        if (problem != null) {
            throw new IllegalMoveException(problem);
        }
        List<GameEvent> out = new ArrayList<>();
        boolean took = takeDeclared;
        Player defenderPlayer = players.get(defender);

        int cardsOnTable = 0;
        for (TablePair pair : table) {
            cardsOnTable++;
            if (took) {
                defenderPlayer.addCard(pair.getAttack());
            } else {
                discard.add(pair.getAttack());
            }
            if (pair.isBeaten()) {
                cardsOnTable++;
                if (took) {
                    defenderPlayer.addCard(pair.getDefense());
                } else {
                    discard.add(pair.getDefense());
                }
            }
        }
        table.clear();
        if (took) {
            emit(out, new GameEvent(EventType.TOOK_CARDS, defender, null, null, cardsOnTable));
        } else {
            emit(out, new GameEvent(EventType.BEAT, -1, null, null, cardsOnTable));
        }

        refillHands(out, took);
        checkGameOver(out);
        if (!finished) {
            int next;
            if (took) {
                next = nextActivePlayer(defender);
            } else if (players.get(defender).handSize() > 0) {
                next = defender;
            } else {
                next = nextActivePlayer(defender);
            }
            startTurn(out, next);
        }
        return out;
    }

    // ------------------------------------------------------------------
    // Проверки и подсказки (для интерфейса и стратегий)
    // ------------------------------------------------------------------

    /**
     * Создаёт объект.
     *
     * @param player номер игрока
     * @param card   карта
     * @return true, если игрок может сейчас положить эту карту
     */
    public boolean canAttack(int player, Card card) {
        return attackProblem(player, card) == null;
    }

    /**
     * Создаёт объект.
     *
     * @param attackCard  карта на столе
     * @param defenseCard карта защитника
     * @return true, если защитник может сейчас отбить attackCard картой defenseCard
     */
    public boolean canDefend(Card attackCard, Card defenseCard) {
        return defendProblem(attackCard, defenseCard) == null;
    }

    /**
     * Создаёт объект.
     *
     * @param player номер игрока
     * @return карты из руки игрока, которые он может сейчас выложить
     */
    public List<Card> getAttackCandidates(int player) {
        List<Card> result = new ArrayList<>();
        if (player < 0 || player >= playerCount) {
            return result;
        }
        for (Card card : players.get(player).getHand()) {
            if (canAttack(player, card)) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * Создаёт объект.
     *
     * @param attackCard карта на столе
     * @return карты защитника, которыми можно отбить attackCard
     */
    public List<Card> getDefenseCandidates(Card attackCard) {
        List<Card> result = new ArrayList<>();
        if (finished) {
            return result;
        }
        for (Card card : players.get(defender).getHand()) {
            if (canDefend(attackCard, card)) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * Неотбитые атакующие карты на столе в порядке выкладывания.
     *
     * @return неотбитые атакующие карты на столе в порядке выкладывания
     */
    public List<Card> getUnbeatenCards() {
        List<Card> result = new ArrayList<>();
        for (TablePair pair : table) {
            if (!pair.isBeaten()) {
                result.add(pair.getAttack());
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Состояние
    // ------------------------------------------------------------------

    /**
     * Число игроков.
     *
     * @return число игроков
     */
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Зерно, с которым создана партия.
     *
     * @return зерно, с которым создана партия
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Генератор партии. Стратегии и тесты должны брать случайность только отсюда.
     *
     * @return единственный генератор случайных чисел партии
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Козырная масть.
     *
     * @return козырная масть
     */
    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    /**
     * Козырная карта (нижняя карта колоды в начале партии).
     *
     * @return козырная карта (нижняя карта колоды в начале партии)
     */
    public Card getTrumpCard() {
        return deck.getTrumpCard();
    }

    /**
     * Сколько карт осталось в колоде.
     *
     * @return сколько карт осталось в колоде
     */
    public int getDeckSize() {
        return deck.size();
    }

    /**
     * Сколько карт в отбое.
     *
     * @return сколько карт в отбое
     */
    public int getDiscardSize() {
        return discard.size();
    }

    /**
     * Создаёт объект.
     *
     * @param index номер игрока
     * @return игрок (его рука доступна только для чтения)
     */
    public Player getPlayer(int index) {
        return players.get(index);
    }

    /**
     * Пары карт на столе (только для чтения).
     *
     * @return пары карт на столе (только для чтения)
     */
    public List<TablePair> getTable() {
        return Collections.unmodifiableList(table);
    }

    /**
     * Номер атакующего в текущем ходу.
     *
     * @return номер атакующего в текущем ходу
     */
    public int getAttacker() {
        return attacker;
    }

    /**
     * Номер защитника в текущем ходу.
     *
     * @return номер защитника в текущем ходу
     */
    public int getDefender() {
        return defender;
    }

    /**
     * True, если защитник объявил, что берёт.
     *
     * @return true, если защитник объявил, что берёт
     */
    public boolean isTakeDeclared() {
        return takeDeclared;
    }

    /**
     * Максимум атакующих карт в текущем ходу:.
     *
     * @return максимум атакующих карт в текущем ходу:
     * меньшее из 6 и числа карт у защитника в начале хода
     */
    public int getAttackLimit() {
        return attackLimit;
    }

    /**
     * True, если партия закончена.
     *
     * @return true, если партия закончена
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Номер проигравшего (дурака); -1, если партия не закончена или закончилась вничью.
     *
     * @return номер проигравшего (дурака); -1, если партия не закончена или закончилась вничью
     */
    public int getLoser() {
        return loser;
    }

    /**
     * Все события партии с самого начала (только для чтения).
     *
     * @return все события партии с самого начала (только для чтения)
     */
    public List<GameEvent> getEventLog() {
        return Collections.unmodifiableList(log);
    }

    // ------------------------------------------------------------------
    // Внутренние методы
    // ------------------------------------------------------------------

    private void emit(List<GameEvent> out, GameEvent event) {
        out.add(event);
        log.add(event);
    }

    /** Возвращает причину запрета хода или null, если ход разрешён. */
    private String attackProblem(int player, Card card) {
        if (finished) {
            return "Партия уже закончена";
        }
        if (player < 0 || player >= playerCount) {
            return "Нет такого игрока";
        }
        if (player == defender) {
            return "Защитник не может атаковать";
        }
        if (!players.get(player).hasCard(card)) {
            return "У игрока нет этой карты";
        }
        if (table.isEmpty() && player != attacker) {
            return "Первую карту хода кладёт атакующий";
        }
        if (table.size() >= attackLimit) {
            return "Достигнут предел подкидывания";
        }
        if (!table.isEmpty() && !rankIsOnTable(card.getRank())) {
            return "Такого достоинства нет на столе";
        }
        return null;
    }

    private String defendProblem(Card attackCard, Card defenseCard) {
        if (finished) {
            return "Партия уже закончена";
        }
        if (takeDeclared) {
            return "Защитник уже берёт карты";
        }
        boolean found = false;
        for (TablePair pair : table) {
            if (!pair.isBeaten() && pair.getAttack().equals(attackCard)) {
                found = true;
            }
        }
        if (!found) {
            return "На столе нет такой неотбитой карты";
        }
        if (!players.get(defender).hasCard(defenseCard)) {
            return "У защитника нет этой карты";
        }
        if (!defenseCard.beats(attackCard, trumpSuit)) {
            return "Эта карта не бьёт атакующую";
        }
        return null;
    }

    private String takeProblem() {
        if (finished) {
            return "Партия уже закончена";
        }
        if (takeDeclared) {
            return "Защитник уже берёт карты";
        }
        if (getUnbeatenCards().isEmpty()) {
            return "Нет неотбитых карт, брать нечего";
        }
        return null;
    }

    private String finishProblem() {
        if (finished) {
            return "Партия уже закончена";
        }
        if (table.isEmpty()) {
            return "Стол пуст";
        }
        if (!takeDeclared && !getUnbeatenCards().isEmpty()) {
            return "Есть неотбитые карты: защитник должен отбиться или взять";
        }
        return null;
    }

    private boolean rankIsOnTable(Rank rank) {
        for (TablePair pair : table) {
            if (pair.getAttack().getRank() == rank) {
                return true;
            }
            if (pair.isBeaten() && pair.getDefense().getRank() == rank) {
                return true;
            }
        }
        return false;
    }

    /** Первый атакующий — владелец младшего козыря; если козырей ни у кого нет — игрок 0. */
    private int findFirstAttacker() {
        int best = 0;
        int bestRank = 100;
        for (Player p : players) {
            for (Card card : p.getHand()) {
                if (card.getSuit() == trumpSuit && card.getRank().ordinal() < bestRank) {
                    bestRank = card.getRank().ordinal();
                    best = p.getIndex();
                }
            }
        }
        return best;
    }

    /** Следующий после from игрок, у которого есть карты; -1, если таких нет. */
    private int nextActivePlayer(int from) {
        for (int step = 1; step <= playerCount; step++) {
            int index = (from + step) % playerCount;
            if (players.get(index).handSize() > 0) {
                return index;
            }
        }
        return -1;
    }

    private void startTurn(List<GameEvent> out, int newAttacker) {
        attacker = newAttacker;
        defender = nextActivePlayer(attacker);
        takeDeclared = false;
        attackLimit = Math.min(MAX_ATTACK_CARDS, players.get(defender).handSize());
        emit(out, new GameEvent(EventType.TURN_START, attacker, null, null, defender));
    }

    /** Добор: атакующий, затем остальные по кругу, защитник последним (если он не брал). */
    private void refillHands(List<GameEvent> out, boolean defenderTook) {
        for (int step = 0; step < playerCount; step++) {
            int index = (attacker + step) % playerCount;
            if (index != defender) {
                drawUpToSix(out, index);
            }
        }
        if (!defenderTook) {
            drawUpToSix(out, defender);
        }
    }

    private void drawUpToSix(List<GameEvent> out, int index) {
        Player p = players.get(index);
        int drawn = 0;
        while (p.handSize() < HAND_SIZE && !deck.isEmpty()) {
            p.addCard(deck.draw());
            drawn++;
        }
        if (drawn > 0) {
            emit(out, new GameEvent(EventType.DRAW, index, null, null, drawn));
        }
    }

    /** Когда колода пуста, игроки без карт выходят; остался один (или никого) — партия окончена. */
    private void checkGameOver(List<GameEvent> out) {
        if (!deck.isEmpty()) {
            return;
        }
        int withCards = 0;
        int lastWithCards = -1;
        for (Player p : players) {
            if (p.handSize() > 0) {
                withCards++;
                lastWithCards = p.getIndex();
            } else if (!outPlayers[p.getIndex()]) {
                outPlayers[p.getIndex()] = true;
                emit(out, new GameEvent(EventType.PLAYER_OUT, p.getIndex(), null, null, 0));
            }
        }
        if (withCards <= 1) {
            finished = true;
            loser = lastWithCards;
            emit(out, new GameEvent(EventType.GAME_OVER, loser, null, null, 0));
        }
    }
}
