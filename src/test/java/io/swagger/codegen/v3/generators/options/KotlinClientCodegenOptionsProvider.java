package io.swagger.codegen.v3.generators.options;

import com.google.common.collect.ImmutableMap;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.generators.kotlin.KotlinClientCodegen;

import java.util.Map;

public class KotlinClientCodegenOptionsProvider implements OptionsProvider {

	public static final String PACKAGE_NAME_VALUE = "io.swagger.tests.kotlin";

	public static final String ARTIFACT_VERSION_VALUE = "0.0.1-SNAPSHOT";

	public static final String ARTIFACT_ID = "swagger-kotlin-test";

	public static final String GROUP_ID = "io.swagger.tests";

	public static final String SOURCE_FOLDER = "./generated/kotlin";

	public static final String DATE_LIBRARY = KotlinClientCodegen.DateLibrary.JAVA8.value;

	@Override
	public String getLanguage() {
		return "kotlin";
	}

	@Override
	public Map<String, String> createOptions() {
		ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
		return builder.put(CodegenConstants.PACKAGE_NAME, PACKAGE_NAME_VALUE)
				.put(CodegenConstants.ARTIFACT_VERSION, ARTIFACT_VERSION_VALUE)
				.put(CodegenConstants.ARTIFACT_ID, ARTIFACT_ID).put(CodegenConstants.GROUP_ID, GROUP_ID)
				.put(CodegenConstants.SOURCE_FOLDER, SOURCE_FOLDER).put(KotlinClientCodegen.DATE_LIBRARY, DATE_LIBRARY)
				.build();
	}

	@Override
	public boolean isServer() {
		return false;
	}

}
