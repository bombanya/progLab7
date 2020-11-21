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
    public ArrayList<Organization> execute(CollectionManager manager, String login, String password) {
        return new ArrayList<>(manager.collectionShell.collection);
    }

    @Override
    public Show validateCommand(LinkedList<String[]> data, CollectionManager manager) {
        if (data.size() == 0 || data.poll().length > 1) {
            System.out.println("Неверно введена комманда.");
            return null;
        }

        String[] fields = new String[]{"id", "name", "coordinatex", "coordinatey", "creationdate", "annualturnover"
                , "organizationtype", "street", "zipcode", "owner"};
        int[] lengthsForPrinting = new int[10];
        StringBuilder formatter = new StringBuilder();
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < 10; i++){
            int forI = i;
            int maxFromOrgs = manager.collectionShell.collection.stream()
                    .mapToInt(p -> p.getLengthsOfFields()[forI]).max().orElse(0);
            lengthsForPrinting[i] = Math.max(maxFromOrgs, fields[i].length());
            formatter.append(" %").append(lengthsForPrinting[i]).append("s |");
            line.append(String.join("", Collections.nCopies(lengthsForPrinting[i] + 2, "-")))
                    .append("+");
        }

        System.out.printf(formatter.toString(), fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]
                , fields[6], fields[7], fields[8], fields[9]);
        System.out.println("\n" + line.toString());
        for (Organization org : manager.collectionShell.collection){
            System.out.printf(formatter.toString(), org.getId(), org.getName(), org.getCoordinates().getX()
                    , org.getCoordinates().getY(), org.getCreationDate(), org.getAnnualTurnover(), org.getType()
                    , org.getOfficialAddress().getStreet(), org.getOfficialAddress().getZipCode(), org.getOwner());
            System.out.println();
        }
        return new Show();
    }
}
