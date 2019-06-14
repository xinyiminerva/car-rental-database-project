import java.math.BigInteger;

public class Customer {

  @InputField(min = 1, max = 64)
  private String name;
  @InputField(min = 1, max = 255)
  private String email;
  @InputField(min = 1, max = 32, password = true)
  private String password;
  private String type = UserType.CUSTOMER.name();
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
  @InputField(min = 1, max = 6, choices = {PaymentType.CREDIT, PaymentType.DEBIT})
  private String paymentType;
  @InputField(min = 1, max = 32, target = TargetType.BIG_NUMBER)
  private BigInteger cardNum;
  @InputField(min = 1, max = 16, target = TargetType.BIG_NUMBER)
  private BigInteger cvv;

  public Customer() {
  }

  public Customer(String name, String email, String password, String galaxy, String planet,
      String country,
      String state, String city, String street, String paymentType, BigInteger cardNum,
      BigInteger cvv) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.galaxy = galaxy;
    this.planet = planet;
    this.country = country;
    this.state = state;
    this.city = city;
    this.street = street;
    this.paymentType = paymentType;
    this.cardNum = cardNum;
    this.cvv = cvv;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getGalaxy() {
    return galaxy;
  }

  public void setGalaxy(String galaxy) {
    this.galaxy = galaxy;
  }

  public String getPlanet() {
    return planet;
  }

  public void setPlanet(String planet) {
    this.planet = planet;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public BigInteger getCardNum() {
    return cardNum;
  }

  public void setCardNum(BigInteger cardNum) {
    this.cardNum = cardNum;
  }

  public BigInteger getCvv() {
    return cvv;
  }

  public void setCvv(BigInteger cvv) {
    this.cvv = cvv;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPaymentType() {
    return paymentType;
  }

  public void setPaymentType(String paymentType) {
    this.paymentType = paymentType;
  }

  @Override
  public String toString() {
    return "Customer{" +
        "name='" + name + '\'' +
        ", email='" + email + '\'' +
        ", galaxy='" + galaxy + '\'' +
        ", planet='" + planet + '\'' +
        ", country='" + country + '\'' +
        ", state='" + state + '\'' +
        ", city='" + city + '\'' +
        ", street='" + street + '\'' +
        ", cardNum=" + cardNum +
        ", cvv=" + cvv +
        '}';
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
