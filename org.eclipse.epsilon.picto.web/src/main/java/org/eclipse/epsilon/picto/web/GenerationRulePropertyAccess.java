package org.eclipse.epsilon.picto.web;

import org.eclipse.epsilon.egl.dom.GenerationRule;
import org.eclipse.epsilon.eol.execute.introspection.recording.PropertyAccess;

public class GenerationRulePropertyAccess extends PropertyAccess {

	protected GenerationRule rule;
	protected Object contextElement;

	public GenerationRulePropertyAccess(Object modelElement, String propertyName, GenerationRule rule, Object contextElement) {
		super(modelElement, propertyName);
		this.rule = rule;
		this.contextElement = contextElement;
	}

	public GenerationRule getRule() {
		return rule;
	}

	public Object getContextElement() {
		return contextElement;
	}

}