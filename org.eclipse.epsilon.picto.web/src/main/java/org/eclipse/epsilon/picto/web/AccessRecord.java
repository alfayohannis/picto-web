package org.eclipse.epsilon.picto.web;

public class AccessRecord {

	protected String fileName;
	protected String ruleName;
	protected String contextUri;
	protected String elementUri;
	protected String PropertyName;
	protected String path;

	public AccessRecord(String fileName, String ruleName, String contextUri, String elementUri, String propertyName,
			String path) {
		super();
		this.fileName = fileName;
		this.ruleName = ruleName;
		this.contextUri = contextUri;
		this.elementUri = elementUri;
		PropertyName = propertyName;
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getContextUri() {
		return contextUri;
	}

	public void setContextUri(String contextUri) {
		this.contextUri = contextUri;
	}

	public String getElementUri() {
		return elementUri;
	}

	public void setElementUri(String elementUri) {
		this.elementUri = elementUri;
	}

	public String getPropertyName() {
		return PropertyName;
	}

	public void setPropertyName(String propertyName) {
		PropertyName = propertyName;
	}

	@Override
	public String toString() {
		return fileName + "," + ruleName + "," + contextUri + "," + elementUri + "," + PropertyName + "," + path;
	}

}
