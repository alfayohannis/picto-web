package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/***
 * The main class for Picto Web Application
 * 
 * @author Alfa Yohannis
 *
 */
@SpringBootApplication
public class PictoApplication {

	/***
	 * Define the relative workspace target
	 */
	public static final String WORKSPACE = ".." + File.separator + "workspace" + File.separator;

	/***
	 * This contains the visualised Picto file's target
	 */
	public static final List<File> PICTO_FILES = new ArrayList<File>();
	
	/***
	 * To keep the arguments accessible.
	 */
	public static String[] args;

	/***
	 * Initialise Picto Application
	 * 
	 * @param args
	 * @throws Exception
	 */
	public PictoApplication() throws Exception {
		String workDir = System.getProperty("user.dir");
		System.out.println("PICTO - Default Picto Application directory: " + workDir);
		System.out.println("PICTO - Workspace directory: " + (new File(WORKSPACE)).getAbsolutePath());
	}

	public static void main(String[] args) {
		PictoApplication.args = args;

		// run the Spring application
		SpringApplication.run(PictoApplication.class, args);
	}

}
