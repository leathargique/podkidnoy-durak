package ru.vsu.durak;

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

    public EventType getType() {
        return type;
    }
    public int getPlayer() {
        return player;
    }
    public Card getCard() {
        return card;
    }
    public Card getOtherCard() {
        return otherCard;
    }
    public int getCount() {
        return count;
    }
    
    @Override
    public String toString() {
        return  type +
                ", player=" + player +
                ", card=" + card +
                ", otherCard=" + otherCard +
                ", count=" + count;
    }

}