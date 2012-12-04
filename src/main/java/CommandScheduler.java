import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * User: Ivan Lyutov
 * Date: 11/25/12
 * Time: 1:21 AM
 */
public class CommandScheduler implements Runnable {
    private static final int commandLimit = 100;
    private DataSource dataSource;
    private BlockingQueue<Runnable> commands;
    private Log logger;

    public CommandScheduler(DataSource dataSource, BlockingQueue<Runnable> commands) {
        this.commands = commands;
        this.dataSource = dataSource;
    }

    public CommandScheduler(DataSource dataSource, BlockingQueue<Runnable> commands, Log logger) {
        this.commands = commands;
        this.dataSource = dataSource;
        this.logger = logger;
    }

    @Override
    public void run() {
        ResultSet resultSet = null;
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement selectStatement = connection.prepareStatement("select id, name, status from commands " +
                    "where status='" + Command.Status.NEW +
                    "' limit " + commandLimit + " for update");
            Statement updateStatement = connection.createStatement();
            int firstId = 0;
            int lastId = 0;
            try {
                while (true) {
                    resultSet = selectStatement.executeQuery();
                    //todo real world exiting conditions
                    if (!resultSet.isBeforeFirst()) {
                        break;
                    }

                    List<Integer> ids = new ArrayList<Integer>();
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        commands.add(new Command(id, resultSet.getString("name"), Command.Status.valueOf(resultSet.getString("status")), logger));
                        ids.add(id);
                    }

                    updateStatement.executeUpdate("update commands set status='" + Command.Status.IN_PROGRESS +
                            "' where id in (" + StringUtils.join(ids.toArray(new Integer[ids.size()]), ",") + ")");
                    connection.commit();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            if (logger != null) {
                logger.error(e.getMessage(), e);
            } else {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
