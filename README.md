# 💰 Personal Finance Tracker (Java Streams Edition)

A clean, modular, console-based **Personal Finance Tracker** built in pure Java that demonstrates modern language features and solid software design.

> Perfect for academic projects, GitHub portfolios, and technical interviews.

---

## ✨ Key Features

| Feature                        | Implementation Highlight                     |
|--------------------------------|----------------------------------------------|
| Transaction Entry              | Validation + Custom Exceptions               |
| Category Aggregation           | `Collectors.groupingBy` + `summingDouble`    |
| Monthly Financial Summary      | Stream filtering by `YearMonth`              |
| Budget Goal Tracker            | Real-time overspend detection                |
| CSV Export Engine              | Professional multi-section monthly reports   |
| Persistent Storage             | Java Serialization (`transactions.dat`)      |
| Search & Delete                | Stream-based filtering                       |

---

## 🏗️ Project Architecture

```
com.financetracker
├── Main.java                     → Entry point & interactive menu
├── model
│   └── Transaction.java          → Immutable domain object
├── service
│   ├── TransactionService.java   → CRUD + heavy Stream API usage
│   ├── CategoryAggregator.java   → HashMap + Collectors analytics
│   └── BudgetGoalTracker.java    → Budget limits & alerts
├── export
│   └── CsvExportEngine.java      → File I/O & CSV generation
├── exception
│   ├── InvalidTransactionException.java
│   └── BudgetExceededException.java
└── util
    └── DateUtils.java
```

### Modules Mapped to Requirements

- **Transaction Entry Module** → `TransactionService.addTransaction()`
- **Category Aggregator** → `CategoryAggregator`
- **Budget Goal Tracker** → `BudgetGoalTracker`
- **CSV Export Engine** → `CsvExportEngine`

---

## 🚀 How to Run

### Prerequisites
- JDK 17 or higher (uses `var`, text blocks, modern date/time API)

### Compile & Run (from project root)

```bash
# Compile
javac -d out src/com/financetracker/**/*.java

# Run
java -cp out com.financetracker.Main
```

Or simply open the project in **IntelliJ IDEA / Eclipse / VS Code** and run `Main.java`.

---

## 📊 Sample Workflow

1. **Add transactions** (Salary, Food, Rent, Uber, Netflix…)
2. **View Monthly Summary** → Income, Expense, Net Balance + Top categories
3. **Category Breakdown** → Beautiful percentage table
4. **Set Budgets** → e.g. Food = ₹8000, Transport = ₹3000
5. **Export CSV** → Professional report saved in `/exports` folder

---

## 🧠 Technical Highlights (for your Report / Viva)

- **Stream API mastery**: `filter`, `map`, `mapToDouble`, `collect`, `groupingBy`, `summingDouble`, `sorted`, `limit`
- **Lambda expressions** everywhere
- **Custom checked exceptions** with rich context
- **Immutable model** (`Transaction`)
- **Separation of concerns** (Service / Aggregator / Export layers)
- **Persistent storage** without external libraries
- **Clean console UI** with Unicode box drawing

---

## 📁 Generated Files

- `data/transactions.dat` → Binary persistence of all transactions
- `exports/Financial_Summary_YYYY-MM.csv` → Excel-ready monthly reports

---

## 🎓 Career / Academic Value

This project clearly demonstrates:

- Fluency in modern Java (Streams, Lambdas, Date/Time API)
- Ability to design modular, maintainable code
- Real-world File I/O and data aggregation skills
- Professional documentation & GitHub readiness

---

## 📸 Screenshots (add after running)

1. Main Menu
2. Monthly Summary box
3. Category Breakdown table
4. Budget Status report
5. Exported CSV opened in Excel

---

Made with ❤️ for learning modern Java.
