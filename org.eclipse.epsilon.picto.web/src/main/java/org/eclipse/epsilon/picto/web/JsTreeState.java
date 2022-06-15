package org.eclipse.epsilon.picto.web;

public class JsTreeState {

	protected boolean selected = false;
	protected boolean opened = false;

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

}
