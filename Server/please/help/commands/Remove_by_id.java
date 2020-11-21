package please.help.commands;

import please.help.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Класс для комманды remove_by_id.
 * Формат комманды: remove_by_id id
 */
public class Remove_by_id extends Command{

    private static final long serialVersionUID = 20200916L;
    private Long id;

    public Remove_by_id(){
        commandName = "remove_by_id";
    }

    private Remove_by_id(Long id){
        commandName = "remove_by_id";
        this.id = id;
    }

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        try{
            String sql = "DELETE FROM " + manager.collectionShell.getOrgsTableName()
                    + " WHERE id = ? AND owner = ?";
            PreparedStatement st = manager.collectionShell.createPreparedStatement(sql);
            if (st == null) return "Элементы не удалены - ошибка при формировании запроса к таблице.";
            else {
                st.setString(2, login);
                st.setLong(1, id);
                synchronized (manager.collectionShell) {
                    st.execute();
                    st.close();
                    boolean result = manager.collectionShell.collection
                            .removeIf(p -> p.getOwner().equals(login) && p.getId().equals(id));
                    if (result) return "Элемент удален.";
                    else return "Элемент не удален - элемент с таким id не существует " +
                            "или он не принадлежит пользователю.";
                }
            }

        }
        catch (SQLException e){
            return "Элементы не удалены - ошибка при обращении к таблице.";
        }
    }

    @Override
    public Remove_by_id validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.peek().length != 2) {
            System.out.println("Неверно введена комманда.");
            data.poll();
            return null;
        }

        String[] polledCommand = data.poll();
        try {
            Long id = Long.parseLong(polledCommand[1]);
            Remove_by_id remove_by_id = new Remove_by_id(id);
            remove_by_id.makeValid();
            return remove_by_id;
        }
        catch (NumberFormatException e){
            System.out.println("Команда должна вводиться вместе со значением типа long.");
            return null;
        }
    }
}
