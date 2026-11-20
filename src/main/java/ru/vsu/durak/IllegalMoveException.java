package ru.vsu.durak;

/**
 * Попытка сделать ход, запрещённый правилами. Сообщение объясняет причину.
 */
public class IllegalMoveException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Создаёт объект.
     *
     * @param message причина отказа
     */
    public IllegalMoveException(String message) {
        super(message);
    }
}