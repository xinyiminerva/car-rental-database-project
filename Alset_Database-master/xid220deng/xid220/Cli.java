import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Cli extends BaseCli {

  public Cli(InputStream input, PrintStream output) {
    super(new Scanner(input), output);
  }

  public void runCli() {
    try {
      new PreWork(input, output).doPre();
      DoWork doWork = DoWork.newInstance(input, output);
      doWork.doWork();
    } catch (RuntimeException e) {
      System.err.println("Runtime error occurred. System will exit now.");
      e.printStackTrace(System.err);
    }
  }
}
