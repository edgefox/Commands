import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * User: Ivan Lyutov
 * Date: 11/15/12
 * Time: 5:14 PM
 */
public class DataPool {
    private static ComboPooledDataSource dataSource;

    //ArtD: I suggest improvement for the next version:
    //        use Spring for data sources initialization, thus,
    //        you can easily switch before pool implementations
    //        without code modifying.
    synchronized public static ComboPooledDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new ComboPooledDataSource();
        }

        return dataSource;
    }
}
