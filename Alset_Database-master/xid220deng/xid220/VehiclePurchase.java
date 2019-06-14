import java.util.Date;

public class VehiclePurchase {

  private final Integer vehicleId;
  private final Date saleDate;
  private final Double factPrice;
  private final String modelName;
  private String skuName;
  private String saleStatus;
  private String vehicleStatus;

  public VehiclePurchase(Integer vehicleId, Date saleDate, Double factPrice,
      String modelName, String skuName, String saleStatus, String vehicleStatus) {
    this.vehicleId = vehicleId;
    this.saleDate = saleDate;
    this.factPrice = factPrice;
    this.modelName = modelName;
    this.skuName = skuName;
    this.saleStatus = saleStatus;
    this.vehicleStatus = vehicleStatus;
  }

  public Integer getVehicleId() {
    return vehicleId;
  }

  public Date getSaleDate() {
    return saleDate;
  }

  public Double getFactPrice() {
    return factPrice;
  }

  public String getModelName() {
    return modelName;
  }

  public String getSkuName() {
    return skuName;
  }

  public void setSkuName(String skuName) {
    this.skuName = skuName;
  }

  public String getSaleStatus() {
    return saleStatus;
  }

  public void setSaleStatus(String saleStatus) {
    this.saleStatus = saleStatus;
  }

  public String getVehicleStatus() {
    return vehicleStatus;
  }

  public void setVehicleStatus(String vehicleStatus) {
    this.vehicleStatus = vehicleStatus;
  }
}
