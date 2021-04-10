package emortal.bs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Set;

public class CommandManager {

    private static CommandMap commandMap;
    private static final String key = "blocksumo";

    public static void init() throws NoSuchFieldException, IllegalAccessException {
        commandMap = getCommandMap();

        Reflections reflections = new Reflections("emortal.bs.commands.impl");

        Set<Class<? extends Command>> allCommands =
                reflections.getSubTypesOf(Command.class);

        for (Class<? extends Command> commandClass : allCommands) {
            try {
                Constructor<? extends Command> constructor = commandClass.getConstructor();
                constructor.setAccessible(true);
                commandMap.register(key, constructor.newInstance());
            } catch (Exception e) {
                System.err.println("Invalid command: " + commandClass.getSimpleName());
            }
        }
    }

    private static CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);
        return (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
    }

}
