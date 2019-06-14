public enum PaymentType {

  CREDIT_CARD("CREDIT"),
  DEBIT_CARD("DEBIT"),
  ;

  public static final String CREDIT = "CREDIT";
  public static final String DEBIT = "DEBIT";

  private final String key;

  PaymentType(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public static PaymentType byKey(String key) {
    if (key == null) {
      return null;
    }
    switch (key) {
      case CREDIT:
        return CREDIT_CARD;
      case DEBIT:
        return DEBIT_CARD;
      default:
        return null;
    }
  }
}
