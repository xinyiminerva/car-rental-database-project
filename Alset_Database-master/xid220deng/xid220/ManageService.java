import java.util.Date;
import java.util.List;

public class ManageService {

  public void showroomNew(PurchaseNew purchaseNew) {
    AlsetDAO.call("{call SHOWROOM_NEW(?, ?)}",
        statement -> {
          statement.setInt(1, purchaseNew.getSkuId());
          statement.setInt(2, purchaseNew.getPickupLocationId());
        }, state -> null);
  }

  public List<Customer> recall(Recall recall) {
    Integer skuId = recall.getSkuId();
    Date fromDate = recall.getFromDate();
    Date toDate = recall.getToDate();

    List<Customer> customers = AlsetDAO.selectOwnerBySkuIdAndDate(skuId, fromDate, toDate);

    AlsetDAO.updateVehicleStatusBySkuIdAndDate(skuId, fromDate, toDate, VehicleStatus.RECALL);

    return customers;
  }

  public Manager findManager(String name) {
    return AlsetDAO.selectManager(name);
  }

  public void resell(int vehicleId) {
    AlsetDAO.call("{call RESELL(?)}",
        statement -> statement.setInt(1, vehicleId),
        state -> null);
  }

  public List<Customer> findMaintenanceVehicle() {
    return AlsetDAO.selectOverDue();
  }
}
