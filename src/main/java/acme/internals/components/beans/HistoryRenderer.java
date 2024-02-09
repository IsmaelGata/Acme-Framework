/*
 * HistoryRenderer.java
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

package acme.internals.components.beans;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;

import org.apache.commons.lang3.tuple.Triple;

public class HistoryRenderer extends JTextArea implements ListCellRenderer<Triple<String, String, Boolean>> {

	// Serialisation identifier -----------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// ListCellRenderer<String> interface -------------------------------------

	private static Color		PALE_GREY			= new Color(242, 240, 240);


	@Override
	public Component getListCellRendererComponent(final JList<? extends Triple<String, String, Boolean>> list, final Triple<String, String, Boolean> value, final int index, final boolean isSelected, final boolean cellHasFocus) {
		assert list != null;
		assert value != null;
		assert index >= 0;

		String text;

		text = value.getLeft().trim(); // .replace("[\r\n]+", "\u21b5").replace("\t", "\u2B7E");

		this.setText(text);
		this.setOpaque(true);

		if (isSelected) {
			this.setBackground(list.getSelectionBackground());
			this.setForeground(list.getSelectionForeground());
		} else if (index % 2 == 0)
			this.setBackground(HistoryRenderer.PALE_GREY);
		else
			this.setBackground(Color.WHITE);

		return this;
	}

}
