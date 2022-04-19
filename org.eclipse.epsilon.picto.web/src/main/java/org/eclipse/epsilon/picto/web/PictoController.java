package org.eclipse.epsilon.picto.web;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

@Controller
public class PictoController {

//	@GetMapping("/greeting")
//	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
//		model.addAttribute("name", name);
//		return "greeting";
//	}

	@MessageMapping("/picto")
	@SendTo("/topic/picto")
	public PictoResult execute(PictoRequest message) throws Exception {
//		Thread.sleep(1000); // simulated delay
		WebFlexmiSource source = new WebFlexmiSource(null);
		String result = source.getResult(message.getCode());
//		String temp = HtmlUtils.htmlEscape(result);
		String temp = result;
		PictoResult pictoResult = new PictoResult(temp);
		return pictoResult;
	}

//	@GetMapping("/")
//	public String main(@RequestParam(name = "name", required = false, defaultValue = "World") String name,
//			Model model) {
//		model.addAttribute("name", name);
//		return "main";
//	}

}
