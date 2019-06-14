import java.util.Date;

public class Sale {

  private final String buyerName;
  private final Date saleDate;
  private final Double factPrice;
  private final String modelName;
  private final String skuName;
  private final SaleStatus saleStatus;

  public Sale(String buyerName, Date saleDate, Double factPrice, String modelName,
      String skuName, SaleStatus saleStatus) {
    this.buyerName = buyerName;
    this.saleDate = saleDate;
    this.factPrice = factPrice;
    this.modelName = modelName;
    this.skuName = skuName;
    this.saleStatus = saleStatus;
  }

  public String getBuyerName() {
    return buyerName;
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

  public SaleStatus getSaleStatus() {
    return saleStatus;
  }
}
