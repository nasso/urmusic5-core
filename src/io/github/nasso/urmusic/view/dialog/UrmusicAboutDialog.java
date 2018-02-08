package io.github.nasso.urmusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import io.github.nasso.urmusic.common.DataUtils;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class UrmusicAboutDialog extends JDialog {
	private static final long serialVersionUID = -1450901720085825932L;

	public UrmusicAboutDialog() {
		JPanel container = new JPanel(new BorderLayout(4, 4));
		container.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		JPanel topPane = new JPanel(new BorderLayout());
		JLabel topLabel = new JLabel("<html><strong>urmusic 5.0 pre-alpha</strong> (JRE " + System.getProperty("java.version") + ")</html>");
		topLabel.setFont(topLabel.getFont().deriveFont(Font.PLAIN, 14));
		topPane.add(topLabel);
		container.add(topPane, BorderLayout.NORTH);
		
		JTextPane textContent = new JTextPane();
		textContent.setEditable(false);
		textContent.setContentType("text/html");
		textContent.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch(IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		try {
			textContent.setText("<pre>" + DataUtils.readFile("res/about.html", true) + "</pre>");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		container.add(new JScrollPane(textContent), BorderLayout.CENTER);
		
		JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(UrmusicStrings.getString("dialog.global.ok"));
		okButton.addActionListener((e) -> {
			this.setVisible(false);
		});
		bottomPane.add(okButton);
		container.add(bottomPane, BorderLayout.SOUTH);
		
		this.setTitle(UrmusicStrings.getString("dialog.about.title"));
		this.setContentPane(container);
		this.setSize(640, 480);
		this.setLocationRelativeTo(null);
	}
}
