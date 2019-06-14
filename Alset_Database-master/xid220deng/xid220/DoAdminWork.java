import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DoAdminWork extends DoWork {

  private final AdminService adminService = new AdminService();

  private final Map<String, Runnable> availableCommands = new LinkedHashMap<>();
  private List<String> commandKeys;

  public DoAdminWork(Scanner input, PrintStream output) {
    super(input, output);
  }

  @Override
  public void doWork() {
    Admin admin;
    while (true) {
      String name = doPrompt("Enter your username: ");
      admin = adminService.findAdmin(name);

      if (admin != null) {
        String password = doPromptPassword("Enter your password: ");
        if (Objects.equals(Utils.md5(password), admin.getPassword())) {
          break;
        } else {
          output.println("Wrong password. Please try again.");
        }
      } else {
        output.println("Unknown username. Please try again.");
      }
    }

    updateAvailableCommands(admin);

    while (true) {
      int selection = doSelection("Admin Portal", commandKeys);
      if (selection == -1) {
        break;
      }
      String command = commandKeys.get(selection);

      Runnable runnable = availableCommands.get(command);

      runnable.run();
    }
  }

  private void updateAvailableCommands(Admin admin) {
    availableCommands.clear();

    availableCommands.put("View Manager(s)", () -> {
      List<Manager> managers = AlsetDAO.selectManagers();

      printMatrix("Choose a manager: ",
          Arrays.asList("Name", "Email", "Managing"),
          managers.stream()
              .map(m -> Arrays.asList(m.getName(), m.getEmail(),
                  m.getServiceLocation() == null ? "N/A" : m.getServiceLocation().getName()))
              .collect(Collectors.toList()));
    });

    availableCommands.put("Add Manager", () -> {
      Manager manager = InputFlow.flow(input, output, Manager.class);

      if (manager == null) {
        return;
      }

      manager.setPassword(Utils.md5(manager.getPassword()));

      adminService.addManager(manager);

      output.println("Successfully inserted.");
    });

    availableCommands.put("View Service Location(s)", () -> {
      List<ServiceLocation> serviceLocations = AlsetDAO.selectServiceLocation();
      printSimple("Service Location(s): ",
          serviceLocations.stream()
              .map(
                  serviceLocation -> serviceLocation.getName() + ": " + serviceLocation.getAddress()
                      .toString())
              .collect(Collectors.toList()));
    });

    availableCommands.put("Add Service Location", () -> {
      ServiceLocationNew locationNew = InputFlow.flow(input, output, ServiceLocationNew.class);
      if (locationNew == null) {
        return;
      }

      List<Manager> managers = AlsetDAO.selectManagerWithOutManaging();

      int mId = doMatrixSelection("Choose a manager: ",
          Arrays.asList("Name", "Email"),
          managers.stream()
              .map(m -> Arrays.asList(m.getName(), m.getEmail())).collect(Collectors.toList()));

      if (mId == -1) {
        return;
      }
      Integer managerId = managers.get(mId).getManagerId();

      List<Model> models = AlsetDAO.selectModels();

      List<Integer> modelIndex = doMatrixMultiSelection("Support Model(s): ",
          Arrays.asList("Model Name", "Description", "Maintenance Period(yr)"),
          models.stream()
              .map(model -> Arrays.asList(
                  model.getName(), model.getDescription(), model.getMaintenancePeriod().toString()))
              .collect(Collectors.toList()));
      List<String> supportModels = modelIndex.stream().map(id -> models.get(id).getName())
          .collect(Collectors.toList());
      adminService.addServiceLocation(locationNew, managerId, supportModels);
      output.println("Adding service location succeed.");
    });

    availableCommands.put("View All Model(s)", () -> {
      List<Model> models = AlsetDAO.selectModels();

      printMatrix("Support Model(s): ",
          Arrays.asList("Model Name", "Description", "Maintenance Period(yr)"),
          models.stream()
              .map(model -> Arrays.asList(
                  model.getName(), model.getDescription(), model.getMaintenancePeriod().toString()))
              .collect(Collectors.toList()));
    });

    availableCommands.put("Config Maintenance Period", () -> {
      List<Model> models = AlsetDAO.selectModels();
      int modelIndex = doMatrixSelection("Available Model(s): ",
          Arrays.asList("Model Name", "Description", "Maintenance Period(yr)"),
          models.stream()
              .map(model -> Arrays.asList(
                  model.getName(), model.getDescription(), model.getMaintenancePeriod().toString()))
              .collect(Collectors.toList()));
      if (modelIndex == -1) {
        return;
      }
      String modelName = models.get(modelIndex).getName();

      int period;
      while (true) {
        period = doPromptInt("Enter new maintenance period in year: ");
        if (period >= 100) {
          output.println("The maintenance period in year should between 1 and 99.");
        } else {
          break;
        }
      }

      if (period == -1) {
        return ;
      }

      adminService.updateMaintenancePeriod(modelName, period);
      output.println("Successfully updated.");
    });

    availableCommands.put("Recall", () -> {
      Recall recall = readRecall();
      if (recall == null) {
        return;
      }
      List<Customer> customers = adminService.recall(recall);

      output.println();
      printMatrix("The following customer will be notified.",
          Arrays.asList("Name", "Email"),
          customers.stream()
              .map(c -> Arrays.asList(c.getName(), c.getEmail()))
              .collect(Collectors.toList()));

      output.println("Successfully recalled.");
    });

    availableCommands.put("View Recall Status", () -> {
      Map<VehicleStatus, Integer> status = adminService.recallStatus();

      printMatrix("Recall Counts: ",
          Arrays.asList("Recalled Vehicle", "Unpicked Vehicle", "Unsold Vehicle"),
          Collections.singletonList(Arrays.asList(
              status.get(VehicleStatus.RECALL).toString(),
              status.get(VehicleStatus.WAIT_PICKUP).toString(),
              status.get(VehicleStatus.SELLING).toString()
          )));
    });

    availableCommands.put("View All SKU configs", () -> {
      List<Sku> skus = AlsetDAO.selectSkus();
      printMatrix("All Available SKU(s):",
          Arrays
              .asList("Model Name", "SKU Name", "Configs", "Price", "Resell Price", "Repair Price"),
          skus.stream()
              .map(sku ->
                  Arrays.asList(
                      sku.getModelName(),
                      sku.getName(),
                      sku.getConfigs(),
                      sku.getPrice().toString(),
                      sku.getResellPrice().toString(),
                      sku.getRepairPrice().toString()))
              .collect(Collectors.toList()));
    });

    availableCommands.put("Modify SKU Price", () -> {
      List<Sku> skus = AlsetDAO.selectSkus();
      int sId = doMatrixSelection("All Available SKU(s):",
          Arrays
              .asList("Model Name", "SKU Name", "Configs", "Price", "Resell Price", "Repair Price"),
          skus.stream()
              .map(sku ->
                  Arrays.asList(
                      sku.getModelName(),
                      sku.getName(),
                      sku.getConfigs(),
                      sku.getPrice().toString(),
                      sku.getResellPrice().toString(),
                      sku.getRepairPrice().toString()))
              .collect(Collectors.toList()));

      if (sId == -1) {
        return;
      }

      Sku sku = skus.get(sId);

      int priceId = doSimpleSelection("Modify Which Price: ",
          Arrays.asList("Sell Price", "Resell Price", "Repair Price"));

      if (priceId == -1) {
        return;
      }

      double newPrice = doPromptDouble("Enter the new price: ");

      switch (priceId) {
        case 0: {
          adminService.updatePrice(sku.getSkuId(), newPrice);
          break;
        }
        case 1: {
          adminService.updateResellPrice(sku.getSkuId(), newPrice);
          break;
        }
        case 2: {
          adminService.updateRepairPrice(sku.getSkuId(), newPrice);
          break;
        }
      }

      output.println("Successfully updated.");
    });

    availableCommands.put("Statistics by Year", () -> {
      List<YearStatistics> stats = adminService.sellsByYear();

      printMatrix("Sale Statistic by Year",
          Arrays.asList("Year", "Sales"),
          stats.stream()
              .map(state -> Arrays.asList(state.getYear().toString(), state.getSell().toString()))
              .collect(Collectors.toList()));
    });

    commandKeys = new ArrayList<>(availableCommands.keySet());
  }


  private Recall readRecall() {
    String modelName;
    int skuId;

    List<Model> models = AlsetDAO.selectModels();
    int modelIndex = doMatrixSelection("Available Model(s): ",
        Arrays.asList("Model Name", "Description"),
        models.stream()
            .map(model -> Arrays
                .asList(model.getName(), model.getDescription()))
            .collect(Collectors.toList()));
    if (modelIndex == -1) {
      return null;
    }
    modelName = models.get(modelIndex).getName();

    List<Sku> skus = AlsetDAO.selectSkus(modelName);
    int skuIndex = doMatrixSelection("Available Config Bundles: ",
        Arrays.asList("Configs", "Price"),
        skus.stream()
            .map(sku -> Arrays.asList(sku.getConfigs(), sku.getPrice().toString()))
            .collect(Collectors.toList()));
    if (skuIndex == -1) {
      return null;
    }
    skuId = skus.get(skuIndex).getSkuId();

    Date from = doReadDate("Enter the date from(eg. 2018-01-01): ");
    Date to = doReadDate("Enter the date to(eg. 2018-01-01): ");

    return new Recall(skuId, from, to);
  }
}



