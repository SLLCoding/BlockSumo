package emortal.bs.games;

import emortal.bs.Util.GamePosition;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private static final List<Game> games = new ArrayList<>();
    private static final Map<Player, Game> playerToGame = new HashMap<>();
    private static final List<GamePosition> gamePositions = new ArrayList<>();

    /**
     * Adds the player to the next available game.
     * @param player to join a game.
     * @return The game that the player joins. Returns null when a game cannot be found or created.
     */
    public static Game addPlayer(Player player) {
        for (Game game : games) {
            if (game.getGamers().size() < Game.maxPlayers) {
                game.addPlayer(player);
                playerToGame.put(player, game);
                return game;
            }
        }
        for (int x = -10; x < 10; x++)
            for (int y = -10; y < 10; y++) {
                final GamePosition pos = new GamePosition(x,y);
                if (gamePositions.contains(pos)) continue;
                final World w = Bukkit.getWorld("sumo");
                Game game = new Game(w, pos);
                games.add(game);
                gamePositions.add(pos);
                game.addPlayer(player);
                return game;
            }
        return null;
    }

    public static void removePlayer(Player player) {
        Game game = playerToGame.get(player);
        if (game == null) return;
        game.removePlayer(player);
    }

    public static Game getGame(Player player) {
        return playerToGame.get(player);
    }

    public static void shutdown() {
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("BlockSumo is restarting. We'll be back soon."));
        gamePositions.clear();
        games.clear();
    }

    public static List<Game> getGames() {
        return games;
    }

    public static List<GamePosition> getGamePositions() {
        return gamePositions;
    }

    public static Map<Player, Game> getPlayerToGame() {
        return playerToGame;
    }

}
