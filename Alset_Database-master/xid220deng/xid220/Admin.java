public class Admin {

  @InputField(min = 1, max = 64)
  private final String name;
  @InputField(min = 1, max = 255)
  private final String email;
  @InputField(min = 1, max = 32)
  private final String password;
  private final String type = UserType.MANAGER.name();

  public Admin(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
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
}
