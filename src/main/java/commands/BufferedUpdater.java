package commands;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
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
    private ExecutionResult poisonResult;
    @Autowired
    private ExecutionResult forceUpdateResult;
    private static final String format = "update commands set status='DONE' where id IN(%s)";
    private int limit;

    public BufferedUpdater() {
    }
    
    public void init() {
        limit = updateQueue.remainingCapacity();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info( "[BufferedUpdater] " + "Forcing update due to timeout");
                    updateQueue.put(forceUpdateResult);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, 100, 300);
    }

    @Override
    public void run() {
        try {
            try {
                LinkedList<ExecutionResult> resultList = new LinkedList<ExecutionResult>();
                ExecutionResult result;
                while (null != (result = updateQueue.take())) {
                    resultList.add(result);
                    limit--;
                    if (limit == 0 || result == poisonResult || result == forceUpdateResult) {
                        flushUpdate(resultList);
                        if (result == poisonResult) {
                            logger.info( "[BufferedUpdater] " + "Empty result detected. Flush and exit...");
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

    public void flushUpdate(LinkedList<ExecutionResult> resultList) throws SQLException {
        if (!resultList.isEmpty()) {
            Statement statement = updaterConnection.createStatement();
            List<Integer> ids = new ArrayList<Integer>();
            while (resultList.size() > 0){
                ids.add(resultList.remove().getId());
                limit++;
            }
            statement.executeUpdate(String.format(format, StringUtils.join(ids.toArray(), ",")));
        }
    }
}
