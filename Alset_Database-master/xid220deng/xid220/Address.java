public class Address {

  private final String galaxy;
  private final String planet;
  private final String country;
  private final String state;
  private final String city;
  private final String street;

  public Address(String galaxy, String planet, String country, String state, String city,
      String street) {
    this.galaxy = galaxy;
    this.planet = planet;
    this.country = country;
    this.state = state;
    this.city = city;
    this.street = street;
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

  @Override
  public String toString() {
    return String
        .format("Address{%s, %s, %s, %s, %s, %s}", street, city, state, country, planet, galaxy);
  }
}
