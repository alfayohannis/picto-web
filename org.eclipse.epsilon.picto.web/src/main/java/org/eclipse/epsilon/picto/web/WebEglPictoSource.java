package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.internal.registry.ConfigurationElementHandle;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
//import org.eclipse.epsilon.common.dt.launching.extensions.ModelTypeExtension;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.common.util.UriUtil;
import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EglTemplateFactoryModuleAdapter;
import org.eclipse.epsilon.egl.EgxModule;
import org.eclipse.epsilon.egl.dom.GenerationRule;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.eol.IEolModule;
//import org.eclipse.epsilon.eol.dt.ExtensionPointToolNativeTypeDelegate;
import org.eclipse.epsilon.eol.execute.context.FrameStack;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.execute.introspection.recording.IPropertyAccess;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.eol.types.EolAnyType;
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;
import org.eclipse.epsilon.picto.Layer;
import org.eclipse.epsilon.picto.LazyEgxModule;
import org.eclipse.epsilon.picto.PictoView;
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
import org.eclipse.epsilon.picto.dummy.IEditorPart;
import org.eclipse.epsilon.picto.source.EglPictoSource;
import org.eclipse.epsilon.picto.transformers.CsvContentTransformer;
import org.eclipse.epsilon.picto.transformers.GraphvizContentTransformer;
import org.eclipse.epsilon.picto.transformers.HtmlContentTransformer;
import org.eclipse.epsilon.picto.transformers.MarkdownContentTransformer;
import org.eclipse.epsilon.picto.transformers.SvgContentTransformer;
import org.eclipse.epsilon.picto.transformers.TextContentTransformer;
import org.eclipse.epsilon.picto.transformers.ViewContentTransformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

public class WebEglPictoSource extends EglPictoSource {

	protected File modelFile;
	protected File pictoFile;
	protected static final List<ViewContentTransformer> TRANSFORMERS = new ArrayList<>();
	protected List<EPackage> ePackages = new ArrayList<>();

	public WebEglPictoSource() throws Exception {

//		this.modelFile = new File(modelFile.getAbsolutePath());
//		this.modelFile = modelFile;

		TRANSFORMERS.add(new GraphvizContentTransformer());
		TRANSFORMERS.add(new SvgContentTransformer());
		TRANSFORMERS.add(new TextContentTransformer());
		TRANSFORMERS.add(new CsvContentTransformer());
		TRANSFORMERS.add(new HtmlContentTransformer());
		TRANSFORMERS.add(new MarkdownContentTransformer());
	}

	public File getModelFile() {
		return modelFile;
	}

	public void setModelFile(File modelFile) {
		this.modelFile = modelFile;
	}

	public File getPictoFile() {
		return pictoFile;
	}

	public void setPictoFile(File pictoFile) {
		this.pictoFile = pictoFile;
	}

	public void transform(String code) throws Exception {
		int x = code.indexOf("\n");
		String nsuri = code.substring(0, x);
		nsuri = nsuri.replace("<?", "");
		nsuri = nsuri.replace("?>", "");
		nsuri = nsuri.replace("nsuri", "").trim() + ".flexmi";

		if (modelFile == null) {
			modelFile = new File(PictoApplication.WORKSPACE + nsuri);
			Files.write(code.getBytes(), modelFile);
		}
		this.transform(modelFile);
	}

