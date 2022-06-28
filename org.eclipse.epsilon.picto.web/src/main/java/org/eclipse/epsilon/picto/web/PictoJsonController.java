package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.picto.dom.PictoPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/***
 * Receive Requests and send back Responses
 * 
 * @author Alfa Yohannis
 *
 */
@RestController
@RequestMapping("/pictojson")
public class PictoJsonController {

	@Autowired
	private ApplicationContext context;

	@Autowired
	public SimpMessagingTemplate template;

	public final FileWatcher FILE_WATCHER = new FileWatcher(this);

	public PictoJsonController() {
		PictoPackage.eINSTANCE.eClass();
		FILE_WATCHER.start();
	}

	@GetMapping(path = "/picto", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getPictoJson(String file, String path, String name, Model model) throws Exception {
		ViewContentCache x = FileViewContentCache.getViewContentCache(file);
		String result = x.getViewContentCache(path);
		return result;
	}

	@MessageMapping("/picto-web")
	@SendTo("/topic/picto")
	public void sendBackFileUpdate(File modifiedFile) throws Exception {
		WebEglPictoSource source = new WebEglPictoSource();
		Map<String, String> modifiedObjects = source.transform(modifiedFile);
		System.out.println("PICTO: number of modified objects = " + modifiedObjects.size());
		
		File pictoFile = modifiedFile;
		if (modifiedFile.getAbsolutePath().endsWith(".model")
				|| modifiedFile.getAbsolutePath().endsWith(".flexmi")
				|| modifiedFile.getAbsolutePath().endsWith(".xmi")) {
			pictoFile = new File(modifiedFile.getAbsolutePath() + ".picto");
		}
		
		MessageChannel brokerChannel = context.getBean("brokerChannel", MessageChannel.class);
		for (Entry<String, String> entry : modifiedObjects.entrySet()) {
			SimpMessagingTemplate messaging = new SimpMessagingTemplate(brokerChannel);
//		messaging.setMessageConverter(new MappingJackson2MessageConverter());
			messaging.convertAndSend("/topic/picto/" + pictoFile.getName(), entry.getValue().getBytes());
		}

//		return modifiedObjects;
	}

}
