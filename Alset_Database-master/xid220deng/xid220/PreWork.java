import java.io.PrintStream;
import java.util.Scanner;

class PreWork extends BaseCli {

  public PreWork(Scanner input, PrintStream output) {
    super(input, output);
  }

  /**
   * Initialize the {@link Context} object.
   */
  public void doPre() {
    Context context = null;
    while (context == null) {
      try {
        String username = doPrompt("Enter your Oracle database username: ");
        String password = doPromptPassword("Enter your Oracle database password: ");
        context = new Context(username, password);
      } catch (RuntimeException e) {
        System.out.println("Wrong username or password. Please try again.");
      }
    }

    int userType = doSelection("Choose your User Type", UserType.USER_TYPE_NAMES);
    if (userType == -1) {
      System.exit(0);
    }

    context.setUserType(UserType.byIndex(userType));

    ContextHolder.setContext(context);
  }
}
