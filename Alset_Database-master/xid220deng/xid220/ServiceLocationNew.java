public class ServiceLocationNew {

  @InputField(min = 1, max = 64)
  private String name;
  @InputField(min = 1, max = 255, defaultValue = "Milky Way")
  private String galaxy;
  @InputField(min = 1, max = 255, defaultValue = "Earth")
  private String planet;
  @InputField(min = 1, max = 255, defaultValue = "United States")
  private String country;
  @InputField(min = 1, max = 255)
  private String state;
  @InputField(min = 1, max = 255)
  private String city;
  @InputField(min = 1, max = 255)
  private String street;

  public ServiceLocationNew() {
  }

  public ServiceLocationNew(String name, String galaxy, String planet, String country, String state,
      String city, String street) {
    this.name = name;
    this.galaxy = galaxy;
    this.planet = planet;
    this.country = country;
    this.state = state;
    this.city = city;
    this.street = street;
  }

  public String getName() {
    return name;
  }

  public String getGalaxy() {
    return galaxy;
  }

  public String getPlanet() {
    return planet;
  }

  public String getCountry() {
    return country;
  }

  public String getState() {
    return state;
  }

  public String getCity() {
    return city;
  }

  public String getStreet() {
    return street;
  }
}