	public Map<String, String> transform(File modifiedFile) throws Exception {
		Map<String, String> modifiedViewContents = new HashMap<>();
		Resource resource = null;
		PictoView pictoView = new PictoView();
		ViewTree rootViewTree = new ViewTree();

		String filename = modifiedFile.getAbsolutePath()
				.replace(new File(PictoApplication.WORKSPACE).getAbsolutePath() + File.separator, "")
				.replace("\\", "/");
		ViewContentCache viewContentCache = FileViewContentCache.addPictoFile(filename);

		try {
			if (modifiedFile.getAbsolutePath().endsWith(".model") || modifiedFile.getAbsolutePath().endsWith(".flexmi")
					|| modifiedFile.getAbsolutePath().endsWith(".xmi")) {
				this.modelFile = new File(modifiedFile.getAbsolutePath());
				this.pictoFile = new File(modifiedFile.getAbsolutePath() + ".picto");
				loadMetamodel(this.pictoFile.getName().replace(".model.picto", ""));
				resource = getResource(this.modelFile);
			} else if (modifiedFile.getAbsolutePath().endsWith(".egx")) {
				String modelFileName = modifiedFile.getName().replace(".egx", ".model");
				this.modelFile = new File(
						modifiedFile.getParentFile().getAbsolutePath() + File.separator + modelFileName);
				this.pictoFile = new File(
						modifiedFile.getParentFile().getAbsolutePath() + File.separator + modelFileName + ".picto");
				loadMetamodel(this.pictoFile.getName().replace(".model.picto", ""));
				resource = getResource(this.modelFile);
			} else if (modifiedFile.getAbsolutePath().endsWith(".model.picto")) {
				this.pictoFile = modifiedFile;
				this.modelFile = new File(modifiedFile.getAbsolutePath().replace(".model.picto", ".model"));
				loadMetamodel(this.pictoFile.getName().replace(".model.picto", ""));
				resource = getResource(this.modelFile);
			} else if (modifiedFile.getAbsolutePath().endsWith("-standalone.picto")) {
				this.pictoFile = modifiedFile;
				this.modelFile = modifiedFile;
//				loadMetamodel(this.pictoFile.getName().replace("-standalone.picto", ""));
			}
		} catch (Exception ex) {
			throw new ResourceLoadingException(ex);
		}

		Picto renderingMetadata = this.getRenderingMetadata(this.pictoFile);

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
				module = new PictoLazyEglModule();
			} else {
				module = new EglTemplateFactoryModuleAdapter(new EglFileGeneratingTemplateFactory());
			}

			IEolContext context = module.getContext();
//			context.getNativeTypeDelegates().add(new ExtensionPointToolNativeTypeDelegate());

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
//				List<org.eclipse.emf.common.util.URI> metamodels = ((EmfModel) model).getMetamodelFileUris();
//				((EmfModel) model).setMetamodelUri("http://www.eclipse.org/emf/2002/Ecore");
////				((EmfModel) model).setMetamodelFileBased(false);
//				model.load();
			}

			context.getModelRepository().addModels(models);

			if ("egx".equals(renderingMetadata.getFormat())) {

//				LazyEgxModule lazyEgxModule = (LazyEgxModule) module;
//				for (GenerationRule generationRule : lazyEgxModule.getGenerationRules()) {
//					LazyGenerationRule lazyGenerationRule = (LazyGenerationRule) generationRule;
//					if (lazyGenerationRule.getSourceParameter() != null) {
//						org.eclipse.epsilon.eol.dom.Parameter parameter = lazyGenerationRule.getSourceParameter();
//						String type = parameter.getTypeExpression().getName();
//						String typePackage = type.split("::")[0].trim();
//						loadMetamodel(typePackage);
//						IModel m = context.getModelRepository().getModels().get(0);
//						System.console();
//					}
//				}

				/** PROPERTY ACCESS RECORDS **/
				// start recording for property access
				((PictoLazyEglModule) module).startRecording();

				@SuppressWarnings("unchecked")
				List<LazyGenerationRuleContentPromise> instances = (List<LazyGenerationRuleContentPromise>) module
						.execute();

				// stop recording for property access
				((PictoLazyEglModule) module).stopRecording();
				/** END **/

				// Handle dynamic views (i.e. where type != null)'
				List<CustomView> customViews = renderingMetadata.getCustomViews().stream()
						.filter(cv -> cv.getType() != null).collect(Collectors.toList());
				for (CustomView customView : customViews) {

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
					String pathString = null;
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
							pathString = "/" + String.join("/", path);
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
					rootViewTree.add(new ArrayList<>(path), vt);

//					System.out.println(String.format("%s, %s, %s", vt.getName(), format, position));

//					// generate target for each view tree
//					Object accessedObject = instance.getVariables().iterator().next().getValue();
//					if (accessedObject instanceof EObject) {
//						String accessedObjectUri = ((EObject) accessedObject).eResource()
//								.getURIFragment((EObject) accessedObject);
//						vt.setUri(accessedObjectUri);
//					} else {
//						vt.setUri(target.iterator().next());
//					}

					// generate the content of each view tree
					ViewContent vc = vt.getContent();
					for (ViewContentTransformer transformer : TRANSFORMERS) {
						if (transformer.canTransform(vc)) {
							vc = transformer.transform(vc, pictoView);
							vt.setContent(vc);
							break;
						}
					}

					// put the content into the FileViewContentCache
					// read all property accesses
					for (IPropertyAccess propertyAccess : ((PictoLazyEglModule) module).getPropertyAccessRecorder()
							.getPropertyAccesses().all()) {
						GenerationRulePropertyAccess generationRulePropertyAccess = (GenerationRulePropertyAccess) propertyAccess;

						String ruleName = generationRulePropertyAccess.getRule().getName();
						EObject contextElement = (EObject) generationRulePropertyAccess.getContextElement();
						String contextUri = contextElement.eResource().getURIFragment(contextElement);
						EObject modelElement = (EObject) generationRulePropertyAccess.getModelElement();
						String elementUri = modelElement.eResource().getURIFragment(modelElement);
						String propertyName = generationRulePropertyAccess.getPropertyName();
						FileViewContentCache.addAccessRecord(filename, ruleName, contextUri, elementUri, propertyName,
								pathString);
					}

					PictoResponse pictoResponse = new PictoResponse();
					pictoResponse.setFilename(filename);
					pictoResponse.setPath(pathString);
					System.out.println(pathString);
					pictoResponse.setType(vt.getContent().getFormat());
					pictoResponse.setContent(vc.getText());
					String jsonString = new ObjectMapper().writerWithDefaultPrettyPrinter()
							.writeValueAsString(pictoResponse);

					String temp = viewContentCache.getViewContentCache(pathString);
					if (!jsonString.equals(viewContentCache.getViewContentCache(pathString))) {
						viewContentCache.putViewContentCache(pathString, jsonString);
						modifiedViewContents.put(pathString, jsonString);
					}
					System.console();
				}

			} else {
				String content = module.execute() + "";
				rootViewTree = new ViewTree();
				rootViewTree.setPromise(new StaticContentPromise(content));
				rootViewTree.setFormat(renderingMetadata.getFormat());
			}
