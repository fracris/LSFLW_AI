package it.unical.model;

public abstract sealed class Difficulty
        permits Difficulty.Easy, Difficulty.Medium, Difficulty.Hard {

    private Difficulty() { }

    public static final class Easy extends Difficulty {
        public Easy() { }
        @Override public String toString() { return "Easy"; }
    }

    public static final class Medium extends Difficulty {
        public Medium() { }
        @Override public String toString() { return "Medium"; }
    }

    public static final class Hard extends Difficulty {
        public Hard() { }
        @Override public String toString() { return "Hard"; }
    }

    public static Difficulty easy()   { return new Easy(); }
    public static Difficulty medium() { return new Medium(); }
    public static Difficulty hard()   { return new Hard(); }
}
