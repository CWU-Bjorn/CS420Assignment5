package packageStockSystem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Tab 2 – Data Modification.
 * Pattern: enter an ID → Search → fields populate → edit → Update.
 * Primary Keys are shown but locked (non-editable).
 * Sub-tabs: Artist | Album | Song.
 */
public class ModificationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // Artist fields  
    private JTextField txtArtistId;
    private JTextField txtArtistName, txtArtistGenre, txtArtistNationality;
    private JTextField txtArtistBirthYear, txtArtistDebutYear;
    private JLabel     lblArtistStatus;

    // Album fields  
    private JTextField    txtAlbumId;
    private JTextField    txtAlbumTitle, txtAlbumYear;
    private JComboBox<String> cmbAlbumArtist;
    private JLabel        lblAlbumStatus;

    // Song fields  
    private JTextField    txtSongId;
    private JTextField    txtSongTitle, txtSongDuration, txtSongSales;
    private JComboBox<String> cmbSongAlbum;
    private JLabel        lblSongStatus;

    public ModificationPanel() {
        setLayout(new BorderLayout(0, 0));

        JLabel hint = new JLabel("  Enter an ID and click Search to load a record. Primary Keys are locked.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));
        add(hint, BorderLayout.NORTH);

        JTabbedPane subTabs = new JTabbedPane(JTabbedPane.LEFT);
        subTabs.addTab("Artist", buildArtistPanel());
        subTabs.addTab("Album",  buildAlbumPanel());
        subTabs.addTab("Song",   buildSongPanel());

        subTabs.addChangeListener(e -> {
            int i = subTabs.getSelectedIndex();
            if (i == 1) loadArtistsIntoCombo(cmbAlbumArtist);
            if (i == 2) loadAlbumsIntoCombo(cmbSongAlbum);
        });

        add(subTabs, BorderLayout.CENTER);
    }

    /** Called by MusicLibraryGUI when this tab gains focus. */
    public void refreshCombos() {
        if (cmbAlbumArtist != null) loadArtistsIntoCombo(cmbAlbumArtist);
        if (cmbSongAlbum   != null) loadAlbumsIntoCombo(cmbSongAlbum);
    }

    //  Artist panel
    private JPanel buildArtistPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Modify Artist"));
        GridBagConstraints gbc = defaultGBC();

        // Search row
        txtArtistId = new JTextField(8);
        JButton btnSearch = new JButton("Search");
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchRow.add(new JLabel("Artist ID:"));
        searchRow.add(txtArtistId);
        searchRow.add(btnSearch);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        p.add(searchRow, gbc);

        // Separator
        gbc.gridy = 1; gbc.insets = new Insets(0, 8, 8, 8);
        p.add(new JSeparator(), gbc);

        // Editable fields
        txtArtistName        = new JTextField(24);
        txtArtistGenre       = new JTextField(24);
        txtArtistNationality = new JTextField(24);
        txtArtistBirthYear   = new JTextField(8);
        txtArtistDebutYear   = new JTextField(8);
        setFieldsEnabled(false, txtArtistName, txtArtistGenre, txtArtistNationality,
                                txtArtistBirthYear, txtArtistDebutYear);

        addRow(p, gbc, 2, "Name *",        txtArtistName);
        addRow(p, gbc, 3, "Genre *",       txtArtistGenre);
        addRow(p, gbc, 4, "Nationality *", txtArtistNationality);
        addRow(p, gbc, 5, "Birth Year *",  txtArtistBirthYear);
        addRow(p, gbc, 6, "Debut Year *",  txtArtistDebutYear);

        JButton btnUpdate = new JButton("Update Artist");
        btnUpdate.setBackground(new Color(60, 160, 90));
        btnUpdate.setForeground(Color.black);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setEnabled(false);

        JButton btnClear = new JButton("Clear");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.add(btnUpdate);
        btnRow.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);
        p.add(btnRow, gbc);

        lblArtistStatus = new JLabel(" ");
        lblArtistStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 8; gbc.insets = new Insets(2, 10, 8, 8);
        p.add(lblArtistStatus, gbc);

        // ── Listeners ──
        btnSearch.addActionListener(e -> {
            if (searchArtist(txtArtistId.getText().trim())) {
                setFieldsEnabled(true, txtArtistName, txtArtistGenre, txtArtistNationality,
                                       txtArtistBirthYear, txtArtistDebutYear);
                btnUpdate.setEnabled(true);
            }
        });
        btnUpdate.addActionListener(e -> updateArtist(txtArtistId.getText().trim(), btnUpdate));
        btnClear.addActionListener(e -> {
            txtArtistId.setText("");
            clearFields(txtArtistName, txtArtistGenre, txtArtistNationality,
                        txtArtistBirthYear, txtArtistDebutYear);
            setFieldsEnabled(false, txtArtistName, txtArtistGenre, txtArtistNationality,
                                    txtArtistBirthYear, txtArtistDebutYear);
            btnUpdate.setEnabled(false);
            lblArtistStatus.setText(" ");
        });
        return p;
    }

    private boolean searchArtist(String idStr) {
        if (idStr.isEmpty()) {
            showStatus(lblArtistStatus, "Enter an Artist ID.", Color.RED);
            return false;
        }
        try {
            int id = Integer.parseInt(idStr);
            String sql = "SELECT Name, Genre, Nationality, BirthYear, DebutYear FROM ARTIST WHERE ArtistID = ?";
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtArtistName.setText(rs.getString("Name"));
                    txtArtistGenre.setText(rs.getString("Genre"));
                    txtArtistNationality.setText(rs.getString("Nationality"));
                    txtArtistBirthYear.setText(String.valueOf(rs.getInt("BirthYear")));
                    txtArtistDebutYear.setText(String.valueOf(rs.getInt("DebutYear")));
                    showStatus(lblArtistStatus, "Artist found. Edit fields and click Update.", new Color(0, 100, 180));
                    return true;
                } else {
                    showStatus(lblArtistStatus, "No artist found with ID " + id + ".", Color.RED);
                    return false;
                }
            }
        } catch (NumberFormatException ex) {
            showStatus(lblArtistStatus, "Artist ID must be an integer.", Color.RED);
        } catch (SQLException ex) {
            showStatus(lblArtistStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
        return false;
    }

    private void updateArtist(String idStr, JButton btnUpdate) {
        String name        = txtArtistName.getText().trim();
        String genre       = txtArtistGenre.getText().trim();
        String nationality = txtArtistNationality.getText().trim();
        String birthStr    = txtArtistBirthYear.getText().trim();
        String debutStr    = txtArtistDebutYear.getText().trim();

        if (name.isEmpty() || genre.isEmpty() || nationality.isEmpty()
                || birthStr.isEmpty() || debutStr.isEmpty()) {
            showStatus(lblArtistStatus, "All artist fields are required.", Color.RED);
            return;
        }
        int birthYear, debutYear;
        try {
            birthYear = Integer.parseInt(birthStr);
            debutYear = Integer.parseInt(debutStr);
        } catch (NumberFormatException ex) {
            showStatus(lblArtistStatus, "Birth Year and Debut Year must be integers.", Color.RED);
            return;
        }
        if (debutYear < birthYear) {
            showStatus(lblArtistStatus, "Debut Year cannot be earlier than Birth Year.", Color.RED);
            return;
        }

        String sql = "UPDATE ARTIST SET Name=?, Genre=?, Nationality=?, BirthYear=?, DebutYear=? WHERE ArtistID=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, genre);
            ps.setString(3, nationality);
            ps.setInt(4, birthYear);
            ps.setInt(5, debutYear);
            ps.setInt(6, Integer.parseInt(idStr));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                showStatus(lblArtistStatus, "Artist updated successfully.", new Color(0, 140, 0));
                btnUpdate.setEnabled(false);
                setFieldsEnabled(false, txtArtistName, txtArtistGenre, txtArtistNationality,
                                        txtArtistBirthYear, txtArtistDebutYear);
            }
        } catch (SQLException ex) {
            showStatus(lblArtistStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
    }

    //  Album panel
    private JPanel buildAlbumPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Modify Album"));
        GridBagConstraints gbc = defaultGBC();

        txtAlbumId = new JTextField(8);
        JButton btnSearch = new JButton("Search");
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchRow.add(new JLabel("Album ID:"));
        searchRow.add(txtAlbumId);
        searchRow.add(btnSearch);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        p.add(searchRow, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 8, 8, 8);
        p.add(new JSeparator(), gbc);

        cmbAlbumArtist = new JComboBox<>();
        txtAlbumTitle  = new JTextField(24);
        txtAlbumYear   = new JTextField(8);
        loadArtistsIntoCombo(cmbAlbumArtist);
        setComponentEnabled(false, cmbAlbumArtist, txtAlbumTitle, txtAlbumYear);

        // Note: ArtistID (FK) IS modifiable — allows re-assigning an album to a different artist.
        // AlbumID (PK) is NOT shown as editable.
        addRow(p, gbc, 2, "Artist",        cmbAlbumArtist);
        addRow(p, gbc, 3, "Title *",       txtAlbumTitle);
        addRow(p, gbc, 4, "Release Year *", txtAlbumYear);

        JButton btnUpdate = new JButton("Update Album");
        btnUpdate.setBackground(new Color(60, 160, 90));
        btnUpdate.setForeground(Color.black);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setEnabled(false);
        JButton btnClear = new JButton("Clear");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.add(btnUpdate); btnRow.add(btnClear);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);
        p.add(btnRow, gbc);

        lblAlbumStatus = new JLabel(" ");
        lblAlbumStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 6; gbc.insets = new Insets(2, 10, 8, 8);
        p.add(lblAlbumStatus, gbc);

        btnSearch.addActionListener(e -> {
            if (searchAlbum(txtAlbumId.getText().trim())) {
                setComponentEnabled(true, cmbAlbumArtist, txtAlbumTitle, txtAlbumYear);
                btnUpdate.setEnabled(true);
            }
        });
        btnUpdate.addActionListener(e -> updateAlbum(txtAlbumId.getText().trim(), btnUpdate));
        btnClear.addActionListener(e -> {
            txtAlbumId.setText("");
            txtAlbumTitle.setText(""); txtAlbumYear.setText("");
            setComponentEnabled(false, cmbAlbumArtist, txtAlbumTitle, txtAlbumYear);
            btnUpdate.setEnabled(false);
            lblAlbumStatus.setText(" ");
        });
        return p;
    }

    private boolean searchAlbum(String idStr) {
        if (idStr.isEmpty()) { showStatus(lblAlbumStatus, "✖  Enter an Album ID.", Color.RED); return false; }
        try {
            int id = Integer.parseInt(idStr);
            String sql = "SELECT Title, ReleaseYear, ArtistID FROM ALBUM WHERE AlbumID = ?";
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtAlbumTitle.setText(rs.getString("Title"));
                    txtAlbumYear.setText(String.valueOf(rs.getInt("ReleaseYear")));
                    selectComboById(cmbAlbumArtist, rs.getInt("ArtistID"));
                    showStatus(lblAlbumStatus, "Album found. Edit and click Update.", new Color(0, 100, 180));
                    return true;
                } else {
                    showStatus(lblAlbumStatus, "No album found with ID " + id + ".", Color.RED);
                }
            }
        } catch (NumberFormatException ex) {
            showStatus(lblAlbumStatus, "Album ID must be an integer.", Color.RED);
        } catch (SQLException ex) {
            showStatus(lblAlbumStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
        return false;
    }

    private void updateAlbum(String idStr, JButton btnUpdate) {
        String title   = txtAlbumTitle.getText().trim();
        String yearStr = txtAlbumYear.getText().trim();
        String artist  = (String) cmbAlbumArtist.getSelectedItem();
        if (title.isEmpty() || yearStr.isEmpty() || artist == null) {
            showStatus(lblAlbumStatus, "All fields are required.", Color.RED); return;
        }
        try {
            int releaseYear = Integer.parseInt(yearStr);
            int artistId    = extractId(artist);
            String sql = "UPDATE ALBUM SET Title=?, ReleaseYear=?, ArtistID=? WHERE AlbumID=?";
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, title);
                ps.setInt(2, releaseYear);
                ps.setInt(3, artistId);
                ps.setInt(4, Integer.parseInt(idStr));
                ps.executeUpdate();
                showStatus(lblAlbumStatus, "Album updated successfully.", new Color(0, 140, 0));
                btnUpdate.setEnabled(false);
                setComponentEnabled(false, cmbAlbumArtist, txtAlbumTitle, txtAlbumYear);
            }
        } catch (NumberFormatException ex) {
            showStatus(lblAlbumStatus, "Release Year must be an integer.", Color.RED);
        } catch (SQLException ex) {
            showStatus(lblAlbumStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
    }

    //  Song panel
    private JPanel buildSongPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Modify Song"));
        GridBagConstraints gbc = defaultGBC();

        txtSongId = new JTextField(8);
        JButton btnSearch = new JButton("Search");
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchRow.add(new JLabel("Song ID:"));
        searchRow.add(txtSongId);
        searchRow.add(btnSearch);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        p.add(searchRow, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 8, 8, 8);
        p.add(new JSeparator(), gbc);

        cmbSongAlbum    = new JComboBox<>();
        txtSongTitle    = new JTextField(24);
        txtSongDuration = new JTextField(8);
        txtSongSales    = new JTextField(12);
        loadAlbumsIntoCombo(cmbSongAlbum);
        setComponentEnabled(false, cmbSongAlbum, txtSongTitle, txtSongDuration, txtSongSales);

        addRow(p, gbc, 2, "Album",             cmbSongAlbum);
        addRow(p, gbc, 3, "Title *",           txtSongTitle);
        addRow(p, gbc, 4, "Duration (sec) *",  txtSongDuration);
        addRow(p, gbc, 5, "Annual Sales",      txtSongSales);

        JButton btnUpdate = new JButton("Update Song");
        btnUpdate.setBackground(new Color(60, 160, 90));
        btnUpdate.setForeground(Color.black);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setEnabled(false);
        JButton btnClear = new JButton("Clear");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.add(btnUpdate); btnRow.add(btnClear);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 4, 8);
        p.add(btnRow, gbc);

        lblSongStatus = new JLabel(" ");
        lblSongStatus.setFont(new Font("SansSerif", Font.ITALIC, 11));
        gbc.gridy = 7; gbc.insets = new Insets(2, 10, 8, 8);
        p.add(lblSongStatus, gbc);

        btnSearch.addActionListener(e -> {
            if (searchSong(txtSongId.getText().trim())) {
                setComponentEnabled(true, cmbSongAlbum, txtSongTitle, txtSongDuration, txtSongSales);
                btnUpdate.setEnabled(true);
            }
        });
        btnUpdate.addActionListener(e -> updateSong(txtSongId.getText().trim(), btnUpdate));
        btnClear.addActionListener(e -> {
            txtSongId.setText("");
            clearFields(txtSongTitle, txtSongDuration, txtSongSales);
            setComponentEnabled(false, cmbSongAlbum, txtSongTitle, txtSongDuration, txtSongSales);
            btnUpdate.setEnabled(false);
            lblSongStatus.setText(" ");
        });
        return p;
    }

    private boolean searchSong(String idStr) {
        if (idStr.isEmpty()) { showStatus(lblSongStatus, "Enter a Song ID.", Color.RED); return false; }
        try {
            int id = Integer.parseInt(idStr);
            String sql = "SELECT Title, Duration, AnnualSales, AlbumID FROM SONG WHERE SongID = ?";
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtSongTitle.setText(rs.getString("Title"));
                    txtSongDuration.setText(String.valueOf(rs.getInt("Duration")));
                    txtSongSales.setText(String.valueOf(rs.getInt("AnnualSales")));
                    selectComboById(cmbSongAlbum, rs.getInt("AlbumID"));
                    showStatus(lblSongStatus, "Song found. Edit and click Update.", new Color(0, 100, 180));
                    return true;
                } else {
                    showStatus(lblSongStatus, "No song found with ID " + id + ".", Color.RED);
                }
            }
        } catch (NumberFormatException ex) {
            showStatus(lblSongStatus, "Song ID must be an integer.", Color.RED);
        } catch (SQLException ex) {
            showStatus(lblSongStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
        return false;
    }

    private void updateSong(String idStr, JButton btnUpdate) {
        String title   = txtSongTitle.getText().trim();
        String durStr  = txtSongDuration.getText().trim();
        String salStr  = txtSongSales.getText().trim();
        String album   = (String) cmbSongAlbum.getSelectedItem();
        if (title.isEmpty() || durStr.isEmpty() || album == null) {
            showStatus(lblSongStatus, "Album, Title, and Duration are required.", Color.RED); return;
        }
        try {
            int duration = Integer.parseInt(durStr);
            int sales    = salStr.isEmpty() ? 0 : Integer.parseInt(salStr);
            int albumId  = extractId(album);
            String sql = "UPDATE SONG SET Title=?, Duration=?, AnnualSales=?, AlbumID=? WHERE SongID=?";
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, title);
                ps.setInt(2, duration);
                ps.setInt(3, sales);
                ps.setInt(4, albumId);
                ps.setInt(5, Integer.parseInt(idStr));
                ps.executeUpdate();
                showStatus(lblSongStatus, "Song updated successfully.", new Color(0, 140, 0));
                btnUpdate.setEnabled(false);
                setComponentEnabled(false, cmbSongAlbum, txtSongTitle, txtSongDuration, txtSongSales);
            }
        } catch (NumberFormatException ex) {
            showStatus(lblSongStatus, "Duration and Sales must be integers.", Color.RED);
        } catch (SQLException ex) {
            showStatus(lblSongStatus, "DB Error: " + ex.getMessage(), Color.RED);
        }
    }

    //  DB loaders
    private void loadArtistsIntoCombo(JComboBox<String> cmb) {
        cmb.removeAllItems();
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT ArtistID, Name FROM ARTIST ORDER BY Name")) {
            while (rs.next()) cmb.addItem(rs.getInt(1) + " – " + rs.getString(2));
        } catch (SQLException ex) { cmb.addItem("(connection error)"); }
    }

    private void loadAlbumsIntoCombo(JComboBox<String> cmb) {
        cmb.removeAllItems();
        String sql = "SELECT al.AlbumID, al.Title, ar.Name "
                   + "FROM ALBUM al JOIN ARTIST ar ON al.ArtistID = ar.ArtistID ORDER BY al.Title";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                cmb.addItem(rs.getInt(1) + " – " + rs.getString(2) + " (" + rs.getString(3) + ")");
        } catch (SQLException ex) { cmb.addItem("(connection error)"); }
    }

    //  Utilities
    private int extractId(String item) {
        return Integer.parseInt(item.split(" – ")[0].trim());
    }

    /** Selects the combo item whose leading ID matches targetId. */
    private void selectComboById(JComboBox<String> cmb, int targetId) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (extractId(cmb.getItemAt(i)) == targetId) { cmb.setSelectedIndex(i); return; }
        }
    }

    private void showStatus(JLabel lbl, String msg, Color color) {
        lbl.setText(msg);
        lbl.setForeground(color);
    }

    private void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    private void setFieldsEnabled(boolean enabled, JTextField... fields) {
        for (JTextField f : fields) f.setEnabled(enabled);
    }

    private void setComponentEnabled(boolean enabled, JComponent... comps) {
        for (JComponent c : comps) c.setEnabled(enabled);
    }

    private GridBagConstraints defaultGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
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
}


