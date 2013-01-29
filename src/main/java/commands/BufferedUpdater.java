package commands;

import commands.entities.ExecutionResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 11/27/12
 * Time: 11:51 PM
 */
@Component
public class BufferedUpdater implements Runnable {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private Log logger;
    @Autowired
    private volatile LinkedBlockingQueue<ExecutionResult> updateQueue;
    @Autowired
    private ExecutionResult poisonResult;
    @Autowired
    private ExecutionResult forceUpdateResult;
    private Integer limit = 1000;
    private static final String FORMAT = "update commands set status='DONE' where id IN(%s)";
    private Timer timer;

    public BufferedUpdater() {
        timer = new Timer();
    }

    @PostConstruct
    public void init() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("[BufferedUpdater] " + "Forcing update due to timeout");
                    updateQueue.put(forceUpdateResult);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, 100, 3000);
    }

    @Override
    public void run() {
        try (Connection connection = dataSource.getConnection()) {
            LinkedList<ExecutionResult> resultList = new LinkedList<>();
            ExecutionResult result;
            while (null != (result = updateQueue.take())) {
                resultList.add(result);
                limit--;
                if (limit == 0 || result == poisonResult || result == forceUpdateResult) {
                    flushUpdate(resultList, connection);
                    if (result == poisonResult) {
                        logger.info("[BufferedUpdater] " + "Empty result detected. Flush and exit...");
                        timer.cancel();
                        return;
                    }
                }
            }
        } catch (InterruptedException | SQLException e) {
            logger.error(e);
        }
    }

    public void flushUpdate(LinkedList<ExecutionResult> resultList, Connection connection) throws SQLException {
        if (!resultList.isEmpty()) {
            Statement statement = connection.createStatement();
            List<Integer> ids = new ArrayList<>();
            while (resultList.size() > 0) {
                ids.add(resultList.remove().getId());
                limit++;
            }
            statement.executeUpdate(String.format(FORMAT, StringUtils.join(ids.toArray(), ",")));
        }
    }
}
