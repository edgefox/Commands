package commands;

import commands.abstracts.Command;
import org.apache.commons.logging.Log;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 3:06 PM
 */
public class CommandOne extends Command {

    public CommandOne(int id, String name, Status status) {
        super(id, name, status);
    }

    public CommandOne(int id, String name, Status status, Log logger) {
        super(id, name, status, logger);
    }

    @Override
    public void submitResult(ExecutionResult result) {
        updateQueue.add(result);
    }
}
