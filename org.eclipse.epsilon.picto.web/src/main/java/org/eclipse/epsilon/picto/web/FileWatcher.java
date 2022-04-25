package org.eclipse.epsilon.picto.web;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/***
 * Monitor file changes.
 * 
 * @author Alfa Yohannis
 *
 */

public class FileWatcher extends Thread {

	@Autowired
	public PictoController pictoController;
	/**
	 * 
	 */
	private boolean isRunning = false;

	public FileWatcher(PictoController pictoController) {
		this.pictoController = pictoController;
		this.setName(FileWatcher.class.getSimpleName());	
	}

	public void terminate() {
		isRunning = false;
	}

	@MessageMapping("/gs-guide-websocket")
	public void notifyFileChange(File modelFile) throws Exception {
		PictoResponse temp = this.pictoController.sendBackFileUpdate(modelFile);
//		template.convertAndSend("/topic/picto", temp);
		System.console();
	}

	@Override
	public void run() {
		try {
			WatchService watcher;

			watcher = FileSystems.getDefault().newWatchService();

			Path dir = Paths.get(PictoController.WORKSPACE);
			dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

			System.out.println("PICTO: Watch Service registered for dir: " + dir.getFileName());

			isRunning = true;

			while (isRunning) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException ex) {
					return;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filePath = ev.context();

					if (kind == ENTRY_MODIFY && filePath.toString().endsWith(".flexmi")) {
						System.out.println("Picto: " + filePath + " has changed!!!");

						File modelFile = new File(PictoController.WORKSPACE + filePath.toString());
						this.notifyFileChange(modelFile);
					}
				}

				boolean valid = key.reset();
				if (!valid) {
					System.out.println("Picto: FileWatcher is not valid anymore!");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}