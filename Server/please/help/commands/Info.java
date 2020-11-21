package please.help.commands;

import please.help.*;

import java.util.LinkedList;


/**
 * Класс для комманды info.
 * Формат комманды: info
 */
public class Info extends Command{

    private static final long serialVersionUID = 20200916L;

    public Info(){
        commandName = "info";
    }

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        return "Информация о коллекции:\n" +
                "Тип: ConcurrentLinkedQueue<Organization>\n" +
                "Количество элементов: " + manager.collectionShell.collection.size() +
                "\nТаблица с организациями: " + manager.collectionShell.getOrgsTableName() +
                "\nТаблица с данными пользователей: " + manager.authorizer.getTableName();
    }

    @Override
    public Info validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        Info info = new Info();
        info.makeValid();
        return info;
    }
}
