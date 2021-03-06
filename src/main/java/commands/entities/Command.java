package commands.entities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.BlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 12/5/12
 * Time: 3:30 PM
 */
public abstract class Command implements Runnable {
    private int id;
    private String name;
    private Status status;
    private volatile BlockingQueue<ExecutionResult> updateQueue;

    public Command() {
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

    public static enum Status {
        NEW, IN_PROGRESS, DONE
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

    //Part of magic :)
    @Override
    public String toString() {
        return String.valueOf(getId());
    }
}
