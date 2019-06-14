import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DoCustomerWork extends DoWork {

  private final CustomerService customerService = new CustomerService();

  private final Map<String, Runnable> availableCommands = new LinkedHashMap<>();
  private List<String> commandKeys;

  {
    availableCommands.put("Register Customer", () -> {
      Customer customer = InputFlow.flow(input, output, Customer.class);
      if (customer == null) {
        return;
      }
      customer = customerService.register(customer);
      updateCommandsAsLogin(customer);
    });

    availableCommands.put("Exit", () -> System.exit(0));

    commandKeys = new ArrayList<>(availableCommands.keySet());
  }

  public DoCustomerWork(Scanner input, PrintStream output) {
    super(input, output);
  }

  @Override
  public void doWork() {
    String name = doPrompt("Enter your name (Leave it blank for new user): ");

    Customer customer = customerService.findCustomer(name);
    if (customer != null) {
      while (true) {
        String password = doPromptPassword("Enter your password to login: ");
        if (Objects.equals(Utils.md5(password), customer.getPassword())) {
          break;
        } else {
          output.println("Wrong password. Please try again.");
        }
      }

      updateCommandsAsLogin(customer);
    }

    while (true) {
      int selection = doSelection("User Portal", commandKeys);
      if (selection == -1) {
        break;
      }

      String command = commandKeys.get(selection);

      Runnable runnable = availableCommands.get(command);

      runnable.run();
    }
  }

  private void updateCommandsAsLogin(Customer customer) {
    availableCommands.clear();
    availableCommands.put("My Vehicle(s)", () -> printMatrix("Your Vehicle(s):",
        Arrays.asList("Sale Date", "Sale Price", "Model Name", "Status"),
        AlsetDAO.selectVehicleByName(customer.getName())
            .stream()
            .map(vp -> Arrays.asList(vp.getSaleDate().toString(), vp.getFactPrice().toString(),
                vp.getModelName(), vp.getVehicleStatus()))
            .collect(Collectors.toList())));

    availableCommands.put("View Order(s)", () -> {
      List<Sale> sales = AlsetDAO.selectSalesByCustomerName(customer.getName());

      printMatrix("Order(s) now: ",
          Arrays.asList("Buyer's Name", "Sale Date", "Sale Price", "Model Name", "SKU", "Status"),
          sales.stream().map(sale -> Arrays.asList(
              sale.getBuyerName(),
              sale.getSaleDate().toString(),
              sale.getFactPrice().toString(),
              sale.getModelName(),
              sale.getSkuName(),
              sale.getSaleStatus().name()
          )).collect(Collectors.toList()));
    });

    availableCommands.put("Purchase New Vehicle", () -> {
      PurchaseNew purchaseNew = readPurchase();
      if (purchaseNew == null) {
        return;
      }
      purchaseNew.setCustomerName(customer.getName());
      customerService.purchase(purchaseNew);
      output.println("Successfully purchased.");
    });

    availableCommands.put("Visit Service Location", () -> {
      List<ServiceLocation> serviceLocations = AlsetDAO.selectServiceLocation();
      int serviceLocationIndex = doSimpleSelection("Available Service Location(s): ",
          serviceLocations.stream()
              .map(
                  serviceLocation -> serviceLocation.getName() + ": " + serviceLocation.getAddress()
                      .toString())
              .collect(Collectors.toList()));

      if (serviceLocationIndex == -1) {
        return;
      }

      updateCommandsAsVisit(customer, serviceLocations.get(serviceLocationIndex));
    });

    availableCommands.put("Exit", () -> System.exit(0));

    commandKeys = new ArrayList<>(availableCommands.keySet());
  }

  private void updateCommandsAsVisit(Customer customer, ServiceLocation serviceLocation) {
    availableCommands.clear();

    availableCommands.put("Show Room", () -> {
      List<Vehicle> vehicles = AlsetDAO
          .selectShowVehicleByLocationId(serviceLocation.getServiceLocationId());

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

    availableCommands.put("Buy Used Vehicle(s)", () -> {
      List<Vehicle> vehicles = AlsetDAO
          .selectSaleVehicleByLocationId(serviceLocation.getServiceLocationId());

      if (vehicles.size() == 0) {
        output.println("No available vehicles now.");
        return;
      }

      int vId = doMatrixSelection("Available Used Vehicle(s): ",
          Arrays.asList("Model Name", "Config", "Resale Price"),
          vehicles.stream().map(sale -> Arrays.asList(
              sale.getModelName(),
              sale.getConfig(),
              sale.getPrice().toString()
          )).collect(Collectors.toList()));

      if (vId == -1) {
        return;
      }

      int vehicleId = vehicles.get(vId).getVehicleId();

      customerService.purchase(new PurchaseUsed(vehicleId, customer.getName()));
      output.println("Successfully purchased.");
    });

    availableCommands.put("Repair", () -> {
      List<VehiclePurchase> vehiclePurchases = AlsetDAO
          .selectVehicleByName(customer.getName());

      vehiclePurchases.removeIf(vp ->
          serviceLocation
              .getSupports()
              .stream()
              .map(Model::getName)
              .noneMatch(m -> m.equals(vp.getModelName()))
      );

      if (vehiclePurchases.size() == 0) {
        output.println("No repairable vehicles now.");
        return;
      }

      int vehicleIndex = doMatrixSelection("Vehicles to Repair: ",
          Arrays.asList("Sale Date", "Sale Price", "Model Name", "Config"),
          vehiclePurchases
              .stream()
              .map(vp -> Arrays.asList(vp.getSaleDate().toString(), vp.getFactPrice().toString(),
                  vp.getModelName(), vp.getSkuName()))
              .collect(Collectors.toList()));

      VehiclePurchase vehiclePurchase = vehiclePurchases.get(vehicleIndex);
      customerService.repair(
          new NewRepair(vehiclePurchase.getVehicleId(), serviceLocation.getServiceLocationId()));
      output.println("Your vehicle has been repaired.");
    });

    availableCommands.put("Recall", () -> {
      List<VehiclePurchase> vehiclePurchases = AlsetDAO
          .selectVehicleByLocationStatus(customer.getName(), serviceLocation.getServiceLocationId(),
              VehicleStatus.RECALL);
      if (vehiclePurchases.size() == 0) {
        output.println("No recallable vehicles now.");
        return;
      }
      int vehicleIndex = doMatrixSelection("Vehicles to Recall: ",
          Arrays.asList("Sale Date", "Sale Price", "Model Name"),
          vehiclePurchases
              .stream()
              .map(vp -> Arrays.asList(vp.getSaleDate().toString(), vp.getFactPrice().toString(),
                  vp.getModelName()))
              .collect(Collectors.toList()));

      VehiclePurchase vehiclePurchase = vehiclePurchases.get(vehicleIndex);
      customerService.changeStatus(vehiclePurchase.getVehicleId(), SaleStatus.PICKUPED,
          VehicleStatus.RECALLED);
      output.println("Successfully recalled.");
    });

    availableCommands.put("Pickup", () -> {
      List<VehiclePurchase> vehiclePurchases = AlsetDAO
          .selectVehicleByLocationStatus(customer.getName(), serviceLocation.getServiceLocationId(),
              VehicleStatus.WAIT_PICKUP);
      if (vehiclePurchases.size() == 0) {
        output.println("No vehicles to pickup now.");
        return;
      }
      int vehicleIndex = doMatrixSelection("Vehicles to Pickup: ",
          Arrays.asList("Sale Date", "Sale Price", "Model Name"),
          vehiclePurchases
              .stream()
              .map(vp -> Arrays.asList(vp.getSaleDate().toString(), vp.getFactPrice().toString(),
                  vp.getModelName()))
              .collect(Collectors.toList()));

      VehiclePurchase vehiclePurchase = vehiclePurchases.get(vehicleIndex);
      customerService
          .changeStatus(vehiclePurchase.getVehicleId(), SaleStatus.PICKUPED, VehicleStatus.SOLD);

      output.println("Thank you for picking up.");
    });

    availableCommands.put("Back", () -> updateCommandsAsLogin(customer));

    availableCommands.put("Exit", () -> System.exit(0));

    commandKeys = new ArrayList<>(availableCommands.keySet());
  }

  private PurchaseNew readPurchase() {
    String modelName;
    int skuId;
    int pickupLocationId;

    List<Model> models = AlsetDAO.selectModels();
    int modelIndex = doMatrixSelection("Available Model(s): ",
        Arrays.asList("Model Name", "Description", "Maintenance Period(yr)"),
        models.stream()
            .map(model -> Arrays.asList(
                model.getName(), model.getDescription(), model.getMaintenancePeriod().toString()))
            .collect(Collectors.toList()));
    if (modelIndex == -1) {
      return null;
    }
    modelName = models.get(modelIndex).getName();

    List<Sku> skus = AlsetDAO.selectSkus(modelName);
    int skuIndex = doMatrixSelection("Available Config Bundle(s): ",
        Arrays.asList("Name", "Config(s)", "Price"),
        skus.stream()
            .map(sku -> Arrays.asList(sku.getName(), sku.getConfigs(), sku.getPrice().toString()))
            .collect(Collectors.toList()));
    if (skuIndex == -1) {
      return null;
    }
    skuId = skus.get(skuIndex).getSkuId();

    List<ServiceLocation> serviceLocations = AlsetDAO.selectLocationSupport(modelName);
    int locationIndex = doSimpleSelection("Available Pickup Location(s): ",
        serviceLocations.stream()
            .map(serviceLocation -> serviceLocation.getName() + ": " + serviceLocation.getAddress()
                .toString())
            .collect(Collectors.toList()));

    if (locationIndex == -1) {
      return null;
    }

    pickupLocationId = serviceLocations.get(locationIndex).getServiceLocationId();

    return new PurchaseNew(skuId, pickupLocationId);
  }

}
