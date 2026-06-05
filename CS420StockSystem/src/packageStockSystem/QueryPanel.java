package packageStockSystem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Tab 3 – Data Query.
 *
 * Four query patterns, each on its own sub-tab:
 *
 *  Pattern 1 – Songs by Artist
 *      Filters : Artist (dropdown), Sort field (Title / Duration / Annual Sales)
 *      Shows   : Song Title, Album, Duration (m:ss), Annual Sales
 *      
 *  Pattern 2 – Albums by Release Year Range
 *      Filters : From Year, To Year, Sort Order (ASC / DESC)
 *      Shows   : Album Title, Artist, Release Year, Song Count
 *
 *  Pattern 3 – Top N Songs by Annual Sales
 *      Filters : Genre (dropdown or "All"), Top N (5 / 10 / 20 / All)
 *      Shows   : Rank, Song Title, Artist, Album, Annual Sales
 *

 */
public class QueryPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // ── Pattern 1  
    private JComboBox<String> cmbP1Artist;
    private JComboBox<String> cmbP1Sort;
    private JTable            tblP1;
    private DefaultTableModel mdlP1;
    private JLabel            lblP1Count;

    // ── Pattern 2
    private JTextField        txtP2FromYear;
    private JTextField        txtP2ToYear;
    private JComboBox<String> cmbP2Sort;
    private JTable            tblP2;
    private DefaultTableModel mdlP2;
    private JLabel            lblP2Count;

    // ── Pattern 3  
    private JComboBox<String> cmbP3Genre;
    private JComboBox<String> cmbP3TopN;
    private JTable            tblP3;
    private DefaultTableModel mdlP3;
    private JLabel            lblP3Count;


    public QueryPanel() {
        setLayout(new BorderLayout(0, 0));

        JLabel hint = new JLabel("  Set filters and click Run Query to display results.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));
        add(hint, BorderLayout.NORTH);

        JTabbedPane subTabs = new JTabbedPane(JTabbedPane.LEFT);
        subTabs.addTab("Songs by Artist",       buildPattern1());
        subTabs.addTab("Albums by Year Range",  buildPattern2());
        subTabs.addTab("Top N by Sales",        buildPattern3());
        
        add(subTabs, BorderLayout.CENTER);
    }

    /** Called by MusicLibraryGUI when this tab gains focus. */
    public void refreshCombos() {
        loadArtistsIntoCombo(cmbP1Artist);
        loadGenresIntoCombo(cmbP3Genre);
//      loadSongsIntoCombo(cmbP2Song);
    }

    //  Pattern 1 – Songs by Artist
    private JPanel buildPattern1() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Filter bar
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filters.setBorder(new TitledBorder("Filters"));

        cmbP1Artist = new JComboBox<>();
        loadArtistsIntoCombo(cmbP1Artist);

        cmbP1Sort = new JComboBox<>(new String[]{
            "Annual Sales (High → Low)",
            "Annual Sales (Low → High)",
            "Duration (High → Low)",
            "Duration (Low → High)",
            "Song Title (A → Z)"
        });

        filters.add(new JLabel("Artist:"));
        filters.add(cmbP1Artist);
        filters.add(Box.createHorizontalStrut(16));
        filters.add(new JLabel("Sort by:"));
        filters.add(cmbP1Sort);

        JButton btnRun = runButton();
        filters.add(Box.createHorizontalStrut(16));
        filters.add(btnRun);

        outer.add(filters, BorderLayout.NORTH);

        // Results table
        mdlP1 = new DefaultTableModel(
            new String[]{"Song Title", "Album", "Duration", "Annual Sales"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblP1 = styledTable(mdlP1);
        outer.add(new JScrollPane(tblP1), BorderLayout.CENTER);

        lblP1Count = countLabel();
        outer.add(lblP1Count, BorderLayout.SOUTH);

        btnRun.addActionListener(e -> runPattern1());
        return outer;
    }

    private void runPattern1() {
        String artistItem = (String) cmbP1Artist.getSelectedItem();
        if (artistItem == null) return;
        int artistId = extractId(artistItem);

        String orderBy;
        switch (cmbP1Sort.getSelectedIndex()) {
            case 0: orderBy = "s.AnnualSales DESC";  break;
            case 1: orderBy = "s.AnnualSales ASC";   break;
            case 2: orderBy = "s.Duration DESC";     break;
            case 3: orderBy = "s.Duration ASC";      break;
            default: orderBy = "s.Title ASC";        break;
        }

        String sql =
            "SELECT s.Title, al.Title AS Album, s.Duration, s.AnnualSales " +
            "FROM SONG s " +
            "JOIN ALBUM al ON s.AlbumID = al.AlbumID " +
            "JOIN ARTIST ar ON al.ArtistID = ar.ArtistID " +
            "WHERE ar.ArtistID = ? " +
            "ORDER BY " + orderBy;

        mdlP1.setRowCount(0);
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, artistId);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                mdlP1.addRow(new Object[]{
                    rs.getString("Title"),
                    rs.getString("Album"),
                    formatDuration(rs.getInt("Duration")),
                    String.format("%,d", rs.getInt("AnnualSales"))
                });
                count++;
            }
            lblP1Count.setText("  " + count + " record(s) returned.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //  Pattern 2 – Albums by Release Year Range
    private JPanel buildPattern2() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filters.setBorder(new TitledBorder("Filters"));

        txtP2FromYear = new JTextField("2000", 6);
        txtP2ToYear   = new JTextField("2024", 6);
        cmbP2Sort     = new JComboBox<>(new String[]{"Year (Oldest First)", "Year (Newest First)", "Title (A → Z)"});

        filters.add(new JLabel("From Year:"));
        filters.add(txtP2FromYear);
        filters.add(new JLabel("To Year:"));
        filters.add(txtP2ToYear);
        filters.add(Box.createHorizontalStrut(10));
        filters.add(new JLabel("Sort:"));
        filters.add(cmbP2Sort);

        JButton btnRun = runButton();
        filters.add(Box.createHorizontalStrut(10));
        filters.add(btnRun);

        outer.add(filters, BorderLayout.NORTH);

        mdlP2 = new DefaultTableModel(
            new String[]{"Album Title", "Artist", "Release Year", "Song Count"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblP2 = styledTable(mdlP2);
        outer.add(new JScrollPane(tblP2), BorderLayout.CENTER);

        lblP2Count = countLabel();
        outer.add(lblP2Count, BorderLayout.SOUTH);

        btnRun.addActionListener(e -> runPattern2());
        return outer;
    }

    private void runPattern2() {
        String fromStr = txtP2FromYear.getText().trim();
        String toStr   = txtP2ToYear.getText().trim();
        if (fromStr.isEmpty() || toStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both From and To years.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fromYear, toYear;
        try {
            fromYear = Integer.parseInt(fromStr);
            toYear   = Integer.parseInt(toStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Years must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String orderBy;
        switch (cmbP2Sort.getSelectedIndex()) {
            case 1:  orderBy = "al.ReleaseYear DESC"; break;
            case 2:  orderBy = "al.Title ASC";        break;
            default: orderBy = "al.ReleaseYear ASC";  break;
        }

        String sql =
            "SELECT al.Title, ar.Name AS Artist, al.ReleaseYear, COUNT(s.SongID) AS SongCount " +
            "FROM ALBUM al " +
            "JOIN ARTIST ar ON al.ArtistID = ar.ArtistID " +
            "LEFT JOIN SONG s ON al.AlbumID = s.AlbumID " +
            "WHERE al.ReleaseYear BETWEEN ? AND ? " +
            "GROUP BY al.AlbumID, al.Title, ar.Name, al.ReleaseYear " +
            "ORDER BY " + orderBy;

        mdlP2.setRowCount(0);
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, fromYear);
            ps.setInt(2, toYear);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                mdlP2.addRow(new Object[]{
                    rs.getString("Title"),
                    rs.getString("Artist"),
                    rs.getInt("ReleaseYear"),
                    rs.getInt("SongCount")
                });
                count++;
            }
            lblP2Count.setText("  " + count + " album(s) returned.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    //  Pattern 3 – Top N Songs by Annual Sales
    private JPanel buildPattern3() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filters.setBorder(new TitledBorder("Filters"));

        cmbP3Genre = new JComboBox<>();
        loadGenresIntoCombo(cmbP3Genre);

        cmbP3TopN = new JComboBox<>(new String[]{"Top 5", "Top 10", "Top 20", "All"});
        cmbP3TopN.setSelectedIndex(1);

        filters.add(new JLabel("Genre:"));
        filters.add(cmbP3Genre);
        filters.add(Box.createHorizontalStrut(16));
        filters.add(new JLabel("Show:"));
        filters.add(cmbP3TopN);

        JButton btnRun = runButton();
        filters.add(Box.createHorizontalStrut(16));
        filters.add(btnRun);

        outer.add(filters, BorderLayout.NORTH);

        mdlP3 = new DefaultTableModel(
            new String[]{"Rank", "Song Title", "Artist", "Album", "Annual Sales"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblP3 = styledTable(mdlP3);
        outer.add(new JScrollPane(tblP3), BorderLayout.CENTER);

        lblP3Count = countLabel();
        outer.add(lblP3Count, BorderLayout.SOUTH);

        btnRun.addActionListener(e -> runPattern3());
        return outer;
    }

    private void runPattern3() {
        String genreItem = (String) cmbP3Genre.getSelectedItem();
        boolean allGenres = genreItem == null || genreItem.equals("All Genres");

        String topNStr = (String) cmbP3TopN.getSelectedItem();
        boolean limitAll = "All".equals(topNStr);
        int limit = limitAll ? Integer.MAX_VALUE : Integer.parseInt(topNStr.replace("Top ", ""));

        StringBuilder sql = new StringBuilder(
            "SELECT s.Title, ar.Name AS Artist, al.Title AS Album, ar.Genre, s.AnnualSales " +
            "FROM SONG s " +
            "JOIN ALBUM al ON s.AlbumID = al.AlbumID " +
            "JOIN ARTIST ar ON al.ArtistID = ar.ArtistID "
        );
        if (!allGenres) sql.append("WHERE ar.Genre = ? ");
        sql.append("ORDER BY s.AnnualSales DESC");
        if (!limitAll) sql.append(" LIMIT ?");

        mdlP3.setRowCount(0);
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (!allGenres)   ps.setString(paramIdx++, genreItem);
            if (!limitAll)    ps.setInt(paramIdx,      limit);
            ResultSet rs = ps.executeQuery();
            int rank = 1, count = 0;
            while (rs.next()) {
                mdlP3.addRow(new Object[]{
                    rank++,
                    rs.getString("Title"),
                    rs.getString("Artist"),
                    rs.getString("Album"),
                    String.format("%,d", rs.getInt("AnnualSales"))
                });
                count++;
            }
            lblP3Count.setText("  " + count + " record(s) returned.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    private void loadGenresIntoCombo(JComboBox<String> cmb) {
        cmb.removeAllItems();
        cmb.addItem("All Genres");
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT Genre FROM ARTIST ORDER BY Genre")) {
            while (rs.next()) cmb.addItem(rs.getString(1));
        } catch (SQLException ex) { /* keep "All Genres" only */ }
    }

    //  Utilities
    private int extractId(String item) {
        return Integer.parseInt(item.split(" – ")[0].trim());
    }

    /** Converts seconds to m:ss format, e.g. 234 → "3:54". */
    private String formatDuration(int seconds) {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    private JButton runButton() {
        JButton btn = new JButton("Run Query");
        btn.setBackground(new Color(60, 130, 200));
        btn.setForeground(Color.blue);
        btn.setFocusPainted(false);
        return btn;
    }

    private JLabel countLabel() {
        JLabel lbl = new JLabel("Run a query to see results.");
        lbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lbl.setForeground(Color.GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
        return lbl;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        return table;
    }
}


