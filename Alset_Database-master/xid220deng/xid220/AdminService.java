import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminService {

  public Admin findAdmin(String name) {
    return AlsetDAO.selectAdmin(name);
  }

  public List<Customer> recall(Recall recall) {
    Integer skuId = recall.getSkuId();
    Date fromDate = recall.getFromDate();
    Date toDate = recall.getToDate();

    List<Customer> customers = AlsetDAO.selectOwnerBySkuIdAndDate(skuId, fromDate, toDate);

    AlsetDAO.updateVehicleStatusBySkuIdAndDate(skuId, fromDate, toDate, VehicleStatus.RECALL);

    return customers;
  }

  public Map<VehicleStatus, Integer> recallStatus() {
    Map<VehicleStatus, Integer> resultMap = new HashMap<>();

    resultMap.put(VehicleStatus.RECALL, AlsetDAO.recallAmount());
    resultMap.put(VehicleStatus.WAIT_PICKUP, AlsetDAO.recallStatus(SaleStatus.WAIT_PICKUP));
    resultMap.put(VehicleStatus.SELLING, AlsetDAO.unsoldRecall());

    return resultMap;
  }

  public void addServiceLocation(ServiceLocationNew locationNew, Integer managerId, List<String> supportModels) {
    Integer serviceId = AlsetDAO.call("{call NEW_SERVICE_LOCATION(?, ?, ?, ?, ?, ?, ?, ?, ?)}",
        statement -> {
          statement.setInt(1, managerId);
          statement.setString(2, locationNew.getName());
          statement.setString(3, locationNew.getGalaxy());
          statement.setString(4, locationNew.getPlanet());
          statement.setString(5, locationNew.getCountry());
          statement.setString(6, locationNew.getState());
          statement.setString(7, locationNew.getCity());
          statement.setString(8, locationNew.getStreet());
          statement.registerOutParameter(9, Types.INTEGER);
        }, statement -> statement.getInt(9));

    for (String modelName : supportModels) {
      AlsetDAO.insertServiceSupports(serviceId, modelName);
    }
  }

  public void updateMaintenancePeriod(String modelName, int period) {
    AlsetDAO.updateMaintenancePeriod(modelName, period);
  }

  public void updatePrice(int skuId, double newPrice) {
    AlsetDAO.updatePrice(skuId, newPrice);
  }

  public void updateResellPrice(int skuId, double newPrice) {
    AlsetDAO.updateResellPrice(skuId, newPrice);
  }

  public void updateRepairPrice(int skuId, double newPrice) {
    AlsetDAO.updateRepairPrice(skuId, newPrice);
  }

  public List<YearStatistics> sellsByYear() {
    return AlsetDAO.sumSaleByYear()
        .stream()
        .map(t -> new YearStatistics(t.left, t.right))
        .collect(Collectors.toList());
  }

  public void addManager(Manager manager) {
    AlsetDAO.insertUser(manager.getName(), manager.getEmail(), manager.getPassword(),
        UserType.MANAGER.name());
  }
}
