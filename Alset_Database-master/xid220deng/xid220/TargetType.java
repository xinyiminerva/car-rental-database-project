import java.math.BigInteger;

public enum TargetType {
  STRING(String.class),
  BIG_NUMBER(BigInteger.class),
  NUMBER(Integer.class),
  DECIMAL(Double.class),
  BOOLEAN(Boolean.class),

  ;

  private final Class<?> clazz;

  TargetType(Class<?> clazz) {
    this.clazz = clazz;
  }

  public Class<?> getClazz() {
    return clazz;
  }
}
