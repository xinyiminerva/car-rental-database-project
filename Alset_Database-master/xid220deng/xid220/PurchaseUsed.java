public class PurchaseUsed {

  private final Integer vehicleId;
  private final String customerName;

  public PurchaseUsed(Integer vehicleId, String customerName) {
    this.vehicleId = vehicleId;
    this.customerName = customerName;
  }

  public Integer getVehicleId() {
    return vehicleId;
  }

  public String getCustomerName() {
    return customerName;
  }
}
