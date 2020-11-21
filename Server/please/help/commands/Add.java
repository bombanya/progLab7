package please.help.commands;

import please.help.*;
import please.help.organizationBuilding.*;

import java.util.LinkedList;

public class Add extends Command{

    private static final long serialVersionUID = 20200916L;
    private Organization org;

    public Add(){
        commandName = "add";
    }

    private Add(Organization org){
        commandName = "add";
        this.org = org;
    }

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        if (manager.collectionShell.insertNewOrg(org, login)) return "Элемент добавлен";
        else return "Элемент не добавлен - произошла ошибка при добавлении элемента в таблицу.";
    }

    @Override
    public Add validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        if (manager.getManagerMode() == Mode.CONSOLE) org = ConsoleValidator.buildFromConsole(null);
        else org = ScriptValidator.buildFromScript(null, data, manager);
        Add add = new Add(org);
        if (org != null) add.makeValid();
        return add;
    }
}