//
			// Handle static views (i.e. where source != null)
			for (CustomView customView : renderingMetadata.getCustomViews().stream()
					.filter(cv -> cv.getSource() != null).collect(Collectors.toList())) {
				String format = customView.getFormat() != null ? customView.getFormat() : getDefaultFormat();
				String icon = customView.getIcon() != null ? customView.getIcon() : getDefaultIcon();

				rootViewTree.add(customView.getPath(), new ViewTree(
						new StaticContentPromise(
								new File(new File(customView.eResource().getURI().toFileString()).getParentFile(),
										customView.getSource())),
						format, icon, customView.getPosition(), customView.getPatches(), Collections.emptyList()));

				// put the content into the elementViewContentMap
				String pathString = "/" + String.join("/", customView.getPath());
				ViewTree vt = rootViewTree.getChildren().get(rootViewTree.getChildren().size() - 1);
				PictoResponse pictoResponse = new PictoResponse();
				pictoResponse.setFilename(filename);
				pictoResponse.setType(vt.getContent().getFormat());
				pictoResponse.setContent(vt.getContent().getText());
				pictoResponse.setPath(pathString);
				String jsonString = new ObjectMapper().writerWithDefaultPrettyPrinter()
						.writeValueAsString(pictoResponse);

				if (!jsonString.equals(viewContentCache.getViewContentCache(pathString))) {
					viewContentCache.putViewContentCache(pathString, jsonString);
					modifiedViewContents.put(pathString, jsonString);
				}
			}

			// Handle patches for existing views (i.e. where source == null and type/rule ==
			// null)
			for (CustomView customView : renderingMetadata.getCustomViews().stream()
					.filter(cv -> cv.getSource() == null && cv.getType() == null).collect(Collectors.toList())) {
				ArrayList<String> path = new ArrayList<>();
				path.add(rootViewTree.getName());
				path.addAll(customView.getPath());

				ViewTree existingView = rootViewTree.forPath(path);

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
				rootViewTree.getBaseUris().add(transformationUri);
				rootViewTree.getBaseUris().add(transformationUri.resolve("./icons/"));
			}

			ViewTree tempViewTree = rootViewTree;
			for (int i = 0; i < tempViewTree.getChildren().size(); i++) {

			}

			rootViewTree.getBaseUris().add(new URI(modelFile.toURI().toString()));

			String jsTreeJson = generateJsTreeData(filename, rootViewTree);
			if (!jsTreeJson.equals(viewContentCache.getViewContentCache(FileViewContentCache.PICTO_TREE))) {
				viewContentCache.putViewContentCache(FileViewContentCache.PICTO_TREE, jsTreeJson);
				modifiedViewContents.put(FileViewContentCache.PICTO_TREE, jsTreeJson);
			}

		} else {
			rootViewTree = createEmptyViewTree();
			String jsTreeJson = generateJsTreeData(filename, rootViewTree);
			if (!jsTreeJson.equals(viewContentCache.getViewContentCache(FileViewContentCache.PICTO_TREE))) {
				viewContentCache.putViewContentCache(FileViewContentCache.PICTO_TREE, jsTreeJson);
				modifiedViewContents.put(FileViewContentCache.PICTO_TREE, jsTreeJson);
			}
		}
		return modifiedViewContents;

	}

	protected String generateJsTreeData(String filename, ViewTree viewTree) throws JsonProcessingException {
		JsTreeNode root = new JsTreeNode();
		copyViewTreeToJsTreeData(filename, viewTree, root);
		List<JsTreeNode> jsTreeNodes = new ArrayList<>();
		jsTreeNodes.addAll(root.getChildren());

		String response = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsTreeNodes);

		PictoResponse pictoResponse = new PictoResponse();
		pictoResponse.setFilename(filename);
		pictoResponse.setPath(FileViewContentCache.PICTO_TREE);
		pictoResponse.setType("json");
		pictoResponse.setContent(response);

		return (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValueAsString(pictoResponse);

	}

	protected void copyViewTreeToJsTreeData(String filename, ViewTree viewTree, JsTreeNode jsTreeNode) {
		for (ViewTree child : viewTree.getChildren()) {
			JsTreeNode jsonChild = new JsTreeNode();
			jsTreeNode.getChildren().add(jsonChild);
			copyViewTreeToJsTreeData(filename, child, jsonChild);
		}
		String text = viewTree.getName();
//		String text = String.format( //
//				"<span id='%s' onclick=\"Picto.draw('%s', '%s')\">%s</span>" //
//				, filename + "#" + viewTree.getPathString() //
//				, filename + "#" + viewTree.getPathString() //
//				, "/picto?file=" + filename + "&target=" + viewTree.getPathString() + "&name=" + viewTree.getName() //
//				, viewTree.getName());
		jsTreeNode.setText(text);
		jsTreeNode.setPath(viewTree.getPathString());
	}

	protected JsonViewTree generateJsonViewTree(ViewTree viewTree) {
		JsonViewTree root = new JsonViewTree();
		copyViewTreeToJsonViewTree(viewTree, root);
		return root;
	}

	protected void copyViewTreeToJsonViewTree(ViewTree viewTree, JsonViewTree jsonViewTree) {
		for (ViewTree child : viewTree.getChildren()) {
			JsonViewTree jsonChild = new JsonViewTree();
			jsonViewTree.getChildren().add(jsonChild);
			copyViewTreeToJsonViewTree(child, jsonChild);
		}
		JsonViewContent jvc = new JsonViewContent();
		jvc.setFormat(viewTree.getContent().getFormat());
		jvc.setText(viewTree.getContent().getText());
		jvc.setLabel(viewTree.getContent().getLabel());
		jsonViewTree.setContent(jvc);
		jsonViewTree.setName(viewTree.getName());
		jsonViewTree.setIcon(viewTree.getIcon());
		jsonViewTree.setPosition(viewTree.getPosition());
		jsonViewTree.setUri(viewTree.getPathString());
	}

	protected IModel loadModel(Model model, File baseFile) throws Exception {
//		IModel m = null;
		if ("EMF".equals(model.getType())) {
//			m = new EmfModel();
			String metamodelName = (String) model.getParameters().stream()
					.filter(p -> EmfModel.PROPERTY_METAMODEL_URI.equals(p.getName())).findFirst().orElse(null)
					.getValue();
			loadMetamodel(metamodelName);
		}
//		else {
//			m = ModelTypeExtension.forType(model.getType()).createModel();
//		}
//		m.setName(model.getName());
//		m.setReadOnLoad(true);
//		m.setStoredOnDisposal(false);
//		StringProperties properties = new StringProperties();
		IRelativePathResolver relativePathResolver = relativePath -> new File(baseFile.getParentFile(), relativePath)
				.getAbsolutePath();
		String filePath = null;
		for (Parameter parameter : model.getParameters()) {
//			properties.put(parameter.getName(),
//					parameter.getFile() != null ? relativePathResolver.resolve(parameter.getFile())
//							: parameter.getValue());
			if (parameter.getFile() != null) {
				filePath = relativePathResolver.resolve(parameter.getFile());
			}
		}
		IModel model2 = new InMemoryEmfModel("M", getResource(new File(filePath)));
		((InMemoryEmfModel) model2).setExpand(true);
		return model2;
//		m.load(properties, relativePathResolver);
//		return m;
	}

	protected IModel loadModel(Model model, String code) throws Exception {
//		IModel m = ModelTypeExtension.forType(model.getType()).createModel();
		IModel m = new EmfModel();
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

	public List<EPackage> loadMetamodel(String metamodelName) throws Exception {
		if (!ePackages.stream().anyMatch(p -> p.getName().equals(metamodelName))) {
			File metamodelFile = new File(
					this.pictoFile.getParentFile().getAbsolutePath() + File.separator + metamodelName + ".ecore");
			if (metamodelFile.exists()) {
				org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI
						.createFileURI(metamodelFile.getAbsolutePath());
				List<EPackage> ePackage = EmfUtil.register(uri, EPackage.Registry.INSTANCE);
				ePackages.addAll(ePackage);
			} else if ("http://www.eclipse.org/emf/2002/Ecore".equals(metamodelName)) {

				if (EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI) == null) {
					EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
				}

//				org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI
//						.createFileURI("../workspace/egldoc/Ecore.ecore");
//				ePackages.addAll(EmfUtil.register(uri, EPackage.Registry.INSTANCE));
//				org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createURI(metamodelName);
				EPackage ePackage = EcorePackage.eINSTANCE.eClass().getEPackage();
//				String x = ePackage.getNsURI();
//				org.eclipse.emf.common.util.URI uri2 = org.eclipse.emf.common.util.URI.createURI(x);
//				EmfUtil.register(uri2, EPackage.Registry.INSTANCE, true);
				ePackages.add(EcorePackage.eINSTANCE.eClass().getEPackage());
				System.console();
			}
		}
		return ePackages;
	}

	public Resource getResource(File modelFile) {
		ResourceSet resourceSet = new ResourceSetImpl();

		if (modelFile.getName().endsWith(".ecore")) {
				resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		}

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("model", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new FlexmiResourceFactory());
		Resource resource = resourceSet
				.getResource(org.eclipse.emf.common.util.URI.createFileURI(modelFile.getAbsolutePath()), true);
//		Resource resource = resourceSet
//				.createResource(org.eclipse.emf.common.util.URI.createFileURI(modelFile.getAbsolutePath()));
		try {
			resource.load(null);
			return resource;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Picto getRenderingMetadata(File file) {
		try {
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new FlexmiResourceFactory());
			Resource resource = resourceSet
					.getResource(org.eclipse.emf.common.util.URI.createFileURI(file.getAbsolutePath()), true);
			resource.load(null);
			return (Picto) resource.getContents().iterator().next();
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public Picto getRenderingMetadata(IEditorPart editorPart) {
		return this.getRenderingMetadata(this.pictoFile);
	}

	@Override
	public void showElement(String id, String uri, IEditorPart editor) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Resource getResource(IEditorPart editorPart) {
		// TODO Auto-generated method stub
		return null;
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
