package io.github.nasso.urmusic.view.data;

import javax.swing.ImageIcon;

import io.github.nasso.urmusic.Urmusic;

public class UrmusicIcons {
	public static final ImageIcon VERTICAL_SPLIT_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-split-vertical-16.png"));
	public static final ImageIcon HORIZONTAL_SPLIT_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-divider-16.png"));
	public static final ImageIcon DELETE_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-delete-16.png"));
	public static final ImageIcon DELETE_ICON_S = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-delete-8.png"));
	public static final ImageIcon POPUP_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-open-in-popup-16.png"));

	public static final ImageIcon UP_ARROW_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-up-16.png"));
	public static final ImageIcon DOWN_ARROW_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-down-arrow-16.png"));
	
	public static final void init() { }
	
	private UrmusicIcons() { }
}
