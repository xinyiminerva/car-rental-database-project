import java.io.Console;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

class BaseCli {

  private static final String EXIT_SIGNAL = "exit";

  final Scanner input;
  final PrintStream output;

  public BaseCli(Scanner input, PrintStream output) {
    this.input = input;
    this.output = output;
  }

  String doPrompt(String prompt) {
    output.print(prompt);
    output.flush();
    return input.nextLine();
  }

  int doPromptInt(String prompt) {
    output.print(prompt);
    output.flush();

    while (true) {
      try {
        String input = this.input.nextLine();
        if (EXIT_SIGNAL.equals(input)) {
          return -1;
        }
        return Integer.parseInt(input);
      } catch (Exception ignored) {
      }
      output.printf("Invalid input. Please enter a number(type '%s' to break): ", EXIT_SIGNAL);
    }
  }

  double doPromptDouble(String prompt) {
    output.print(prompt);
    output.flush();

    while (true) {
      try {
        String input = this.input.nextLine();
        if (EXIT_SIGNAL.equals(input)) {
          return -1;
        }
        return Double.parseDouble(input);
      } catch (Exception ignored) {
      }
      output.printf("Invalid input. Please enter a decimal(type '%s' to break): ", EXIT_SIGNAL);
    }
  }

  String doPromptPassword(String prompt) {
    output.print(prompt);
    output.flush();
    Console console = System.console();
    if (console == null) {
      // for pseudo-tty, fall back to raw input.
      return input.nextLine();
    }
    return new String(console.readPassword());
  }

  Date doReadDate(String prompt) {
    output.print(prompt);
    output.flush();
    while (true) {
      String dateStr = input.nextLine();
      if (EXIT_SIGNAL.equals(dateStr)) {
        return null;
      }
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      try {
        return format.parse(dateStr);
      } catch (ParseException ignored) {
      }
      output.printf("Malformed date format. Please try again(type '%s' to break): ", EXIT_SIGNAL);
    }
  }

  /**
   * Available models: ID   Model    Price 1    Model U  5000.0 2    Model M  6000.0
   *
   * Enter a selection:
   *
   * @param header menu title
   * @param items items to list
   * @return entered selection, based on 0.
   */
  int doMatrixSelection(String title, List<String> header, List<List<String>> items) {
    printMatrix(title, header, items);

    if (items == null || items.size() == 0) {
      return -1;
    }
    output.print("Enter a selection: ");
    output.flush();

    while (true) {
      try {
        String input = this.input.nextLine();
        if (EXIT_SIGNAL.equals(input)) {
          return -1;
        }
        int choose = Integer.parseInt(input);
        if (choose >= 1 && choose <= items.size()) {
          return choose - 1;
        }
      } catch (Exception ignored) {
      }
      output
          .printf("Invalid input. Please enter a number between %d and %d(type '%s' to break): ", 1,
              items.size(), EXIT_SIGNAL);
    }
  }

  List<Integer> doMatrixMultiSelection(String title, List<String> header,
      List<List<String>> items) {
    printMatrix(title, header, items);

    if (items == null || items.size() == 0) {
      return null;
    }
    output.print("Enter selections(-1 to finish): ");
    output.flush();

    Set<Integer> result = new HashSet<>();
    while (true) {
      try {
        String input = this.input.nextLine();
        if (EXIT_SIGNAL.equals(input)) {
          return null;
        }
        int choose = Integer.parseInt(input);
        if (choose == -1) {
          return new ArrayList<>(result);
        }
        if (choose >= 1 && choose <= items.size()) {
          result.add(choose - 1);
          output.print("Enter selections(-1 to finish): ");
          output.flush();
        }
      } catch (Exception ignored) {
        output
            .printf("Invalid input. Please enter a number between %d and %d(type '%s' to break): ",
                1,
                items.size(), EXIT_SIGNAL);
      }
    }
  }


