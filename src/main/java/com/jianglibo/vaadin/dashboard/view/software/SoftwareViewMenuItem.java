package com.jianglibo.vaadin.dashboard.view.software;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.jianglibo.vaadin.dashboard.annotation.MainMenu;
import com.jianglibo.vaadin.dashboard.event.ui.DashboardEventBus;
import com.jianglibo.vaadin.dashboard.view.DboardViewUtil;
import com.jianglibo.vaadin.dashboard.view.ValoMenuItemButton;
import com.jianglibo.vaadin.dashboard.view.menuatleft.MenuItemWrapper;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

@MainMenu(menuOrder = 500)
public class SoftwareViewMenuItem implements MenuItemWrapper {

	private Component menuItem;

	private Label notificationsBadge;

	private int count1;

	private int count2;

	@Autowired
	public SoftwareViewMenuItem(MessageSource messageSource) {
		this.notificationsBadge = new Label();
		this.menuItem = DboardViewUtil.buildBadgeWrapper(
				new ValoMenuItemButton(SoftwareListView.VIEW_NAME, SoftwareListView.ICON_VALUE, messageSource),
				notificationsBadge);
		DashboardEventBus.register(this);
	}

	public Component getMenuItem() {
		return menuItem;
	}

	public void updateNotificationsCount(int newCount) {
		if (newCount == 0) {
			count1 = count2;
		} else {
			count2 = newCount;
		}
		notificationsBadge.setValue(String.valueOf(count2 - count1));
		notificationsBadge.setVisible((count2 - count1) > 0);
	}

	@Override
	public void onAttach() {
		updateNotificationsCount(0);
	}
}
