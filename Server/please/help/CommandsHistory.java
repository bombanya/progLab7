package please.help;

import please.help.commands.Command;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class CommandsHistory{

    private final ConcurrentHashMap<String, ArrayList<String>> history = new ConcurrentHashMap<>();

    public void addCommand(Command c, String login){
        history.putIfAbsent(login, new ArrayList<>());
        synchronized (history.get(login)) {
            history.get(login).add(c.getCommandName());
            if (history.get(login).size() > 5) history.get(login).remove(0);
        }
    }

    public String printUserCommands(String login){
        if (history.containsKey(login)) {
            synchronized (history.get(login)) {
                StringBuilder res = new StringBuilder();
                history.get(login).forEach(p -> res.append(p).append("\n"));
                return res.toString();
            }
        }
        else return "";
    }
}
