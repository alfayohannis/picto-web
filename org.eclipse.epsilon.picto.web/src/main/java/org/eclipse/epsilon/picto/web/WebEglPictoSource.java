package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epsilon.common.dt.launching.extensions.ModelTypeExtension;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.common.util.UriUtil;
import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EglTemplateFactoryModuleAdapter;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dt.ExtensionPointToolNativeTypeDelegate;
import org.eclipse.epsilon.eol.execute.context.FrameStack;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.eol.types.EolAnyType;
import org.eclipse.epsilon.picto.Layer;
import org.eclipse.epsilon.picto.LazyEgxModule;
import org.eclipse.epsilon.picto.LazyEgxModule.LazyGenerationRule;
import org.eclipse.epsilon.picto.LazyEgxModule.LazyGenerationRuleContentPromise;
import org.eclipse.epsilon.picto.ResourceLoadingException;
import org.eclipse.epsilon.picto.StaticContentPromise;
import org.eclipse.epsilon.picto.ViewContent;
import org.eclipse.epsilon.picto.ViewTree;
import org.eclipse.epsilon.picto.dom.CustomView;
import org.eclipse.epsilon.picto.dom.Model;
import org.eclipse.epsilon.picto.dom.Parameter;
import org.eclipse.epsilon.picto.dom.Patch;
import org.eclipse.epsilon.picto.dom.Picto;
import org.eclipse.epsilon.picto.dom.PictoPackage;
import org.eclipse.epsilon.picto.source.EglPictoSource;
import org.eclipse.epsilon.picto.transformers.GraphvizContentTransformer;

import com.google.common.io.Files;

public abstract class WebEglPictoSource extends EglPictoSource {

	protected File modelFile;

	public WebEglPictoSource(File modelFile) throws Exception {

//		modelFile = new File(modelFile.getAbsolutePath());
		this.modelFile = modelFile;
	}
	
	public String getViewTree(String code) throws Exception {
		int x = code.indexOf("\n");
		String nsuri = code.substring(0, x);
		nsuri = nsuri.replace("<?", "");
		nsuri = nsuri.replace("?>", "");
		nsuri = nsuri.replace("nsuri", "").trim() + ".flexmi";

		
		if (modelFile == null) {
			modelFile = new File(PictoController.WORKSPACE + nsuri);
			Files.write(code.getBytes(), modelFile);
		}
		return this.getViewTree(modelFile);
	}
	
