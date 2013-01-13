import commands.BufferedUpdater;
import commands.CommandSchedulerFactory;
import commands.ExecutionResult;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * User: Ivan Lyutov
 * Date: 12/24/12
 * Time: 12:32 PM
 */
public class AbstractPETest extends TestCase {
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
    private ExecutionResult poisonResult;
    @Autowired
    private BufferedUpdater bufferedUpdater;

    @org.junit.Test
    public void testCommands() {
        ExecutorService updaterPool = Executors.newSingleThreadExecutor();
        updaterPool.execute(bufferedUpdater);
        updaterPool.shutdown();
        for (int i = 0; i < 100; i++) {
            schedulerPool.execute(schedulerFactory.createCommandScheduler());
        }
        schedulerPool.shutdown();

        try {
            schedulerPool.awaitTermination(5, TimeUnit.SECONDS);
            executionPool.shutdown();
            executionPool.awaitTermination(5, TimeUnit.SECONDS);
            updateQueue.put(poisonResult);
            updaterPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
