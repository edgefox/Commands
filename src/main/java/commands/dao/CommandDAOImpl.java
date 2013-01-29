package commands.dao;

import commands.entities.CommandOne;
import commands.entities.Command;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Ivan Lyutov
 * Date: 1/27/13
 * Time: 4:32 PM
 */
@Repository
@Scope("prototype")
public class CommandDAOImpl implements CommandDAO {
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private DataSource dataSource;
    private Connection connection;

    @Override
    public List<Command> getListForUpdate(int maxSize) throws SQLException {
        List<Command> commands = new ArrayList<>();
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        PreparedStatement selectStatement = connection.prepareStatement(String.format(SELECT_FORMAT,
                                                                                      CommandOne.Status.NEW,
                                                                                      fromId.getAndAdd(maxSize),
                                                                                      maxSize));
        ResultSet resultSet = selectStatement.executeQuery();
        Command command;
        while (resultSet.next()) {
            command = (Command)applicationContext.getBean("command");
            command.setId(resultSet.getInt("id"));
            command.setName(resultSet.getString("name"));
            command.setStatus(Command.Status.valueOf(resultSet.getString("status")));
            commands.add(command);
        }
        
        return commands;
    }

    @Override
    public void updateListToStatus(List<Command> commands, Command.Status status) throws SQLException {
        try {
            if (connection == null) {
                throw new SQLException("You should lock rows for update first");
            }
            Statement updateStatement = connection.createStatement();
            updateStatement.executeUpdate(String.format(UPDATE_FORMAT,
                                                        status,
                                                        StringUtils.join(commands.toArray(), ",")));
            connection.commit();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

}