	public String getViewTree(File modelFile) throws Exception {
		this.modelFile = modelFile;
		Resource resource = null;
		ViewTree viewTree = new ViewTree();
		String metamodelFile = null;
		try {
			
			metamodelFile = modelFile.getAbsolutePath();
			String extension = metamodelFile.substring(metamodelFile.lastIndexOf("."), metamodelFile.length());
			metamodelFile = metamodelFile.replace(extension, ".ecore");
			List<EPackage> ePackages = EmfUtil.register(org.eclipse.emf.common.util.URI.createFileURI(metamodelFile),
					EPackage.Registry.INSTANCE);
			resource = getResource(null);
			System.console();
		} catch (Exception ex) {
			throw new ResourceLoadingException(ex);
		}

		Picto renderingMetadata = getRenderingMetadata(null);

		if (renderingMetadata != null) {
			IEolModule module;
			IModel model = null;

//			 if (renderingMetadata. getNsuri() != null) {
//			 EPackage ePackage =
//			 EPackage.Registry.INSTANCE.getEPackage(renderingMetadata.getNsuri());
//			 model = new InMemoryEmfModel("M", resource, ePackage);
//			 }
			// else {
			if (resource != null) {
				// synchronization prevents races when using multiple Picto views
				synchronized (resource) {
					model = new InMemoryEmfModel("M", resource);
					((InMemoryEmfModel) model).setExpand(true);
				}
			}
			// }

			if (renderingMetadata.getFormat() == null)
				renderingMetadata.setFormat("egx");

			if ("egx".equals(renderingMetadata.getFormat())) {
				module = new LazyEgxModule();
			} else {
				module = new EglTemplateFactoryModuleAdapter(new EglFileGeneratingTemplateFactory());
			}

			IEolContext context = module.getContext();
			context.getNativeTypeDelegates().add(new ExtensionPointToolNativeTypeDelegate());

			FrameStack fs = context.getFrameStack();
			for (Parameter customParameter : renderingMetadata.getParameters()) {
				fs.put(new Variable(customParameter.getName(), getValue(customParameter), EolAnyType.Instance));
			}

			URI transformationUri = null;

			if (renderingMetadata.getTransformation() != null) {
//				module.parse(code);
				transformationUri = UriUtil.resolve(renderingMetadata.getTransformation(), modelFile.toURI());
				module.parse(transformationUri);
			} else {
				module.parse("");
			}

//			context.setOutputStream(EpsilonConsole.getInstance().getDebugStream());
//			context.setErrorStream(EpsilonConsole.getInstance().getErrorStream());
//			context.setWarningStream(EpsilonConsole.getInstance().getWarningStream());

			if (model != null)
				context.getModelRepository().addModel(model);

			for (Model pictoModel : renderingMetadata.getModels()) {
//				model = loadModel(pictoModel, code);
				model = loadModel(pictoModel, modelFile);
				if (model != null)
					models.add(model);
			}

			context.getModelRepository().addModels(models);

			if ("egx".equals(renderingMetadata.getFormat())) {

				@SuppressWarnings("unchecked")
				List<LazyGenerationRuleContentPromise> instances = (List<LazyGenerationRuleContentPromise>) module
						.execute();

				// Handle dynamic views (i.e. where type != null)
				for (CustomView customView : renderingMetadata.getCustomViews().stream()
						.filter(cv -> cv.getType() != null).collect(Collectors.toList())) {

					LazyGenerationRule generationRule = ((LazyEgxModule) module).getGenerationRules().stream()
							.filter(r -> r.getName().equals(customView.getType()) && r instanceof LazyGenerationRule)
							.map(LazyGenerationRule.class::cast).findFirst().orElse(null);

					if (generationRule != null) {
						Object source = null;
						if (generationRule.getSourceParameter() != null) {
							String sourceParameterName = generationRule.getSourceParameter().getName();
							Parameter sourceParameter = customView.getParameters().stream()
									.filter(sp -> sp.getName().equals(sourceParameterName)).findFirst().orElse(null);
							if (sourceParameter != null) {
								customView.getParameters().remove(sourceParameter);
								source = sourceParameter.getValue();
							}
						}

						if (customView.getPath() != null)
							customView.getParameters().add(createParameter("path", customView.getPath()));
						if (customView.getIcon() != null)
							customView.getParameters().add(createParameter("icon", customView.getIcon()));
						if (customView.getFormat() != null)
							customView.getParameters().add(createParameter("format", customView.getFormat()));
						customView.getParameters().add(createParameter("patches", customView.getPatches()));
						if (customView.getPosition() != null)
							customView.getParameters().add(createParameter("position", customView.getPosition()));
						if (customView.eIsSet(PictoPackage.eINSTANCE.getCustomView_Layers())) {
							customView.getParameters().add(createParameter("activeLayers", customView.getLayers()));
						}

						for (Parameter customViewParameter : customView.getParameters()) {
							fs.put(new Variable(customViewParameter.getName(), getValue(customViewParameter),
									EolAnyType.Instance));
						}

						LazyGenerationRuleContentPromise contentPromise = (LazyGenerationRuleContentPromise) generationRule
								.execute(context, source);

						Collection<Variable> variables = contentPromise.getVariables();

						for (Parameter parameter : customView.getParameters()) {
							Object value = getValue(parameter);
							String paramName = parameter.getName();

							Variable variable = variables.stream().filter(v -> v.getName().equals(paramName)).findAny()
									.orElse(null);

							if (variable != null) {
								variable.setValue(value, context);
							} else {
								variables.add(new Variable(paramName, value, EolAnyType.Instance, false));
							}
						}
						instances.add(contentPromise);
					}
				}

				for (LazyGenerationRuleContentPromise instance : instances) {
					String format = getDefaultFormat();
					String icon = getDefaultIcon();
					List<Patch> patches = new ArrayList<>(1);
					Collection<String> path = Arrays.asList("");
					List<Layer> layers = new ArrayList<>();
					Variable layersVariable = null;
					Integer position = null;

					Collection<Variable> instanceVariables = instance.getVariables();

					for (Variable variable : instanceVariables) {
						Object varValue = variable.getValue();
						switch (variable.getName()) {
						case "format": {
							format = varValue + "";
							break;
						}
						case "path": {
							if (!(varValue instanceof Collection)) {
								(path = (Collection<String>) (varValue = new ArrayList<>(1)))
										.add(Objects.toString(varValue));
							} else if (!((Collection<?>) varValue).isEmpty()) {
								path = ((Collection<?>) varValue).stream().map(Objects::toString)
										.collect(Collectors.toList());
							}
							break;
						}
						case "icon": {
							icon = varValue + "";
							break;
						}
						case "position": {
							if (varValue instanceof Integer) {
								position = (Integer) varValue;
							} else if (varValue != null) {
								position = Integer.parseInt(varValue.toString());
							}
							break;
						}
						case "layers": {
							layersVariable = variable;
							for (Object layerMapObject : (Iterable<?>) varValue) {
								Map<Object, Object> layerMap = (Map<Object, Object>) layerMapObject;
								Layer layer = new Layer();
								layer.setId(layerMap.get("id") + "");
								layer.setTitle(layerMap.get("title") + "");
								if (layerMap.containsKey("active")) {
									layer.setActive((boolean) layerMap.get("active"));
								}
								layers.add(layer);
							}
							break;
						}
						case "patches": {
							if (varValue instanceof List) {
								patches = (List<Patch>) varValue;
							} else if (varValue instanceof Patch) {
								patches.add((Patch) varValue);
							} else if (varValue instanceof Collection) {
								patches.addAll((Collection<? extends Patch>) varValue);
							}
							break;
						}
						}

					}

					// If this is a custom view there may be an activeLayers variable in the
					// variables list
					Variable activeLayersVariable = instanceVariables.stream()
							.filter(v -> v.getName().equals("activeLayers")).findAny().orElse(null);
					if (activeLayersVariable != null) {
						Collection<?> activeLayers = (Collection<?>) activeLayersVariable.getValue();
						for (Layer layer : layers) {
							layer.setActive(activeLayers.contains(layer.getId()));
						}
					}

					// Replace layers variable from list of maps to list of Layer objects
					if (layersVariable != null) {
						instanceVariables.remove(layersVariable);
					}
					instanceVariables.add(Variable.createReadOnlyVariable("layers", layers));

					ViewTree vt = new ViewTree(instance, format, icon, position, patches, layers);
					viewTree.add(new ArrayList<>(path), vt);

					// get the graphviz
//					String output = instance.getContent();
//					System.out.println(output);
//					
					ViewContent vc = vt.getContent();
//					System.out.println(vc.getText());

					GraphvizContentTransformer transformer = new GraphvizContentTransformer();
					if (transformer.canTransform(vc)) {
						ViewContent vc2 = transformer.transform(vc, null);
						String text = vc2.getText();
						System.out.println(text);
						return text;
					}

					System.console();
				}

			}
			else {
				String content = module.execute() + "";
				viewTree = new ViewTree();
				viewTree.setPromise(new StaticContentPromise(content));
				viewTree.setFormat(renderingMetadata.getFormat());
			}
//
			// Handle static views (i.e. where source != null)
			for (CustomView customView : renderingMetadata.getCustomViews().stream()
					.filter(cv -> cv.getSource() != null).collect(Collectors.toList())) {
				String format = customView.getFormat() != null ? customView.getFormat() : getDefaultFormat();
				String icon = customView.getIcon() != null ? customView.getIcon() : getDefaultIcon();

				viewTree.add(customView.getPath(), new ViewTree(
						new StaticContentPromise(
								new File(new File(customView.eResource().getURI().toFileString()).getParentFile(),
										customView.getSource())),
						format, icon, customView.getPosition(), customView.getPatches(), Collections.emptyList()));
			}

			// Handle patches for existing views (i.e. where source == null and type/rule ==
			// null)
			for (CustomView customView : renderingMetadata.getCustomViews().stream()
					.filter(cv -> cv.getSource() == null && cv.getType() == null).collect(Collectors.toList())) {
				ArrayList<String> path = new ArrayList<>();
				path.add(viewTree.getName());
				path.addAll(customView.getPath());

				ViewTree existingView = viewTree.forPath(path);

				if (existingView != null) {
					if (customView.getIcon() != null)
						existingView.setIcon(customView.getIcon());
					if (customView.getFormat() != null)
						existingView.setFormat(customView.getFormat());
					if (customView.getPosition() != null)
						existingView.setPosition(customView.getPosition());

					existingView.getPatches().addAll(customView.getPatches());
					if (customView.eIsSet(PictoPackage.eINSTANCE.getCustomView_Layers())) {
						Collection<?> layers = customView.getLayers();
						for (Layer layer : existingView.getLayers()) {
							layer.setActive(layers.contains(layer.getId()));
						}
					}
				}
			}

			if (transformationUri != null) {
				viewTree.getBaseUris().add(transformationUri);
				viewTree.getBaseUris().add(transformationUri.resolve("./icons/"));
			}

			viewTree.getBaseUris().add(new URI(modelFile.toURI().toString()));
//			
//			return viewTree;
		}
		else {
			viewTree = createEmptyViewTree();
		}
		return null;

	}

