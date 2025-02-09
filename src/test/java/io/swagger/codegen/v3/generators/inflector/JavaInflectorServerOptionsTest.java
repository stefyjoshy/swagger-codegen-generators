package io.swagger.codegen.v3.generators.inflector;

import io.swagger.codegen.v3.generators.AbstractOptionsTest;
import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.generators.java.JavaInflectorServerCodegen;
import io.swagger.codegen.v3.generators.options.JavaInflectorServerOptionsProvider;
import mockit.Expectations;
import mockit.Tested;

public class JavaInflectorServerOptionsTest extends AbstractOptionsTest {

	@Tested
	private JavaInflectorServerCodegen clientCodegen;

	public JavaInflectorServerOptionsTest() {
		super(new JavaInflectorServerOptionsProvider());
	}

	@Override
	protected CodegenConfig getCodegenConfig() {
		return clientCodegen;
	}

	@SuppressWarnings("unused")
	@Override
	protected void setExpectations() {
		new Expectations(clientCodegen) {
			{
				clientCodegen.setModelPackage(JavaInflectorServerOptionsProvider.MODEL_PACKAGE_VALUE);
				times = 1;
				clientCodegen.setApiPackage(JavaInflectorServerOptionsProvider.API_PACKAGE_VALUE);
				times = 1;
				clientCodegen.setSortParamsByRequiredFlag(
						Boolean.valueOf(JavaInflectorServerOptionsProvider.SORT_PARAMS_VALUE));
				times = 1;
				clientCodegen.setInvokerPackage(JavaInflectorServerOptionsProvider.INVOKER_PACKAGE_VALUE);
				times = 1;
				clientCodegen.setGroupId(JavaInflectorServerOptionsProvider.GROUP_ID_VALUE);
				times = 1;
				clientCodegen.setArtifactId(JavaInflectorServerOptionsProvider.ARTIFACT_ID_VALUE);
				times = 1;
				clientCodegen.setArtifactVersion(JavaInflectorServerOptionsProvider.ARTIFACT_VERSION_VALUE);
				times = 1;
				clientCodegen.setSourceFolder(JavaInflectorServerOptionsProvider.SOURCE_FOLDER_VALUE);
				times = 1;
				clientCodegen.setLocalVariablePrefix(JavaInflectorServerOptionsProvider.LOCAL_PREFIX_VALUE);
				times = 1;
				clientCodegen.setSerializableModel(
						Boolean.valueOf(JavaInflectorServerOptionsProvider.SERIALIZABLE_MODEL_VALUE));
				times = 1;
				clientCodegen.setFullJavaUtil(Boolean.valueOf(JavaInflectorServerOptionsProvider.FULL_JAVA_UTIL_VALUE));
				times = 1;
				clientCodegen.setSerializeBigDecimalAsString(true);
				times = 1;
				clientCodegen.setUseOas2(true);
				times = 1;
			}
		};
	}

}
