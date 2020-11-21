package please.help.commands;

import please.help.*;
import please.help.organizationBuilding.Organization;

import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Класс для комманды average_of_annual_turnover.
 * Формат комманды: average_of_annual_turnover
 */
public class Average_of_annual_turnover extends Command{

    private static final long serialVersionUID = 20200916L;

    public Average_of_annual_turnover(){
        commandName = "average_of_annual_turnover";
    }

    @Override
    public String execute(CollectionManager manager, String login, String password) {
        double average = manager.collectionShell.collection.stream()
                .collect(Collectors.averagingDouble(Organization::getAnnualTurnover));
        return "Среднее значение поля annualTurnover для всех элементов коллекции: " + average;
    }

    @Override
    public Average_of_annual_turnover validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }
        Average_of_annual_turnover average_of_annual_turnover = new Average_of_annual_turnover();
        average_of_annual_turnover.makeValid();
        return average_of_annual_turnover;
    }
}
