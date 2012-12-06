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
    public Command createCommand() {
        Command command = new CommandOne();
        command.setUpdateQueue(getUpdateQueue());
        command.setLogger(getLogger());
        return  command;
    }
}
