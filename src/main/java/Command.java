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

    private static enum Status {
        NEW("NEW"), IN_PROGRESS("IN_PROGRESS"), DONE("DONE");

        private String value;

        private Status(final String value) {
            this.value = value;
        }
    }
}
