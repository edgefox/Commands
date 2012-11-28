import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 11/27/12
 * Time: 11:51 PM
 */
public class BufferedUpdater {
    private BlockingQueue<Integer> updateQueue;
    private String format;
    private DataSource dataSource;
    private Boolean hasError = false;

    public BufferedUpdater(int size, String format, DataSource dataSource) {
        updateQueue = new LinkedBlockingQueue<Integer>(size);
        this.format = format;
        this.dataSource = dataSource;
    }

    synchronized public void add(Integer id) {
        updateQueue.add(id);
        if (updateQueue.remainingCapacity() == 0) {
            flushUpdate();
        }
    }

    synchronized public void flushUpdate() {
        if (!updateQueue.isEmpty()) {
            try {
                Connection connection = null;
                try {
                    connection = dataSource.getConnection();
                    Statement statement = connection.createStatement();
                    int updated = statement.executeUpdate(String.format(format, StringUtils.join(updateQueue.toArray(), ",")));
                    if (updated != updateQueue.size()) {
                        hasError = true;
                    }
                    updateQueue.clear();
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean hasError() {
        return hasError;
    }
}
