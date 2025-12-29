import javax.swing.*;
import java.awt.*;

public class BankingManagementSystemGUI extends JFrame {

    private Bank bank;
    private JPanel mainPanel;

    public BankingManagementSystemGUI() {
        bank = new Bank("Ved Bank");
        bank.loadState();

        setTitle("Ved Bank - Management System");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createFooter(), BorderLayout.SOUTH);

        mainPanel = new JPanel(new CardLayout());
        mainPanel.add(new JLabel("Welcome to Ved Bank", SwingConstants.CENTER), "HOME");
        add(mainPanel, BorderLayout.CENTER);
    }

    // ---------------- HEADER ----------------
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(33, 150, 243));
        header.setPreferredSize(new Dimension(1000, 60));

        JLabel title = new JLabel("Ved Bank – Management System");
        title.setForeground(Color.white);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        header.add(title);
        return header;
    }

    // ---------------- SIDEBAR ----------------
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new GridLayout(10, 1, 0, 10));

        sidebar.add(sidebarButton("Create Account", "CREATE"));
        sidebar.add(sidebarButton("Deposit", "DEPOSIT"));
        sidebar.add(sidebarButton("Withdraw", "WITHDRAW"));
        sidebar.add(sidebarButton("Transfer", "TRANSFER"));
        sidebar.add(sidebarButton("Balance Enquiry", "BALANCE"));
        sidebar.add(sidebarButton("Mini Statement", "STATEMENT"));
        sidebar.add(sidebarButton("List Accounts", "LIST"));
        sidebar.add(sidebarButton("Save & Exit", "EXIT"));

        return sidebar;
    }

    private JButton sidebarButton(String text, String command) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(Color.white);

        btn.addActionListener(e -> showScreen(command));

        return btn;
    }

    // ---------------- FOOTER ----------------
    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(new Color(230, 230, 230));
        footer.setPreferredSize(new Dimension(1000, 40));

        JLabel foot = new JLabel("© 2025 OpenSim Bank – All Rights Reserved");
        foot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(foot);

        return footer;
    }

    // ---------------- MAIN SCREEN ROUTER ----------------
    private void showScreen(String screen) {
        JPanel newPanel = switch (screen) {
            case "CREATE" -> createAccountScreen();
            case "DEPOSIT" -> depositScreen();
            case "WITHDRAW" -> withdrawScreen();
            case "TRANSFER" -> transferScreen();
            case "BALANCE" -> balanceScreen();
            case "STATEMENT" -> statementScreen();
            case "LIST" -> listAccountsScreen();
            case "EXIT" -> { bank.saveState(); System.exit(0); yield null; }
            default -> new JPanel();
        };

        mainPanel.removeAll();
        mainPanel.add(newPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ---------------- SCREENS ----------------

    private JPanel createAccountScreen() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(6, 2, 10, 10));

        JTextField name = new JTextField();
        JTextField govId = new JTextField();
        JTextField initDep = new JTextField();

        JButton create = new JButton("Create Account");
        JLabel msg = new JLabel("");

        create.addActionListener(e -> {
            try {
                Account acc = bank.createAccount(
                        name.getText(),
                        govId.getText(),
                        Double.parseDouble(initDep.getText())
                );
                msg.setText("Account created: " + acc.getAccountNumber());
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        p.add(new JLabel("Full Name:"));
        p.add(name);
        p.add(new JLabel("Government ID:"));
        p.add(govId);
        p.add(new JLabel("Initial Deposit:"));
        p.add(initDep);
        p.add(create);
        p.add(msg);

        return p;
    }

    private JPanel depositScreen() {
        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));

        JTextField accNo = new JTextField();
        JTextField amt = new JTextField();
        JLabel msg = new JLabel();

        JButton btn = new JButton("Deposit");

        btn.addActionListener(e -> {
            try {
                bank.deposit(
                        Integer.parseInt(accNo.getText()),
                        Double.parseDouble(amt.getText())
                );
                msg.setText("Deposit successful.");
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        p.add(new JLabel("Account Number:"));
        p.add(accNo);
        p.add(new JLabel("Amount:"));
        p.add(amt);
        p.add(btn);
        p.add(msg);

        return p;
    }

    private JPanel withdrawScreen() {
        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));

        JTextField accNo = new JTextField();
        JTextField amt = new JTextField();
        JLabel msg = new JLabel();

        JButton btn = new JButton("Withdraw");

        btn.addActionListener(e -> {
            try {
                bank.withdraw(
                        Integer.parseInt(accNo.getText()),
                        Double.parseDouble(amt.getText())
                );
                msg.setText("Withdrawal successful.");
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        p.add(new JLabel("Account Number:"));
        p.add(accNo);
        p.add(new JLabel("Amount:"));
        p.add(amt);
        p.add(btn);
        p.add(msg);

        return p;
    }

    private JPanel transferScreen() {
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField from = new JTextField();
        JTextField to = new JTextField();
        JTextField amt = new JTextField();
        JLabel msg = new JLabel();

        JButton btn = new JButton("Transfer");

        btn.addActionListener(e -> {
            try {
                bank.transfer(
                        Integer.parseInt(from.getText()),
                        Integer.parseInt(to.getText()),
                        Double.parseDouble(amt.getText())
                );
                msg.setText("Transfer successful.");
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        p.add(new JLabel("From Account:"));
        p.add(from);
        p.add(new JLabel("To Account:"));
        p.add(to);
        p.add(new JLabel("Amount:"));
        p.add(amt);
        p.add(btn);
        p.add(msg);

        return p;
    }

    private JPanel balanceScreen() {
        JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));

        JTextField accNo = new JTextField();
        JLabel msg = new JLabel();
        JButton btn = new JButton("Check Balance");

        btn.addActionListener(e -> {
            try {
                double bal = bank.getBalance(Integer.parseInt(accNo.getText()));
                String name = bank.getName(Integer.parseInt(accNo.getText()));
                msg.setText(name + " | Balance: " + bal);
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        p.add(new JLabel("Account Number:"));
        p.add(accNo);
        p.add(btn);
        p.add(msg);

        return p;
    }

    private JPanel statementScreen() {
        JPanel p = new JPanel(new BorderLayout());

        JTextField accNo = new JTextField();
        JButton btn = new JButton("Load Statement");
        JTextArea area = new JTextArea();
        area.setEditable(false);

        btn.addActionListener(e -> {
            try {
                var list = bank.getMiniStatement(Integer.parseInt(accNo.getText()), 10);
                area.setText("");
                for (Transaction t : list) {
                    area.append(t + "\n");
                }
            } catch (Exception ex) {
                area.setText("Error: " + ex.getMessage());
            }
        });

        JPanel top = new JPanel(new GridLayout(1, 3));
        top.add(new JLabel("Account No:"));
        top.add(accNo);
        top.add(btn);

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);

        return p;
    }

    private JPanel listAccountsScreen() {
        JPanel p = new JPanel(new BorderLayout());

        JTextArea area = new JTextArea();
        area.setEditable(false);

        StringBuilder sb = new StringBuilder();
        bank.listAllAccounts().forEach((k, v) -> sb.append(v).append("\n"));
        area.setText(sb.toString());

        p.add(new JScrollPane(area), BorderLayout.CENTER);

        return p;
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankingManagementSystemGUI().setVisible(true));
    }
}
