package commands.service;

import java.sql.SQLException;

/**
 * User: Ivan Lyutov
 * Date: 2/5/13
 * Time: 2:13 PM
 */
public interface BufferedUpdater extends Runnable {
    void flushUpdate() throws SQLException;
}
