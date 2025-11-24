import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BankingManagementSystem {
    public static void main(String[] args) {
        Bank bank = new Bank("OpenSim Bank");
        bank.loadState(); // attempt to load saved accounts and transactions

        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to " + bank.getName() + " - Console Banking System");

        boolean running = true;
        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Balance Enquiry");
            System.out.println("6. Mini Statement");
            System.out.println("7. List Accounts (admin)");
            System.out.println("8. Save & Exit");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        createAccountInteractive(bank, sc);
                        break;
                    case "2":
                        depositInteractive(bank, sc);
                        break;
                    case "3":
                        withdrawInteractive(bank, sc);
                        break;
                    case "4":
                        transferInteractive(bank, sc);
                        break;
                    case "5":
                        balanceInteractive(bank, sc);
                        break;
                    case "6":
                        statementInteractive(bank, sc);
                        break;
                    case "7":
                        listAccountsInteractive(bank);
                        break;
                    case "8":
                        bank.saveState();
                        System.out.println("State saved. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (BankException be) {
                System.out.println("Operation failed: " + be.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        sc.close();
    }

    // ---------------- Interactive helper methods ----------------
    private static void createAccountInteractive(Bank bank, Scanner sc) throws IOException, BankException {
        System.out.println("\n--- Create Account ---");
        System.out.print("Full Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Government ID (e.g. Aadhaar/Passport): ");
        String govId = sc.nextLine().trim();
        System.out.print("Initial Deposit: ");
        String amtS = sc.nextLine().trim();
        double initialDeposit = parseAmount(amtS);

        Account acc = bank.createAccount(name, govId, initialDeposit);
        System.out.println("Account created. Account No: " + acc.getAccountNumber());
    }

    private static void depositInteractive(Bank bank, Scanner sc) throws BankException {
        System.out.println("\n--- Deposit ---");
        System.out.print("Account No: ");
        int accNo = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Amount: ");
        double amt = parseAmount(sc.nextLine().trim());
        bank.deposit(accNo, amt);
        System.out.println("Deposit successful.");
    }

    private static void withdrawInteractive(Bank bank, Scanner sc) throws BankException {
        System.out.println("\n--- Withdraw ---");
        System.out.print("Account No: ");
        int accNo = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Amount: ");
        double amt = parseAmount(sc.nextLine().trim());
        bank.withdraw(accNo, amt);
        System.out.println("Withdrawal successful.");
    }

    private static void transferInteractive(Bank bank, Scanner sc) throws BankException {
        System.out.println("\n--- Transfer ---");
        System.out.print("From Account No: ");
        int from = Integer.parseInt(sc.nextLine().trim());
        System.out.print("To Account No: ");
        int to = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Amount: ");
        double amt = parseAmount(sc.nextLine().trim());
        bank.transfer(from, to, amt);
        System.out.println("Transfer successful.");
    }

    private static void balanceInteractive(Bank bank, Scanner sc) throws BankException {
        System.out.println("\n--- Balance Enquiry ---");
        System.out.print("Account No: ");
        int accNo = Integer.parseInt(sc.nextLine().trim());
        double bal = bank.getBalance(accNo);
        System.out.println("Account Holder's Name: " + bank.getName(accNo));
        System.out.printf("Balance for account no. %d: %.2f\n", accNo, bal);
    }

    private static void statementInteractive(Bank bank, Scanner sc) throws BankException {
        System.out.println("\n--- Mini Statement ---");
        System.out.print("Account No: ");
        int accNo = Integer.parseInt(sc.nextLine().trim());
        List<Transaction> txns = bank.getMiniStatement(accNo, 10);
        System.out.println("Last " + txns.size() + " transactions:");
        for (Transaction t : txns) {
            System.out.println(t);
        }
    }

    private static void listAccountsInteractive(Bank bank) {
        System.out.println("\n--- Accounts List ---");
        bank.listAllAccounts().forEach((k, v) -> System.out.println(v));
    }

    private static double parseAmount(String s) throws BankException {
        try {
            double d = Double.parseDouble(s);
            if (d <= 0) throw new BankException("Amount must be positive");
            return d;
        } catch (NumberFormatException nfe) {
            throw new BankException("Invalid amount format");
        }
    }
}

// ---------------- Bank class ----------------
class Bank implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Map<Integer, Account> accounts = new HashMap<>();
    private List<Transaction> transactions = new ArrayList<>();
    private int nextAccountNumber = 1001;

    // Constants / config
    private final double MIN_BALANCE = 500.0; // enforce minimum balance

    // Persistence files
    private final transient File ACCOUNTS_FILE = new File("accounts.ser");
    private final transient File TXN_FILE = new File("transactions.ser");
    private final transient File AUDIT_FILE = new File("audit.log");

    public Bank(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    // Create new account with simple KYC validation
    public synchronized Account createAccount(String fullName, String governmentId, double initialDeposit) throws BankException, IOException {
        if (fullName == null || fullName.length() < 3) throw new KYCException("Name too short for KYC");
        if (governmentId == null || governmentId.length() < 4) throw new KYCException("Invalid government ID for KYC");
        if (initialDeposit < MIN_BALANCE) throw new BankException("Initial deposit must be at least minimum balance: " + MIN_BALANCE);

        int accNo = nextAccountNumber++;
        Account acc = new Account(accNo, fullName, governmentId, initialDeposit);
        accounts.put(accNo, acc);

        Transaction t = new Transaction(accNo, Transaction.Type.DEPOSIT, initialDeposit, "Initial deposit");
        transactions.add(t);
        acc.addTransaction(t);

        writeAudit("CREATE_ACCOUNT", String.format("Acc:%d Name:%s Init:%.2f", accNo, fullName, initialDeposit));
        saveState();
        return acc;
    }

    public synchronized void deposit(int accountNumber, double amount) throws BankException {
        Account acc = findAccount(accountNumber);
        acc.credit(amount);
        Transaction t = new Transaction(accountNumber, Transaction.Type.DEPOSIT, amount, "Deposit");
        transactions.add(t);
        acc.addTransaction(t);
        writeAudit("DEPOSIT", String.format("Acc:%d Amount:%.2f", accountNumber, amount));
        saveState();
    }

    public synchronized void withdraw(int accountNumber, double amount) throws BankException {
        Account acc = findAccount(accountNumber);
        if (acc.getBalance() - amount < MIN_BALANCE) {
            throw new InsufficientBalanceException("Withdrawal would breach minimum balance of " + MIN_BALANCE);
        }
        acc.debit(amount);
        Transaction t = new Transaction(accountNumber, Transaction.Type.WITHDRAW, amount, "Withdraw");
        transactions.add(t);
        acc.addTransaction(t);
        writeAudit("WITHDRAW", String.format("Acc:%d Amount:%.2f", accountNumber, amount));
        saveState();
    }

    public synchronized void transfer(int fromAcc, int toAcc, double amount) throws BankException {
        if (fromAcc == toAcc) throw new BankException("Cannot transfer to same account");
        Account aFrom = findAccount(fromAcc);
        Account aTo = findAccount(toAcc);

        if (aFrom.getBalance() - amount < MIN_BALANCE) {
            throw new InsufficientBalanceException("Transfer would breach minimum balance of " + MIN_BALANCE);
        }
        aFrom.debit(amount);
        aTo.credit(amount);

        Transaction t1 = new Transaction(fromAcc, Transaction.Type.TRANSFER_OUT, amount, "Transfer to " + toAcc);
        Transaction t2 = new Transaction(toAcc, Transaction.Type.TRANSFER_IN, amount, "Transfer from " + fromAcc);
        transactions.add(t1); transactions.add(t2);
        aFrom.addTransaction(t1); aTo.addTransaction(t2);

        writeAudit("TRANSFER", String.format("From:%d To:%d Amount:%.2f", fromAcc, toAcc, amount));
        saveState();
    }

    public synchronized double getBalance(int accountNumber) throws BankException {
        Account acc = findAccount(accountNumber);
        return acc.getBalance();
    }

    public synchronized String getName(int accountNumber) throws BankException {
        Account acc = findAccount(accountNumber);
        return acc.getHolderName();
    }

    public synchronized List<Transaction> getMiniStatement(int accountNumber, int n) throws BankException {
        Account acc = findAccount(accountNumber);
        List<Transaction> lst = acc.getRecentTransactions(n);
        return lst;
    }

    public Map<Integer, Account> listAllAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    // ---------------- Persistence ----------------
    public synchronized void saveState() {
        // save accounts
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACCOUNTS_FILE))) {
            oos.writeObject(accounts);
            oos.writeInt(nextAccountNumber);
        } catch (IOException e) {
            System.err.println("Failed to save accounts: " + e.getMessage());
        }
        // save transactions
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TXN_FILE))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            System.err.println("Failed to save transactions: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadState() {
        if (ACCOUNTS_FILE.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ACCOUNTS_FILE))) {
                Object obj = ois.readObject();
                if (obj instanceof Map) {
                    this.accounts = (Map<Integer, Account>) obj;
                    this.nextAccountNumber = ois.readInt();
                }
            } catch (Exception e) {
                System.err.println("Failed to load accounts: " + e.getMessage());
            }
        }
        if (TXN_FILE.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TXN_FILE))) {
                Object obj = ois.readObject();
                if (obj instanceof List) {
                    this.transactions = (List<Transaction>) obj;
                }
            } catch (Exception e) {
                System.err.println("Failed to load transactions: " + e.getMessage());
            }
        }
        // reattach transactions into accounts (in case we loaded things)
        for (Transaction t : transactions) {
            Account a = accounts.get(t.getAccountNumber());
            if (a != null) a.addTransaction(t);
        }
    }

    // ---------------- Utilities ----------------
    private Account findAccount(int accountNumber) throws AccountNotFoundException {
        Account acc = accounts.get(accountNumber);
        if (acc == null) throw new AccountNotFoundException("Account not found: " + accountNumber);
        return acc;
    }

    private void writeAudit(String action, String message) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String line = ts + " | " + action + " | " + message + System.lineSeparator();
        try (FileWriter fw = new FileWriter(AUDIT_FILE, true)) {
            fw.write(line);
        } catch (IOException e) {
            System.err.println("Failed to write audit: " + e.getMessage());
        }
    }
}