	protected IModel loadModel(Model model, File baseFile) throws Exception {
		IModel m = ModelTypeExtension.forType(model.getType()).createModel();
		m.setName(model.getName());
		m.setReadOnLoad(true);
		m.setStoredOnDisposal(false);
		StringProperties properties = new StringProperties();
		IRelativePathResolver relativePathResolver = relativePath ->
			new File(baseFile.getParentFile(), relativePath).getAbsolutePath();
		
		for (Parameter parameter : model.getParameters()) {
			properties.put(parameter.getName(), parameter.getFile() != null ?
					relativePathResolver.resolve(parameter.getFile()) : parameter.getValue()
			);
		}
		m.load(properties, relativePathResolver);
		return m;
	}
	
	protected IModel loadModel(Model model, String code) throws Exception {
		IModel m = ModelTypeExtension.forType(model.getType()).createModel();
		m.setName(model.getName());
		m.setReadOnLoad(true);
		m.setStoredOnDisposal(false);
		StringProperties properties = new StringProperties();
//		IRelativePathResolver relativePathResolver = relativePath ->
//			new File(baseFile.getParentFile(), relativePath).getAbsolutePath();

//		for (Parameter parameter : model.getParameters()) {
//			properties.put(parameter.getName(), parameter.getFile() != null ?
//					relativePathResolver.resolve(parameter.getFile()) : parameter.getValue()
//			);
//		}
		m.load(properties, code);
//		m.load(properties, relativePathResolver);
		return m;
	}
	
}
