package packageStockSystem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Tab 1 – Data Insertion.
 * Sub-tabs: Artist | Album | Song.
 * Fields marked (*) are required; others may be left blank and will
 * receive their database default values.
 */
public class InsertionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // ── Investor fields ────────────────────────────────────────────
    private JTextField txtInvestorLastName;
    private JTextField txtInvestorFirstName;
    private JTextField txtInvestorCountry;
    private JTextField txtInvestorEmail;
    private JTextField txtInvestorPhone;
    private JLabel     lblInvestorStatus;

    // ── Company fields ─────────────────────────────────────────────
    private JTextField txtCompanyName;
    private JTextField txtCompanyIndustry;
    private JTextField txtCompanyCountry;
    private JTextField txtCompanyHeadquarters;
    private JTextField txtCompanyFoundedYear;
    private JLabel     lblCompanyStatus;

    // ── Stock fields ──────────────────────────────────────────────
    private JComboBox<CompanyItem> cmbStockCompany;
    private JTextField        txtStockTickerSymbol;
    private JTextField        txtStockExchangeName;   // seconds
    private JTextField        txtStockCurrentPrice;      // optional — defaults to 0
    private JLabel            lblStockStatus;

    public InsertionPanel() {
        setLayout(new BorderLayout(0, 0));

        JLabel hint = new JLabel("  Fields marked with * are required. Others may be left blank.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));
        add(hint, BorderLayout.NORTH);

        JTabbedPane subTabs = new JTabbedPane(JTabbedPane.LEFT);
        subTabs.addTab("Investor", buildInvestorPanel());
        subTabs.addTab("Company",  buildCompanyPanel());
        subTabs.addTab("Stock",   buildStockPanel());

        // Reload combos when the user switches to Album or Song sub-tab
        subTabs.addChangeListener(e -> {
            int i = subTabs.getSelectedIndex();
            if (i == 2) { 
            	
            	loadCompaniesIntoCombo(null);
            	
            	}
            
        });

        add(subTabs, BorderLayout.CENTER);
    }

    //  Investor panel
    private JPanel buildInvestorPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Insert New Artist"));
        GridBagConstraints gbc = defaultGBC();

        txtInvestorLastName        = new JTextField(24);
        txtInvestorFirstName       = new JTextField(24);
        txtInvestorCountry = new JTextField(24);
        txtInvestorEmail   = new JTextField(8);
        txtInvestorPhone   = new JTextField(8);

        addRow(p, gbc, 0, "Last Name *", txtInvestorLastName);
        addRow(p, gbc, 1, "First Name *", txtInvestorFirstName);
        addRow(p, gbc, 2, "Country *", txtInvestorCountry);
        addRow(p, gbc, 3, "Email *",  txtInvestorEmail);
        addRow(p, gbc, 4, "Phone *",  txtInvestorPhone);

        JButton btnInsert = new JButton("Insert Investor");
        btnInsert.setBackground(new Color(60, 130, 200));
        btnInsert.setForeground(Color.black);
        btnInsert.setFocusPainted(false);

        JButton btnClear = new JButton("Clear");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.add(btnInsert);
        btnRow.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);
        p.add(btnRow, gbc);

        lblInvestorStatus = new JLabel(" ");
        lblInvestorStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 6; gbc.insets = new Insets(2, 10, 8, 8);
        p.add(lblInvestorStatus, gbc);

        // ── Listeners ──
        btnInsert.addActionListener(e -> insertInvestor());
        btnClear.addActionListener(e -> {
            clearFields(txtInvestorLastName, txtInvestorFirstName, txtInvestorCountry,
            		txtInvestorEmail, txtInvestorPhone);
            lblInvestorStatus.setText(" ");
        });
        return p;
    }

    private void insertInvestor() {
        String LastName        = txtInvestorLastName.getText().trim();
        String FirstName       = txtInvestorFirstName.getText().trim();
        String Country = txtInvestorCountry.getText().trim();
        String Email    = txtInvestorEmail.getText().trim();
        String Phone    = txtInvestorPhone.getText().trim();

        // Validation
        if (LastName.isEmpty() || FirstName.isEmpty() || Country.isEmpty()
                || Email.isEmpty() || Phone.isEmpty()) {
            showStatus(lblInvestorStatus, "All fields are required for Investor.", Color.RED);
            return;
        }
        int birthYear, debutYear;
        try {
            birthYear = Integer.parseInt(Email);
            debutYear = Integer.parseInt(Phone);
        } catch (NumberFormatException ex) {
            showStatus(lblInvestorStatus, "Birth Year and Debut Year must be integers.", Color.RED);
            return;
        }
        if (debutYear < birthYear) {
            showStatus(lblInvestorStatus, "Debut Year cannot be earlier than Birth Year.", Color.RED);
            return;
        }

        String sql = "INSERT INTO INVESTOR (LastName, FirstName, Country, Email, Phone) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, LastName);
            ps.setString(2, FirstName);
            ps.setString(3, Country);
            ps.setInt(4, birthYear);
            ps.setInt(5, debutYear);
            ps.executeUpdate();
            showStatus(lblInvestorStatus, "Investor " + LastName + " inserted successfully.", new Color(0, 140, 0));
            clearFields(txtInvestorLastName, txtInvestorFirstName, txtInvestorCountry,
            		txtInvestorEmail, txtInvestorPhone);
        } catch (SQLException ex) {
            showStatus(lblInvestorStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
    }
    
    //  Album panel
    private JPanel buildCompanyPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Insert New Company"));
        GridBagConstraints gbc = defaultGBC();

        txtCompanyName = new JTextField(24);
        txtCompanyIndustry = new JTextField(24);
        txtCompanyCountry = new JTextField(24);
         txtCompanyHeadquarters = new JTextField(24);
        txtCompanyFoundedYear = new JTextField(24);

        addRow(p, gbc, 0, "Company Name *",       txtCompanyName);
        addRow(p, gbc, 1, "Industry *",        txtCompanyIndustry);
        addRow(p, gbc, 2, "Country *", txtCompanyCountry);
        addRow(p, gbc, 3, "Headquarters *", txtCompanyHeadquarters);
        addRow(p, gbc, 4, "Founded Year *", txtCompanyFoundedYear);
  

        JButton btnInsert = new JButton("Insert Company");
        btnInsert.setBackground(new Color(60, 130, 200));
        btnInsert.setForeground(Color.black);
        btnInsert.setFocusPainted(false);
        JButton btnClear = new JButton("Clear");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.add(btnInsert);
        btnRow.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);
        p.add(btnRow, gbc);

        lblCompanyStatus = new JLabel(" ");
        lblCompanyStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 4; gbc.insets = new Insets(2, 10, 8, 8);
        p.add(lblCompanyStatus, gbc);

        btnInsert.addActionListener(e -> insertCompany());
        btnClear.addActionListener(e -> {
        	clearFields(
        			txtCompanyName,
        		    txtCompanyIndustry,
        		    txtCompanyCountry,
        		     txtCompanyHeadquarters,
        		    txtCompanyFoundedYear
        		       );
        	
        	lblCompanyStatus.setText("");
        });
        return p;
    }

    private void insertCompany() {

        String companyName    = txtCompanyName.getText().trim();
        String industry    = txtCompanyIndustry.getText().trim();
        String country    = txtCompanyCountry.getText().trim();
        String headquarters    = txtCompanyHeadquarters.getText().trim();
        String foundedYear    = txtCompanyFoundedYear.getText().trim();


        if (companyName.isEmpty() || industry.isEmpty() || country.isEmpty()|| headquarters.isEmpty()|| foundedYear.isEmpty()) {
            showStatus(lblCompanyStatus, "All fields are required for Company.", Color.RED);
            return;
        }
        Integer foundedYearNull = null;
        
        try {
        	foundedYearNull = Integer.parseInt(foundedYear);
        } catch (NumberFormatException ex) {
            showStatus(lblCompanyStatus, "Founded Year must be an integer.", Color.RED);
            return;
        }

        String sql = "INSERT INTO COMPANY (CompanyName, Industry, Headquarters,FoundedYear, Country) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, companyName);
            ps.setString(2, industry);
            ps.setString(3, headquarters);
            ps.executeUpdate();
            
            showStatus(lblCompanyStatus, "Album " + txtCompanyName + " inserted successfully.", new Color(0, 140, 0));
            txtCompanyName.setText("");
            txtCompanyFoundedYear.setText("");
        } catch (SQLException ex) {
            showStatus(lblCompanyStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
    }

    
    //  Song panel
    private JPanel buildStockPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Insert New Song"));
        GridBagConstraints gbc = defaultGBC();

        cmbStockCompany    = new JComboBox<>();
        txtStockExchangeName    = new JTextField(24);
        txtStockTickerSymbol = new JTextField(8);
        txtStockCurrentPrice    = new JTextField(12);

        loadAlbumsIntoCombo(cmbStockCompany);

        addRow(p, gbc, 0, "Exchange Name *",              txtStockExchangeName);
        addRow(p, gbc, 1, "Ticker Symbol *",              txtStockTickerSymbol);
        addRow(p, gbc, 2, "Price *",     txtStockCurrentPrice);
        addRow(p, gbc, 3, "Company", cmbStockCompany);   // optional

        JLabel hintSales = new JLabel("(leave blank to default to 0)");
        hintSales.setFont(new Font("SansSerif", Font.ITALIC, 10));
        hintSales.setForeground(Color.GRAY);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 4, 6, 8);
        p.add(hintSales, gbc);

        JButton btnInsert = new JButton("Insert Song");
        btnInsert.setBackground(new Color(60, 130, 200));
        btnInsert.setForeground(Color.black);
        btnInsert.setFocusPainted(false);
        JButton btnClear = new JButton("Clear");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.add(btnInsert);
        btnRow.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);
        p.add(btnRow, gbc);

        lblStockStatus = new JLabel(" ");
        lblStockStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 6; gbc.insets = new Insets(2, 10, 8, 8);
        p.add(lblStockStatus, gbc);

        btnInsert.addActionListener(e -> insertSong());
        btnClear.addActionListener(e -> {
        	txtStockTickerSymbol.setText("");
        	txtStockExchangeName.setText("");
        	txtStockCurrentPrice.setText("");
            lblStockStatus.setText(" ");
        });
        return p;
    }

    private void insertSong() {
        String selected   = (String) cmbStockCompany.getSelectedItem();
        String title      = txtStockTickerSymbol.getText().trim();
        String durStr     = txtStockExchangeName.getText().trim();
        String salesStr   = txtStockCurrentPrice.getText().trim();

        if (selected == null || title.isEmpty() || durStr.isEmpty()) {
            showStatus(lblStockStatus, "Album, Title, and Duration are required.", Color.RED);
            return;
        }
        int albumId, duration, sales = 0;
        try {
            albumId  = extractId(selected);
            duration = Integer.parseInt(durStr);
            if (duration <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showStatus(lblStockStatus, " Duration must be a positive integer (seconds).", Color.RED);
            return;
        }
        if (!salesStr.isEmpty()) {
            try { sales = Integer.parseInt(salesStr); }
            catch (NumberFormatException ex) {
                showStatus(lblStockStatus, "Annual Sales must be an integer.", Color.RED);
                return;
            }
        }

        String sql = "INSERT INTO SONG (Title, Duration, AnnualSales, AlbumID) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, duration);
            ps.setInt(3, sales);
            ps.setInt(4, albumId);
            ps.executeUpdate();
            showStatus(lblStockStatus, "Song " + title + " inserted successfully.", new Color(0, 140, 0));
            txtStockTickerSymbol.setText("");
            txtStockExchangeName.setText("");
            txtStockCurrentPrice.setText("");
        } catch (SQLException ex) {
            showStatus(lblStockStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
    }

    //  DB loaders
    private void loadCompaniesIntoCombo(JComboBox<String> cmb) {
        cmb.removeAllItems();
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT ArtistID, Name FROM ARTIST ORDER BY Name")) {
            while (rs.next()) {
                cmb.addItem(rs.getInt(1) + " – " + rs.getString(2));
            }
        } catch (SQLException ex) {
            cmb.addItem("(connection error)");
        }
    }

    private void loadAlbumsIntoCombo(JComboBox<String> cmb) {
        cmb.removeAllItems();
        String sql = "SELECT al.AlbumID, al.Title, ar.Name "
                   + "FROM ALBUM al JOIN ARTIST ar ON al.ArtistID = ar.ArtistID ORDER BY al.Title";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cmb.addItem(rs.getInt(1) + " – " + rs.getString(2) + " (" + rs.getString(3) + ")");
            }
        } catch (SQLException ex) {
            cmb.addItem("(connection error)");
        }
    }

    //  Utilities
    /** Extracts the leading integer ID from a combo item like "3 – Shape of You". */
    private int extractId(String item) {
        return Integer.parseInt(item.split(" – ")[0].trim());
    }

    private void showStatus(JLabel lbl, String msg, Color color) {
        lbl.setText(msg);
        lbl.setForeground(color);
    }

    private void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    private GridBagConstraints defaultGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(6, 8, 6, 8);
        gbc.anchor  = GridBagConstraints.WEST;
        return gbc;
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.insets = new Insets(6, 10, 6, 4);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 4, 6, 12);
        p.add(field, gbc);
    }
    
    private static class companyItem() {
    	
    }
}