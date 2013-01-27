package commands.dao;

import commands.entities.Command;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: Ivan Lyutov
 * Date: 1/22/13
 * Time: 12:09 PM
 */
public interface CommandDAO {
    public static AtomicInteger fromId = new AtomicInteger(0);
    public String SELECT_FORMAT = "select id, name, status from commands " +
                                  "where status='%s' and id>%s limit %s for update";
    public String UPDATE_FORMAT = "update commands set status='%s' where id in(%s)";
    
    public List<Command> getListForUpdate(int maxSize) throws SQLException;
    public void updateListToStatus(List<Command> commands, Command.Status status) throws SQLException;
}
