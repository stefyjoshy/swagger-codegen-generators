package io.swagger.codegen.v3.generators.nodejs;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.swagger.codegen.v3.generators.OperationParameters;
import io.swagger.codegen.v3.utils.URLPathUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.core.util.Yaml;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public class NodeJSServerCodegen extends DefaultCodegenConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeJSServerCodegen.class);

	protected String implFolder = "service";

	public static final String GOOGLE_CLOUD_FUNCTIONS = "googleCloudFunctions";

	public static final String EXPORTED_NAME = "exportedName";

	protected String apiVersion = "1.0.0";

	protected int serverPort = 8080;

	protected String projectName = "swagger-server";

	protected boolean googleCloudFunctions;

	protected String exportedName;

	public NodeJSServerCodegen() {
		super();

		// set the output folder here
		outputFolder = "generated-code/nodejs";

		/*
		 * Models. You can write model files using the modelTemplateFiles map. if you want
		 * to create one template for file, you can do so here. for multiple files for
		 * model, just put another entry in the `modelTemplateFiles` with a different
		 * extension
		 */
		modelTemplateFiles.clear();

		/*
		 * Api classes. You can write classes for each Api file with the apiTemplateFiles
		 * map. as with models, add multiple entries with different extensions for
		 * multiple files per class
		 */
		apiTemplateFiles.put("controller.mustache", // the template to use
				".js"); // the extension for each file to write

		/*
		 * Reserved words. Override this with reserved words specific to your language
		 */
		setReservedWordsLowerCase(Arrays.asList("break", "case", "class", "catch", "const", "continue", "debugger",
				"default", "delete", "do", "else", "export", "extends", "finally", "for", "function", "if", "import",
				"in", "instanceof", "let", "new", "return", "super", "switch", "this", "throw", "try", "typeof", "var",
				"void", "while", "with", "yield"));

		/*
		 * Additional Properties. These values can be passed to the templates and are
		 * available in models, apis, and supporting files
		 */
		additionalProperties.put("apiVersion", apiVersion);
		additionalProperties.put("serverPort", serverPort);
		additionalProperties.put("implFolder", implFolder);

		supportingFiles.add(new SupportingFile("writer.mustache", ("utils").replace(".", "/"), "writer.js"));

		cliOptions.add(CliOption.newBoolean(GOOGLE_CLOUD_FUNCTIONS,
				"When specified, it will generate the code which runs within Google Cloud Functions "
						+ "instead of standalone Node.JS server. See "
						+ "https://cloud.google.com/functions/docs/quickstart for the details of how to "
						+ "deploy the generated code."));
		cliOptions.add(new CliOption(EXPORTED_NAME,
				"When the generated code will be deployed to Google Cloud Functions, this option can be "
						+ "used to update the name of the exported function. By default, it refers to the "
						+ "basePath. This does not affect normal standalone nodejs server code."));
	}

	@Override
	public String apiPackage() {
		return "controllers";
	}

	/**
	 * Configures the type of generator.
	 * @return the CodegenType for this generator
	 * @see CodegenType
	 */
	@Override
	public CodegenType getTag() {
		return CodegenType.SERVER;
	}

	/**
	 * Configures a friendly name for the generator. This will be used by the generator to
	 * select the library with the -l flag.
	 * @return the friendly name for the generator
	 */
	@Override
	public String getName() {
		return "nodejs-server";
	}

	/**
	 * Returns human-friendly help for the generator. Provide the consumer with help tips,
	 * parameters here
	 * @return A string value for the help message
	 */
	@Override
	public String getHelp() {
		return "Generates a nodejs server library using the swagger-tools project.  By default, "
				+ "it will also generate service classes--which you can disable with the `-Dnoservice` environment variable.";
	}

	@Override
	public String toApiName(String name) {
		if (name.length() == 0) {
			return "DefaultController";
		}
		return initialCaps(name);
	}

	@Override
	public String toApiFilename(String name) {
		return toApiName(name);
	}

	@Override
	public String apiFilename(String templateName, String tag) {
		String result = super.apiFilename(templateName, tag);

		if (templateName.equals("service.mustache")) {
			String stringToMatch = File.separator + "controllers" + File.separator;
			String replacement = File.separator + implFolder + File.separator;
			result = StringUtils.replace(result, stringToMatch, replacement);
		}
		return result;
	}

	@Override
	public String getDefaultTemplateDir() {
		return "nodejs";
	}

	private String implFileFolder(String output) {
		return outputFolder + "/" + output + "/" + apiPackage().replace('.', '/');
	}

	/**
	 * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
	 * those terms here. This logic is only called if a variable matches the reserved
	 * words
	 * @return the escaped term
	 */
	@Override
	public String escapeReservedWord(String name) {
		if (this.reservedWordsMappings().containsKey(name)) {
			return this.reservedWordsMappings().get(name);
		}
		return "_" + name;
	}

	/**
	 * Location to write api files. You can use the apiPackage() as defined when the class
	 * is instantiated
	 */
	@Override
	public String apiFileFolder() {
		return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
	}

	public boolean getGoogleCloudFunctions() {
		return googleCloudFunctions;
	}

	public void setGoogleCloudFunctions(boolean value) {
		googleCloudFunctions = value;
	}

	public String getExportedName() {
		return exportedName;
	}

	public void setExportedName(String name) {
		exportedName = name;
	}

	@Override
	public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
		@SuppressWarnings("unchecked")
		Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
		@SuppressWarnings("unchecked")
		List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
		for (CodegenOperation operation : operations) {
			operation.httpMethod = operation.httpMethod.toLowerCase();

			List<CodegenParameter> params = operation.allParams;
			if (params != null && params.size() == 0) {
				operation.allParams = null;
			}
			List<CodegenResponse> responses = operation.responses;
			if (responses != null) {
				for (CodegenResponse resp : responses) {
					if ("0".equals(resp.code)) {
						resp.code = "default";
					}
				}
			}
			if (operation.examples != null && !operation.examples.isEmpty()) {
				// Leave application/json* items only
				for (Iterator<Map<String, String>> it = operation.examples.iterator(); it.hasNext();) {
					final Map<String, String> example = it.next();
					final String contentType = example.get("contentType");
					if (contentType == null || !contentType.startsWith("application/json")) {
						it.remove();
					}
				}
			}
		}
		return objs;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> getOperations(Map<String, Object> objs) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> apiInfo = (Map<String, Object>) objs.get("apiInfo");
		List<Map<String, Object>> apis = (List<Map<String, Object>>) apiInfo.get("apis");
		for (Map<String, Object> api : apis) {
			result.add((Map<String, Object>) api.get("operations"));
		}
		return result;
	}

	private static List<Map<String, Object>> sortOperationsByPath(List<CodegenOperation> ops) {
		Multimap<String, CodegenOperation> opsByPath = ArrayListMultimap.create();

		for (CodegenOperation op : ops) {
			opsByPath.put(op.path, op);
		}

		List<Map<String, Object>> opsByPathList = new ArrayList<Map<String, Object>>();
		for (Entry<String, Collection<CodegenOperation>> entry : opsByPath.asMap().entrySet()) {
			Map<String, Object> opsByPathEntry = new HashMap<String, Object>();
			opsByPathList.add(opsByPathEntry);
			opsByPathEntry.put("path", entry.getKey());
			opsByPathEntry.put("operation", entry.getValue());
			List<CodegenOperation> operationsForThisPath = Lists.newArrayList(entry.getValue());
			operationsForThisPath.get(operationsForThisPath.size() - 1).getVendorExtensions()
					.put(CodegenConstants.HAS_MORE_EXT_NAME, Boolean.FALSE);
			if (opsByPathList.size() < opsByPath.asMap().size()) {
				opsByPathEntry.put("hasMore", "true");
			}
		}

		return opsByPathList;
	}

	@Override
	public void processOpts() {
		super.processOpts();

		if (additionalProperties.containsKey(GOOGLE_CLOUD_FUNCTIONS)) {
			setGoogleCloudFunctions(Boolean.valueOf(additionalProperties.get(GOOGLE_CLOUD_FUNCTIONS).toString()));
		}

		if (additionalProperties.containsKey(EXPORTED_NAME)) {
			setExportedName((String) additionalProperties.get(EXPORTED_NAME));
		}

		/*
		 * Supporting Files. You can write single files for the generator with the entire
		 * object tree available. If the input file has a suffix of `.mustache it will be
		 * processed by the template engine. Otherwise, it will be copied
		 */
		// supportingFiles.add(new SupportingFile("controller.mustache",
		// "controllers",
		// "controller.js")
		// );
		supportingFiles.add(new SupportingFile("swagger.mustache", "api", "openapi.yaml"));
		if (getGoogleCloudFunctions()) {
			writeOptional(outputFolder, new SupportingFile("index-gcf.mustache", "", "index.js"));
		}
		else {
			writeOptional(outputFolder, new SupportingFile("index.mustache", "", "index.js"));
		}
		writeOptional(outputFolder, new SupportingFile("package.mustache", "", "package.json"));
		writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
		if (System.getProperty("noservice") == null) {
			apiTemplateFiles.put("service.mustache", // the template to use
					"Service.js"); // the extension for each file to write
		}
	}

	@Override
	public void preprocessOpenAPI(OpenAPI openAPI) {
		this.openAPI = openAPI;
		URL url = URLPathUtil.getServerURL(openAPI);
		String host = URLPathUtil.LOCAL_HOST;
		String port = "8080";
		String basePath = null;
		if (url != null) {
			// port = String.valueOf(url.getPort()); TODO: fix port resolving in URL.
			host = url.getHost();
			basePath = url.getPath();
		}
		this.additionalProperties.put("serverPort", port);

		if (openAPI.getInfo() != null) {
			Info info = openAPI.getInfo();
			if (info.getTitle() != null) {
				// when info.title is defined, use it for projectName
				// used in package.json
				projectName = info.getTitle().replaceAll("[^a-zA-Z0-9]", "-").replaceAll("^[-]*", "")
						.replaceAll("[-]*$", "").replaceAll("[-]{2,}", "-").toLowerCase();
				this.additionalProperties.put("projectName", projectName);
			}
		}

		if (getGoogleCloudFunctions()) {
			// Note that Cloud Functions don't allow customizing port name, simply
			// checking host
			// is good enough.
			if (!host.endsWith(".cloudfunctions.net")) {
				LOGGER.warn("Host " + host + " seems not matching with cloudfunctions.net URL.");
			}
			if (!additionalProperties.containsKey(EXPORTED_NAME)) {
				if (basePath == null || basePath.equals("/")) {
					LOGGER.warn("Cannot find the exported name properly. Using 'openapi' as the exported name");
					basePath = "/openapi";
				}
				additionalProperties.put(EXPORTED_NAME, basePath.substring(1));
			}
		}

		// need vendor extensions for x-swagger-router-controller
		Paths paths = openAPI.getPaths();
		if (paths != null) {
			for (String pathname : paths.keySet()) {
				PathItem path = paths.get(pathname);
				Map<PathItem.HttpMethod, Operation> operationMap = path.readOperationsMap();
				if (operationMap != null) {
					for (PathItem.HttpMethod method : operationMap.keySet()) {
						Operation operation = operationMap.get(method);
						String tag = "default";
						if (operation.getTags() != null && operation.getTags().size() > 0) {
							tag = toApiName(operation.getTags().get(0));
						}
						if (operation.getOperationId() == null) {
							operation.setOperationId(getOrGenerateOperationId(operation, pathname, method.toString()));
						}

						if (operation.getExtensions() == null) {
							operation.setExtensions(new HashMap<>());
						}
						if (operation.getExtensions() != null
								&& operation.getExtensions().get("x-swagger-router-controller") == null) {
							operation.getExtensions().put("x-swagger-router-controller", sanitizeTag(tag));
						}
					}
				}
			}
		}
	}

	@Override
	public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
		OpenAPI openAPI = (OpenAPI) objs.get("openAPI");
		if (openAPI != null) {
			try {
				SimpleModule module = new SimpleModule();
				module.addSerializer(Double.class, new JsonSerializer<Double>() {
					@Override
					public void serialize(Double val, JsonGenerator jgen, SerializerProvider provider)
							throws IOException, JsonProcessingException {
						jgen.writeNumber(new BigDecimal(val));
					}
				});
				objs.put("swagger-yaml", Yaml.mapper().registerModule(module).writeValueAsString(openAPI));
			}
			catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		for (Map<String, Object> operations : getOperations(objs)) {
			@SuppressWarnings("unchecked")
			List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");

			List<Map<String, Object>> opsByPathList = sortOperationsByPath(ops);
			operations.put("operationsByPath", opsByPathList);
		}
		return super.postProcessSupportingFileData(objs);
	}

	@Override
	public String removeNonNameElementToCamelCase(String name) {
		return removeNonNameElementToCamelCase(name, "[-:;#]");
	}

	@Override
	public String escapeUnsafeCharacters(String input) {
		return input.replace("*/", "*_/").replace("/*", "/_*");
	}

	@Override
	public String escapeQuotationMark(String input) {
		// remove " to avoid code injection
		return input.replace("\"", "");
	}

	protected void configuresParameterForMediaType(CodegenOperation codegenOperation,
			List<CodegenContent> codegenContents) {
		if (codegenContents.isEmpty()) {
			CodegenContent content = new CodegenContent();
			content.getParameters().addAll(codegenOperation.allParams);
			codegenContents.add(content);

			codegenOperation.getContents().add(content);
			return;
		}
		for (CodegenContent content : codegenContents) {
			addParameters(content, codegenOperation.bodyParams);
			addParameters(content, codegenOperation.queryParams);
			addParameters(content, codegenOperation.pathParams);
			addParameters(content, codegenOperation.headerParams);
			addParameters(content, codegenOperation.cookieParams);
		}
		for (CodegenContent content : codegenContents) {
			OperationParameters.addHasMore(content.getParameters());
		}
		codegenOperation.getContents().addAll(codegenContents);
	}

}
