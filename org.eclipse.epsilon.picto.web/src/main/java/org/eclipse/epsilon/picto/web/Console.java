package org.eclipse.epsilon.picto.web;

import java.io.File;



public class Console {

	public static void main(String[] args) throws Exception {
		
		File modelFile = new File("socialnetwork.flexmi");
		
		WebEglPictoSourceImpl source = new WebEglPictoSourceImpl(modelFile);
		
		System.out.println("Finished!");
	}

}
