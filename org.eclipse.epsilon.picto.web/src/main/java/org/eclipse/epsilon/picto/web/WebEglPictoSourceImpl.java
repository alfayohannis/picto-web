package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.common.dt.util.LogUtil;
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;
import org.eclipse.epsilon.picto.dom.Picto;
import org.eclipse.epsilon.picto.dummy.IEditorPart;

public class WebEglPictoSourceImpl extends WebEglPictoSource {

	public WebEglPictoSourceImpl(File modelFile) throws Exception {
		super(modelFile);
	}

	public Picto getRenderingMetadata(File file) {
		try {
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new FlexmiResourceFactory());
			Resource resource = resourceSet.getResource(URI.createFileURI(file.getAbsolutePath()), true);
			resource.load(null);
			return (Picto) resource.getContents().iterator().next();
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public Picto getRenderingMetadata(IEditorPart editorPart) {
		return this.getRenderingMetadata(PictoApplication.PICTO_FILE);

//		FlexmiResource resource = (FlexmiResource) getResource(editorPart);
//		if (resource == null)
//			return null;
//
//		return resource.getProcessingInstructions().stream().filter(p -> p.getTarget().startsWith("render-"))
//				.findFirst().map(renderProcessingInstruction -> {
//					Picto metadata = PictoFactory.eINSTANCE.createPicto();
//					metadata.setFormat(renderProcessingInstruction.getTarget().substring("render-".length()));
//					metadata.setTransformation(renderProcessingInstruction.getData().trim());
//					return metadata;
//				}).orElse(null);
	}

	public Resource getResource(File modelFile) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("model", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new FlexmiResourceFactory());
		Resource resource = resourceSet.getResource(URI.createFileURI(modelFile.getAbsolutePath()), true);
//		Resource resource = resourceSet
//				.createResource(org.eclipse.emf.common.util.URI.createFileURI(modelFile.getAbsolutePath()));
		try {
			resource.load(null);
			return resource;
		} catch (IOException e) {
			LogUtil.log(e);
		}
		return null;
	}

	@Override
	public Resource getResource(IEditorPart editorPart) {
		return getResource(this.modelFile);
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
