package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.epsilon.picto.dom.PictoPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

/***
 * Receive Requests and send back Responses
 * 
 * @author Alfa Yohannis
 *
 */
@RestController
@RequestMapping("/pictojson")
public class PictoJsonController {

	@GetMapping(path = "/picto", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getPictoJson(String file, String uri, String name, Model model) throws Exception {
		String result = PictoElementsMap.getElementViewContentMap(file).getElementViewTree(uri);
		return result;
	}
}
