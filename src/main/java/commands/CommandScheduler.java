package commands;

import commands.dao.CommandDAO;
import commands.entities.Command;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * User: Ivan Lyutov
 * Date: 11/25/12
 * Time: 1:21 AM
 */
@Component(value = "commandScheduler")
@Scope(value = "prototype")
public class CommandScheduler implements Runnable {
    private static final int COMMAND_LIMIT = 100;
    @Autowired
    private CommandDAO commandDAO;
    @Autowired
    private ExecutorService executionPool;
    @Autowired
    private Log logger;

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                List<Command> commands;
                commands = commandDAO.getListForUpdate(COMMAND_LIMIT);
                if (commands.size() == 0) {
                    return;
                }
                commandDAO.updateListToStatus(commands, Command.Status.IN_PROGRESS);
                for (Command command : commands) {
                    executionPool.execute(command);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
