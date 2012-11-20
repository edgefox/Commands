import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 5:09 PM
 */
public class CommandsTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    protected static Test suite() {
        return new TestSuite(CommandsTest.class);
    }

    @Override
    protected void setUp() throws Exception {

    }

    public void testCommands() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.execute(new CommandExecutor());
        }
        executorService.shutdown();

        while (!executorService.isTerminated()){
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
        }
        ConnectionPool.getDataSource().close();
    }
}
