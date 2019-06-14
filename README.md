# car-rental-database-project
# Alset Database

## Overview

Homework project for `Database` class at Lehigh University.

## Installation

1. Execute following files in order
    1. `create_table.sql`
    2. `procedure.sql`
    3. `init_data.sql`
2. Modify `Context.java` class to fit your database configurations
2. Compile & package files
3. Run it  

## Default User

|      | username | password | type          |
| ---- | -------- | -------- | ------------- |
| 1    | user     | 123456   | CUSTOMER      |
| 2    | manager  | 123456   | MANAGER       |
| 3    | admin    | 123456   | ADMINISTRATOR |
  
## Feature List

- [X] Customer Interface
  - [X] Registration / Login
  - [X] View owned vehicle information
  - [X] View orders
  - [X] Purchase a new car
  - [X] Visit a Service Location
    - [X] Look at show vehicles
    - [X] Repair vehicles
    - [X] Give back recalled vehicles
    - [X] Pick up purchased vehicles
    - [X] Purchase a used vehicle(show room's vehicle)
    
- [X] Manager Interface
  - [X] Login by username / password
  - [X] View orders by service location
  - [X] View its show room
  - [X] Order new show vehicles
  - [X] Make a vehicle out of show room(to sell it)
  - [X] Recall vehicles(by model, skuName, sell date)
  - [X] View vehicles required maintenance

- [X] Administrator Interface
  - [X] Login by username / password
  - [X] Recall vehicles(by model, skuName, sell date)
  - [X] View recalled vehicles(total counts, unsold count, unpicked count)
  - [X] View / Add manager(s)
  - [X] View all service locations
  - [X] Add new service location
  - [X] View Models
  - [X] Config maintenance period for vehicle models
  - [X] Query / Modify every SKU's price / resell price
  - [X] Query sum(price) group by a year

## Data Generation

All address data loaded from https://www.randomlists.com/random-addresses.

Demo company names loaded from https://www.fantasynamegenerators.com/company-names.php.

## License

No license available now. All rights reserved.
