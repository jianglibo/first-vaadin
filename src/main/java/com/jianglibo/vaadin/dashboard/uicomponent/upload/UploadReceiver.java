package com.jianglibo.vaadin.dashboard.uicomponent.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.jianglibo.vaadin.dashboard.config.ApplicationConfigWrapper;
import com.jianglibo.vaadin.dashboard.domain.PkSource;
import com.jianglibo.vaadin.dashboard.event.view.UploadFinishEvent;
import com.jianglibo.vaadin.dashboard.repositories.PkSourceRepository;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload.Receiver;


@SuppressWarnings("serial")
@SpringComponent
@Scope("prototype")
public class UploadReceiver implements Receiver  {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(UploadReceiver.class);
	
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ApplicationConfigWrapper applicationConfigWrapper;
	
	
	@Autowired
	private PkSourceRepository pkSourceRepository;

	public File file;
	
	public String filename;
	
	private String mimeType;
	
	private EventBus eventBus;
	
	public UploadReceiver afterInjection(EventBus eventBus) {
		this.eventBus = eventBus;
		return this;
	}

	public OutputStream receiveUpload(String filename, String mimeType) {
		// Create upload stream
		
		this.filename = filename;
		this.mimeType = mimeType;
		FileOutputStream fos = null; // Stream to write to
		try {
			// Open the file for writing.
			String uuid = UUID.randomUUID().toString();
			file = applicationConfigWrapper.unwrap().getUploadDstPath().resolve(uuid).toFile();
			fos = new FileOutputStream(file);
		} catch (final java.io.FileNotFoundException e) {
			new Notification(messageSource.getMessage("component.upload.cantopenfile", new String[]{file.toString()}, UI.getCurrent().getLocale()), "", Notification.Type.ERROR_MESSAGE)
					.show(Page.getCurrent());
			return null;
		}
		return fos;
	}

	public void uploadSuccessed() {
		try {
			String md5 = Files.asByteSource(file).hash(Hashing.md5()).toString();
			PkSource ps = pkSourceRepository.findByFileMd5(md5);
			if (ps == null) {
				String extNoDot = Files.getFileExtension(filename);
				File nf = new File(file.getParentFile(), md5 + "." + extNoDot);
				if (!nf.exists()) {
					Files.move(file, nf);
				}
				ps = new PkSource.PkSourceBuilder(md5, filename, nf.length(), extNoDot, mimeType).build();
				pkSourceRepository.save(ps);
				eventBus.post(new UploadFinishEvent(ps));
			} else {
				eventBus.post(new UploadFinishEvent(ps));
				new Notification(messageSource.getMessage("component.upload.duplicated", new String[]{filename}, UI.getCurrent().getLocale()), "", Notification.Type.ERROR_MESSAGE)
				.show(Page.getCurrent());
			}
			if (file.exists()) {
				file.delete();
			}
		} catch (IOException e) {
			new Notification(messageSource.getMessage("component.upload.hashing", new String[]{filename}, UI.getCurrent().getLocale()), "", Notification.Type.ERROR_MESSAGE)
			.show(Page.getCurrent());
			LOGGER.error("hashing {} failed.", filename);
		}
	}
	
	public void uploadNotSuccess() {
		if (file != null) {
			file.delete();
		}
	}
}
