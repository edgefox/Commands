import commands.entities.ExecutionResult;
import commands.service.TimedBufferedUpdater;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

/**
 * User: Ivan Lyutov
 * Date: 2/5/13
 * Time: 2:27 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:c3p0.xml"})
public class BufferedUpdaterTest {
    @Autowired
    TimedBufferedUpdater bufferedUpdater;
    @Autowired
    private LinkedBlockingQueue<ExecutionResult> updateQueue;
    @Autowired
    DataSource dataSource;
    @Autowired
    ExecutionResult poisonResult;
    @Autowired
    Log logger;
    
    @Before
    public void setUp() {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            logger.info("Cleaning up the mess...");
            statement.executeUpdate("update commands set status='NEW'");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testFlush() throws InterruptedException, SQLException {
        bufferedUpdater.stopTimer();
        ExecutorService updaterPool = Executors.newSingleThreadExecutor();
        updaterPool.execute(bufferedUpdater);
        updaterPool.shutdown();

        int updateExpected = 800;
        for (int i = 1; i <= updateExpected; i++) {
            updateQueue.put(new ExecutionResult(i, "DONE"));
        }
        while (!updateQueue.isEmpty()) {
            TimeUnit.SECONDS.sleep(1);
        }
        bufferedUpdater.flushUpdate();

        assertTrue(getDoneCount() == updateExpected);

        updateQueue.put(poisonResult);
    }

    @Test
    public void testAutoFlushWithoutTimer() throws SQLException, InterruptedException {
        ExecutorService updaterPool = Executors.newSingleThreadExecutor();
        updaterPool.execute(bufferedUpdater);
        updaterPool.shutdown();

        int updateExpected = 998;
        bufferedUpdater.stopTimer();
        for (int i = 1; i <= updateExpected; i++) {
            updateQueue.put(new ExecutionResult(i, "DONE"));
        }
        updateQueue.put(poisonResult);
        Thread.yield();

        try {
            updaterPool.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        assertTrue(getDoneCount() == updateExpected);
    }

    @Test
    public void testAutoFlushWithTimer() throws SQLException, InterruptedException {
        bufferedUpdater.initTimer(1, 1000);
        ExecutorService updaterPool = Executors.newSingleThreadExecutor();
        updaterPool.execute(bufferedUpdater);
        updaterPool.shutdown();

        int updateExpected = 500;
        for (int i = 1; i <= updateExpected; i++) {
            updateQueue.put(new ExecutionResult(i, "DONE"));
        }
        Thread.yield();

        while (!updateQueue.isEmpty()) {
            TimeUnit.SECONDS.sleep(1);
        }
        updateQueue.put(poisonResult);
        updaterPool.awaitTermination(1000, TimeUnit.MILLISECONDS);

        assertTrue(getDoneCount() == updateExpected);
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
