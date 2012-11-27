import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: Ivan Lyutov
 * Date: 11/25/12
 * Time: 1:49 AM
 */
public class CommandPool extends ThreadPoolExecutor {
    private DataSource dataSource;
    private static boolean concurrentModificationError = false;

    public CommandPool(DataSource dataSource) {
        super(0,
              Integer.MAX_VALUE,
              60L, TimeUnit.SECONDS,
              new LinkedBlockingQueue<Runnable>());
        this.dataSource = dataSource;
    }

    public void run() {
        while (!getQueue().isEmpty()) {
            execute(getQueue().remove());
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Connection connection = null;
        Command command = (Command)r;
        try {
            try {
                connection = dataSource.getConnection();
                Statement updateStatement = connection.createStatement();
                int updated = updateStatement.executeUpdate("update commands set status='" + Command.Status.DONE +
                                                            "' where id=" + command.getId() + " and status='" + Command.Status.IN_PROGRESS + "'");
                if (updated == 0) {
                    concurrentModificationError = true;
                }
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean hasConcurrentModificationError() {
        return concurrentModificationError;
    }
}