// ---------------- Account class ----------------
class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private int accountNumber;
    private String holderName;
    private String governmentId; // KYC
    private double balance;

    // transient to avoid duplication during serialization of full bank state
    private transient Deque<Transaction> recentTransactions = new ArrayDeque<>();
    private final int RECENT_LIMIT = 100;

    public Account(int accNo, String name, String govId, double initBalance) {
        this.accountNumber = accNo;
        this.holderName = name;
        this.governmentId = govId;
        this.balance = initBalance;
        this.recentTransactions = new ArrayDeque<>();
    }

    public int getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public String getGovernmentId() { return governmentId; }
    public double getBalance() { return balance; }

    public synchronized void credit(double amount) {
        this.balance += amount;
    }
    public synchronized void debit(double amount) {
        this.balance -= amount;
    }

    public synchronized void addTransaction(Transaction t) {
        if (recentTransactions == null) recentTransactions = new ArrayDeque<>();
        recentTransactions.addFirst(t);
        while (recentTransactions.size() > RECENT_LIMIT) recentTransactions.removeLast();
    }

    public synchronized List<Transaction> getRecentTransactions(int n) {
        List<Transaction> out = new ArrayList<>();
        int i = 0;
        for (Transaction t : recentTransactions) {
            if (i++ >= n) break;
            out.add(t);
        }
        return out;
    }

    @Override
    public String toString() {
        return String.format("Acc[%d] %s Bal:%.2f", accountNumber, holderName, balance);
    }

    // custom serialization to keep transient deque usable after deserialization
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        recentTransactions = new ArrayDeque<>();
    }
}

// ---------------- Transaction class ----------------
class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type { DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT }

    private int accountNumber;
    private Type type;
    private double amount;
    private String note;
    private LocalDateTime timestamp;

    public Transaction(int accountNumber, Type type, double amount, String note) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.note = note;
        this.timestamp = LocalDateTime.now();
    }

    public int getAccountNumber() { return accountNumber; }
    public Type getType() { return type; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s | %s | %.2f | %s", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), type, amount, note);
    }
}

// ---------------- Custom exceptions ----------------
class BankException extends Exception {
    public BankException(String msg) { super(msg); }
}
class AccountNotFoundException extends BankException { public AccountNotFoundException(String msg) { super(msg); } }
class InsufficientBalanceException extends BankException { public InsufficientBalanceException(String msg) { super(msg); } }
class KYCException extends BankException { public KYCException(String msg) { super(msg); } }
