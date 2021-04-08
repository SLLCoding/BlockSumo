package emortal.bs.games;

public enum GameState {

    WAITING(false),
    STARTING(false),
    PLAYING(true),
    ENDING(false);

    private final boolean ongoing;

    GameState(boolean ongoing) {
        this.ongoing = ongoing;
    }

    public boolean isOngoing() {
        return ongoing;
    }

}
