package commands;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
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
public class BufferedUpdater extends Thread {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private Log logger;
    @Autowired
    private LinkedBlockingQueue<ExecutionResult> updateQueue;
    @Autowired
    private ExecutionResult emptyResult;
    private LinkedBlockingQueue<ExecutionResult> innerQueue;
    private static final String format = "update commands set status='DONE' where id IN(%s)";

    public BufferedUpdater() {
    }

    @Override
    public void run() {
        try {
            innerQueue = new LinkedBlockingQueue<ExecutionResult>(1000);
            ExecutionResult result;
            while (null != (result = updateQueue.take())) {
                innerQueue.add(result);
                if (innerQueue.remainingCapacity() == 0 || result == emptyResult) {
                    flushUpdate();
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public void flushUpdate() {
        if (!innerQueue.isEmpty()) {
            try {
                Connection connection = null;
                try {
                    connection = dataSource.getConnection();
                    Statement statement = connection.createStatement();
                    List<Integer> ids = new ArrayList<Integer>();
                    for(ExecutionResult item : innerQueue) {
                        ids.add(item.getId());
                    }
                    statement.executeUpdate(String.format(format, StringUtils.join(ids.toArray(), ",")));
                    innerQueue.clear();
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
