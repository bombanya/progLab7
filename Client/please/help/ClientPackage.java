package please.help;

import please.help.commands.Command;

import java.io.Serializable;

public class ClientPackage implements Serializable {

    private static final long serialVersionUID = 20201016L;
    private final Command commandToExecute;
    private final String login;
    private final String password;

    public ClientPackage(Command commandToExecute, String login, String password){
        this.commandToExecute = commandToExecute;
        this.login = login;
        this.password = password;
    }

    public Command getCommandToExecute() {
        return commandToExecute;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
