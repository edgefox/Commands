import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 3:26 PM
 */
public class CommandExecutor implements Runnable {
    final static int taskLimit = 10;

    @Override
    public void run() {
        DataSource dataSource = DataPool.getDataSource();
        Connection connection = null;
        try {
            try {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                PreparedStatement selectStatement = connection.prepareStatement("select id, name, status from commands " +
                        "where status='" + Command.Status.NEW +
                        "' limit " + taskLimit + " for update");
                PreparedStatement updateStatement = connection.prepareStatement("update commands set status=? WHERE id=?");
                HashSet<Command> commands = new HashSet<Command>(taskLimit);
                ResultSet resultSet = null;
                while (true) {
                    resultSet = selectStatement.executeQuery();
                    if (!resultSet.isBeforeFirst()) {
                        break;
                    }

                    while (resultSet.next()) {
                        commands.add(new Command(resultSet.getInt("id"),
                                resultSet.getString("name"),
                                Command.Status.valueOf(resultSet.getString("status"))));
                        updateStatement.setString(1, Command.Status.IN_PROGRESS.toString());
                        updateStatement.setInt(2, resultSet.getInt("id"));
                        updateStatement.executeUpdate();
                    }
                    connection.commit();

                    //ArtD: You run several CommandExecutor's threads, but in the each thread
                    //        you run all selected commands sequentially.
                    //      I don't recommend such limiting architectural decision
                    //        and suggest to use the benefits of ThreadPoolExecutor

                    //SingleThreadExecutor is used here for the previous behaviour imitation purposes
                    //Later, you should go with some more useful implementation here.

                    //ArtD: this should not be a local variable. Just didn't want to spread changes wider.
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    for (Command command : commands)
                        executor.execute(command);

                    //ArtD: why for?
                    //commands.clear();
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
}
