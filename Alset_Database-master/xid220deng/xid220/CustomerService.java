import java.math.BigDecimal;
import java.sql.Types;

public class CustomerService {

  public Customer register(Customer customer) {
    Customer exist = findCustomer(customer.getName());
    if (exist != null) {
      return exist;
    }
    customer.setPassword(Utils.md5(customer.getPassword()));

    AlsetDAO.call("{call REGISTER(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}",
        statement -> {
          statement.setString(1, customer.getName());
          statement.setString(2, customer.getEmail());
          statement.setString(3, customer.getPassword());
          statement.setString(4, customer.getType());
          statement.setString(5, customer.getGalaxy());
          statement.setString(6, customer.getPlanet());
          statement.setString(7, customer.getCountry());
          statement.setString(8, customer.getState());
          statement.setString(9, customer.getCity());
          statement.setString(10, customer.getStreet());
          statement.setString(11, customer.getPaymentType());
          statement.setBigDecimal(12, new BigDecimal(customer.getCardNum()));
          statement.setBigDecimal(13, new BigDecimal(customer.getCvv().toString()));

          statement.registerOutParameter(14, Types.INTEGER);
        }, statement -> statement.getInt(14));

    return customer;
  }

  public Customer findCustomer(String name) {
    if (name == null || name.trim().length() == 0) {
      return null;
    }

    return AlsetDAO.selectCustomerByName(name);
  }

  public void purchase(PurchaseNew purchaseNew) {
    AlsetDAO.call("{call PURCHASE_NEW(?, ?, ?)}",
        statement -> {
          statement.setString(1, purchaseNew.getCustomerName());
          statement.setInt(2, purchaseNew.getSkuId());
          statement.setInt(3, purchaseNew.getPickupLocationId());

        }, state -> null);
  }

  public void repair(NewRepair newRepair) {
    AlsetDAO.call("{call NEW_REPAIR(?, ?)}",
        statement -> {
          statement.setInt(1, newRepair.getVehicleId());
          statement.setInt(2, newRepair.getServiceLocationId());

        }, state -> null);
  }

  public void changeStatus(Integer vehicleId, SaleStatus status, VehicleStatus vehicleStatus) {
    AlsetDAO.updateSaleStatus(vehicleId, status.name());
    AlsetDAO.updateVehicleStatus(vehicleId, vehicleStatus.name());
  }

  public void purchase(PurchaseUsed purchaseUsed) {
    AlsetDAO.call("{call PURCHASE_USED(?, ?)}",
        statement -> {
          statement.setString(1, purchaseUsed.getCustomerName());
          statement.setInt(2, purchaseUsed.getVehicleId());

        }, state -> null);
  }
}
