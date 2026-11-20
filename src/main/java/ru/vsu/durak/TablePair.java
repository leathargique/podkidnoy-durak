package ru.vsu.durak;

/**
 * Пара на столе: атакующая карта и (если уже отбита) карта защитника.
 */
public class TablePair {
    private final Card attack;
    private Card defense;

    TablePair(Card attack) {
        this.attack = attack;
    }

    /**
     * Карта атаки.
     *
     * @return карта атаки
     */
    public Card getAttack() {
        return attack;
    }

    /**
     * Карта защиты или null, если пара ещё не отбита.
     *
     * @return карта защиты или null, если пара ещё не отбита
     */
    public Card getDefense() {
        return defense;
    }

    /**
     * True, если атака отбита.
     *
     * @return true, если атака отбита
     */
    public boolean isBeaten() {
        return defense != null;
    }

    void setDefense(Card defense) {
        this.defense = defense;
    }
}
