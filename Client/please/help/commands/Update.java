package please.help.commands;

import please.help.*;
import please.help.organizationBuilding.ConsoleValidator;
import please.help.organizationBuilding.Organization;
import please.help.organizationBuilding.ScriptValidator;

import java.util.LinkedList;

/**
 * Класс для комманды update.
 * Формат комманды: update id
 */
public class Update extends Command{

    private static final long serialVersionUID = 20200916L;
    private Long id;
    private Organization org;

    public Update(){
        commandName = "update";
    }

    private Update(Long id, Organization org){
        commandName = "update";
        this.id = id;
        this.org = org;
    }

    @Override
    public Update validateCommand(LinkedList<String[]> data, ClientManager manager) {
        if (data.size() == 0 || data.peek().length != 2) {
            System.out.println("Неверно введена комманда.");
            data.poll();
            return null;
        }

        String[] polledCommand = data.poll();

        try {
            id = Long.parseLong(polledCommand[1]);
            Object oldOrg = manager.getClient().sendCommand(new GetOrgById(id), manager.getLogin()
                    , manager.getPassword());
            if (oldOrg instanceof Organization){
                if (((Organization) oldOrg).getId() == null){
                    System.out.println("Элемент с таким id не существует или он не принадлежит пользователю.");
                    return new Update();
                }
                if (manager.getManagerMode() == Mode.CONSOLE) org = ConsoleValidator
                        .buildFromConsole((Organization) oldOrg);
                else org = ScriptValidator.buildFromScript((Organization) oldOrg, data, manager);
                Update update = new Update(id, org);
                if (org != null) update.makeValid();
                else System.out.println("Элемент не был изменен.");
                return update;
            }
            else return new Update();
        }
        catch (NumberFormatException e){
            System.out.println("Команда должна вводиться вместе со значением типа long.");
            return null;
        }
    }
}
