package org.eclipse.epsilon.picto.web;

import java.io.File;



public class Console {

	public static void main(String[] args) throws Exception {
		
		File modelFile = new File("socialnetwork.flexmi");
		
		WebFlexmiSource source = new WebFlexmiSource(modelFile);
		
		System.out.println("Finished!");
	}

}
