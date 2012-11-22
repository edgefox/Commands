import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
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
        //ArtD: You can do this waitings in a little bit more natural way (see )
        //But according to what I can see you want to do here,
        //you should consider hooking ThreadPoolExecutor's
        //beforeExecute(Thread, Runnable) and afterExecute(Runnable, Throwable) methods
        //to obtain and reveal some lock correspondingly .

        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for (int i = 0; i < 50; i++)
            executorService.execute(new CommandExecutor());

        executorService.shutdown();

        try {
            while (executorService.awaitTermination(10, TimeUnit.SECONDS)) ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DataPool.getDataSource().close();
    }
}
