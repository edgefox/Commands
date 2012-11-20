import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 5:14 PM
 */
public class ConnectionPool {
    private static ComboPooledDataSource dataSource;

    public static ComboPooledDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new ComboPooledDataSource();
        }

        return dataSource;
    }
}
