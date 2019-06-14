import java.util.List;

public class ServiceLocation {

  private final Integer serviceLocationId;
  private String name;
  private Address address;
  private List<Model> supports;

  public ServiceLocation(Integer serviceLocationId, String name) {
    this.serviceLocationId = serviceLocationId;
    this.name = name;
  }

  public ServiceLocation(Integer serviceLocationId, String name, Address address) {
    this.serviceLocationId = serviceLocationId;
    this.name = name;
    this.address = address;
  }

  public Integer getServiceLocationId() {
    return serviceLocationId;
  }

  public Address getAddress() {
    return address;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Model> getSupports() {
    return supports;
  }

  public void setSupports(List<Model> supports) {
    this.supports = supports;
  }
}
