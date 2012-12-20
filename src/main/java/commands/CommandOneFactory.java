package commands;

import commands.abstracts.Command;
import commands.abstracts.CommandFactory;

/**
 * User: Ivan Lyutov
 * Date: 12/6/12
 * Time: 2:24 PM
 */
public class CommandOneFactory extends CommandFactory {

    @Override
    public Command createCommand(int id, String name, Command.Status status) {
        Command command = new CommandOne();
        command.setId(id);
        command.setName(name);
        command.setStatus(status);
        command.setUpdateQueue(getUpdateQueue());
        command.setLogger(getLogger());
        return  command;
    }
}
