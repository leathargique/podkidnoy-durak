package ru.vsu.durak;

/**
 * Виды событий партии.
 */
public enum EventType {
    TRUMP_REVEALED,
    DEAL,
    TURN_START,
    ATTACK,
    DEFEND,
    TAKE_DECLARED,
    TOOK_CARDS,
    BEAT,
    DRAW,
    PLAYER_OUT,
    GAME_OVER
}