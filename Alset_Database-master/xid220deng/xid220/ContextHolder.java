public class ContextHolder {

  /**
   * Since it's a thread-local, this app currently doesn't support multi-threading.
   */
  private static final ThreadLocal<Context> context = ThreadLocal.withInitial(() -> null);

  public static Context getContext() {
    return context.get();
  }

  public static void setContext(Context context) {
    ContextHolder.context.set(context);
  }

  static {
    // add shutdown hook at Class initialization.
    Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
  }


  private static class ShutdownHook implements Runnable {

    @Override
    public void run() {
      Context context = ContextHolder.getContext();

      if (context != null) {
        context.release();
      }
    }
  }
}
