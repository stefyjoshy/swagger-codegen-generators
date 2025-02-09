package io.swagger.codegen.v3.generators.scala;

import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.SupportingFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class ScalaClientCodegen extends AbstractScalaCodegen {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScalaClientCodegen.class);

	protected String authScheme = "";

	protected String gradleWrapperPackage = "gradle.wrapper";

	protected boolean authPreemptive;

	protected boolean asyncHttpClient = !authScheme.isEmpty();

	protected String groupId = "io.swagger";

	protected String artifactId = "swagger-scala-client";

	protected String artifactVersion = "1.0.0";

	protected String clientName = "AsyncClient";

	protected String invokerPackage = "io.swagger.client";

	public ScalaClientCodegen() {
		super();
		outputFolder = "generated-code/scala";
		modelTemplateFiles.put("model.mustache", ".scala");
		apiTemplateFiles.put("api.mustache", ".scala");
		apiPackage = "io.swagger.client.api";
		modelPackage = "io.swagger.client.model";

		setReservedWordsLowerCase(Arrays.asList(
				// local variable names used in API methods (endpoints)
				"path", "contentTypes", "contentType", "queryParams", "headerParams", "formParams", "postBody", "mp",
				"basePath", "apiInvoker",

				// scala reserved words
				"abstract", "case", "catch", "class", "def", "do", "else", "extends", "false", "final", "finally",
				"for", "forSome", "if", "implicit", "import", "lazy", "match", "new", "null", "object", "override",
				"package", "private", "protected", "return", "sealed", "super", "this", "throw", "trait", "try", "true",
				"type", "val", "var", "while", "with", "yield"));

		additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
		additionalProperties.put(CodegenConstants.GROUP_ID, groupId);
		additionalProperties.put(CodegenConstants.ARTIFACT_ID, artifactId);
		additionalProperties.put(CodegenConstants.ARTIFACT_VERSION, artifactVersion);
		additionalProperties.put("asyncHttpClient", asyncHttpClient);
		additionalProperties.put("authScheme", authScheme);
		additionalProperties.put("authPreemptive", authPreemptive);
		additionalProperties.put("clientName", clientName);

		supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
		supportingFiles.add(new SupportingFile("apiInvoker.mustache",
				(sourceFolder + File.separator + invokerPackage).replace(".", java.io.File.separator),
				"ApiInvoker.scala"));
		supportingFiles.add(new SupportingFile("client.mustache",
				(sourceFolder + File.separator + invokerPackage).replace(".", java.io.File.separator),
				clientName + ".scala"));
		supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
		supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
		// gradle settings
		supportingFiles.add(new SupportingFile("build.gradle.mustache", "", "build.gradle"));
		supportingFiles.add(new SupportingFile("settings.gradle.mustache", "", "settings.gradle"));
		supportingFiles.add(new SupportingFile("gradle.properties.mustache", "", "gradle.properties"));
		// gradleWrapper files
		supportingFiles.add(new SupportingFile("gradlew.mustache", "", "gradlew"));
		supportingFiles.add(new SupportingFile("gradlew.bat.mustache", "", "gradlew.bat"));
		supportingFiles.add(new SupportingFile("gradle-wrapper.properties.mustache",
				gradleWrapperPackage.replace(".", File.separator), "gradle-wrapper.properties"));
		supportingFiles.add(new SupportingFile("gradle-wrapper.jar", gradleWrapperPackage.replace(".", File.separator),
				"gradle-wrapper.jar"));

		supportingFiles.add(new SupportingFile("build.sbt.mustache", "", "build.sbt"));
		supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));

		importMapping.remove("List");
		importMapping.remove("Set");
		importMapping.remove("Map");

		importMapping.put("Date", "java.util.Date");
		importMapping.put("ListBuffer", "scala.collection.mutable.ListBuffer");

		typeMapping = new HashMap<>();
		typeMapping.put("enum", "NSString");
		typeMapping.put("array", "List");
		typeMapping.put("set", "Set");
		typeMapping.put("boolean", "Boolean");
		typeMapping.put("string", "String");
		typeMapping.put("int", "Int");
		typeMapping.put("integer", "Integer");
		typeMapping.put("long", "Long");
		typeMapping.put("float", "Float");
		typeMapping.put("byte", "Byte");
		typeMapping.put("short", "Short");
		typeMapping.put("char", "Char");
		typeMapping.put("double", "Double");
		typeMapping.put("object", "Any");
		typeMapping.put("file", "File");
		typeMapping.put("binary", "Array[Byte]");
		typeMapping.put("ByteArray", "Array[Byte]");
		typeMapping.put("ArrayByte", "Array[Byte]");
		typeMapping.put("date-time", "Date");
		typeMapping.put("DateTime", "Date");
		typeMapping.put("map", "Map");

		instantiationTypes.put("array", "ListBuffer");
		instantiationTypes.put("map", "HashMap");

		cliOptions
				.add(new CliOption(CodegenConstants.MODEL_PROPERTY_NAMING, CodegenConstants.MODEL_PROPERTY_NAMING_DESC)
						.defaultValue("camelCase"));
	}

	@Override
	public void processOpts() {
		super.processOpts();

		if (additionalProperties.containsKey(CodegenConstants.MODEL_PROPERTY_NAMING)) {
			setModelPropertyNaming((String) additionalProperties.get(CodegenConstants.MODEL_PROPERTY_NAMING));
		}
	}

	public void setModelPropertyNaming(String naming) {
		if ("original".equals(naming) || "camelCase".equals(naming) || "PascalCase".equals(naming)
				|| "snake_case".equals(naming)) {
			this.modelPropertyNaming = naming;
		}
		else {
			throw new IllegalArgumentException("Invalid model property naming '" + naming
					+ "'. Must be 'original', 'camelCase', " + "'PascalCase' or 'snake_case'");
		}
	}

	public String getModelPropertyNaming() {
		return this.modelPropertyNaming;
	}

	@Override
	public String toVarName(String name) {
		// sanitize name
		name = sanitizeName(name); // FIXME: a parameter should not be assigned. Also
									// declare the methods parameters as 'final'.

		if ("_".equals(name)) {
			name = "_u";
		}

		// if it's all uppper case, do nothing
		if (name.matches("^[A-Z_]*$")) {
			return name;
		}

		name = getNameUsingModelPropertyNaming(name);

		// for reserved word or word starting with number, append _
		if (isReservedWord(name) || name.matches("^\\d.*")) {
			name = escapeReservedWord(name);
		}

		return name;
	}

	@Override
	public String toParamName(String name) {
		// should be the same as variable name
		return toVarName(name);
	}

	public String getNameUsingModelPropertyNaming(String name) {
		switch (CodegenConstants.MODEL_PROPERTY_NAMING_TYPE.valueOf(getModelPropertyNaming())) {
		case original:
			return name;
		case camelCase:
			return camelize(name, true);
		case PascalCase:
			return camelize(name);
		case snake_case:
			return underscore(name);
		default:
			throw new IllegalArgumentException("Invalid model property naming '" + name
					+ "'. Must be 'original', 'camelCase', " + "'PascalCase' or 'snake_case'");
		}

	}

	@Override
	public CodegenType getTag() {
		return CodegenType.CLIENT;
	}

	@Override
	public String getName() {
		return "scala";
	}

	@Override
	public String getHelp() {
		return "Generates a Scala client library (beta).";
	}

	@Override
	public String toOperationId(String operationId) {
		// throw exception if method name is empty
		if (StringUtils.isEmpty(operationId)) {
			throw new RuntimeException("Empty method name (operationId) not allowed");
		}

		// method name cannot use reserved keyword, e.g. return
		if (isReservedWord(operationId)) {
			throw new RuntimeException(operationId + " (reserved word) cannot be used as method name");
		}

		return camelize(operationId, true);
	}

	@Override
	public String toModelName(final String name) {
		final String sanitizedName = sanitizeName(modelNamePrefix + this.stripPackageName(name) + modelNameSuffix);

		// camelize the model name
		// phone_number => PhoneNumber
		final String camelizedName = camelize(sanitizedName);

		// model name cannot use reserved keyword, e.g. return
		if (isReservedWord(camelizedName)) {
			final String modelName = "Model" + camelizedName;
			LOGGER.warn(camelizedName + " (reserved word) cannot be used as model name. Renamed to " + modelName);
			return modelName;
		}

		// model name starts with number
		if (name.matches("^\\d.*")) {
			final String modelName = "Model" + camelizedName; // e.g. 200Response =>
																// Model200Response (after
																// camelize)
			LOGGER.warn(
					name + " (model name starts with number) cannot be used as model name. Renamed to " + modelName);
			return modelName;
		}

		return camelizedName;
	}

	@Override
	public String getDefaultTemplateDir() {
		return "scala/client";
	}

	@Override
	public String toEnumName(CodegenProperty property) {
		return formatIdentifier(stripPackageName(property.baseName), true);
	}

	@Override
	public String escapeQuotationMark(String input) {
		// remove " to avoid code injection
		return input.replace("\"", "");
	}

}