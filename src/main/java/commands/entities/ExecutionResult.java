package commands.entities;

/**
 * User: Ivan Lyutov
 * Date: 12/5/12
 * Time: 3:38 PM
 */
public class ExecutionResult {
    private int id;
    private String status;

    public ExecutionResult() {
    }

    public ExecutionResult(int id, String status) {
        this.id = id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
