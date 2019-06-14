public class NewRepair {

  private final Integer vehicleId;
  private final Integer serviceLocationId;

  public NewRepair(Integer vehicleId, Integer serviceLocationId) {
    this.vehicleId = vehicleId;
    this.serviceLocationId = serviceLocationId;
  }

  public Integer getVehicleId() {
    return vehicleId;
  }

  public Integer getServiceLocationId() {
    return serviceLocationId;
  }
}
