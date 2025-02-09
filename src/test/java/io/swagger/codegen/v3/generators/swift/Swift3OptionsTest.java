package io.swagger.codegen.v3.generators.swift;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.generators.AbstractOptionsTest;
import io.swagger.codegen.v3.generators.options.Swift3OptionsProvider;
import mockit.Expectations;
import mockit.Tested;

public class Swift3OptionsTest extends AbstractOptionsTest {

	@Tested
	private Swift3Codegen clientCodegen;

	public Swift3OptionsTest() {
		super(new Swift3OptionsProvider());
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
				clientCodegen.setSortParamsByRequiredFlag(Boolean.valueOf(Swift3OptionsProvider.SORT_PARAMS_VALUE));
				times = 1;
				clientCodegen.setProjectName(Swift3OptionsProvider.PROJECT_NAME_VALUE);
				times = 1;
				clientCodegen.setResponseAs(Swift3OptionsProvider.RESPONSE_AS_VALUE.split(","));
				times = 1;
				clientCodegen.setUnwrapRequired(Boolean.valueOf(Swift3OptionsProvider.UNWRAP_REQUIRED_VALUE));
				times = 1;
				clientCodegen.setObjcCompatible(Boolean.valueOf(Swift3OptionsProvider.OBJC_COMPATIBLE_VALUE));
				times = 1;
				clientCodegen.setLenientTypeCast(Boolean.valueOf(Swift3OptionsProvider.LENIENT_TYPE_CAST_VALUE));
				times = 1;
			}
		};
	}

}
