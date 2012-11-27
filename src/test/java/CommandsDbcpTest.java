import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @org.junit.Test
    public void testCommands() {
        CommandPool commandPool = new CommandPool(datasource);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.execute(new CommandScheduler(datasource, commandPool.getQueue()));
        }
        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            commandPool.run();
            commandPool.shutdown();
            commandPool.awaitTermination(10, TimeUnit.SECONDS);

            assertFalse(commandPool.hasConcurrentModificationError());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
