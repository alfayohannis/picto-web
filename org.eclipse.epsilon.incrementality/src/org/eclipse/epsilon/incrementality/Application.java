package org.eclipse.epsilon.incrementality;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.common.util.UriUtil;
import org.eclipse.epsilon.egl.dom.GenerationRule;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.execute.introspection.recording.IPropertyAccess;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;
import org.eclipse.epsilon.picto.LazyEgxModule.LazyGenerationRuleContentPromise;

public class Application {

	public static void main(String[] args) throws Exception {

		File metamodelFile = new File("models" + File.separator + "socialnetwork.ecore");
		File modelFile = new File("models" + File.separator + "socialnetwork.model");
		File egxFile = new File("models" + File.separator + "picto" + File.separator + "socialnetwork.egx");

		// register metamodel
		org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI
				.createFileURI(metamodelFile.getAbsolutePath());
		EmfUtil.register(uri, EPackage.Registry.INSTANCE);

		// load model
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("model", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new FlexmiResourceFactory());
		Resource resource = resourceSet
				.getResource(org.eclipse.emf.common.util.URI.createFileURI(modelFile.getAbsolutePath()), true);
		resource.load(null);

		IModel model = new InMemoryEmfModel("M", resource);
		((InMemoryEmfModel) model).setExpand(true);

		// load transformation
		IncrementalResource incrementalResource = new IncrementalTable();
		IEolModule module = new IncrementalLazyEgxModule(incrementalResource);
		IEolContext context = module.getContext();
		URI transformationUri = egxFile.toURI();
		module.parse(transformationUri);

		// add model to context
		context.getModelRepository().addModel(model);

		// start recording for property access
		((IncrementalLazyEgxModule) module).startRecording();

		// execute transformation
		@SuppressWarnings("unchecked")
		List<LazyGenerationRuleContentPromise> instances = (List<LazyGenerationRuleContentPromise>) module.execute();

		// stop recording for property access
		((IncrementalLazyEgxModule) module).stopRecording();

		for (IPropertyAccess propertyAccess : ((IncrementalLazyEgxModule) module).getPropertyAccessRecorder()
				.getPropertyAccesses().all()) {
			GenerationRulePropertyAccess generationRulePropertyAccess = (GenerationRulePropertyAccess) propertyAccess;
			String filename = ((IncrementalLazyEgxModule) module).getSourceFile().getName();
			String ruleName = generationRulePropertyAccess.getRule().getName();
			EObject contextElement = (EObject) generationRulePropertyAccess.getContextElement();
			String contextUri = contextElement.eResource().getURIFragment(contextElement);
			EObject modelElement = (EObject) generationRulePropertyAccess.getModelElement();
			String elementUri = modelElement.eResource().getURIFragment(modelElement);
			String propertyName = generationRulePropertyAccess.getPropertyName();
			Object value = modelElement.eGet(modelElement.eClass().getEStructuralFeature(propertyName));

			
			IncrementalRecord record = new IncrementalRecord(module, generationRulePropertyAccess.getRule(),
					contextElement, modelElement, propertyName, value, null);
			System.out.println(record.toString());
			incrementalResource.add(record);
		}

		for (LazyGenerationRuleContentPromise instance : instances) {
			for (Variable variable : instance.getVariables()) {
				if ("target".equals(variable.getName())) {
					System.out.println(variable.getName() + ": " + variable.getValue());
				}
			}
		}

		System.out.println("Finished!");
	}

}
