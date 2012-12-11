package commands;

import commands.abstracts.Command;
import org.apache.commons.logging.Log;

import java.util.concurrent.BlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 3:06 PM
 */
public class CommandOne extends Command {

    public CommandOne() {
    }

    public CommandOne(int id, String name, Status status, BlockingQueue<ExecutionResult> updateQueue) {
        super(id, name, status, updateQueue);
    }

    public CommandOne(int id, String name, Status status, BlockingQueue<ExecutionResult> updateQueue, Log logger) {
        super(id, name, status, updateQueue, logger);
    }

    @Override
    public void submitResult(ExecutionResult result) {
        try {
            updateQueue.put(result);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
