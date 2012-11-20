import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 3:06 PM
 */
public class Command {
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

        private String value;

        private Status(final String value) {
            this.value = value;
        }
    }
}
