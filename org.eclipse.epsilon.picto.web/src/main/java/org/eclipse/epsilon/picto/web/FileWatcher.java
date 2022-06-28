package org.eclipse.epsilon.picto.web;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;

/***
 * Monitor file changes.
 * 
 * @author Alfa Yohannis
 *
 */

public class FileWatcher extends Thread {

	@Autowired
	public PictoJsonController pictoJsonController;
	/**
	 * 
	 */
	private boolean isRunning = false;

	public FileWatcher(PictoJsonController pictoController) {
		this.pictoJsonController = pictoController;
		this.setName(FileWatcher.class.getSimpleName());
	}

	public void terminate() {
		isRunning = false;
	}

	@MessageMapping("/gs-guide-websocket")
	public void notifyFileChange(File modelFile) throws Exception {
		this.pictoJsonController.sendBackFileUpdate(modelFile);
//		template.convertAndSend("/topic/picto", temp);
		System.console();
	}

	@Override
	public void run() {
		try {
			WatchService watcher;

			watcher = FileSystems.getDefault().newWatchService();

			registerDirectory(watcher, PictoApplication.WORKSPACE);

			isRunning = true;

//			this.notifyFileChange(PictoApplication.PICTO_FILES);

			while (isRunning) {

				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException ex) {
					return;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
//					WatchEvent.Kind<?> kind = event.kind();

					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filePath = ev.context();

					if (filePath.toString().endsWith(".picto") || filePath.toString().endsWith(".egx")
							|| filePath.toString().endsWith(".egl") || filePath.toString().endsWith(".flexmi")
							|| filePath.toString().endsWith(".model") || filePath.toString().endsWith(".xmi")) {
						System.out.println("Picto: " + filePath + " has changed!!!");

						File modelFile = new File(PictoApplication.WORKSPACE + filePath.toString());
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

	private void registerDirectory(WatchService watcher, String directory) throws IOException {
		Path dir = Paths.get(directory).toAbsolutePath();
		dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		System.out.println("PICTO: Watch Service registered for dir: " + dir);
		
		File file = new File(directory);
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				registerDirectory(watcher, f.getAbsolutePath());
			}
		}
	}
}