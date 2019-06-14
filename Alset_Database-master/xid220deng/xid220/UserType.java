import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserType {
  CUSTOMER,
  MANAGER,
  ADMINISTRATOR,

  ;

  public static final List<String> USER_TYPE_NAMES =
      Stream.of(UserType.values())
          .map(Enum::name)
          .collect(Collectors.toList());

  public static UserType byIndex(int index) {
    return UserType.valueOf(USER_TYPE_NAMES.get(index));
  }
}
