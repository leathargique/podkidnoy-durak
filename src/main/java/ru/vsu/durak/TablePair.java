package ru.vsu.durak;

public class TablePair {
    private final Card attack;
    private Card defense;

    TablePair(Card attack) {
        this.attack = attack;
    }

    public Card getAttack() {
        return attack;
    }

    public Card getDefense() {
        return defense;
    }

    public boolean isBeaten() {
        return defense != null;
    }

    void setDefense(Card defense) {
        this.defense = defense;
    }
}