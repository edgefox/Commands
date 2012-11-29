import org.apache.commons.logging.Log;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 3:06 PM
 */
public class Command implements Runnable {
    private int id;
    private String name;
    private Status status;
    private Log logger;

    public Command(int id, String name, Status status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public Command(int id, String name, Status status, Log logger) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.logger = logger;
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
        if (logger != null) {
            logger.info("executing " + name);
        } else {
            System.out.println("executing " + name);
        }
        status = Status.DONE;
    }

    public static enum Status {
        NEW("NEW"), IN_PROGRESS("IN_PROGRESS"), DONE("DONE");

        private String value;

        private Status(final String value) {
            this.value = value;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Command))
            return false;

        if (((Command)obj).getId() == getId())
            return true;

        return false;
    }
}
