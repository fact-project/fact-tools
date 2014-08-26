/**
 * 
 */
package fact.demo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.demo.NodeComponent;
import fact.demo.ui.Button.ClickListener;

/**
 * @author chris
 * 
 */
public class ParameterDialog extends JDialog {

	/** The unique class ID */
	private static final long serialVersionUID = -8520036851206364651L;
	final static Logger log = LoggerFactory.getLogger(ParameterDialog.class);

	final JPanel content = new JPanel();
	final JPanel buttons = new JPanel();

	final ParamTable parameters = new ParamTable();

	final Color bgColor = new Color(39, 39, 34);
	final Color gridColor = new Color(52, 52, 47);

	public ParameterDialog(NodeComponent n) {
		setAutoRequestFocus(true);
		setUndecorated(true);

		setBackground(bgColor);
		content.setOpaque(true);
		content.setBackground(bgColor);
		content.setPreferredSize(new Dimension(200, 250));

		buttons.setBackground(bgColor);
		buttons.setOpaque(true);
		this.setOpacity(0.7f);

		setLayout(new BorderLayout());

		Map<String, String> attributes = n.getNode().attributes();
		if (attributes != null) {
			parameters.set(attributes);
			log.info("Setting parameters: {}", attributes);
		}

		final JTable table = new JTable(parameters);
		table.setOpaque(true);
		table.setBackground(bgColor);
		table.setForeground(Color.WHITE);
		table.setGridColor(gridColor);
		content.setLayout(new BorderLayout());

		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setDefaultRenderer(
				new ParameterTableHeaderRenderer());

		table.setFont(new Font("Monospaced", Font.PLAIN, 12));
		table.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

		table.setRowHeight(24);
		table.getTableHeader().setVisible(true);

		content.add(new JScrollPane(table), BorderLayout.CENTER);

		content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(content, BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);

		final Button cl = new Button("Close");
		cl.addClickListener(new ClickListener() {
			public void onClick() {
				setVisible(false);
			}
		});
		cl.setMaximumSize(new Dimension(120, 18));

		buttons.add(cl);

		this.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				ParameterDialog.this.setVisible(false);
			}
		});
	}

	public class ParamTable implements TableModel {

		/** The unique class ID */
		protected static final long serialVersionUID = 3667509468852384141L;
		final ArrayList<String> keys = new ArrayList<String>();
		final LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		final ArrayList<TableModelListener> listener = new ArrayList<TableModelListener>();

		/**
		 * @see javax.swing.table.DefaultTableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return params.size();
		}

		/**
		 * @see javax.swing.table.DefaultTableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		/**
		 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			if (column < 1) {
				return "Key";
			} else {
				return "Value";
			}
		}

		/**
		 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int row, int column) {
			return false; // column > 0; // super.isCellEditable(row, column);
		}

		/**
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int row, int column) {

			String key = keys.get(row);
			if (column < 1) {
				return key;
			}

			if (key != null) {
				return params.get(key);
			}

			return null;
		}

		public void set(String key, String value) {
			if (value == null) {
				params.remove(key);
				keys.remove(key);
			} else {
				params.put(key, value);
				if (!keys.contains(key)) {
					keys.add(key);
				}
			}
		}

		public void set(Map<String, String> map) {
			params.putAll(map);
			keys.clear();
			keys.addAll(params.keySet());
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		/**
		 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int,
		 *      int)
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		}

		/**
		 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
		 */
		@Override
		public void addTableModelListener(TableModelListener l) {
			listener.add(l);
		}

		/**
		 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
		 */
		@Override
		public void removeTableModelListener(TableModelListener l) {
			listener.remove(l);
		}
	}
}
