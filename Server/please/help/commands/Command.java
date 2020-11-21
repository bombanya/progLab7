package please.help.commands;

import please.help.*;

import java.io.Serializable;
import java.util.LinkedList;

public abstract class Command implements Serializable {

    private static final long serialVersionUID = 20200916L;
    protected String commandName;
    private boolean valid = false;

    public String getCommandName() {
        return commandName;
    }

    public abstract Object execute(CollectionManager manager, String login, String password);

    protected void makeValid(){
        valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public abstract Command validateCommand(LinkedList<String[]> data, CollectionManager manager);
}
