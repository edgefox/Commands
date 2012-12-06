package commands;

import commands.abstracts.CommandFactory;
import org.apache.commons.logging.Log;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

/**
 * User: Ivan Lyutov
 * Date: 12/6/12
 * Time: 2:36 PM
 */
public class CommandSchedulerFactory {
    private DataSource dataSource;
    private ExecutorService commandsPool;
    private CommandFactory commandFactory;
    private Log logger;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setCommandsPool(ExecutorService commandsPool) {
        this.commandsPool = commandsPool;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public DataSource getDataSource() {

        return dataSource;
    }

    public ExecutorService getCommandsPool() {
        return commandsPool;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public Log getLogger() {
        return logger;
    }

    public CommandScheduler createCommandScheduler() {
        return new CommandScheduler(dataSource, commandsPool, commandFactory, logger);
    }
}
