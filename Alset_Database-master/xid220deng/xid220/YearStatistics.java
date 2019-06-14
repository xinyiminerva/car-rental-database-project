public class YearStatistics {

  private final int year;
  private final double sell;

  public YearStatistics(int year, double sell) {
    this.year = year;
    this.sell = sell;
  }

  public Integer getYear() {
    return year;
  }

  public Double getSell() {
    return sell;
  }
}
