import commands.CommandScheduler;
import commands.entities.ExecutionResult;
import commands.service.BufferedUpdater;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.*;

import static junit.framework.TestCase.assertTrue;

/**
 * User: Ivan Lyutov
 * Date: 12/24/12
 * Time: 12:32 PM
 */
public abstract class AbstractPETest {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Log logger;
    @Autowired
    private ExecutorService schedulerPool;
    @Autowired
    private ExecutorService executionPool;
    @Autowired
    private LinkedBlockingQueue<ExecutionResult> updateQueue;
    @Autowired
    private ExecutionResult poisonResult;
    @Autowired
    private BufferedUpdater bufferedUpdater;
    @Autowired
    private DataSource dataSource;
    private int toUpdate = 0;

    @Before
    public void setUp() {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            logger.info("Cleaning up the mess...");
            toUpdate = statement.executeUpdate("update commands set status='NEW'");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testCommands() throws SQLException {
        ((ThreadPoolExecutor)executionPool).setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put( r );
                } catch (InterruptedException e) {
                    
                }
            }
        });
        ExecutorService updaterPool = Executors.newSingleThreadExecutor();
        updaterPool.execute(bufferedUpdater);
        updaterPool.shutdown();
        for (int i = 0; i < 100; i++) {
            schedulerPool.execute((CommandScheduler)applicationContext.getBean("commandScheduler"));
        }
        schedulerPool.shutdown();

        try {
            while(!schedulerPool.isTerminated()) {
                schedulerPool.awaitTermination(5, TimeUnit.SECONDS);
            }
            executionPool.shutdown();
            while(!executionPool.isTerminated()) {
                executionPool.awaitTermination(5, TimeUnit.SECONDS);
            }
            updateQueue.put(poisonResult);
            while(!updaterPool.isTerminated()) {
                updaterPool.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        
        assertTrue(toUpdate == getDoneCount());
    }

    private int getDoneCount() throws SQLException {
        int result = 0;
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(id) as count from commands where status='DONE'");
            resultSet.next();
            result = resultSet.getInt("count");
        }

        return result;
    }
}
