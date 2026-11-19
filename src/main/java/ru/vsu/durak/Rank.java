package ru.vsu.durak;

public enum Rank {
    SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), JACK("J"), QUEEN("Q"), KING("K"), ACE("A");

    private final String label;

    Rank(String label) {
        this.label = label;
    }

    String getLabel() {
        return label;
    }
}