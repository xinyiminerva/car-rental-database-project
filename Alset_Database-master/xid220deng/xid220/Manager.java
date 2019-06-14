public class Manager {

  private Integer managerId;
  @InputField(min = 1, max = 64)
  private String name;
  @InputField(min = 1, max = 255)
  private String email;
  @InputField(min = 1, max = 32)
  private String password;
  private final String type = UserType.MANAGER.name();
  private ServiceLocation serviceLocation;

  public Manager() {
  }

  public Manager(Integer managerId, String name, String email, String password,
      ServiceLocation serviceLocation) {
    this.managerId = managerId;
    this.name = name;
    this.email = email;
    this.password = password;
    this.serviceLocation = serviceLocation;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getType() {
    return type;
  }

  public ServiceLocation getServiceLocation() {
    return serviceLocation;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Integer getManagerId() {
    return managerId;
  }

  public void setManagerId(Integer managerId) {
    this.managerId = managerId;
  }
}