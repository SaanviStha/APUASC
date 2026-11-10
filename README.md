# APU Automotive Service Centre (APUASC)

A Java desktop app for running a car service centre. Staff book and assign service appointments, technicians record their work, customers see their history, and the manager looks after prices, staff and reports.

## Built With

- Java 17 with Swing
- NetBeans (Ant project)
- Plain text files for storage (in the `data/` folder)

## Features

- **Manager:** staff accounts, service prices, feedback and comments, reports
- **Counter Staff:** customer accounts, book appointments and assign technicians, collect payments and view receipts
- **Technician:** view assigned appointments, add service feedback, mark jobs as completed
- **Customer:** service history, payment history, comment on a service
- **Everyone:** log in and edit their own profile

## How to Run

1. Open the `APUASC` folder in NetBeans as a project.
2. Run the project (main class: `main.Main`).
3. Log in with one of the sample accounts:

| Role | Username | Password |
|---|---|---|
| Manager | admin | admin123 |
| Counter Staff | staff1 | staff123 |
| Technician | tech1 | tech123 |
| Customer | cust1 | cust123 |

All data is saved in the text files in `data/`.

## Project Structure

```
src/
  main/       start of the app
  model/      users, appointments, payments, etc.
  storage/    reading and writing the data files
  ui/         login screen and dashboards
  util/       dates, validation, activity log
  exception/  custom exceptions
data/         sample data
```
