package org.eclipse.epsilon.picto.web;

public class PictoRequest {

	private String type;
	private String code;

	public PictoRequest() {
	}

	public PictoRequest(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}