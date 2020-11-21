package please.help.commands;

import please.help.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    public String execute(CollectionManager manager, String login, String password) {
        try {
            String sql = "DELETE FROM " + manager.collectionShell.getOrgsTableName() + " WHERE owner = ?";
            PreparedStatement st = manager.collectionShell.createPreparedStatement(sql);
            if (st == null) return "Элементы не удалены - ошибка при формировании запроса к таблице.";
            else {
                st.setString(1, login);
                synchronized (manager.collectionShell) {
                    st.execute();
                    st.close();
                    manager.collectionShell.collection.removeIf(p -> p.getOwner().equals(login));
                    return "Все элементы, принадлежащие пользователю, удалены.";
                }
            }
        }
        catch (SQLException e){
            return "Элементы не удалены - ошибка при обращении к таблице.";
        }
    }

    @Override
    public Clear validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        Clear clear = new Clear();
        clear.makeValid();
        return clear;
    }
}
