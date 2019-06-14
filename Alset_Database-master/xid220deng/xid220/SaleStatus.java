public enum SaleStatus {
  WAIT_PICKUP,
  PICKUPED,
  RECALLED,
  ;

  public static SaleStatus byName(String name) {
    for (SaleStatus value : SaleStatus.values()) {
      if (value.name().equals(name)) {
        return value;
      }
    }
    return null;
  }
}
