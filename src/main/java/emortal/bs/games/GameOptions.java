package emortal.bs.games;

public class GameOptions {

    /**
     * IMPORTANT NOTE: All variables must have a default value.
     */
    private boolean isPrivate = false;
    private boolean skyBorder = true;
    private int maxPlayers = 12;
    private int diamondBlockTimer = 20;
    private int tntRainTimer = 8;
    private int respawnTime = 5;
    private int gameStartTimer = 10;
    private int midSpawnTimer = 40;
    private int everywhereSpawnTimer = 30;
    private int startingLives = 5;

    public boolean isPrivate() {
        return isPrivate;
    }

    public GameOptions setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
        return this;
    }

    public boolean hasSkyBorder() {
        return skyBorder;
    }

    public GameOptions setSkyBorder(boolean hasSkyBorder) {
        this.skyBorder = hasSkyBorder;
        return this;
    }

    public GameOptions setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public GameOptions setDiamondBlockTimer(int diamondBlockTimer) {
        this.diamondBlockTimer = diamondBlockTimer;
        return this;
    }

    public int getDiamondBlockTimer() {
        return diamondBlockTimer;
    }

    public int getEverywhereSpawnTimer() {
        return everywhereSpawnTimer;
    }

    public GameOptions setEverywhereSpawnTimer(int everywhereSpawnTimer) {
        this.everywhereSpawnTimer = everywhereSpawnTimer;
        return this;
    }

    public int getGameStartTimer() {
        return gameStartTimer;
    }

    public GameOptions setGameStartTimer(int gameStartTimer) {
        this.gameStartTimer = gameStartTimer;
        return this;
    }

    public int getMidSpawnTimer() {
        return midSpawnTimer;
    }

    public GameOptions setMidSpawnTimer(int midSpawnTimer) {
        this.midSpawnTimer = midSpawnTimer;
        return this;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public GameOptions setRespawnTime(int respawnTime) {
        this.respawnTime = respawnTime;
        return this;
    }

    public int getTntRainTimer() {
        return tntRainTimer;
    }

    public GameOptions setTntRainTimer(int tntRainTimer) {
        this.tntRainTimer = tntRainTimer;
        return this;
    }

    public int getStartingLives() {
        return startingLives;
    }

    public GameOptions setStartingLives(int startingLives) {
        this.startingLives = startingLives;
        return this;
    }

}
