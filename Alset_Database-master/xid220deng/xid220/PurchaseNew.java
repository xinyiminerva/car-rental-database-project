public class PurchaseNew {

  private String customerName;
  private Integer skuId;
  private Integer pickupLocationId;

  public PurchaseNew() {
  }

  public PurchaseNew(Integer skuId, Integer pickupLocationId) {
    this.skuId = skuId;
    this.pickupLocationId = pickupLocationId;
  }

  public Integer getSkuId() {
    return skuId;
  }

  public void setSkuId(Integer skuId) {
    this.skuId = skuId;
  }

  public Integer getPickupLocationId() {
    return pickupLocationId;
  }

  public void setPickupLocationId(Integer pickupLocationId) {
    this.pickupLocationId = pickupLocationId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }
}
