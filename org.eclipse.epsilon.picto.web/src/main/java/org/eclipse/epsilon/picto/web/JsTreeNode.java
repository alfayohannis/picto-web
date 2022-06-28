/*********************************************************************
* Copyright (c) 2022 The University of York.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.picto.web;

import java.util.ArrayList;
import java.util.List;

public class JsTreeNode {

	protected List<JsTreeNode> children = new ArrayList<>();
	protected String text;
	protected String path;
	protected String icon = "jstree-folder";
	protected JsTreeState state = new JsTreeState();

	public List<JsTreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<JsTreeNode> children) {
		this.children = children;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public JsTreeState getState() {
		return state;
	}

	public void setState(JsTreeState state) {
		this.state = state;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
