/*
 * DatabaseInquirer.java
 *
 * Copyright (C) 2024 Manuel J. Jiménez (Original author).
 * Copyright (C) 2024 Rafael Corchuelo (Refactoring).
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.internals.components.database;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import acme.client.helpers.PrinterHelper;
import acme.client.helpers.StringHelper;
import acme.internals.components.beans.HistoryRenderer;
import acme.internals.components.beans.SwingLauncher;
import acme.internals.helpers.PasswordHelper;
import lombok.CustomLog;

@Component
@CustomLog
public class DatabaseInquirer {

	// Business methods -------------------------------------------------------

	public void run() {
		SwingLauncher launcher;

		launcher = new SwingLauncher(this.components.appFrame);
		launcher.run();
	}

	// Inner classes ----------------------------------------------------------


	protected class Model {

		private DefaultListModel<Triple<String, String, Boolean>> history; // HINT: index -> (command, output, error)		
	}

	protected class Components {

		private JFrame									appFrame;
		private UndoManager								undoManager;
		private JList<Triple<String, String, Boolean>>	historyList;
		private JButton									copyButton;
		private JTextArea								inputArea;
		private JTextArea								outputArea;
		private JCheckBox								summaryToggle;
		private JCheckBox								wrapToggle;
		private JButton									helpButton;
		private JButton									submitButton;
	}

	protected class Layout {

		private JPanel		historyPanel;
		private JPanel		outputPanel;
		private JPanel		optionsPanel;
		private JSplitPane	topPanel;
		private JPanel		bottomPanel;
		private JSplitPane	mainPanel;
	}

	// Internal state ---------------------------------------------------------


	@Autowired
	private DatabaseManager	manager;

	private Model			model;
	private Components		components;
	@SuppressWarnings("unused")
	private Layout			layout;  // HINT: the layout is required, but not actually used in this utility. 

	// Constructors -----------------------------------------------------------


	protected DatabaseInquirer() {
		this.model = this.buildmodel();
		this.components = this.buildComponents(this.model);
		this.layout = this.buildLayout(this.components);
	}

	// Ancillary methods ------------------------------------------------------

	private Model buildmodel() {
		Model result;

		result = new Model();
		result.history = new DefaultListModel<Triple<String, String, Boolean>>();

		return result;
	}

	// UI ancillary builders --------------------------------------------------

	private Components buildComponents(final Model model) {
		assert model != null;

		Components result;

		result = this.createComponents(model);
		this.attachListeners(model, result);
		this.attachActions(model, result);

		return result;
	}

	protected Components createComponents(final Model model) {
		assert model != null;

		Components result;

		result = new Components();

		{
			Toolkit toolkit;
			Dimension screenSize, minimumSize;
			int x, y;

			minimumSize = new Dimension(400, 300);

			result.appFrame = new JFrame("Inquirer");
			result.appFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			result.appFrame.setSize(800, 600);
			result.appFrame.setMinimumSize(minimumSize);

			toolkit = Toolkit.getDefaultToolkit();
			screenSize = toolkit.getScreenSize();
			x = (screenSize.width - result.appFrame.getWidth()) / 2;
			y = (screenSize.height - result.appFrame.getHeight()) / 2;

			result.appFrame.setLocation(x, y);
		}

		{
			result.undoManager = new UndoManager();
		}

		{
			result.historyList = new JList<Triple<String, String, Boolean>>(model.history);
			result.historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			result.historyList.setCellRenderer(new HistoryRenderer());
		}

		{
			result.inputArea = new JTextArea();
			result.inputArea.setLineWrap(true);
			result.inputArea.setWrapStyleWord(true);
			result.inputArea.setDragEnabled(true);
			result.inputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			result.inputArea.setMargin(new Insets(5, 5, 5, 5));
			result.inputArea.getDocument().addUndoableEditListener(result.undoManager);
		}

		{
			result.outputArea = new JTextArea();
			result.outputArea.setEditable(false);
			result.outputArea.setWrapStyleWord(false);
			result.outputArea.setLineWrap(false);
		}

		{
			result.summaryToggle = new JCheckBox();
			result.summaryToggle.setText("Summary");
			result.summaryToggle.setSelected(false);
		}

		{
			result.wrapToggle = new JCheckBox();
			result.wrapToggle.setText("Wrap output");
			result.wrapToggle.setSelected(false);
		}

		{
			result.copyButton = new JButton();
			result.copyButton.setText("Copy");
		}

		{
			result.helpButton = new JButton();
			result.helpButton.setText("Help");
		}

		{
			result.submitButton = new JButton();
			result.submitButton.setText("Submit");
		}

		return result;
	}

	protected void attachListeners(final Model model, final Components components) {
		assert model != null;
		assert components != null;

		{
			components.historyList.addListSelectionListener(event -> {
				assert event != null;

				Triple<String, String, Boolean> selectedValue;

				if (!event.getValueIsAdjusting()) {
					selectedValue = components.historyList.getSelectedValue();
					if (selectedValue != null)
						this.updateOutputPanel(selectedValue);
				}

			});

			components.historyList.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(final MouseEvent event) {
					assert event != null;

					String command;
					int confirmation;
					Triple<String, String, Boolean> selectedValue;

					if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2 && components.historyList.getSelectedIndex() != -1) {
						command = components.inputArea.getText();
						if (StringHelper.isBlank(command))
							confirmation = JOptionPane.YES_OPTION;
						else
							confirmation = JOptionPane.showConfirmDialog(null, "Discard current command?");
						if (confirmation == JOptionPane.YES_OPTION) {
							selectedValue = components.historyList.getSelectedValue();
							assert selectedValue != null;
							DatabaseInquirer.this.updateOutputPanel(selectedValue);
							components.inputArea.setText(selectedValue.getLeft());
							components.inputArea.setCaretPosition(0);
						}
					}
				}
			});

			components.historyList.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(final KeyEvent event) {
					assert event != null;

					int selectedIndex;
					int confirmation;
					Triple<String, String, Boolean> selectedValue;

					selectedIndex = components.historyList.getSelectedIndex();
					if (selectedIndex != -1 && event.getKeyChar() == KeyEvent.VK_DELETE) {
						confirmation = JOptionPane.showConfirmDialog(null, "Delete this entry?");
						if (confirmation == JOptionPane.YES_OPTION) {
							model.history.removeElementAt(selectedIndex);
							if (selectedIndex >= model.history.size())
								DatabaseInquirer.this.updateOutputPanel(null);
							else {
								components.historyList.setSelectedIndex(selectedIndex);
								components.historyList.ensureIndexIsVisible(selectedIndex);
								selectedValue = components.historyList.getSelectedValue();
								DatabaseInquirer.this.updateOutputPanel(selectedValue);
							}
						}
					}
				}

				@Override
				public void keyPressed(final KeyEvent event) {
					assert event != null;
				}

				@Override
				public void keyReleased(final KeyEvent event) {
					assert event != null;
				}
			});
		}

		{
			components.copyButton.addActionListener(event -> {
				assert event != null;

				Clipboard clipboard;
				StringBuilder contents;
				StringSelection selection;

				clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				contents = new StringBuilder();

				for (int index = 0; index <= components.historyList.getLastVisibleIndex(); index++) {
					Triple<String, String, Boolean> entry;

					entry = components.historyList.getModel().getElementAt(index);
					contents.append(entry.getLeft());
					contents.append(";");
					contents.append(System.lineSeparator());
					contents.append(System.lineSeparator());
				}

				selection = new StringSelection(contents.toString());
				clipboard.setContents(selection, null);
			});
		}

		{
			components.summaryToggle.addChangeListener(event -> components.inputArea.requestFocus());
		}

		{
			components.wrapToggle.addChangeListener(event -> {
				assert event != null;

				components.inputArea.requestFocus();
				components.outputArea.setLineWrap(components.wrapToggle.isSelected());
				components.outputArea.setWrapStyleWord(components.wrapToggle.isSelected());
			});
		}

		{
			components.helpButton.addActionListener(event -> {
				assert event != null;

				String message;

				message = this.interpretCommand("help").getMiddle();
				JOptionPane.showMessageDialog(components.appFrame, message, "Help", JOptionPane.INFORMATION_MESSAGE);
				components.inputArea.requestFocus();
			});
		}

		{
			components.submitButton.addActionListener(event -> {
				assert event != null;

				this.handleSubmit();
				components.inputArea.requestFocus();
			});
		}
	}

	protected void attachActions(final Model model, final Components components) {
		assert model != null;
		assert components != null;

		InputMap inputMap;
		ActionMap actionMap;

		inputMap = components.inputArea.getInputMap();
		actionMap = components.inputArea.getActionMap();

		{
			Action submitAction;

			submitAction = new AbstractAction() {

				private static final long serialVersionUID = 1L;


				@Override
				public void actionPerformed(final ActionEvent event) {
					assert event != null;

					DatabaseInquirer.this.handleSubmit();
				}
			};

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "submit");
			actionMap.put("submit", submitAction);
		}

		{
			Action undoAction;

			undoAction = new AbstractAction() {

				private static final long serialVersionUID = 1L;


				@Override
				public void actionPerformed(final ActionEvent event) {
					assert event != null;

					try {
						DatabaseInquirer.this.components.undoManager.undo();
					} catch (CannotUndoException oops) {
						;
					}
				}
			};
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "undo");
			actionMap.put("undo", undoAction);
		}

		{
			Action redoAction;

			redoAction = new AbstractAction() {

				private static final long serialVersionUID = 1L;


				@Override
				public void actionPerformed(final ActionEvent event) {
					assert event != null;

					try {
						DatabaseInquirer.this.components.undoManager.redo();
					} catch (CannotRedoException oops) {
						;
					}
				}

			};

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "redo");
			actionMap.put("redo", redoAction);
		}

	}

	protected Layout buildLayout(final Components components) {
		assert components != null;

		Layout result;

		result = new Layout();

		{
			JScrollPane historyScroller;
			historyScroller = new JScrollPane(components.historyList);

			result.historyPanel = new JPanel(new GridBagLayout());
			result.historyPanel.add(historyScroller, this.createConstraints(1.00, 0.95, 0, 0));
			result.historyPanel.add(components.copyButton, this.createConstraints(1.00, 0.05, 0, 1));
		}

		{
			JScrollPane outputScroller;

			outputScroller = new JScrollPane(components.outputArea);

			result.outputPanel = new JPanel(new BorderLayout());
			result.outputPanel.add(outputScroller, BorderLayout.CENTER);
		}

		{
			result.topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, result.historyPanel, result.outputPanel);
			result.topPanel.setResizeWeight(0.10);
		}

		{
			result.optionsPanel = new JPanel(new GridBagLayout());
			result.optionsPanel.add(components.summaryToggle, this.createConstraints(1.00, 0.25, 0, 0));
			result.optionsPanel.add(components.wrapToggle, this.createConstraints(1.00, 0.25, 0, 1));
			result.optionsPanel.add(components.helpButton, this.createConstraints(1.00, 0.25, 0, 2));
			result.optionsPanel.add(components.submitButton, this.createConstraints(1.00, 0.25, 0, 3));
		}

		{
			JScrollPane inputScroller;

			inputScroller = new JScrollPane(components.inputArea);

			result.bottomPanel = new JPanel(new GridBagLayout());
			result.bottomPanel.add(inputScroller, this.createConstraints(0.90, 1.00, 0, 0));
			result.bottomPanel.add(result.optionsPanel, this.createConstraints(0.10, 1.00, 1, 0));
		}

		{
			result.mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, result.topPanel, result.bottomPanel);
			result.mainPanel.setResizeWeight(0.80);
		}

		components.appFrame.getContentPane().add(result.mainPanel);

		return result;
	}

	// Ancillary methods ------------------------------------------------------

	protected GridBagConstraints createConstraints(final double wx, final double wy, final int gx, final int gy) {
		assert wx >= 0.00 && wx <= 1.00;
		assert wy >= 0.00 && wy <= 1.00;
		assert gx >= 0.00;
		assert gy >= 0.00;

		GridBagConstraints result;

		result = new GridBagConstraints();

		result.fill = GridBagConstraints.BOTH;
		result.weightx = wx;
		result.weighty = wy;
		result.gridx = gx;
		result.gridy = gy;

		return result;
	}


	private static String RULE = StringHelper.makeString("=", 80);


	protected void updateOutputPanel(final Triple<String, String, Boolean> entry) {
		// HINT: entry can be null

		StringBuilder buffer;

		if (entry == null)
			this.components.outputArea.setText("");
		else {
			buffer = new StringBuilder();
			buffer.append(DatabaseInquirer.RULE);
			buffer.append(System.lineSeparator());
			buffer.append(entry.getLeft());
			buffer.append(System.lineSeparator());
			buffer.append(DatabaseInquirer.RULE);
			buffer.append(System.lineSeparator());
			buffer.append(System.lineSeparator());
			buffer.append(entry.getMiddle());

			this.components.outputArea.setForeground(entry.getRight() ? Color.RED : Color.BLACK);
			this.components.outputArea.setText(buffer.toString());
			this.components.outputArea.setCaretPosition(0);
		}
	}

	protected void handleSubmit() {
		String text;
		String[] statements;

		text = this.components.inputArea.getText();
		statements = text.split(";");
		for (String statement : statements) {
			String command;
			Triple<String, String, Boolean> output;
			int lastIndex;

			command = statement.trim();
			if (!StringHelper.isBlank(command)) {
				output = this.interpretCommand(command);
				this.model.history.addElement(output);
				lastIndex = this.model.history.size() - 1;
				this.components.historyList.setSelectedIndex(lastIndex);
				this.components.historyList.ensureIndexIsVisible(lastIndex);
			}
		}
	}

	protected Triple<String, String, Boolean> interpretCommand(final String command) {
		assert !StringHelper.isBlank(command);

		Triple<String, String, Boolean> result;
		String verb;
		String output;
		boolean error;

		try {
			verb = StringUtils.substringBefore(command, " ");
			switch (verb) {
			case "help": {
				StringBuilder buffer;

				buffer = new StringBuilder();
				buffer.append("* Non-transactional commands\n");
				buffer.append(System.lineSeparator());
				buffer.append("help: shows this help message.\n");
				buffer.append("hash <string>: computes a database hash for <string>.\n");
				buffer.append(System.lineSeparator());
				buffer.append("* Transactional commands\n");
				buffer.append(System.lineSeparator());
				buffer.append("select <query>: executes <query> as a select statement.\n");
				buffer.append("update <query>: executes <query> as an update statement.\n");
				buffer.append("delete <query>: executes <query> as a delete statement.\n");

				output = buffer.toString();
				error = false;

				break;
			}
			case "hash": {
				String text, encoding;

				text = StringUtils.substringAfter(command, " ");
				if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\""))
					text = text.substring(1, text.length() - 1);
				encoding = PasswordHelper.encode(text);
				output = String.format("hash \"%s\" = %s%n", text, encoding);
				error = false;
				break;
			}
			case "select": {
				long startTime, endTime;
				Collection<Object> objects;
				StringBuilder buffer;

				startTime = System.currentTimeMillis();
				this.manager.startTransaction();
				objects = this.manager.executeSelect(command);
				this.manager.commitTransaction();
				endTime = System.currentTimeMillis();

				buffer = new StringBuilder();
				buffer.append(String.format("%d object%s were retrieved in %d ms%n%n", objects.size(), objects.size() == 1 ? "" : "s", endTime - startTime));
				for (final Object object : objects) {
					PrinterHelper.printObject(buffer, object, this.components.summaryToggle.isSelected());
					buffer.append(System.lineSeparator());
				}
				output = buffer.toString().replace("\t", "    ");
				error = false;

				break;
			}
			case "update", "delete": {
				long startTime, endTime;
				int affected;

				startTime = System.currentTimeMillis();
				this.manager.startTransaction();
				affected = this.manager.executeUpdate(command);
				this.manager.commitTransaction();
				endTime = System.currentTimeMillis();
				output = String.format("%d object%s affected in %d ms%n%n%n", affected, affected == 1 ? "" : "s", endTime - startTime);
				error = false;

				break;
			}
			default:
				output = String.format("Could not understand command \"%s\"%n", command);
				error = true;
			}
		} catch (final Throwable oops) {
			output = oops.getMessage();
			error = true;
			DatabaseInquirer.logger.error(output);
			if (this.manager.isTransactionActive())
				this.manager.rollbackTransaction();
		}

		result = Triple.of(command, output, error);

		return result;
	}

}
