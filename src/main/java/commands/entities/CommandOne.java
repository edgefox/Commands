package commands.entities;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 3:06 PM
 */
@Component("command")
@Scope(value = "prototype")
public class CommandOne extends Command {
    @Autowired
    private ApplicationContext applicationContext;

    public CommandOne() {
    }

    @Override
    public void run() {
        setLogger((Log)applicationContext.getBean("logger"));
        setUpdateQueue((BlockingQueue<ExecutionResult>)applicationContext.getBean("updateQueue"));
        
        getLogger().info("executing " + getName());
        setStatus(Status.DONE);
        submitResult(new ExecutionResult(getId(), getStatus().toString()));
    }

    @Override
    public void submitResult(ExecutionResult result) {
        try {
            getUpdateQueue().put(result);
        } catch (InterruptedException e) {
            getLogger().error(e.getMessage());
        }
    }
}
