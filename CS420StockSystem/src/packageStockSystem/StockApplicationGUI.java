package packageStockSystem;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class StockApplicationGUI extends JFrame{	

		private static final long serialVersionUID = 1L;

	    private JTabbedPane tabbedPane;
	    private InsertionPanel     insertionPanel;
	    private ModificationPanel  modificationPanel;
	    private QueryPanel         queryPanel;

		/**
		 * Launch the application.
		 */
	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            try {
	                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	                StockApplicationGUI frame = new StockApplicationGUI();
	                frame.setVisible(true);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        });
	    }

		/**
		 * Create the application.
		 */
	    public StockApplicationGUI() {
	        setTitle("Stock Portfolio management system  –  CS420");
	        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	        setBounds(120, 80, 980, 700);
	        setMinimumSize(new Dimension(820, 580));

	        // Graceful shutdown: close DB connection before exit
	        addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	                int choice = JOptionPane.showConfirmDialog(
	                		StockApplicationGUI.this,
	                        "Exit the application?",
	                        "Confirm Exit",
	                        JOptionPane.YES_NO_OPTION);
	                if (choice == JOptionPane.YES_OPTION) {
	                    DatabaseConnection.closeConnection();
	                    dispose();
	                    System.exit(0);
	                }
	            }
	        });

	        // ---- Status bar at the bottom ----
	        JLabel statusBar = new JLabel("  Ready");
	        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
	        statusBar.setPreferredSize(new Dimension(getWidth(), 22));
	        getContentPane().add(statusBar, BorderLayout.SOUTH);

	        // ---- Header banner ----
	        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
	        headerPanel.setBackground(new Color(45, 45, 60));
	        JLabel headerLabel = new JLabel("Stock portfolio");
	        headerLabel.setForeground(Color.WHITE);
	        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
	        headerPanel.add(headerLabel);
	        getContentPane().add(headerPanel, BorderLayout.NORTH);

	        // ---- Three main tabs ----
	        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));
	        getContentPane().add(tabbedPane, BorderLayout.CENTER);

	        insertionPanel    = new InsertionPanel();
	        modificationPanel = new ModificationPanel();
	        queryPanel        = new QueryPanel();

	        tabbedPane.addTab("Data Insertion  ",   null, insertionPanel,    "Add new stocks and companys");
	        tabbedPane.addTab("Data Modification  ", null, modificationPanel, "Modify existing records");
	        tabbedPane.addTab("Data Query  ",        null, queryPanel,        "Query and filter database records");

	        // Refresh combo-boxes whenever a tab becomes active
	        tabbedPane.addChangeListener(e -> {
	            int idx = tabbedPane.getSelectedIndex();
	            if (idx == 1) modificationPanel.refreshCombos();
	            if (idx == 2) queryPanel.refreshCombos();
	        });
	    

		

	}
	    }

