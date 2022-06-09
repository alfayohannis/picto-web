package org.eclipse.epsilon.picto.web;

public class PictoRequest {

	private String pictoFile;
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

	/**
	 * @return the pictoFile
	 */
	public String getPictoFile() {
		return pictoFile;
	}

	/**
	 * @param pictoFile the pictoFile to set
	 */
	public void setPictoFile(String pictoFile) {
		this.pictoFile = pictoFile;
	}

}