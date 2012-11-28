import junit.framework.TestCase;
import org.junit.After;
import org.junit.Test;
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
@ContextConfiguration(locations = {"classpath:c3p0.xml"})
public class CommandsC3p0Test extends TestCase {
    @Autowired
    private DataSource datasource;

    @Test
    public void testCommands() {
        CommandPool commandPool = new CommandPool(datasource);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            executorService.execute(new CommandScheduler(datasource, commandPool.getQueue()));
        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            commandPool.run();
            commandPool.shutdown();
            commandPool.awaitTermination(10, TimeUnit.SECONDS);

            assertFalse(commandPool.hasError());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}