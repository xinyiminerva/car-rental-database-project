import java.io.PrintStream;
import java.util.Scanner;

public abstract class DoWork extends BaseCli {

  DoWork(Scanner input, PrintStream output) {
    super(input, output);
  }

  public abstract void doWork();

  /**
   * Get new instance of this class by Context.userType
   */
  public static DoWork newInstance(Scanner input, PrintStream output) {
    UserType userType = ContextHolder.getContext().getUserType();

    switch (userType) {
      case CUSTOMER:
        return new DoCustomerWork(input, output);
      case MANAGER:
        return new DoManagerWork(input, output);
      case ADMINISTRATOR:
        return new DoAdminWork(input, output);
      default:
        throw new RuntimeException("Unsupported user type.");
    }
  }
}
