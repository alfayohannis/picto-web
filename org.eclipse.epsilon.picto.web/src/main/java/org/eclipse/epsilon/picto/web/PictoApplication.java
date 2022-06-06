package org.eclipse.epsilon.picto.web;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/***
 * The main class for Picto Web Application
 * @author Alfa Yohannis
 *
 */
@SpringBootApplication
public class PictoApplication {

	/***
	 * This contains the visualised Picto file's path  
	 */
	public static File PICTO_FILE = null;

	/***
	 * Initialise Picto Application
	 * @param args
	 */
	public static void main(String[] args) {
		String workDir = System.getProperty("user.dir");
		System.out.println("PICTO - Default working directory: " + workDir);
		System.out.println("PICTO - Picto file: " + args[0]);
		PICTO_FILE = new File(args[0]);
		if (PICTO_FILE.exists()) {
			PICTO_FILE = new File(PICTO_FILE.getAbsolutePath());
		}
		System.out.println("PICTO - Absoule Picto file: " + PICTO_FILE.getAbsolutePath());

		// run the Spring application
		SpringApplication.run(PictoApplication.class, args);
	}
}
