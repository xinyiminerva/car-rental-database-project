import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlsetDAO {

  private static final StatementFiller<PreparedStatement> NOP = preparedStatement -> { /* NO OP */ };

  public static Connection connect(String connStr, String username, String password) {
    try {
      Connection connection = DriverManager.getConnection(connStr, username, password);
      // check if actually connected.
      connection.getMetaData();

      return connection;
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private static int executeUpdate(String sql, StatementFiller<PreparedStatement> fillParams) {
    Connection connection = ContextHolder.getContext().getConnection();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      fillParams.fill(statement);

      return statement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private static <T> List<T> execute(String sql, StatementFiller<PreparedStatement> fillParams,
      ResultHandler<ResultSet, T> function) {
    Connection connection = ContextHolder.getContext().getConnection();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      fillParams.fill(statement);

      ResultSet result = statement.executeQuery();

      List<T> results = new ArrayList<>();
      while (result.next()) {
        results.add(function.apply(result));
      }

      return results;
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static <T> T call(String spCall, StatementFiller<CallableStatement> filler,
      ResultHandler<CallableStatement, T> handler) {
    Connection connection = ContextHolder.getContext().getConnection();
    try (CallableStatement statement = connection.prepareCall(spCall)) {
      filler.fill(statement);

      statement.execute();

      return handler.apply(statement);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }


  public static List<Model> selectModels() {
    return execute("SELECT NAME, DESCRIPTION, MAINTENANCE_PERIOD FROM MODEL", NOP,
        result -> new Model(
            result.getString("NAME"),
            result.getString("DESCRIPTION"),
            result.getInt("MAINTENANCE_PERIOD")
        ));
  }

  public static List<Sku> selectSkus(String modelName) {
    return execute(
        "SELECT SKU.SKU_ID, SKU.NAME, CONFIG, MODEL_NAME, PRICE, RESELL_PRICE, REPAIR_PRICE\n"
            + "FROM (SELECT SKU_ID, LISTAGG(CONFIG, ', ') WITHIN GROUP (ORDER BY CONFIG) AS CONFIG\n"
            + "      FROM SKU_INCLUDES\n"
            + "      GROUP BY SKU_ID) C\n"
            + "JOIN SKU ON SKU.SKU_ID = C.SKU_ID\n"
            + "WHERE MODEL_NAME = ?",
        preparedStatement -> preparedStatement.setString(1, modelName),
        result -> new Sku(
            result.getInt("SKU_ID"),
            result.getString("MODEL_NAME"),
            result.getString("NAME"),
            result.getString("CONFIG"),
            result.getDouble("PRICE"),
            result.getDouble("RESELL_PRICE"),
            result.getDouble("REPAIR_PRICE")
        ));
  }


  public static List<ServiceLocation> selectLocationSupport(String modelName) {
    return execute(
        "SELECT L.SERVICE_LOCATION_ID, NAME, GALAXY, PLANET, COUNTRY, STATE, CITY, STREET\n"
            + "FROM SERVICE_LOCATION L\n"
            + "       JOIN SERVICE_SUPPORTS S on L.SERVICE_LOCATION_ID = S.SERVICE_LOCATION_ID\n"
            + "       JOIN ADDRESS A2 on L.SERVICE_ADDRESS_ID = A2.ADDRESS_ID\n"
            + "WHERE MODEL_NAME = ?",
        statement -> statement.setString(1, modelName),
        result -> {
          Address address = new Address(
              result.getString("GALAXY"),
              result.getString("PLANET"),
              result.getString("COUNTRY"),
              result.getString("STATE"),
              result.getString("CITY"),
              result.getString("STREET")
          );
          return new ServiceLocation(result.getInt("SERVICE_LOCATION_ID"),
              result.getString("NAME"), address);
        });
  }

  public static List<VehiclePurchase> selectVehicleByName(String name) {
    return execute(
        "SELECT V.VEHICLE_ID AS VEHICLE_ID,\n"
            + "       FACT_PRICE,\n"
            + "       SALE.STATUS  AS STATUS,\n"
            + "       SALE_DATE,\n"
            + "       S.MODEL_NAME AS MODEL_NAME,\n"
            + "       S.NAME       AS SKU_NAME,\n"
            + "       V.STATUS     AS VEHICLE_STATUS\n"
            + "FROM SALE\n"
            + "       JOIN \"USER\" U on SALE.BUYER_ID = U.USER_ID\n"
            + "       JOIN VEHICLE V on SALE.VEHICLE_ID = V.VEHICLE_ID\n"
            + "       JOIN SKU S on V.SKU_ID = S.SKU_ID\n"
            + "WHERE U.NAME = ? AND SALE.STATUS != 'RECALLED'",
        statement -> statement.setString(1, name),
        result -> new VehiclePurchase(
            result.getInt("VEHICLE_ID"),
            result.getDate("SALE_DATE"),
            result.getDouble("FACT_PRICE"),
            result.getString("MODEL_NAME"),
            result.getString("SKU_NAME"),
            result.getString("STATUS"),
            result.getString("VEHICLE_STATUS")));
  }

  public static List<ServiceLocation> selectServiceLocation() {
    List<ServiceLocation> serviceLocations = execute(
        "SELECT L.SERVICE_LOCATION_ID, NAME, GALAXY, PLANET, COUNTRY, STATE, CITY, STREET\n"
            + "FROM SERVICE_LOCATION L\n"
            + "       JOIN ADDRESS A2 on L.SERVICE_ADDRESS_ID = A2.ADDRESS_ID"
        , NOP, result -> {
          Address address = new Address(
              result.getString("GALAXY"),
              result.getString("PLANET"),
              result.getString("COUNTRY"),
              result.getString("STATE"),
              result.getString("CITY"),
              result.getString("STREET")
          );
          return new ServiceLocation(result.getInt("SERVICE_LOCATION_ID"),
              result.getString("NAME"), address);
        });
    for (ServiceLocation serviceLocation : serviceLocations) {
      serviceLocation.setSupports(
          execute("SELECT MODEL_NAME, M.DESCRIPTION, M.MAINTENANCE_PERIOD\n"
                  + "FROM SERVICE_SUPPORTS JOIN MODEL M on SERVICE_SUPPORTS.MODEL_NAME = M.NAME WHERE SERVICE_LOCATION_ID = ?",
              statement -> statement.setInt(1, serviceLocation.getServiceLocationId()),
              result -> new Model(
                  result.getString("MODEL_NAME"),
                  result.getString("DESCRIPTION"),
                  result.getInt("MAINTENANCE_PERIOD")
              )));
    }

    return serviceLocations;
  }

  public static List<Model> selectSupportModels(Integer serviceLocationId) {
    return execute("SELECT S.MODEL_NAME AS NAME, M.DESCRIPTION, M.MAINTENANCE_PERIOD\n"
            + "FROM SERVICE_LOCATION L\n"
            + "       JOIN SERVICE_SUPPORTS S on L.SERVICE_LOCATION_ID = S.SERVICE_LOCATION_ID\n"
            + "       JOIN MODEL M on S.MODEL_NAME = M.NAME\n"
            + "WHERE L.SERVICE_LOCATION_ID = ?",
        statement -> statement.setInt(1, serviceLocationId),
        result -> new Model(
            result.getString("NAME"),
            result.getString("DESCRIPTION"),
            result.getInt("MAINTENANCE_PERIOD")
        ));
  }

  public static List<VehiclePurchase> selectVehicleByLocationStatus(String name,
      Integer serviceLocationId, VehicleStatus status) {
    return execute(""
            + "SELECT V.VEHICLE_ID AS VEHICLE_ID,\n"
            + "       FACT_PRICE,\n"
            + "       SALE.STATUS  AS STATUS,\n"
            + "       SALE_DATE,\n"
            + "       S.MODEL_NAME AS MODEL_NAME,\n"
            + "       S.NAME       AS SKU_NAME,"
            + "       V.STATUS     AS VEHICLE_STATUS\n"
            + "FROM SALE\n"
            + "       JOIN \"USER\" U on SALE.BUYER_ID = U.USER_ID\n"
            + "       JOIN VEHICLE V on SALE.VEHICLE_ID = V.VEHICLE_ID\n"
            + "       JOIN SKU S on V.SKU_ID = S.SKU_ID\n"
            + "WHERE U.NAME = ? AND PICKUP_LOCATION_ID = ? AND V.STATUS = ?",
        statement -> {
          statement.setString(1, name);
          statement.setInt(2, serviceLocationId);
          statement.setString(3, status.name());
        }, result -> new VehiclePurchase(
            result.getInt("VEHICLE_ID"),
            result.getDate("SALE_DATE"),
            result.getDouble("FACT_PRICE"),
            result.getString("MODEL_NAME"),
            result.getString("SKU_NAME"),
            result.getString("STATUS"),
            result.getString("VEHICLE_STATUS")));
  }

  public static Customer selectCustomerByName(String name) {
    List<Customer> customer = execute(""
            + "SELECT NAME,\n"
            + "       PASSWORD,\n"
            + "       EMAIL,\n"
            + "       TYPE,\n"
            + "       PAYMENT_TYPE,\n"
            + "       CARD_NUM,\n"
            + "       CVV,\n"
            + "       GALAXY,\n"
            + "       PLANET,\n"
            + "       COUNTRY,\n"
            + "       STATE,\n"
            + "       CITY,\n"
            + "       STREET\n"
            + "FROM \"USER\"\n"
            + "       JOIN PAYMENT P on \"USER\".USER_PAYMENT_ID = P.PAYMENT_ID\n"
            + "       JOIN ADDRESS A2 on \"USER\".USER_ADDRESS_ID = A2.ADDRESS_ID\n"
            + "WHERE NAME = ?\n"
            + "  AND \"TYPE\" = ?",
        statement -> {
          statement.setString(1, name);
          statement.setString(2, UserType.CUSTOMER.name());
        },
        result -> new Customer(
            result.getString("NAME"),
            result.getString("EMAIL"),
            result.getString("PASSWORD"),
            result.getString("GALAXY"),
            result.getString("PLANET"),
            result.getString("COUNTRY"),
            result.getString("STATE"),
            result.getString("CITY"),
            result.getString("STREET"),
            result.getString("PAYMENT_TYPE"),
            result.getBigDecimal("CARD_NUM").toBigInteger(),
            result.getBigDecimal("CVV").toBigInteger()
        ));
    if (customer == null || customer.size() == 0) {
      return null;
    }
    return customer.get(0);
  }

  public static void updateVehicleStatus(Integer vehicleId, String status) {
    executeUpdate("UPDATE VEHICLE SET STATUS = ? WHERE VEHICLE_ID = ?",
        statement -> {
          statement.setString(1, status);
          statement.setInt(2, vehicleId);
        });
  }

  public static void updateSaleStatus(Integer vehicleId, String status) {
    executeUpdate("UPDATE SALE SET STATUS = ? WHERE VEHICLE_ID = ?",
        statement -> {
          statement.setString(1, status);
          statement.setInt(2, vehicleId);
        });
  }

  public static List<Sale> selectSalesByLocationId(Integer serviceLocationId) {
    return execute(
        "SELECT V.VEHICLE_ID AS VEHICLE_ID,\n"
            + "       FACT_PRICE,\n"
            + "       SALE.STATUS  AS STATUS,\n"
            + "       SALE_DATE,\n"
            + "       S.MODEL_NAME AS MODEL_NAME,\n"
            + "       S.NAME       AS SKU_NAME,\n"
            + "       V.STATUS     AS VEHICLE_STATUS,\n"
            + "       U.NAME       AS BUYER_NAME\n"
            + "FROM SALE\n"
            + "       JOIN \"USER\" U on SALE.BUYER_ID = U.USER_ID\n"
            + "       JOIN VEHICLE V on SALE.VEHICLE_ID = V.VEHICLE_ID\n"
            + "       JOIN SKU S on V.SKU_ID = S.SKU_ID\n"
            + "WHERE PICKUP_LOCATION_ID = ?",
        statement -> statement.setInt(1, serviceLocationId),
        result -> new Sale(
            result.getString("BUYER_NAME"),
            result.getDate("SALE_DATE"),
            result.getDouble("FACT_PRICE"),
            result.getString("MODEL_NAME"),
            result.getString("SKU_NAME"),
            SaleStatus.byName(result.getString("STATUS"))
        ));
  }

  public static List<Sale> selectSalesByCustomerName(String name) {
    return execute(
        "SELECT U.\"NAME\", SALE_DATE, FACT_PRICE, MODEL_NAME, SALE.STATUS, S.NAME AS SKU_NAME\n"
            + "FROM SALE\n"
            + "       JOIN \"USER\" U on SALE.BUYER_ID = U.USER_ID\n"
            + "       JOIN VEHICLE V on SALE.VEHICLE_ID = V.VEHICLE_ID\n"
            + "       JOIN SKU S on V.SKU_ID = S.SKU_ID\n"
            + "WHERE U.NAME = ?",
        statement -> statement.setString(1, name),
        result -> new Sale(
            result.getString("NAME"),
            result.getDate("SALE_DATE"),
            result.getDouble("FACT_PRICE"),
            result.getString("MODEL_NAME"),
            result.getString("SKU_NAME"),
            SaleStatus.byName(result.getString("STATUS"))
        ));
  }

  public static List<Vehicle> selectShowVehicleByLocationId(Integer serviceLocationId) {
    return execute(
        "SELECT V.VEHICLE_ID, MODEL_NAME, S.NAME AS SKU_NAME, PRICE\n"
            + "FROM VEHICLE_SHOWS\n"
            + "       JOIN VEHICLE V on VEHICLE_SHOWS.VEHICLE_ID = V.VEHICLE_ID\n"
            + "       JOIN SKU S on V.SKU_ID = S.SKU_ID\n"
            + "WHERE LOCATION_ID = ?",
        statement -> statement.setInt(1, serviceLocationId),
        result -> new Vehicle(
            result.getInt("VEHICLE_ID"),
            result.getString("MODEL_NAME"),
            result.getString("SKU_NAME"),
            result.getDouble("PRICE")
        ));
  }

  public static void updateVehicleStatusBySkuIdAndDate(Integer skuId, Date fromDate, Date toDate,
      VehicleStatus status) {
    // TODO
    executeUpdate("UPDATE VEHICLE SET STATUS = ? WHERE SKU_ID = ?",
        statement -> {
          statement.setString(1, status.name());
          statement.setInt(2, skuId);
        });
  }

  public static List<Vehicle> selectSaleVehicleByLocationId(Integer serviceLocationId) {
    return execute(
        "SELECT VEHICLE_STOCK.VEHICLE_ID, S.NAME AS SKU_NAME, S.MODEL_NAME, S.RESELL_PRICE\n"
            + "FROM VEHICLE_STOCK\n"
            + "       JOIN VEHICLE V on VEHICLE_STOCK.VEHICLE_ID = V.VEHICLE_ID\n"
            + "       JOIN SKU S on V.SKU_ID = S.SKU_ID\n"
            + "WHERE LOCATION_ID = ?",
        statement -> statement.setInt(1, serviceLocationId),
        result -> new Vehicle(
            result.getInt("VEHICLE_ID"),
            result.getString("MODEL_NAME"),
            result.getString("SKU_NAME"),
            result.getDouble("RESELL_PRICE")
        ));
  }

  public static Manager selectManager(String name) {
    List<Manager> managers = execute(
        "SELECT USER_ID, \"USER\".NAME, PASSWORD, EMAIL, S.SERVICE_LOCATION_ID, S.NAME AS SERVICE_NAME\n"
            + "FROM \"USER\"\n"
            + "       LEFT JOIN SERVICE_LOCATION S on \"USER\".USER_ID = S.MANAGER_USER_ID\n"
            + "WHERE TYPE = 'MANAGER'\n"
            + "AND \"USER\".NAME = ?",
        statement -> statement.setString(1, name),
        result -> {
          ServiceLocation serviceLocation = new ServiceLocation(
              result.getInt("SERVICE_LOCATION_ID"),
              result.getString("SERVICE_NAME"));
          if (result.getString("SERVICE_NAME") == null) {
            serviceLocation = null;
          }
          return new Manager(
              result.getInt("USER_ID"),
              result.getString("NAME"),
              result.getString("EMAIL"),
              result.getString("PASSWORD"),
              serviceLocation
          );
        });

    if (managers == null || managers.size() == 0) {
      return null;
    }

    return managers.get(0);
  }

  public static List<Sku> selectSkus() {
    return execute(
        "SELECT SKU.SKU_ID, SKU.NAME, CONFIG, MODEL_NAME, PRICE, RESELL_PRICE, REPAIR_PRICE\n"
            + "FROM (SELECT SKU_ID, LISTAGG(CONFIG, ', ') WITHIN GROUP (ORDER BY CONFIG) AS CONFIG\n"
            + "      FROM SKU_INCLUDES\n"
            + "      GROUP BY SKU_ID) C\n"
            + "JOIN SKU ON SKU.SKU_ID = C.SKU_ID\n",
        NOP,
        result -> new Sku(
            result.getInt("SKU_ID"),
            result.getString("MODEL_NAME"),
            result.getString("NAME"),
            result.getString("CONFIG"),
            result.getDouble("PRICE"),
            result.getDouble("RESELL_PRICE"),
            result.getDouble("REPAIR_PRICE")
        ));
  }

  public static List<Customer> selectOwnerBySkuIdAndDate(
      Integer skuId, Date fromDate, Date toDate) {
    return execute("SELECT DISTINCT U.NAME, U.EMAIL\n"
            + "FROM SALE\n"
            + "       JOIN \"USER\" U on SALE.BUYER_ID = U.USER_ID\n"
            + "       JOIN VEHICLE V on SALE.VEHICLE_ID = V.VEHICLE_ID\n"
            + "WHERE SKU_ID = ? AND SALE_DATE BETWEEN ? AND ?",
        statement -> {
          statement.setInt(1, skuId);
          statement.setDate(2, new java.sql.Date(fromDate.getTime()));
          statement.setDate(3, new java.sql.Date(toDate.getTime()));
        },
        result -> {
          Customer customer = new Customer();
          customer.setName(result.getString("NAME"));
          customer.setEmail(result.getString("EMAIL"));
          return customer;
        });
  }

  public static List<Customer> selectOverDue() {
    return execute("SELECT U.NAME, U.EMAIL\n"
            + "FROM SALE\n"
            + "       JOIN VEHICLE V on SALE.VEHICLE_ID = V.VEHICLE_ID\n"
            + "       JOIN SKU S on V.SKU_ID = S.SKU_ID\n"
            + "       JOIN MODEL M on S.MODEL_NAME = M.NAME\n"
            + "       JOIN \"USER\" U on SALE.BUYER_ID = U.USER_ID\n"
            + "WHERE EXTRACT(YEAR FROM SYSDATE) - EXTRACT(YEAR FROM SALE_DATE) > M.MAINTENANCE_PERIOD \n"
            + "  AND SALE.STATUS != 'RECALLED'",
        NOP,
        result -> {
          Customer customer = new Customer();
          customer.setName(result.getString("NAME"));
          customer.setEmail(result.getString("EMAIL"));
          return customer;
        });
  }

  public static Admin selectAdmin(String name) {
    List<Admin> managers = execute(
        "SELECT USER_ID, \"USER\".NAME, PASSWORD, EMAIL\n"
            + "FROM \"USER\"\n"
            + "WHERE \"USER\".NAME = ?\n"
            + "  AND TYPE = 'ADMINISTRATOR'",
        statement -> statement.setString(1, name),
        result -> new Admin(
            result.getString("NAME"),
            result.getString("EMAIL"),
            result.getString("PASSWORD")
        ));

    if (managers == null || managers.size() == 0) {
      return null;
    }

    return managers.get(0);
  }

  public static Integer recallAmount() {
    return execute("SELECT COUNT(1) AS CNT FROM VEHICLE WHERE STATUS IN ('RECALL', 'RECALLED')",
        NOP,
        result -> result.getInt("CNT"))
        .get(0);
  }

  public static Integer unsoldRecall() {
    return execute("SELECT COUNT(1) AS CNT\n"
            + "FROM VEHICLE\n"
            + "       JOIN VEHICLE_STOCK S on VEHICLE.VEHICLE_ID = S.VEHICLE_ID\n"
            + "WHERE VEHICLE.STATUS IN ('RECALL', 'RECALLED')\n"
            + "  AND S.LOCATION_ID IS NOT NULL",
        NOP,
        result -> result.getInt("CNT"))
        .get(0);
  }

  public static Integer recallStatus(SaleStatus status) {
    return execute("SELECT COUNT(1) AS CNT\n"
            + "FROM VEHICLE\n"
            + "       JOIN SALE S on VEHICLE.VEHICLE_ID = S.VEHICLE_ID\n"
            + "WHERE VEHICLE.STATUS IN ('RECALL', 'RECALLED')\n"
            + "  AND S.STATUS = ?",
        statement -> statement.setString(1, status.name()),
        result -> result.getInt("CNT"))
        .get(0);
  }

  public static List<Manager> selectManagers() {
    return execute(
        "SELECT USER_ID, \"USER\".NAME, PASSWORD, EMAIL, S.SERVICE_LOCATION_ID, S.NAME AS SERVICE_NAME\n"
            + "FROM \"USER\"\n"
            + "      LEFT JOIN SERVICE_LOCATION S on \"USER\".USER_ID = S.MANAGER_USER_ID\n"
            + "WHERE TYPE = 'MANAGER'",
        NOP,
        result -> {
          ServiceLocation serviceLocation = new ServiceLocation(
              result.getInt("SERVICE_LOCATION_ID"),
              result.getString("SERVICE_NAME")
          );
          if (result.getString("SERVICE_NAME") == null) {
            serviceLocation = null;
          }
          return new Manager(
              result.getInt("USER_ID"),
              result.getString("NAME"),
              result.getString("EMAIL"),
              result.getString("PASSWORD"),
              serviceLocation
          );
        });
  }

  public static void insertUser(String name, String email, String password, String type) {
    executeUpdate("INSERT INTO \"USER\" (\"NAME\", EMAIL, PASSWORD, \"TYPE\")\n"
            + "    VALUES (?, ?, ?, ?)",
        statement -> {
          statement.setString(1, name);
          statement.setString(2, email);
          statement.setString(3, password);
          statement.setString(4, type);
        });
  }

  public static List<Manager> selectManagerWithOutManaging() {
    return execute(
        "SELECT USER_ID, \"USER\".NAME, PASSWORD, EMAIL\n"
            + "FROM \"USER\"\n"
            + "       LEFT JOIN SERVICE_LOCATION S on \"USER\".USER_ID = S.MANAGER_USER_ID\n"
            + "WHERE TYPE = 'MANAGER'\n"
            + "  AND S.SERVICE_LOCATION_ID IS NULL",
        NOP,
        result -> new Manager(
            result.getInt("USER_ID"),
            result.getString("NAME"),
            result.getString("EMAIL"),
            result.getString("PASSWORD"),
            null
        ));
  }

  public static void insertServiceSupports(Integer serviceId, String modelName) {
    executeUpdate("INSERT INTO SERVICE_SUPPORTS (SERVICE_LOCATION_ID, MODEL_NAME) VALUES (?, ?)",
        statement -> {
          statement.setInt(1, serviceId);
          statement.setString(2, modelName);
        });
  }

  public static void updateMaintenancePeriod(String modelName, int period) {
    executeUpdate("UPDATE MODEL SET MAINTENANCE_PERIOD = ? WHERE NAME = ?",
        statement -> {
          statement.setInt(1, period);
          statement.setString(2, modelName);
        });
  }

  public static void updatePrice(int skuId, double newPrice) {
    executeUpdate("UPDATE SKU SET PRICE = ? WHERE SKU_ID = ?",
        statement -> {
          statement.setDouble(1, newPrice);
          statement.setInt(2, skuId);
        });
  }

  public static void updateResellPrice(int skuId, double newPrice) {
    executeUpdate("UPDATE SKU SET RESELL_PRICE = ? WHERE SKU_ID = ?",
        statement -> {
          statement.setDouble(1, newPrice);
          statement.setInt(2, skuId);
        });
  }

  public static void updateRepairPrice(int skuId, double newPrice) {
    executeUpdate("UPDATE SKU SET REPAIR_PRICE = ? WHERE SKU_ID = ?",
        statement -> {
          statement.setDouble(1, newPrice);
          statement.setInt(2, skuId);
        });
  }

  public static List<Tuple<Integer, Double>> sumSaleByYear() {
    return execute(
        "SELECT EXTRACT(YEAR FROM SALE_DATE) AS \"YEAR\", SUM(FACT_PRICE) AS PRICE FROM SALE GROUP BY EXTRACT(YEAR FROM SALE_DATE)",
        NOP, result ->
            new Tuple<>(
                result.getInt("YEAR"),
                result.getDouble("PRICE")
            ));
  }

  public interface ResultHandler<R, T> {

    T apply(R result) throws SQLException;
  }

  public interface StatementFiller<T extends Statement> {

    void fill(T statement) throws SQLException;
  }
}
