package org.eclipse.epsilon.picto.web;

public class PictoRequest {

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
}