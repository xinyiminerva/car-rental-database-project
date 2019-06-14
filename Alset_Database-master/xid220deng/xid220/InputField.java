import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InputField {

  String NULL_STRING = "\ue001";

  String name() default NULL_STRING;

  boolean required() default true;

  String defaultValue() default NULL_STRING;

  /**
   * @return For String or Number, checks the length.
   */
  int max();

  /**
   * @return For String or Number, checks the length.
   */
  int min();

  String[] choices() default {};

  TargetType target() default TargetType.STRING;

  boolean password() default false;
}
