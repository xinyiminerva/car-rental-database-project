public class Sku {

  private final int skuId;
  private final String modelName;
  private String name;

  /**
   * AUTO_PILOT,HI_RES_AUDIO, comma concat
   */
  private final String configs;
  private final Double price;
  private final Double resellPrice;
  private final Double repairPrice;

  public Sku(int skuId, String modelName, String name, String configs, Double price,
      Double resellPrice, Double repairPrice) {
    this.skuId = skuId;
    this.modelName = modelName;
    this.name = name;
    this.configs = configs;
    this.price = price;
    this.resellPrice = resellPrice;
    this.repairPrice = repairPrice;
  }

  public int getSkuId() {
    return skuId;
  }

  public String getModelName() {
    return modelName;
  }

  public String getConfigs() {
    return configs;
  }

  public Double getPrice() {
    return price;
  }

  public Double getResellPrice() {
    return resellPrice;
  }

  public Double getRepairPrice() {
    return repairPrice;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
