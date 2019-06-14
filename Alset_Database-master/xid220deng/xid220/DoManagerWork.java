import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DoManagerWork extends DoWork {

  private final Map<String, Runnable> availableCommands = new LinkedHashMap<>();
  private List<String> commandKeys;

  private final ManageService manageService = new ManageService();

  public DoManagerWork(Scanner input, PrintStream output) {
    super(input, output);
  }

  @Override
  public void doWork() {
    Manager manager;
    while (true) {
      String name = doPrompt("Enter your username: ");
      manager = manageService.findManager(name);

      if (manager != null) {
        String password = doPromptPassword("Enter your password: ");
        if (Objects.equals(Utils.md5(password), manager.getPassword())) {
          if (manager.getServiceLocation() != null) {
            break;
          }

          output.println("You are not in charge of any Service Location. Please try again.");
        } else {
          output.println("Wrong password. Please try again.");
        }
      } else {
        output.println("Unknown username. Please try again.");
      }
    }

    updateAvailableCommands(manager.getServiceLocation().getServiceLocationId());

    while (true) {
      int selection = doSelection("Manager Portal", commandKeys);
      if (selection == -1) {
        break;
      }
      String command = commandKeys.get(selection);

      Runnable runnable = availableCommands.get(command);

      runnable.run();
    }
  }

  private void updateAvailableCommands(Integer serviceLocationId) {
    availableCommands.clear();

    availableCommands.put("View Orders", () -> {
      List<Sale> sales = AlsetDAO.selectSalesByLocationId(serviceLocationId);

      if (sales.size() == 0) {
        output.println("No orders at now.");
        return;
      }

      printMatrix("Orders at now: ",
          Arrays
              .asList("Buyer's Name", "Sale Date", "Sale Price", "Model Name", "Config", "Status"),
          sales.stream().map(sale -> Arrays.asList(
              sale.getBuyerName(),
              sale.getSaleDate().toString(),
              sale.getFactPrice().toString(),
              sale.getModelName(),
              sale.getSkuName(),
              sale.getSaleStatus().name()
          )).collect(Collectors.toList()));
    });

    availableCommands.put("Show Room", () -> {
      List<Vehicle> vehicles = AlsetDAO
          .selectShowVehicleByLocationId(serviceLocationId);

      if (vehicles.size() == 0) {
        output.println("No available vehicles now.");
        return;
      }

      printMatrix("Show Room: ",
          Arrays.asList("Model Name", "Config", "Sale Price"),
          vehicles.stream().map(sale -> Arrays.asList(
              sale.getModelName(),
              sale.getConfig(),
              sale.getPrice().toString()
          )).collect(Collectors.toList()));
    });

    availableCommands.put("Manage", () -> updateCommandAsManaging(serviceLocationId));

    availableCommands.put("Exit", () -> System.exit(0));

    commandKeys = new ArrayList<>(availableCommands.keySet());
  }

  private void updateCommandAsManaging(Integer serviceLocationId) {
    availableCommands.clear();

    availableCommands.put("Order Show Vehicle", () -> {
      PurchaseNew purchaseNew = readPurchase(serviceLocationId);
      if (purchaseNew != null) {
        manageService.showroomNew(purchaseNew);
      }
    });

    availableCommands.put("Sell a Vehicle in Show Room", () -> {
      List<Vehicle> vehicles = AlsetDAO
          .selectShowVehicleByLocationId(serviceLocationId);

      if (vehicles.size() == 0) {
        output.println("No available vehicles now.");
        return;
      }

      int vid = doMatrixSelection("Show Room: ",
          Arrays.asList("Model Name", "Config", "Sale Price"),
          vehicles.stream().map(sale -> Arrays.asList(
              sale.getModelName(),
              sale.getConfig(),
              sale.getPrice().toString()
          )).collect(Collectors.toList()));

      int vehicleId = vehicles.get(vid).getVehicleId();

      manageService.resell(vehicleId);
    });

    availableCommands.put("Recall Vehicle", () -> {
      Recall recall = readRecall(serviceLocationId);
      if (recall == null) {
        return;
      }
      List<Customer> customers = manageService.recall(recall);

      output.println();
      printMatrix("The following customer will be notified.",
          Arrays.asList("Name", "Email"),
          customers.stream()
              .map(c -> Arrays.asList(c.getName(), c.getEmail()))
              .collect(Collectors.toList()));

      output.println("Successfully recalled.");
    });

    availableCommands.put("View Maintenance Vehicle", () -> {
      List<Customer> customers = manageService.findMaintenanceVehicle();

      output.println();
      printMatrix("The following customer will be notified.",
          Arrays.asList("Name", "Email"),
          customers.stream()
              .map(c -> Arrays.asList(c.getName(), c.getEmail()))
              .collect(Collectors.toList()));
    });

    availableCommands.put("Back", () -> updateAvailableCommands(serviceLocationId));

    availableCommands.put("Exit", () -> System.exit(0));

    commandKeys = new ArrayList<>(availableCommands.keySet());
  }

  private Recall readRecall(Integer serviceLocationId) {
    String modelName;
    int skuId;

    List<Model> models = AlsetDAO.selectSupportModels(serviceLocationId);
    int modelIndex = doMatrixSelection("Available Model(s): ",
        Arrays.asList("Model Name", "Description", "Maintenance Period(yr)"),
        models.stream()
            .map(model -> Arrays
                .asList(model.getName(), model.getDescription(),
                    model.getMaintenancePeriod().toString()))
            .collect(Collectors.toList()));
    if (modelIndex == -1) {
      return null;
    }
    modelName = models.get(modelIndex).getName();

    List<Sku> skus = AlsetDAO.selectSkus(modelName);
    int skuIndex = doMatrixSelection("Available Config Bundles: ",
        Arrays.asList("Name", "Configs", "Price"),
        skus.stream()
            .map(sku -> Arrays.asList(sku.getName(), sku.getConfigs(), sku.getPrice().toString()))
            .collect(Collectors.toList()));
    if (skuIndex == -1) {
      return null;
    }
    skuId = skus.get(skuIndex).getSkuId();

    Date from = doReadDate("Enter the date from(eg. 2018-01-01): ");
    Date to = doReadDate("Enter the date to(eg. 2018-01-01): ");

    return new Recall(skuId, from, to);
  }

  private PurchaseNew readPurchase(Integer serviceLocationId) {
    String modelName;
    int skuId;
    int pickupLocationId;

    List<Model> models = AlsetDAO.selectSupportModels(serviceLocationId);
    int modelIndex = doMatrixSelection("Available Model(s): ",
        Arrays.asList("Model Name", "Description", "Maintenance Period(yr)"),
        models.stream()
            .map(model -> Arrays
                .asList(model.getName(), model.getDescription(),
                    model.getMaintenancePeriod().toString()))
            .collect(Collectors.toList()));
    if (modelIndex == -1) {
      return null;
    }
    modelName = models.get(modelIndex).getName();

    List<Sku> skus = AlsetDAO.selectSkus(modelName);
    int skuIndex = doMatrixSelection("Available Config Bundles: ",
        Arrays.asList("Name", "Configs", "Price"),
        skus.stream()
            .map(sku -> Arrays.asList(sku.getName(), sku.getConfigs(), sku.getPrice().toString()))
            .collect(Collectors.toList()));
    if (skuIndex == -1) {
      return null;
    }
    skuId = skus.get(skuIndex).getSkuId();

    pickupLocationId = serviceLocationId;

    return new PurchaseNew(skuId, pickupLocationId);
  }
}
