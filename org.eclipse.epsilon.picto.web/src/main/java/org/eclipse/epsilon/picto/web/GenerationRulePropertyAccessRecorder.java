package org.eclipse.epsilon.picto.web;

import org.eclipse.epsilon.egl.dom.GenerationRule;
import org.eclipse.epsilon.eol.execute.introspection.recording.PropertyAccess;
import org.eclipse.epsilon.eol.execute.introspection.recording.PropertyAccessRecorder;

public class GenerationRulePropertyAccessRecorder extends PropertyAccessRecorder {

	protected GenerationRule rule = null;
	protected Object contextElement = null;
	
	public GenerationRulePropertyAccessRecorder() {
		super();
	}
	
	public Object getContextElement() {
		return contextElement;
	}
	
	public void setContextElement(Object contextElement) {
		this.contextElement = contextElement;
	}
	
	public void setRule(GenerationRule rule) {
		this.rule = rule;
	}
	
	public GenerationRule getRule() {
		return rule;
	}
	
	@Override
	protected PropertyAccess createPropertyAccess(Object modelElement, String propertyName) {
		return new GenerationRulePropertyAccess(modelElement, propertyName, rule, contextElement);
	}

}