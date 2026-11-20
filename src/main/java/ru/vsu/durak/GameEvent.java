package ru.vsu.durak;

/**
 * Событие партии. Неизменяемый объект; ненужные поля равны -1 или null.
 */
public final class GameEvent {
    private final EventType type;
    private final int player;
    private final Card card;
    private final Card otherCard;
    private final int count;

    GameEvent(EventType type, int player, Card card, Card otherCard, int count) {
        this.type = type;
        this.player = player;
        this.card = card;
        this.otherCard = otherCard;
        this.count = count;
    }

    /**
     * Вид события.
     *
     * @return вид события
     */
    public EventType getType() {
        return type;
    }

    /**
     * Номер игрока, к которому относится событие, или -1.
     *
     * @return номер игрока, к которому относится событие, или -1
     */
    public int getPlayer() {
        return player;
    }

    /**
     * Основная карта события или null.
     *
     * @return основная карта события или null
     */
    public Card getCard() {
        return card;
    }

    /**
     * Вторая карта события (для DEFEND — отбитая карта) или null.
     *
     * @return вторая карта события (для DEFEND — отбитая карта) или null
     */
    public Card getOtherCard() {
        return otherCard;
    }

    /**
     * Количество карт (DEAL, DRAW, TOOK_CARDS, BEAT) или номер защитника для TURN_START.
     *
     * @return количество карт (DEAL, DRAW, TOOK_CARDS, BEAT) или номер защитника для TURN_START
     */
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return type + " player=" + player + " card=" + card + " other=" + otherCard + " count=" + count;
    }
}
