package please.help.commands;

import please.help.*;
import please.help.organizationBuilding.Organization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Класс для комманды show.
 * Формат комманды: show
 */
public class Show extends Command{

    private static final long serialVersionUID = 20200916L;

    public Show(){
        commandName = "show";
    }


    @Override
    public Show validateCommand(LinkedList<String[]> data, ClientManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }

        Object feedbackFromServer = manager.getClient().sendCommand(new Show(), manager.getLogin(), manager.getPassword());
        if (feedbackFromServer instanceof String) System.out.println(feedbackFromServer);
        else if (feedbackFromServer instanceof ArrayList<?>){
            ArrayList<Organization> orgs = new ArrayList<>();

            try {
                ((ArrayList<?>) feedbackFromServer).forEach(p -> orgs.add((Organization) p));
            }
            catch (ClassCastException e){
                System.out.println("Невозможно распечатать коллекцию - некорректный ответ от сервера.");
                return new Show();
            }

            String[] fields = new String[]{"id", "name", "coordinatex", "coordinatey", "creationdate", "annualturnover"
                    , "organizationtype", "street", "zipcode", "owner"};
            int[] lengthsForPrinting = new int[10];
            StringBuilder formatter = new StringBuilder();
            StringBuilder line = new StringBuilder();

            for (int i = 0; i < 10; i++){
                int forI = i;
                int maxFromOrgs = orgs.stream().mapToInt(p -> p.getLengthsOfFields()[forI]).max().orElse(0);
                lengthsForPrinting[i] = Math.max(maxFromOrgs, fields[i].length());
                formatter.append(" %").append(lengthsForPrinting[i]).append("s |");
                line.append(String.join("", Collections.nCopies(lengthsForPrinting[i] + 2, "-")))
                        .append("+");
            }

            System.out.printf(formatter.toString(), fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]
                    , fields[6], fields[7], fields[8], fields[9]);
            System.out.println("\n" + line.toString());
            for (Organization org : orgs){
                System.out.printf(formatter.toString(), org.getId(), org.getName(), org.getCoordinates().getX()
                        , org.getCoordinates().getY(), org.getCreationDate(), org.getAnnualTurnover(), org.getType()
                        , org.getOfficialAddress().getStreet(), org.getOfficialAddress().getZipCode(), org.getOwner());
                System.out.println();
            }
        }
        else{
            System.out.println("Некорректный ответ от сервера.");
        }
        return new Show();
    }
}
