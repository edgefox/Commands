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
    private Connection connection;
    @Autowired
    private Log logger;
    @Autowired
    private volatile LinkedBlockingQueue<ExecutionResult> updateQueue;
    @Autowired
    private ExecutionResult poisonResult;
    @Autowired
    private ExecutionResult forceUpdateResult;
    private int limit;
    private static final String FORMAT = "update commands set status='DONE' where id IN(%s)";
    private Timer timer;

    public BufferedUpdater() {
        limit = 1000;
    }
    
    public BufferedUpdater(int limit) {
        this.limit = limit;
    }

    @PostConstruct
    public void initTimer() {
        initTimer(100, 3000);
    }
    
    public void initTimer(int delay, int period) {
        timer = new Timer();
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
        }, delay, period);
    }
    
    public void stopTimer(){
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void run() {
        try (Connection connection = dataSource.getConnection()) {
            this.connection = connection;
            Queue<ExecutionResult> resultList = new LinkedList<>();
            ExecutionResult result;
            while (null != (result = updateQueue.take())) {
                resultList.add(result);
                limit--;
                if (limit == 0 || result == poisonResult || result == forceUpdateResult) {
                    flushUpdate(resultList);
                    if (result == poisonResult) {
                        logger.info("[BufferedUpdater] " + "Empty result detected. Flush and exit...");
                        timer.cancel();
                        return;
                    }
                }
            }
        } catch (InterruptedException | SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void flushUpdate(Queue<ExecutionResult> resultList) throws SQLException {
        if (!resultList.isEmpty()) {
            try(Statement statement = connection.createStatement()) {
                List<Integer> ids = new ArrayList<>();
                while (resultList.size() > 0) {
                    ids.add(resultList.remove().getId());
                    limit++;
                }
                statement.executeUpdate(String.format(FORMAT, StringUtils.join(ids.toArray(), ",")));
            }
        }
    }
}
