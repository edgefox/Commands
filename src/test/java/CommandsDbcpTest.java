import commands.CommandScheduler;
import commands.ExecutionResult;
import commands.abstracts.Command;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.*;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 5:09 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:dbcp.xml"})
public class CommandsDbcpTest extends TestCase {
    @Autowired
    private DataSource datasource;
    @Autowired
    private Log logger;
    @Autowired
    private ExecutorService schedulerPool;
    @Autowired
    private ExecutorService executionPool;
    @Autowired
    private LinkedBlockingQueue<ExecutionResult> updateQueue;

    @org.junit.Test
    public void testCommands() {
        Command.setQueue(updateQueue);
        for (int i = 0; i < 100; i++) {
            schedulerPool.execute(new CommandScheduler(datasource, executionPool, logger));
        }
        schedulerPool.shutdown();

        try {
            schedulerPool.awaitTermination(10, TimeUnit.SECONDS);
            executionPool.shutdown();
            while (!executionPool.isTerminated() || !updateQueue.isEmpty()) {
                TimeUnit.SECONDS.sleep(10);
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    @Override
    public void tearDown() {
        Connection connection = null;
        try {
            connection = datasource.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("update commands set status='NEW'");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}