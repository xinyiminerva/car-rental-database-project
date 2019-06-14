public class Vehicle {

  private final Integer vehicleId;
  private final String modelName;
  private final String config;
  private final Double price;

  public Vehicle(Integer vehicleId, String modelName, String config, Double price) {
    this.vehicleId = vehicleId;
    this.modelName = modelName;
    this.config = config;
    this.price = price;
  }

  public String getModelName() {
    return modelName;
  }

  public String getConfig() {
    return config;
  }

  public Double getPrice() {
    return price;
  }

  public Integer getVehicleId() {
    return vehicleId;
  }
}
