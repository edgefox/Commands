import commands.BufferedUpdater;
import commands.CommandSchedulerFactory;
import commands.ExecutionResult;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.*;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 5:09 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:bonecp.xml"})
public class CommandsBonecpTest extends TestCase {
    @Autowired
    private Log logger;
    @Autowired
    private ExecutorService schedulerPool;
    @Autowired
    private ExecutorService executionPool;
    @Autowired
    private LinkedBlockingQueue<ExecutionResult> updateQueue;
    @Autowired
    private CommandSchedulerFactory schedulerFactory;
    @Autowired
    private ExecutionResult emptyResult;
    @Autowired
    private BufferedUpdater bufferedUpdater;

    @org.junit.Test
    public void testCommands() {
        for (int i = 0; i < 100; i++) {
            schedulerPool.execute(schedulerFactory.createCommandScheduler());
        }
        schedulerPool.shutdown();
        bufferedUpdater.start();

        try {
            schedulerPool.awaitTermination(5, TimeUnit.SECONDS);
            executionPool.shutdown();
            executionPool.awaitTermination(5, TimeUnit.SECONDS);
            updateQueue.add(emptyResult);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}