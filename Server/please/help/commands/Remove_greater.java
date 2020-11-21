package please.help.commands;

import please.help.*;
import please.help.organizationBuilding.ConsoleValidator;
import please.help.organizationBuilding.Organization;
import please.help.organizationBuilding.ScriptValidator;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Класс для комманды remove_greater.
 * Формат комманды: remove_greater
 */
public class Remove_greater extends Command{

    private static final long serialVersionUID = 20200916L;
    private Organization org;

    public Remove_greater(){
        commandName = "remove_greater";
    }

    private Remove_greater(Organization org){
        commandName = "remove_greater";
        this.org = org;
    }

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        try{
            String sql = "DELETE FROM " + manager.collectionShell.getOrgsTableName() +
                    " WHERE annualturnover > ? AND owner = ?";
            PreparedStatement st = manager.collectionShell.createPreparedStatement(sql);
            if (st == null) return "Элементы не удалены - ошибка при формировании запроса к таблице.";
            else {
                st.setString(2, login);
                st.setDouble(1, org.getAnnualTurnover());
                synchronized (manager.collectionShell) {
                    st.execute();
                    st.close();
                    manager.collectionShell.collection
                            .removeIf(p -> p.getOwner().equals(login) && p.compareTo(org) > 0);
                    return "Элементы удалены.";
                }
            }
        }
        catch (SQLException e){
            return "Элементы не удалены - ошибка при обращении к таблице.";
        }
    }

    @Override
    public Remove_greater validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        if (manager.getManagerMode() == Mode.CONSOLE) org = ConsoleValidator.buildFromConsole(null);
        else org = ScriptValidator.buildFromScript(null, data, manager);
        Remove_greater remove_greater = new Remove_greater(org);
        if (org != null) remove_greater.makeValid();
        return remove_greater;
    }
}
