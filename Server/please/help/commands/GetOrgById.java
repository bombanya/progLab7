package please.help.commands;

import please.help.CollectionManager;
import please.help.organizationBuilding.Organization;

import java.util.LinkedList;

public class GetOrgById extends Command{

    private static final long serialVersionUID = 20200916L;
    private final Long id;

    public GetOrgById(Long id){
        commandName = "getOrgById";
        this.id = id;
    }

    @Override
    public Organization execute(CollectionManager manager, String login, String password) {
        for (Organization org : manager.collectionShell.collection){
            if (org.getId().equals(id)) {
                if (org.getOwner().equals(login)) return org;
                else return new Organization(null, null, 0, null, null);
            }
        }
        return new Organization(null, null, 0, null, null);
    }

    @Override
    public GetOrgById validateCommand(LinkedList<String[]> data, CollectionManager manager){
        return null;
    }
}
