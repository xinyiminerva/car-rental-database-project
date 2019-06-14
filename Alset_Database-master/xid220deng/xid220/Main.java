class Main {

  static {
    try {
      Class.forName("oracle.jdbc.OracleDriver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }

  public static void main(String[] args) {

    // Makes test on local database in order not to ruin production DB.
    if (args.length >= 2 && "-p".equalsIgnoreCase(args[0]) && "test".equalsIgnoreCase(args[1])) {
      Context.JDBC_CONN_STR = "jdbc:oracle:thin:@localhost:1521:ORCLCDB";
    }

    new Cli(System.in, System.out)
        .runCli();
  }
}
