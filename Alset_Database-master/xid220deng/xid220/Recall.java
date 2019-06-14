import java.util.Date;

public class Recall {

  private final Integer skuId;
  private final Date fromDate;
  private final Date toDate;

  public Recall(Integer skuId, Date fromDate, Date toDate) {
    this.skuId = skuId;
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  public Integer getSkuId() {
    return skuId;
  }

  public Date getFromDate() {
    return fromDate;
  }

  public Date getToDate() {
    return toDate;
  }
}
