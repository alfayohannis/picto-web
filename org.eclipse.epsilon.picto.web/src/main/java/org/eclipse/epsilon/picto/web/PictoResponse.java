package org.eclipse.epsilon.picto.web;

public class PictoResponse {

	private String type;
	private String content;

	public PictoResponse() {
	}

	public PictoResponse(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}