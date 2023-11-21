import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParcelCRUDApp extends JFrame {
    private Connection connection;
    private JList<String> parcelList;
    private DefaultListModel<String> parcelListModel;
    private JTextField destinationField;
    private JTextField senderContactField;
    private JTextField receiverContactField;
    private JTextField feeField;
    private JTextField weightField;
    private JComboBox<String> statusComboBox;
    private JButton deleteParcelButton;
    private JButton updateParcelButton;
    private JButton updateStatusButton;
    private JButton addParcelButton;  // New button for adding a parcel

    private JPanel rightPanel;  // Assuming you have a JPanel named rightPanel
    public ParcelCRUDApp() {
        rightPanel = new JPanel();  // Instantiate rightPanel

        // Initialize parcels components
        parcelListModel = new DefaultListModel<>();
        parcelList = new JList<>(parcelListModel);
        destinationField = new JTextField(20);
        senderContactField = new JTextField(20);
        receiverContactField = new JTextField(20);
        feeField = new JTextField(20);
        weightField = new JTextField(20);
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Shipped", "Delivered"});
        deleteParcelButton = new JButton("Delete Parcel");
        updateParcelButton = new JButton("Update Parcel");
        updateStatusButton = new JButton("Update Status");
        addParcelButton = new JButton("Add Parcel");
        // Initialize GridBagConstraints for parcels components
        GridBagConstraints gbcParcels = new GridBagConstraints();
        gbcParcels.anchor = GridBagConstraints.WEST;
        gbcParcels.insets = new Insets(5, 5, 5, 5);

        gbcParcels.gridy++;
        rightPanel.add(addParcelButton, gbcParcels);

        addParcelButton.addActionListener(e -> addNewParcel());

        // Create GridBagConstraints for parcels components
        gbcParcels = new GridBagConstraints();
        gbcParcels.anchor = GridBagConstraints.WEST;
        gbcParcels.insets = new Insets(5, 5, 5, 5);

        // Add parcels components with labels in a single row
        // Add parcels components with labels in a single row
        addRow(rightPanel, gbcParcels, new JLabel("Destination:"), destinationField);
        addRow(rightPanel, gbcParcels, new JLabel("Sender Contact:"), senderContactField);
        addRow(rightPanel, gbcParcels, new JLabel("Receiver Contact:"), receiverContactField);
        addRow(rightPanel, gbcParcels, new JLabel("Fee:"), feeField);
        addRow(rightPanel, gbcParcels, new JLabel("Weight:"), weightField);
        addRow(rightPanel, gbcParcels, new JLabel("Status:"), statusComboBox);

        // Add parcel buttons at the bottom
        gbcParcels.gridx = 0;
        gbcParcels.gridy++;
        gbcParcels.gridwidth = GridBagConstraints.REMAINDER;
        gbcParcels.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(deleteParcelButton, gbcParcels);
        gbcParcels.gridy++;
        rightPanel.add(updateParcelButton, gbcParcels);
        gbcParcels.gridy++;
        rightPanel.add(updateStatusButton, gbcParcels);

        // Set up event listeners for parcels
        parcelList.addListSelectionListener(e -> displaySelectedParcel());
        deleteParcelButton.addActionListener(e -> deleteParcel());
        updateParcelButton.addActionListener(e -> updateParcel());
        updateStatusButton.addActionListener(e -> updateParcelStatus());

        // Other initialization code...

        // Set layout for the main frame
        setLayout(new BorderLayout());
        add(new JScrollPane(parcelList), BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Set up the frame properties
        setTitle("Parcel CRUD App");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        if (connectToDatabase()) {
            loadParcels();
        }
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, JComponent label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);

        // Remove the following line
        // gbc.gridy++;
    }


    // Database connection setup
    private boolean connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/swiftrail";
        String user = "root";
        String password = "200434";

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void deleteParcel() {
        int selectedIndex = parcelList.getSelectedIndex();
        if (selectedIndex != -1) {
            int parcelId = getParcelId(selectedIndex);

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM parcel_table WHERE parcel_id=?")) {

                preparedStatement.setInt(1, parcelId);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Parcel deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    parcelListModel.remove(selectedIndex);
                    clearParcelFields();
                } else {
                    JOptionPane.showMessageDialog(this, "No parcel deleted", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting parcel", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateParcel() {
        int selectedIndex = parcelList.getSelectedIndex();
        if (selectedIndex != -1) {
            int parcelId = getParcelId(selectedIndex);

            String destination = destinationField.getText();
            String senderContact = senderContactField.getText();
            String receiverContact = receiverContactField.getText();
            String fee = feeField.getText();
            String weight = weightField.getText();

            if (destination.isEmpty() || senderContact.isEmpty() || receiverContact.isEmpty() || fee.isEmpty() || weight.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "UPDATE parcel_table SET destination=?, sender_contact=?, receiver_contact=?, fee=?, weight=? WHERE parcel_id=?")) {

                    preparedStatement.setString(1, destination);
                    preparedStatement.setString(2, senderContact);
                    preparedStatement.setString(3, receiverContact);
                    preparedStatement.setString(4, fee);
                    preparedStatement.setString(5, weight);
                    preparedStatement.setInt(6, parcelId);

                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Parcel updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        parcelListModel.setElementAt(parcelId + " - " + destination, selectedIndex);
                        loadParcels();
                        clearParcelFields();
                    } else {
                        JOptionPane.showMessageDialog(this, "No parcel updated", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error updating parcel", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void updateParcelStatus() {
        int selectedIndex = parcelList.getSelectedIndex();
        if (selectedIndex != -1) {
            int parcelId = getParcelId(selectedIndex);
            String newStatus = (String) statusComboBox.getSelectedItem();

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE parcel_table SET status=? WHERE parcel_id=?")) {

                preparedStatement.setString(1, newStatus);
                preparedStatement.setInt(2, parcelId);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Parcel status updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    notifySenderReceiver(parcelId, newStatus);
                    loadParcels();
                    clearParcelFields();
                } else {
                    JOptionPane.showMessageDialog(this, "No parcel status updated", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating parcel status", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int getParcelId(int selectedIndex) {
        String parcelInfo = parcelListModel.getElementAt(selectedIndex);

        // Use regular expression to extract numeric part (parcel_id)
        Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher matcher = pattern.matcher(parcelInfo);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        // Handle the case where parcel_id is not found
        return -1; // or another appropriate value
    }

    private void clearParcelFields() {
        destinationField.setText("");
        senderContactField.setText("");
        receiverContactField.setText("");
        feeField.setText("");
        weightField.setText("");
        statusComboBox.setSelectedIndex(0);
    }

    private void displaySelectedParcel() {
        int selectedIndex = parcelList.getSelectedIndex();
        if (selectedIndex != -1) {
            int parcelId = getParcelId(selectedIndex);

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM parcel_table WHERE parcel_id=?")) {

                preparedStatement.setInt(1, parcelId);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    destinationField.setText(resultSet.getString("destination"));
                    senderContactField.setText(resultSet.getString("sender_contact"));
                    receiverContactField.setText(resultSet.getString("receiver_contact"));
                    feeField.setText(resultSet.getString("fee"));
                    weightField.setText(resultSet.getString("weight"));
                    String status = resultSet.getString("status");
                    statusComboBox.setSelectedItem(status);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading parcel details", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void notifySenderReceiver(int parcelId, String newStatus) {
        // Implementation for notifying sender and receiver
        // You can use the SmsSender class or any other notification mechanism
    }

    private void loadParcels() {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM parcel_table");

            // Clear the existing data from the DefaultListModel
            parcelListModel.clear();

            while (resultSet.next()) {
                int parcelId = resultSet.getInt("parcel_id");
                String destination = resultSet.getString("destination");
                String status = resultSet.getString("status");

                // Build the information string, including the parcel_id and status
                String parcelInfo = parcelId + " - " + destination + " - " + status;

                // Add the string to the DefaultListModel
                parcelListModel.addElement(parcelInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading parcels", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewParcel() {
        String destination = destinationField.getText();
        String senderContact = senderContactField.getText();
        String receiverContact = receiverContactField.getText();
        String fee = feeField.getText();
        String weight = weightField.getText();
        String status = (String) statusComboBox.getSelectedItem();

        if (destination.isEmpty() || senderContact.isEmpty() || receiverContact.isEmpty() || fee.isEmpty() || weight.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO parcel_table (destination, sender_contact, receiver_contact, fee, weight, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

                preparedStatement.setString(1, destination);
                preparedStatement.setString(2, senderContact);
                preparedStatement.setString(3, receiverContact);
                preparedStatement.setString(4, fee);
                preparedStatement.setString(5, weight);
                preparedStatement.setString(6, status);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Parcel added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearParcelFields();
                    loadParcels();  // Refresh the list of parcels
                } else {
                    JOptionPane.showMessageDialog(this, "No parcel added", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding parcel", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ParcelCRUDApp::new);
    }
}