package please.help.commands;

import please.help.*;

import java.util.LinkedList;

/**
 * Класс для комманды clear.
 * Формат комманды: clear
 */
public class Clear extends Command{

    private static final long serialVersionUID = 20200916L;

    public Clear(){
        commandName = "clear";
    }

    @Override
    public Clear validateCommand(LinkedList<String[]> data, ClientManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        Clear clear = new Clear();
        clear.makeValid();
        return clear;
    }
}
