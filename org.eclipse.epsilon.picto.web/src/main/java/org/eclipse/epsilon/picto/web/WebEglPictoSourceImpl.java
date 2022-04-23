package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.epsilon.common.dt.launching.extensions.ModelTypeExtension;
import org.eclipse.epsilon.common.dt.util.LogUtil;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.flexmi.FlexmiResource;
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;
import org.eclipse.epsilon.picto.dom.Model;
import org.eclipse.epsilon.picto.dom.Picto;
import org.eclipse.epsilon.picto.dom.PictoFactory;
import org.eclipse.ui.IEditorPart;

public class WebEglPictoSourceImpl extends WebEglPictoSource {

	public WebEglPictoSourceImpl(File modelFile) throws Exception {
		super(modelFile);
	}

	@Override
	public Picto getRenderingMetadata(IEditorPart editorPart) {
		FlexmiResource resource = (FlexmiResource) getResource(editorPart);
		if (resource == null)
			return null;

		return resource.getProcessingInstructions().stream().filter(p -> p.getTarget().startsWith("render-"))
				.findFirst().map(renderProcessingInstruction -> {
					Picto metadata = PictoFactory.eINSTANCE.createPicto();
					metadata.setFormat(renderProcessingInstruction.getTarget().substring("render-".length()));
					metadata.setTransformation(renderProcessingInstruction.getData().trim());
					return metadata;
				}).orElse(null);
	}

	@Override
	public Resource getResource(IEditorPart editorPart) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new FlexmiResourceFactory());
		Resource resource = resourceSet
				.createResource(org.eclipse.emf.common.util.URI.createFileURI(modelFile.getName()));
		try {
			resource.load(null);
			return resource;
		} catch (IOException e) {
			LogUtil.log(e);
		}
		return null;
	}

	

	@Override
	public void showElement(String id, String uri, IEditorPart editor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected IFile getFile(IEditorPart editorPart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean supportsEditorType(IEditorPart editorPart) {
		// TODO Auto-generated method stub
		return false;
	}

}