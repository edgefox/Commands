package commands.abstracts;

import commands.ExecutionResult;
import org.apache.commons.logging.Log;

import java.util.concurrent.BlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 12/5/12
 * Time: 3:30 PM
 */
public abstract class Command implements Runnable {
    protected int id;
    protected String name;
    protected Status status;
    protected Log logger;
    protected volatile BlockingQueue<ExecutionResult> updateQueue;

    public Command() {
    }

    public Command(int id, String name, Status status, BlockingQueue<ExecutionResult> updateQueue) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.updateQueue = updateQueue;
    }

    public Command(int id, String name, Status status, BlockingQueue<ExecutionResult> updateQueue, Log logger) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.updateQueue = updateQueue;
        this.logger = logger;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public BlockingQueue<ExecutionResult> getUpdateQueue() {
        return updateQueue;
    }

    public Log getLogger() {
        return logger;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setUpdateQueue(BlockingQueue<ExecutionResult> updateQueue) {
        this.updateQueue = updateQueue;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public static enum Status {
        NEW("NEW"), IN_PROGRESS("IN_PROGRESS"), DONE("DONE");

        private String value;

        private Status(final String value) {
            this.value = value;
        }
    }

    public void run() {
        if (logger != null) {
            logger.info("executing " + name);
        } else {
            System.out.println("executing " + name);
        }
        status = Status.DONE;
        submitResult(new ExecutionResult(id, status.toString()));
    }

    public abstract void submitResult(ExecutionResult result);

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Command && ((Command) obj).getId() == getId();

    }
}
