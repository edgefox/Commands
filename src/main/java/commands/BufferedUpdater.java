package commands;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import javax.sql.DataSource;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 11/27/12
 * Time: 11:51 PM
 */
public class BufferedUpdater {
    private DataSource dataSource;
    private Log logger;
    private BlockingQueue<ExecutionResult> updateQueue;
    private static final String format = "update commands set status='DONE' where id IN(%s)";

    public BufferedUpdater() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                flushUpdate();
            }
        }, 100, 1000);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Log getLogger() {
        return logger;
    }

    public BlockingQueue<ExecutionResult> getUpdateQueue() {
        return updateQueue;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public void setUpdateQueue(BlockingQueue<ExecutionResult> updateQueue) {
        this.updateQueue = updateQueue;
    }

    synchronized public void flushUpdate() {
        if (!updateQueue.isEmpty()) {
            try {
                Connection connection = null;
                try {
                    connection = dataSource.getConnection();
                    Statement statement = connection.createStatement();
                    List<Integer> ids = new ArrayList<Integer>();
                    for(ExecutionResult item : updateQueue) {
                        ids.add(item.getId());
                    }
                    statement.executeUpdate(String.format(format, StringUtils.join(ids.toArray(), ",")));
                    updateQueue.clear();
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
