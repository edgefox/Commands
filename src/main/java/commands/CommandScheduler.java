package commands;

import commands.abstracts.Command;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * User: Ivan Lyutov
 * Date: 11/25/12
 * Time: 1:21 AM
 */
public class CommandScheduler implements Runnable {
    private static final int commandLimit = 100;
    private DataSource dataSource;
    private ExecutorService commandsPool;
    private Log logger;

    public CommandScheduler(DataSource dataSource, ExecutorService commandsPool) {
        this.dataSource = dataSource;
        this.commandsPool = commandsPool;
    }

    public CommandScheduler(DataSource dataSource, ExecutorService commandsPool, Log logger) {
        this.dataSource = dataSource;
        this.commandsPool = commandsPool;
        this.logger = logger;
    }

    @Override
    public void run() {
        ResultSet resultSet = null;
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement selectStatement = connection.prepareStatement("select id, name, status from commands " +
                                                                            "where status='" + CommandOne.Status.NEW +
                                                                            "' limit " + commandLimit + " for update");
            Statement updateStatement = connection.createStatement();
            try {
                List<Integer> ids = null;
                Queue<Command> taskQueue = null;
                while (true) {
                    resultSet = selectStatement.executeQuery();
                    //TODO: real world exiting conditions
                    if (!resultSet.isBeforeFirst()) {
                        break;
                    }
                    ids = new ArrayList<Integer>();
                    taskQueue = new LinkedList<Command>();
                    while (resultSet.next()) {
                        taskQueue.add(new CommandOne(resultSet.getInt("id"),
                                                     resultSet.getString("name"),
                                                     Command.Status.valueOf(resultSet.getString("status")),
                                                     logger));
                        ids.add(resultSet.getInt("id"));
                    }
                    updateStatement.executeUpdate("update commands set status='" + CommandOne.Status.IN_PROGRESS +
                                                  "' where id in (" + StringUtils.join(ids.toArray(), ",") + ")");
                    connection.commit();
                    while (!taskQueue.isEmpty()) {
                        commandsPool.execute(taskQueue.remove());
                    }
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
