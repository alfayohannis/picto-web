/*********************************************************************
* Copyright (c) 2008 The University of York.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.picto;

import org.eclipse.epsilon.picto.dummy.ViewPart;
import org.eclipse.swt.widgets.Composite;

public class PictoView extends ViewPart {
	
	public static final String ID = "org.eclipse.epsilon.picto.PictoView";

	protected ViewRenderer viewRenderer;
	
	public ViewRenderer getViewRenderer() {
		return viewRenderer;
	}
	
	@Override
	public void createPartControl(Composite arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
}
