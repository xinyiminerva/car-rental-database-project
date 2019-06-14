import java.io.Console;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Scanner;

public class InputFlow {

  private static final String EXIT_SIGNAL = "exit";

  public static <T> T flow(Scanner reader, PrintStream writer, Class<T> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    boolean exit = false;

    try {
      writer.printf("Enter the information for %s (type '%s' to break).\n",
          clazz.getSimpleName(), EXIT_SIGNAL);
      writer.flush();
      T instance = clazz.newInstance();
      for (int fieldIndex = 0, fieldsLength = fields.length;
          fieldIndex < fieldsLength; fieldIndex++) {
        Field field = fields[fieldIndex];
        InputField fieldAnnotation = field.getAnnotation(InputField.class);

        if (!field.isAccessible()) {
          field.setAccessible(true);
        }

        if (fieldAnnotation == null || field.get(instance) != null) {
          // Not an InputField or has default value
          continue;
        }
        prompt(field, fieldAnnotation, writer);

        String line;
        Console console = System.console();
        if (fieldAnnotation.password() && console != null) {
          line = new String(console.readPassword());
        } else {
          line = reader.nextLine();
        }

        if (EXIT_SIGNAL.equalsIgnoreCase(line)) {
          exit = true;
          break;
        }

        Object value = resolve(field, fieldAnnotation, line);

        if (value == null) {
          if (fieldAnnotation.required()) {
            writer.println("Malformed input, please try again.");
            fieldIndex--;
          }
          continue;
        }

        boolean valid = validate(field, fieldAnnotation, value);

        if (!valid) {
          writer.println("Invalid input, please try again.");
          fieldIndex--;
          continue;
        }

        field.set(instance, value);
      }

      if (exit) {
        writer.println("Insufficient information was provided. Break now.");
        return null;
      }

      return instance;
    } catch (Exception e) {
      // It should not happen
      throw new RuntimeException(e);
    }
  }

  private static boolean validate(Field field, InputField fieldAnnotation, Object value) {
    String[] choices = fieldAnnotation.choices();
    if (choices.length > 0) {
      for (String choice : choices) {
        if (choice.equals(value)) {
          return true;
        }
      }
      return false;
    }

    int length = value.toString().length();
    return length >= fieldAnnotation.min() && length <= fieldAnnotation.max();
  }

  private static Object resolve(Field field, InputField fieldAnnotation, String line) {
    TargetType target = fieldAnnotation.target();
    if (line == null || line.trim().length() == 0) {
      line = fieldAnnotation.defaultValue();
    }

    if (InputField.NULL_STRING.equals(line)) {
      return null;
    }

    try {
      switch (target) {
        case STRING: {
          return line;
        }
        case NUMBER: {
          return Integer.valueOf(line);
        }
        case DECIMAL: {
          return Double.valueOf(line);
        }
        case BIG_NUMBER: {
          return new BigInteger(line);
        }
      }

      return target.getClazz().cast(line);
    } catch (Exception e) {
      return null;
    }
  }

  private static void prompt(Field field, InputField fieldAnnotation, PrintStream writer) {
    StringBuilder promptBuilder = new StringBuilder();
    promptBuilder.append("Enter the ");

    if (InputField.NULL_STRING.equals(fieldAnnotation.name())) {
      promptBuilder.append(field.getName());
    } else {
      promptBuilder.append(fieldAnnotation.name());
    }
    promptBuilder.append(" ");

    if (!InputField.NULL_STRING.equals(fieldAnnotation.defaultValue())) {
      promptBuilder.append("[default=");
      promptBuilder.append(fieldAnnotation.defaultValue());
      promptBuilder.append("] ");
    }

    String[] choices = fieldAnnotation.choices();
    if (choices.length > 0) {
      promptBuilder.append("(");
      for (int i = 0, choicesLength = choices.length; i < choicesLength; i++) {
        if (i != 0) {
          promptBuilder.append(", ");
        }
        String choice = choices[i];
        promptBuilder.append(choice);
      }
      promptBuilder.append(")");
    } else {
      promptBuilder.append("(");
      promptBuilder.append(fieldAnnotation.min());
      promptBuilder.append("~");
      promptBuilder.append(fieldAnnotation.max());
      promptBuilder.append(")");
    }

    promptBuilder.append(": ");
    writer.print(promptBuilder.toString());
    writer.flush();
  }
}
