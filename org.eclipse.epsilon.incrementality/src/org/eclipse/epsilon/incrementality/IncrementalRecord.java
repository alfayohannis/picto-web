package org.eclipse.epsilon.incrementality;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.egl.dom.GenerationRule;
import org.eclipse.epsilon.eol.IEolModule;

public class IncrementalRecord {

	protected IEolModule module;
	protected GenerationRule generationRule;
	protected EObject contextObject;
	protected EObject elementObject;
	protected String propertyName;
	protected Object value;
	protected Object target;

	public IncrementalRecord(IEolModule module, GenerationRule generationRule, EObject contextObject,
			EObject elementObject, String propertyName, Object value, Object target) {
		this.module = module;
		this.generationRule = generationRule;
		this.contextObject = contextObject;
		this.elementObject = elementObject;
		this.propertyName = propertyName;
		this.value = value;
		this.target = target;
	}

	public IEolModule getModule() {
		return module;
	}

	public void setModule(IEolModule module) {
		this.module = module;
	}

	public GenerationRule getGenerationRule() {
		return generationRule;
	}

	public void setGenerationRule(GenerationRule generationRule) {
		this.generationRule = generationRule;
	}

	public EObject getContextObject() {
		return contextObject;
	}

	public void setContextObject(EObject contextObject) {
		this.contextObject = contextObject;
	}

	public EObject getElementObject() {
		return elementObject;
	}

	public void setElementObject(EObject elementObject) {
		this.elementObject = elementObject;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return module.getFile().getName() + "," + generationRule.getName() + "," //
				+ EcoreUtil.getID(contextObject) + "," + EcoreUtil.getID(elementObject) + "," //
				+ propertyName + "," + value + ", " + target;
	}

}
