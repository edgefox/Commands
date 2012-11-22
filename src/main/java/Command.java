import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 3:06 PM
 */
public class Command implements Runnable /* ArtD: To use as a task for an ExecutorService */{
    private int id;
    private String name;
    public Status status;

    public Command(int id, String name, Status status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void run() {
        DataSource dataSource = DataPool.getDataSource();
        Connection connection = null;
        try {
            try{
                //ArtD: I don't recommend spread database working all over the Command objects.
                //  Consider creating (and running in a background thread) an object
                //    dedicated to commands' statuses updating task
                //    and some way to apply communication between Сommand instances and this object.
                //  You can find some java.util.concurrent.BlockingQueue implementing classes to be useful.

                connection = dataSource.getConnection();
                Statement updateStatement = connection.createStatement();
                updateStatement.executeUpdate("update commands set status='" + Status.DONE + "' where id=" + id);
                System.out.println("executing " + name);
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static enum Status {
        NEW("NEW"), IN_PROGRESS("IN_PROGRESS"), DONE("DONE");

        //ArtD: You doubtly need this here as there are standard enums' methods .toString() and .valueOf(String).
        private String value;

        private Status(final String value) {
            this.value = value;
        }
    }
}
