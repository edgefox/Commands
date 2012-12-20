package commands.abstracts;

import commands.ExecutionResult;
import org.apache.commons.logging.Log;

import java.util.concurrent.BlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 12/6/12
 * Time: 2:01 PM
 */
public abstract class CommandFactory {
    private BlockingQueue<ExecutionResult> updateQueue;
    private Log logger;

    public CommandFactory() {
    }

    public CommandFactory(BlockingQueue<ExecutionResult> updateQueue, Log logger) {
        this.updateQueue = updateQueue;
        this.logger = logger;
    }

    public BlockingQueue<ExecutionResult> getUpdateQueue() {
        return updateQueue;
    }

    public Log getLogger() {
        return logger;
    }

    public void setUpdateQueue(BlockingQueue<ExecutionResult> updateQueue) {
        this.updateQueue = updateQueue;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public abstract Command createCommand(int id, String name, Command.Status status);
}
