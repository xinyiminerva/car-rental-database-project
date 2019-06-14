import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Scanner;
import org.junit.Test;

public class BaseCliTest {

  @Test
  public void printMenuAndSelect() {

    BaseCli baseCli = new BaseCli(new Scanner(new StringReader("1\n")), System.out);

    assertEquals(1, baseCli.doSelection("Main", Arrays.asList("Item1", "Item2", "Item3")));

  }
}