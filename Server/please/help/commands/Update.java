package please.help.commands;

import please.help.*;
import please.help.organizationBuilding.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    public String  execute(CollectionManager manager, String login, String password) {
        try{
            String sql = "UPDATE " + manager.collectionShell.getOrgsTableName() +
                    " SET name = ?, coordinatex = ?, coordinatey = ?, annualturnover = ?," +
                    " organizationtype = ?, street = ?, zipcode = ? WHERE id = ? AND owner = ?";
            PreparedStatement st = manager.collectionShell.createPreparedStatement(sql);
            if (st == null) return "Элемент не обновлен - ошибка при формировании запроса к таблице.";
            st.setString(1, org.getName());
            st.setInt(2, org.getCoordinates().getX());
            st.setInt(3, org.getCoordinates().getY());
            st.setDouble(4, org.getAnnualTurnover());
            st.setString(5, org.getType().toString());
            st.setString(6, org.getOfficialAddress().getStreet());
            st.setString(7, org.getOfficialAddress().getZipCode());
            st.setLong(8, id);
            st.setString(9, login);
            synchronized (manager.collectionShell) {
                st.execute();
                st.close();
                boolean result = manager.collectionShell.collection
                        .removeIf(p -> p.getOwner().equals(login) && p.getId().equals(id));
                if (!result) return "Элемент не обновлен - нет элемента с таким id.";
                else {
                    org.setOwner(login);
                    manager.collectionShell.collection.add(org);
                    return "Элемент обновлен.";
                }
            }
        }
        catch (SQLException e){
            return "Элемент не обновлен - ошибка при обращении к таблице.";
        }
    }

    @Override
    public Update validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.peek().length != 2) {
            System.out.println("Неверно введена комманда.");
            data.poll();
            return null;
        }

        String[] polledCommand = data.poll();

        try {
            id = Long.parseLong(polledCommand[1]);
            Organization oldOrg = (new GetOrgById(id)).execute(manager, manager.getLogin(), manager.getPassword());
            if (oldOrg.getId() != null){
                if (manager.getManagerMode() == Mode.CONSOLE) org = ConsoleValidator.buildFromConsole(oldOrg);
                else org = ScriptValidator.buildFromScript(oldOrg, data, manager);
                Update update = new Update(id, org);
                if (org != null) update.makeValid();
                else System.out.println("Элемент не был изменен.");
                return update;
            }
            else{
                System.out.println("Элемент с таким id не существует или он не принадлежит пользователю.");
                return new Update();
            }
        }
        catch (NumberFormatException e){
            System.out.println("Команда должна вводиться вместе со значением типа long.");
            return null;
        }
    }
}
