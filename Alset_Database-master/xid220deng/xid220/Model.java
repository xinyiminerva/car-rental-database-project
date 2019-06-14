public class Model {

  private final String name;
  private final String description;
  private final Integer maintenancePeriod;

  public Model(String name, String description, Integer maintenancePeriod) {
    this.name = name;
    this.description = description;
    this.maintenancePeriod = maintenancePeriod;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Integer getMaintenancePeriod() {
    return maintenancePeriod;
  }
}
