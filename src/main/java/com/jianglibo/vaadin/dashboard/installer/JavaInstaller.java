package com.jianglibo.vaadin.dashboard.installer;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jianglibo.vaadin.dashboard.annotation.SoftwareInstaller;
import com.jianglibo.vaadin.dashboard.domain.Software;
import com.jianglibo.vaadin.dashboard.ssh.JschExecuteResult;

@SoftwareInstaller(name = "Java")
@Component
public class JavaInstaller implements Installer {

	@Override
	public List<JschExecuteResult> install(Software software) {
		return null;
	}
}