  void printMatrix(String title, List<String> header, List<List<String>> items) {
    output.println(title);
    List<Integer> maxLen = calcMaxLen(header, items);
    output.print("    ");
    for (int i = 0, headerSize = header.size(); i < headerSize; i++) {
      String s = header.get(i);
      output.printf("%" + maxLen.get(i) + "s    ", s);
    }
    output.println();
    if (items == null || items.size() == 0) {
      output.println("No data available.");
      return;
    }
    for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
      List<String> item = items.get(i);
      output.printf("%2d. ", i + 1);
      for (int j = 0, itemSize = item.size(); j < itemSize; j++) {
        String s = item.get(j);
        output.printf("%" + maxLen.get(j) + "s    ", s);
      }
      output.println();
    }
  }

  private List<Integer> calcMaxLen(List<String> header, List<List<String>> items) {
    List<Integer> maxLen = new ArrayList<>();
    for (int i = 0, size = header.size(); i < size; i++) {
      int max = header.get(i).length();
      for (List<String> item : items) {
        max = Math.max(max, item.get(i).length());
      }
      maxLen.add(i, max);
    }
    return maxLen;
  }

  void printSimple(String header, List<String> items) {
    output.println(header);
    if (items == null || items.size() == 0) {
      output.println("No data available.");
      return;
    }
    for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
      String item = items.get(i);
      output.printf("%d) %s\n", i + 1, item);
    }

  }

  /**
   * Available models: 1) Model M; 2) Model U; 3) Model T; Enter a selection:
   *
   * @param header menu title
   * @param items items to list
   * @return entered selection, based on 0.
   */
  int doSimpleSelection(String header, List<String> items) {
    printSimple(header, items);

    output.print("Enter a selection: ");
    output.flush();

    while (true) {
      try {
        String input = this.input.nextLine();
        if (EXIT_SIGNAL.equals(input)) {
          return -1;
        }
        int choose = Integer.parseInt(input);
        if (choose >= 1 && choose <= items.size()) {
          return choose - 1;
        }
      } catch (Exception ignored) {
      }
      output
          .printf("Invalid input. Please enter a number between %d and %d(type '%s' to break): ", 1,
              items.size(), EXIT_SIGNAL);
    }
  }


  /**
   * ************************* *       Main Menu       * *                       * * 1. Item1 * * 2.
   * Item2              * * 3. Item3              * *                       *
   * *************************
   *
   * @param title menu title
   * @param items items to list
   * @return entered selection, based on 0.
   */
  int doSelection(String title, List<String> items) {
    printSelection(title, items);
    output.print("Enter a selection: ");
    output.flush();

    while (true) {
      try {
        String input = this.input.nextLine();
        if (EXIT_SIGNAL.equals(input)) {
          return -1;
        }
        int choose = Integer.parseInt(input);
        if (choose >= 1 && choose <= items.size()) {
          return choose - 1;
        }
      } catch (Exception ignored) {
      }
      output
          .printf("Invalid input. Please enter a number between %d and %d(type '%s' to break): ", 1,
              items.size(), EXIT_SIGNAL);
    }
  }

  private void printSelection(String title, List<String> items) {
    StringBuilder menuBuilder = new StringBuilder();
    menuBuilder
        .append("*").append(paddingRight("", '*', 40)).append("*\n")
        .append("*").append(padding(title, ' ', 40)).append("*\n")
        .append("*").append(paddingRight("", ' ', 40)).append("*\n");

    for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
      String item = items.get(i);
      String content = String.format("%d. %s", i + 1, item);
      menuBuilder.append("* ").append(paddingRight(content, ' ', 39)).append("*\n");
    }
    menuBuilder.append("*").append(paddingRight("", ' ', 40)).append("*\n");
    menuBuilder.append("*").append(paddingRight("", '*', 40)).append("*\n");

    output.print(menuBuilder.toString());
  }

  private String padding(String str, char pad, int length) {
    int diff = length - str.length();

    if (diff <= 0) {
      return str;
    }

    StringBuilder result = new StringBuilder();
    for (int i = 0; i < diff / 2; i++) {
      result.append(pad);
    }
    result.append(str);

    for (int i = 0; i < diff / 2; i++) {
      result.append(pad);
    }

    if (diff % 2 == 1) {
      result.append(pad);
    }

    return result.toString();
  }

  private String paddingRight(String str, char pad, int length) {
    int diff = length - str.length();

    if (diff <= 0) {
      return str;
    }

    StringBuilder result = new StringBuilder();
    result.append(str);

    for (int i = 0; i < diff; i++) {
      result.append(pad);
    }

    return result.toString();
  }
}
