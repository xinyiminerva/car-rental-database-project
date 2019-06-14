import java.sql.Connection;
import java.sql.SQLException;

public class Context {

  // Static Contexts
  public static String JDBC_CONN_STR = "jdbc:oracle:thin:@edgar0.cse.lehigh.edu:1521:cse241";
  public static String USERNAME = "xid220";
  public static String PASSWORD = "P816398530";

  // Dynamic Context
  private final Connection connection;

  private UserType userType;

  public Context(String username, String password) {
    USERNAME = username;
    PASSWORD = password;
    this.connection = AlsetDAO.connect(JDBC_CONN_STR, USERNAME, PASSWORD);
  }

  public UserType getUserType() {
    return userType;
  }

  public void setUserType(UserType userType) {
    this.userType = userType;
  }

  public Connection getConnection() {
    return connection;
  }

  public void release() {
    try {
      connection.close();
    } catch (SQLException ignored) {
      // already closed, ignore
    }
  }
}
