package commands;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 11/27/12
 * Time: 11:51 PM
 */
public class BufferedUpdater implements Runnable {
    @Autowired
    private Connection updaterConnection;
    @Autowired
    private Log logger;
    @Autowired
    private LinkedBlockingQueue<ExecutionResult> updateQueue;
    @Autowired
    private ExecutionResult emptyResult;
    private LinkedBlockingQueue<ExecutionResult> innerQueue;
    private static final String format = "update commands set status='DONE' where id IN(%s)";
    private boolean closed = false;

    public BufferedUpdater() {
    }

    @Override
    public void run() {
        try {
            try {
                innerQueue = new LinkedBlockingQueue<ExecutionResult>(1000);
                ExecutionResult result;
                while (null != (result = updateQueue.take())) {
                    innerQueue.add(result);
                    if (result == emptyResult) {
                        logger.info( "[BufferedUpdater] " + "Empty result detected. Flush and exit...");
                        closed = true;
                    }
                    if (innerQueue.remainingCapacity() == 0 || closed) {
                        logger.info( "[BufferedUpdater] " + "Remaining capacity is " + innerQueue.remainingCapacity());
                        flushUpdate();
                        if (closed) {
                            return;
                        }
                    }
                }
            } finally {
                if (updaterConnection != null) {
                    logger.info( "[BufferedUpdater] " + "Closed connection");
                    updaterConnection.close();
                }
            }
        } catch (InterruptedException e) {
            logger.error(e);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public void flushUpdate() throws SQLException {
        if (!innerQueue.isEmpty()) {
            Statement statement = updaterConnection.createStatement();
            List<Integer> ids = new ArrayList<Integer>();
            while (!innerQueue.isEmpty()) {
                ids.add(innerQueue.remove().getId());
            }
            statement.executeUpdate(String.format(format, StringUtils.join(ids.toArray(), ",")));
        }
    }
}
