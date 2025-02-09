package io.swagger.codegen.v3.generators.java;

import com.google.common.collect.Sets;
import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenResponse;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import io.swagger.codegen.v3.generators.AbstractCodegenTest;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.XML;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.junit.rules.TemporaryFolder;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class JavaModelTest extends AbstractCodegenTest {

	private TemporaryFolder folder = new TemporaryFolder();

	@Test(description = "convert a simple java model")
	public void simpleModelTest() {
		final Schema model = new Schema().description("a sample model")
				.addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
				.addProperties("name", new StringSchema().example("Tony"))
				.addProperties("createdAt", new DateTimeSchema()).addRequiredItem("id").addRequiredItem("name");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", model);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.vars.size(), 3);

		final List<CodegenProperty> vars = cm.vars;

		final CodegenProperty property1 = vars.get(0);
		Assert.assertEquals(property1.baseName, "id");
		Assert.assertEquals(property1.getter, "getId");
		Assert.assertEquals(property1.setter, "setId");
		Assert.assertEquals(property1.datatype, "Long");
		Assert.assertEquals(property1.name, "id");
		Assert.assertEquals(property1.defaultValue, "null");
		Assert.assertEquals(property1.baseType, "Long");
		Assert.assertTrue(getBooleanValue(property1, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertTrue(property1.required);
		Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

		final CodegenProperty property2 = vars.get(1);
		Assert.assertEquals(property2.baseName, "name");
		Assert.assertEquals(property2.getter, "getName");
		Assert.assertEquals(property2.setter, "setName");
		Assert.assertEquals(property2.datatype, "String");
		Assert.assertEquals(property2.name, "name");
		Assert.assertEquals(property2.defaultValue, "null");
		Assert.assertEquals(property2.baseType, "String");
		Assert.assertEquals(property2.example, "Tony");
		Assert.assertTrue(getBooleanValue(property2, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertTrue(property2.required);
		Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

		final CodegenProperty property3 = vars.get(2);
		Assert.assertEquals(property3.baseName, "createdAt");
		Assert.assertEquals(property3.getter, "getCreatedAt");
		Assert.assertEquals(property3.setter, "setCreatedAt");
		Assert.assertEquals(property3.datatype, "Date");
		Assert.assertEquals(property3.name, "createdAt");
		Assert.assertEquals(property3.defaultValue, "null");
		Assert.assertEquals(property3.baseType, "Date");
		Assert.assertFalse(getBooleanValue(property3, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertFalse(property3.required);
		Assert.assertTrue(getBooleanValue(property3, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model with list property")
	public void listPropertyTest() {
		final Schema schema = new Schema().description("a sample model")
				.addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
				.addProperties("urls", new ArraySchema().items(new StringSchema())).addRequiredItem("id");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.vars.size(), 2);

		final CodegenProperty property = cm.vars.get(1);
		Assert.assertEquals(property.baseName, "urls");
		Assert.assertEquals(property.getter, "getUrls");
		Assert.assertEquals(property.setter, "setUrls");
		Assert.assertEquals(property.datatype, "List<String>");
		Assert.assertEquals(property.name, "urls");
		Assert.assertEquals(property.defaultValue, "new ArrayList<String>()");
		Assert.assertEquals(property.baseType, "List");
		Assert.assertEquals(property.containerType, "array");
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model with a map property")
	public void mapPropertyTest() {
		final Schema schema = new Schema().description("a sample model")
				.addProperties("translations", new MapSchema().additionalProperties(new StringSchema()))
				.addRequiredItem("id");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "translations");
		Assert.assertEquals(property.getter, "getTranslations");
		Assert.assertEquals(property.setter, "setTranslations");
		Assert.assertEquals(property.datatype, "Map<String, String>");
		Assert.assertEquals(property.name, "translations");
		Assert.assertEquals(property.defaultValue, "new HashMap<String, String>()");
		Assert.assertEquals(property.baseType, "Map");
		Assert.assertEquals(property.containerType, "map");
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model with a map with complex list property")
	public void mapWithListPropertyTest() {
		final Schema schema = new Schema().description("a sample model")
				.addProperties("translations",
						new MapSchema().additionalProperties(new ArraySchema().items(new Schema().$ref("Pet"))))
				.addRequiredItem("id");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "translations");
		Assert.assertEquals(property.getter, "getTranslations");
		Assert.assertEquals(property.setter, "setTranslations");
		Assert.assertEquals(property.datatype, "Map<String, List<Pet>>");
		Assert.assertEquals(property.name, "translations");
		Assert.assertEquals(property.defaultValue, "new HashMap<String, List<Pet>>()");
		Assert.assertEquals(property.baseType, "Map");
		Assert.assertEquals(property.containerType, "map");
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model with a 2D list property")
	public void list2DPropertyTest() {
		final Schema model = new Schema().name("sample").addProperties("list2D",
				new ArraySchema().items(new ArraySchema().items(new Schema().$ref("Pet"))));
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", model);

		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "list2D");
		Assert.assertEquals(property.getter, "getList2D");
		Assert.assertEquals(property.setter, "setList2D");
		Assert.assertEquals(property.datatype, "List<List<Pet>>");
		Assert.assertEquals(property.name, "list2D");
		Assert.assertEquals(property.defaultValue, "new ArrayList<List<Pet>>()");
		Assert.assertEquals(property.baseType, "List");
		Assert.assertEquals(property.containerType, "array");
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model with complex properties")
	public void complexPropertiesTest() {
		final Schema schema = new Schema().description("a sample model").addProperties("children",
				new Schema().$ref("#/components/schemas/Children"));
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "children");
		Assert.assertEquals(property.getter, "getChildren");
		Assert.assertEquals(property.setter, "setChildren");
		Assert.assertEquals(property.datatype, "Children");
		Assert.assertEquals(property.name, "children");
		Assert.assertEquals(property.defaultValue, "null");
		Assert.assertEquals(property.baseType, "Children");
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model with complex list property")
	public void complexListPropertyTest() {
		final Schema schema = new Schema().description("a sample model").addProperties("children",
				new ArraySchema().items(new Schema().$ref("#/components/schemas/Children")));
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "children");
		Assert.assertEquals(property.complexType, "Children");
		Assert.assertEquals(property.getter, "getChildren");
		Assert.assertEquals(property.setter, "setChildren");
		Assert.assertEquals(property.datatype, "List<Children>");
		Assert.assertEquals(property.name, "children");
		Assert.assertEquals(property.defaultValue, "new ArrayList<Children>()");
		Assert.assertEquals(property.baseType, "List");
		Assert.assertEquals(property.containerType, "array");
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model with complex map property")
	public void complexMapPropertyTest() {
		final Schema schema = new Schema().description("a sample model").addProperties("children",
				new MapSchema().additionalProperties(new Schema().$ref("#/components/schemas/Children")));
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.vars.size(), 1);
		Assert.assertEquals(Sets.intersection(cm.imports, Sets.newHashSet("Map", "List", "Children")).size(), 3);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "children");
		Assert.assertEquals(property.complexType, "Children");
		Assert.assertEquals(property.getter, "getChildren");
		Assert.assertEquals(property.setter, "setChildren");
		Assert.assertEquals(property.datatype, "Map<String, Children>");
		Assert.assertEquals(property.name, "children");
		Assert.assertEquals(property.defaultValue, "new HashMap<String, Children>()");
		Assert.assertEquals(property.baseType, "Map");
		Assert.assertEquals(property.containerType, "map");
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
		Assert.assertFalse(getBooleanValue(property, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

	}

	@Test(description = "convert a model with an array property with item name")
	public void arrayModelWithItemNameTest() {
		final Schema propertySchema = new ArraySchema().items(new Schema().$ref("#/components/schemas/Child"))
				.description("an array property");
		propertySchema.addExtension("x-item-name", "child");
		final Schema schema = new Schema().type("object").description("a sample model").addProperties("children",
				propertySchema);

		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.vars.size(), 1);
		Assert.assertEquals(Sets.intersection(cm.imports, Sets.newHashSet("List", "Child")).size(), 2);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "children");
		Assert.assertEquals(property.complexType, "Child");
		Assert.assertEquals(property.getter, "getChildren");
		Assert.assertEquals(property.setter, "setChildren");
		Assert.assertEquals(property.datatype, "List<Child>");
		Assert.assertEquals(property.name, "children");
		Assert.assertEquals(property.defaultValue, "new ArrayList<Child>()");
		Assert.assertEquals(property.baseType, "List");
		Assert.assertEquals(property.containerType, "array");
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
		Assert.assertFalse(getBooleanValue(property, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

		final CodegenProperty itemsProperty = property.items;
		Assert.assertEquals(itemsProperty.baseName, "child");
		Assert.assertEquals(itemsProperty.name, "child");
	}

	@Test(description = "convert an array model")
	public void arrayModelTest() {
		final Schema schema = new ArraySchema()
				.items(new Schema().name("elobjeto").$ref("#/components/schemas/Children")).name("arraySchema")
				.description("an array model");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "an array model");
		Assert.assertEquals(cm.vars.size(), 0);
		Assert.assertEquals(cm.parent, "ArrayList<Children>");
		Assert.assertEquals(cm.imports.size(), 4);
		Assert.assertEquals(
				Sets.intersection(cm.imports, Sets.newHashSet("Schema", "List", "ArrayList", "Children")).size(), 4);
	}

	@Test(description = "convert an array model")
	public void arrayModelTestUsingOas2() {
		final Schema schema = new ArraySchema()
				.items(new Schema().name("elobjeto").$ref("#/components/schemas/Children")).name("arraySchema")
				.description("an array model");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		codegen.setUseOas2(true);
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "an array model");
		Assert.assertEquals(cm.vars.size(), 0);
		Assert.assertEquals(cm.parent, "ArrayList<Children>");
		Assert.assertEquals(cm.imports.size(), 4);
		Assert.assertEquals(
				Sets.intersection(cm.imports, Sets.newHashSet("ApiModel", "List", "ArrayList", "Children")).size(), 4);
	}

	@Test(description = "convert an map model")
	public void mapModelTest() {
		final Schema schema = new MapSchema().description("an map model")
				.additionalProperties(new Schema().$ref("#/components/schemas/Children"));
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "an map model");
		Assert.assertEquals(cm.vars.size(), 0);
		Assert.assertEquals(cm.parent, "HashMap<String, Children>");
		Assert.assertEquals(cm.imports.size(), 4);
		Assert.assertEquals(
				Sets.intersection(cm.imports, Sets.newHashSet("Schema", "Map", "HashMap", "Children")).size(), 4);
	}

	@Test(description = "convert an map model")
	public void mapModelTestUsingOas2() {
		final Schema schema = new MapSchema().description("an map model")
				.additionalProperties(new Schema().$ref("#/components/schemas/Children"));
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		codegen.setUseOas2(true);
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "an map model");
		Assert.assertEquals(cm.vars.size(), 0);
		Assert.assertEquals(cm.parent, "HashMap<String, Children>");
		Assert.assertEquals(cm.imports.size(), 4);
		Assert.assertEquals(
				Sets.intersection(cm.imports, Sets.newHashSet("ApiModel", "Map", "HashMap", "Children")).size(), 4);
	}

	@Test(description = "convert a model with upper-case property names")
	public void upperCaseNamesTest() {
		final Schema schema = new Schema().description("a model with upper-case property names")
				.addProperties("NAME", new StringSchema()).addRequiredItem("NAME");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "NAME");
		Assert.assertEquals(property.getter, "getNAME");
		Assert.assertEquals(property.setter, "setNAME");
		Assert.assertEquals(property.datatype, "String");
		Assert.assertEquals(property.name, "NAME");
		Assert.assertEquals(property.defaultValue, "null");
		Assert.assertEquals(property.baseType, "String");
		Assert.assertFalse(getBooleanValue(property, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertTrue(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model with a 2nd char upper-case property names")
	public void secondCharUpperCaseNamesTest() {
		final Schema schema = new Schema().description("a model with a 2nd char upper-case property names")
				.addProperties("pId", new StringSchema()).addRequiredItem("pId");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "pId");
		Assert.assertEquals(property.getter, "getPId");
		Assert.assertEquals(property.setter, "setPId");
		Assert.assertEquals(property.datatype, "String");
		Assert.assertEquals(property.name, "pId");
		Assert.assertEquals(property.defaultValue, "null");
		Assert.assertEquals(property.baseType, "String");
		Assert.assertFalse(getBooleanValue(property, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertTrue(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a model starting with two upper-case letter property names")
	public void firstTwoUpperCaseLetterNamesTest() {
		final Schema schema = new Schema()
				.description("a model with a property name starting with two upper-case letters")
				.addProperties("ATTName", new StringSchema()).addRequiredItem("ATTName");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "ATTName");
		Assert.assertEquals(property.getter, "getAtTName");
		Assert.assertEquals(property.setter, "setAtTName");
		Assert.assertEquals(property.datatype, "String");
		Assert.assertEquals(property.name, "atTName");
		Assert.assertEquals(property.defaultValue, "null");
		Assert.assertEquals(property.baseType, "String");
		Assert.assertFalse(getBooleanValue(property, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertTrue(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert hyphens per issue 503")
	public void hyphensTest() {
		final Schema schema = new Schema().description("a sample model").addProperties("created-at",
				new DateTimeSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "created-at");
		Assert.assertEquals(property.getter, "getCreatedAt");
		Assert.assertEquals(property.setter, "setCreatedAt");
		Assert.assertEquals(property.name, "createdAt");
	}

	@Test(description = "convert query[password] to queryPassword")
	public void squareBracketsTest() {
		final Schema schema = new Schema().description("a sample model").addProperties("query[password]",
				new StringSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "query[password]");
		Assert.assertEquals(property.getter, "getQueryPassword");
		Assert.assertEquals(property.setter, "setQueryPassword");
		Assert.assertEquals(property.name, "queryPassword");
	}

	@Test(description = "properly escape names per 567")
	public void escapeNamesTest() {
		final Schema schema = new Schema().description("a sample model").addProperties("created-at",
				new DateTimeSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("with.dots", schema);

		Assert.assertEquals(cm.classname, "WithDots");
	}

	@Test(description = "convert a model with binary data")
	public void binaryDataTest() {
		final Schema schema = new Schema().description("model with binary").addProperties("inputBinaryData",
				new ByteArraySchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "inputBinaryData");
		Assert.assertEquals(property.getter, "getInputBinaryData");
		Assert.assertEquals(property.setter, "setInputBinaryData");
		Assert.assertEquals(property.datatype, "byte[]");
		Assert.assertEquals(property.name, "inputBinaryData");
		Assert.assertEquals(property.defaultValue, "null");
		Assert.assertEquals(property.baseType, "byte[]");
		Assert.assertFalse(getBooleanValue(property, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertFalse(property.required);
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
	}

	@Test(description = "translate an invalid param name")
	public void invalidParamNameTest() {
		final Schema schema = new Schema().description("a model with a 2nd char upper-case property names")
				.addProperties("_", new StringSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.vars.size(), 1);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, "_");
		Assert.assertEquals(property.getter, "getU");
		Assert.assertEquals(property.setter, "setU");
		Assert.assertEquals(property.datatype, "String");
		Assert.assertEquals(property.name, "u");
		Assert.assertEquals(property.defaultValue, "null");
		Assert.assertEquals(property.baseType, "String");
		Assert.assertFalse(getBooleanValue(property, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
	}

	@Test(description = "convert a parameter")
	public void convertParameterTest() {
		final Parameter parameter = new QueryParameter().name("limit").required(true);
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenParameter cm = codegen.fromParameter(parameter, null);

		Assert.assertNull(cm.allowableValues);
	}

	@Test(description = "types used by inner properties should be imported")
	public void mapWithAnListOfBigDecimalTest() {
		final CodegenModel cm1 = new JavaClientCodegen().fromModel("sample",
				new Schema().description("model with Map<String, List<BigDecimal>>").addProperties("map",
						new MapSchema().additionalProperties(new ArraySchema().items(new NumberSchema()))));
		Assert.assertEquals(cm1.vars.get(0).datatype, "Map<String, List<BigDecimal>>");
		Assert.assertTrue(cm1.imports.contains("BigDecimal"));

		final CodegenModel cm2 = new JavaClientCodegen().fromModel("sample",
				new Schema().description("model with Map<String, Map<String, List<BigDecimal>>>").addProperties("map",
						new MapSchema().additionalProperties(
								new MapSchema().additionalProperties(new ArraySchema().items(new NumberSchema())))));
		Assert.assertEquals(cm2.vars.get(0).datatype, "Map<String, Map<String, List<BigDecimal>>>");
		Assert.assertTrue(cm2.imports.contains("BigDecimal"));
	}

	@DataProvider(name = "modelNames")
	public static Object[][] primeNumbers() {
		return new Object[][] { { "sample", "Sample" }, { "sample_name", "SampleName" },
				{ "sample__name", "SampleName" }, { "/sample", "Sample" }, { "\\sample", "Sample" },
				{ "sample.name", "SampleName" }, { "_sample", "Sample" }, { "Sample", "Sample" }, };
	}

	@Test(dataProvider = "modelNames", description = "avoid inner class")
	public void modelNameTest(String name, String expectedName) {
		final Schema schema = new Schema();
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel(name, schema);

		Assert.assertEquals(cm.name, name);
		Assert.assertEquals(cm.classname, expectedName);
	}

	@DataProvider(name = "classProperties")
	public static Object[][] classProperties() {
		return new Object[][] { { "class", "getPropertyClass", "setPropertyClass", "propertyClass" },
				{ "_class", "getPropertyClass", "setPropertyClass", "propertyClass" },
				{ "__class", "getPropertyClass", "setPropertyClass", "propertyClass" } };
	}

	@Test(dataProvider = "classProperties", description = "handle 'class' properties")
	public void classPropertyTest(String baseName, String getter, String setter, String name) {
		final Schema schema = new Schema().description("a sample model").addProperties(baseName, new StringSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		final CodegenProperty property = cm.vars.get(0);
		Assert.assertEquals(property.baseName, baseName);
		Assert.assertEquals(property.getter, getter);
		Assert.assertEquals(property.setter, setter);
		Assert.assertEquals(property.name, name);
	}

	@Test(description = "test models with xml")
	public void modelWithXmlTest() {
		final Schema schema = new Schema().description("a sample model")
				.xml(new XML().prefix("my").namespace("xmlNamespace").name("customXmlName"))
				.addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
				.addProperties("name",
						new StringSchema().example("Tony").xml(new XML().attribute(true).prefix("my").name("myName")))
				.addProperties("createdAt",
						new DateTimeSchema().xml(new XML().prefix("my").namespace("myNamespace").name("myCreatedAt")))
				.addRequiredItem("id").addRequiredItem("name");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.xmlPrefix, "my");
		Assert.assertEquals(cm.xmlName, "customXmlName");
		Assert.assertEquals(cm.xmlNamespace, "xmlNamespace");
		Assert.assertEquals(cm.vars.size(), 3);

		final List<CodegenProperty> vars = cm.vars;

		final CodegenProperty property2 = vars.get(1);
		Assert.assertEquals(property2.baseName, "name");
		Assert.assertEquals(property2.getter, "getName");
		Assert.assertEquals(property2.setter, "setName");
		Assert.assertEquals(property2.datatype, "String");
		Assert.assertEquals(property2.name, "name");
		Assert.assertEquals(property2.defaultValue, "null");
		Assert.assertEquals(property2.baseType, "String");
		Assert.assertEquals(property2.example, "Tony");
		Assert.assertTrue(getBooleanValue(property2, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertTrue(property2.required);
		Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
		Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_XML_ATTRIBUTE_EXT_NAME));
		Assert.assertEquals(property2.xmlName, "myName");
		Assert.assertNull(property2.xmlNamespace);

		final CodegenProperty property3 = vars.get(2);
		Assert.assertEquals(property3.baseName, "createdAt");
		Assert.assertEquals(property3.getter, "getCreatedAt");
		Assert.assertEquals(property3.setter, "setCreatedAt");
		Assert.assertEquals(property3.datatype, "Date");
		Assert.assertEquals(property3.name, "createdAt");
		Assert.assertEquals(property3.defaultValue, "null");
		Assert.assertEquals(property3.baseType, "Date");
		Assert.assertFalse(getBooleanValue(property3, CodegenConstants.HAS_MORE_EXT_NAME));
		Assert.assertFalse(property3.required);
		Assert.assertTrue(getBooleanValue(property3, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
		Assert.assertFalse(getBooleanValue(property3, CodegenConstants.IS_XML_ATTRIBUTE_EXT_NAME));
		Assert.assertEquals(property3.xmlName, "myCreatedAt");
		Assert.assertEquals(property3.xmlNamespace, "myNamespace");
		Assert.assertEquals(property3.xmlPrefix, "my");
	}

	@Test(description = "test models with wrapped xml")
	public void modelWithWrappedXmlTest() {
		final Schema schema = new Schema().description("a sample model")
				.xml(new XML().prefix("my").namespace("xmlNamespace").name("customXmlName"))
				.addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
				.addProperties("array",
						new ArraySchema().items(new StringSchema().xml(new XML().name("i")))
								.xml(new XML().prefix("my").wrapped(true).namespace("myNamespace").name("xmlArray")))
				.addRequiredItem("id");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenModel cm = codegen.fromModel("sample", schema);

		Assert.assertEquals(cm.name, "sample");
		Assert.assertEquals(cm.classname, "Sample");
		Assert.assertEquals(cm.description, "a sample model");
		Assert.assertEquals(cm.xmlPrefix, "my");
		Assert.assertEquals(cm.xmlName, "customXmlName");
		Assert.assertEquals(cm.xmlNamespace, "xmlNamespace");
		Assert.assertEquals(cm.vars.size(), 2);

		final List<CodegenProperty> vars = cm.vars;

		final CodegenProperty property2 = vars.get(1);
		Assert.assertEquals(property2.baseName, "array");
		Assert.assertEquals(property2.getter, "getArray");
		Assert.assertEquals(property2.setter, "setArray");
		Assert.assertEquals(property2.datatype, "List<String>");
		Assert.assertEquals(property2.name, "array");
		Assert.assertEquals(property2.defaultValue, "new ArrayList<String>()");
		Assert.assertEquals(property2.baseType, "List");
		Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_CONTAINER_EXT_NAME));
		Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_XML_WRAPPED_EXT_NAME));
		Assert.assertEquals(property2.xmlName, "xmlArray");
		Assert.assertNotNull(property2.xmlNamespace);
		Assert.assertNotNull(property2.items);
		CodegenProperty items = property2.items;
		Assert.assertEquals(items.xmlName, "i");
		Assert.assertEquals(items.baseName, "array");
	}

	@Test(description = "convert a boolean parameter")
	public void booleanPropertyTest() {
		final BooleanSchema property = new BooleanSchema();
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenProperty cp = codegen.fromProperty("property", property);

		Assert.assertEquals(cp.baseName, "property");
		Assert.assertEquals(cp.datatype, "Boolean");
		Assert.assertEquals(cp.name, "property");
		Assert.assertEquals(cp.baseType, "Boolean");
		Assert.assertTrue(getBooleanValue(cp, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
		Assert.assertTrue(getBooleanValue(cp, CodegenConstants.IS_BOOLEAN_EXT_NAME));
		Assert.assertEquals(cp.getter, "isProperty");
	}

	@Test(description = "convert an integer property")
	public void integerPropertyTest() {
		final IntegerSchema property = new IntegerSchema();
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenProperty cp = codegen.fromProperty("property", property);

		Assert.assertEquals(cp.baseName, "property");
		Assert.assertEquals(cp.datatype, "Integer");
		Assert.assertEquals(cp.name, "property");
		Assert.assertEquals(cp.baseType, "Integer");
		Assert.assertTrue(getBooleanValue(cp, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
		Assert.assertTrue(getBooleanValue(cp, CodegenConstants.IS_INTEGER_EXT_NAME));
		Assert.assertFalse(getBooleanValue(cp, CodegenConstants.IS_LONG_EXT_NAME));
		Assert.assertEquals(cp.getter, "getProperty");
	}

	@Test(description = "convert a long property")
	public void longPropertyTest() {
		final IntegerSchema property = new IntegerSchema().format("int64");
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		final CodegenProperty cp = codegen.fromProperty("property", property);

		Assert.assertEquals(cp.baseName, "property");
		Assert.assertEquals(cp.datatype, "Long");
		Assert.assertEquals(cp.name, "property");
		Assert.assertEquals(cp.baseType, "Long");
		Assert.assertTrue(getBooleanValue(cp, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
		Assert.assertTrue(getBooleanValue(cp, CodegenConstants.IS_LONG_EXT_NAME));
		Assert.assertFalse(getBooleanValue(cp, CodegenConstants.IS_INTEGER_EXT_NAME));
		Assert.assertEquals(cp.getter, "getProperty");
	}

	@Test(description = "convert a long property in a referenced schema")
	public void longPropertyInReferencedSchemaTest() {
		final IntegerSchema longProperty = new IntegerSchema().format("int64");
		final Schema TestSchema = new ObjectSchema()
				.addProperties("Long1", new Schema<>().$ref("#/components/schemas/LongProperty"))
				.addProperties("Long2", new IntegerSchema().format("int64"));
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final Map<String, Schema> allDefinitions = Collections.singletonMap("LongProperty", longProperty);
		final CodegenModel cm = codegen.fromModel("test", TestSchema, allDefinitions);

		Assert.assertEquals(cm.vars.size(), 2);

		CodegenProperty cp1 = cm.vars.get(0);
		Assert.assertEquals(cp1.baseName, "Long1");
		Assert.assertEquals(cp1.datatype, "Long");
		Assert.assertEquals(cp1.name, "long1");
		Assert.assertEquals(cp1.baseType, "Long");
		Assert.assertEquals(cp1.getter, "getLong1");

		CodegenProperty cp2 = cm.vars.get(1);
		Assert.assertEquals(cp2.baseName, "Long2");
		Assert.assertEquals(cp2.datatype, "Long");
		Assert.assertEquals(cp2.name, "long2");
		Assert.assertEquals(cp2.baseType, "Long");
		Assert.assertEquals(cp2.getter, "getLong2");
	}

	@Test(description = "convert an integer property in a referenced schema")
	public void integerPropertyInReferencedSchemaTest() {
		final IntegerSchema longProperty = new IntegerSchema().format("int32");
		final Schema testSchema = new ObjectSchema()
				.addProperties("Integer1", new Schema<>().$ref("#/components/schemas/IntegerProperty"))
				.addProperties("Integer2", new IntegerSchema().format("int32"));
		final Map<String, Schema> allDefinitions = Collections.singletonMap("IntegerProperty", longProperty);
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("test", testSchema, allDefinitions);

		Assert.assertEquals(cm.vars.size(), 2);

		CodegenProperty cp1 = cm.vars.get(0);
		Assert.assertEquals(cp1.baseName, "Integer1");
		Assert.assertEquals(cp1.datatype, "Integer");
		Assert.assertEquals(cp1.name, "integer1");
		Assert.assertEquals(cp1.baseType, "Integer");
		Assert.assertEquals(cp1.getter, "getInteger1");

		CodegenProperty cp2 = cm.vars.get(1);
		Assert.assertEquals(cp2.baseName, "Integer2");
		Assert.assertEquals(cp2.datatype, "Integer");
		Assert.assertEquals(cp2.name, "integer2");
		Assert.assertEquals(cp2.baseType, "Integer");
		Assert.assertEquals(cp2.getter, "getInteger2");
	}

	@Test(description = "convert an array schema")
	public void arraySchemaTest() {
		final Schema testSchema = new ObjectSchema().addProperties("pets",
				new ArraySchema().items(new Schema<>().$ref("#/components/schemas/Pet")));
		final Map<String, Schema> allDefinitions = Collections.singletonMap("Pet", new ObjectSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("test", testSchema, allDefinitions);

		Assert.assertEquals(cm.vars.size(), 1);
		CodegenProperty cp1 = cm.vars.get(0);
		Assert.assertEquals(cp1.baseName, "pets");
		Assert.assertEquals(cp1.datatype, "List<Pet>");
		Assert.assertEquals(cp1.name, "pets");
		Assert.assertEquals(cp1.baseType, "List");
		Assert.assertEquals(cp1.getter, "getPets");

		Assert.assertTrue(cm.imports.contains("List"));
		Assert.assertTrue(cm.imports.contains("Pet"));
	}

	@Test(description = "convert an array schema in a RequestBody")
	public void arraySchemaTestInRequestBody() {
		final Schema testSchema = new ArraySchema().items(new Schema<>().$ref("#/components/schemas/Pet"));
		Operation operation = new Operation()
				.requestBody(new RequestBody()
						.content(new Content().addMediaType("application/json", new MediaType().schema(testSchema))))
				.responses(new ApiResponses().addApiResponse("204", new ApiResponse().description("Ok response")));
		final Map<String, Schema> allDefinitions = Collections.singletonMap("Pet", new ObjectSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenOperation co = codegen.fromOperation("testSchema", "GET", operation, allDefinitions);

		Assert.assertEquals(co.bodyParams.size(), 1);
		CodegenParameter cp1 = co.bodyParams.get(0);
		Assert.assertEquals(cp1.baseType, "Pet");
		Assert.assertEquals(cp1.dataType, "List<Pet>");
		Assert.assertEquals(cp1.items.baseType, "Pet");
		Assert.assertEquals(cp1.items.complexType, "Pet");
		Assert.assertEquals(cp1.items.datatype, "List<Pet>");

		Assert.assertEquals(co.responses.size(), 1);

		Assert.assertTrue(co.imports.contains("Pet"));
	}

	@Test(description = "convert an array schema in a ApiResponse")
	public void arraySchemaTestInOperationResponse() {
		final Schema testSchema = new ArraySchema().items(new Schema<>().$ref("#/components/schemas/Pet"));
		Operation operation = new Operation()
				.responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("Ok response")
						.content(new Content().addMediaType("application/json", new MediaType().schema(testSchema)))));
		final Map<String, Schema> allDefinitions = Collections.singletonMap("Pet", new ObjectSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenOperation co = codegen.fromOperation("testSchema", "GET", operation, allDefinitions);

		Assert.assertEquals(co.responses.size(), 1);
		CodegenResponse cr = co.responses.get(0);
		Assert.assertEquals(cr.baseType, "Pet");
		Assert.assertEquals(cr.dataType, "List<Pet>");
		Assert.assertEquals(cr.containerType, "array");

		Assert.assertTrue(co.imports.contains("Pet"));
	}

	@Test(description = "convert a array of array schema")
	public void arrayOfArraySchemaTest() {
		final Schema testSchema = new ObjectSchema().addProperties("pets",
				new ArraySchema().items(new ArraySchema().items(new Schema<>().$ref("#/components/schemas/Pet"))));
		final Map<String, Schema> allDefinitions = Collections.singletonMap("Pet", new ObjectSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenModel cm = codegen.fromModel("test", testSchema, allDefinitions);

		Assert.assertEquals(cm.vars.size(), 1);
		CodegenProperty cp1 = cm.vars.get(0);
		Assert.assertEquals(cp1.baseName, "pets");
		Assert.assertEquals(cp1.datatype, "List<List<Pet>>");
		Assert.assertEquals(cp1.name, "pets");
		Assert.assertEquals(cp1.baseType, "List");
		Assert.assertEquals(cp1.getter, "getPets");

		Assert.assertTrue(cm.imports.contains("List"));
		Assert.assertTrue(cm.imports.contains("Pet"));
	}

	@Test(description = "convert an array of array schema in a RequestBody")
	public void arrayOfArraySchemaTestInRequestBody() {
		final Schema testSchema = new ArraySchema()
				.items(new ArraySchema().items(new Schema<>().$ref("#/components/schemas/Pet")));
		Operation operation = new Operation()
				.requestBody(new RequestBody()
						.content(new Content().addMediaType("application/json", new MediaType().schema(testSchema))))
				.responses(new ApiResponses().addApiResponse("204", new ApiResponse().description("Ok response")));
		final Map<String, Schema> allDefinitions = Collections.singletonMap("Pet", new ObjectSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenOperation co = codegen.fromOperation("testSchema", "GET", operation, allDefinitions);

		Assert.assertEquals(co.bodyParams.size(), 1);
		CodegenParameter cp1 = co.bodyParams.get(0);
		Assert.assertEquals(cp1.baseType, "List");
		Assert.assertEquals(cp1.dataType, "List<List<Pet>>");
		Assert.assertEquals(cp1.items.baseType, "List");
		Assert.assertEquals(cp1.items.complexType, "List");
		Assert.assertEquals(cp1.items.datatype, "List<List<Pet>>");

		Assert.assertEquals(co.responses.size(), 1);

		Assert.assertTrue(co.imports.contains("Pet"));
	}

	@Test(description = "convert a array schema in a ApiResponse")
	public void arrayOfArraySchemaTestInOperationResponse() {
		final Schema testSchema = new ArraySchema()
				.items(new ArraySchema().items(new Schema<>().$ref("#/components/schemas/Pet")));
		Operation operation = new Operation()
				.responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("Ok response")
						.content(new Content().addMediaType("application/json", new MediaType().schema(testSchema)))));
		final Map<String, Schema> allDefinitions = Collections.singletonMap("Pet", new ObjectSchema());
		final DefaultCodegenConfig codegen = new JavaClientCodegen();
		codegen.preprocessOpenAPI(new OpenAPI().components(new Components()));
		final CodegenOperation co = codegen.fromOperation("testSchema", "GET", operation, allDefinitions);

		Assert.assertEquals(co.responses.size(), 1);
		CodegenResponse cr = co.responses.get(0);
		Assert.assertEquals(cr.baseType, "Pet");
		Assert.assertEquals(cr.dataType, "List<List<Pet>>");
		Assert.assertEquals(cr.containerType, "array");

		Assert.assertTrue(co.imports.contains("Pet"));
	}

	@Test
	public void generateModel() throws Exception {
		folder.create();
		final File output = folder.getRoot();

		final CodegenConfigurator configurator = new CodegenConfigurator().setLang("java")
				.setInputSpecURL("src/test/resources/3_0_0/petstore.yaml").setOutputDir(output.getAbsolutePath());

		final ClientOptInput clientOptInput = configurator.toClientOptInput();
		new DefaultGenerator().opts(clientOptInput).generate();

		File orderFile = new File(output, "src/main/java/io/swagger/client/model/Order.java");
		Assert.assertTrue(orderFile.exists());
		folder.delete();
	}

	@Test
	public void generateModelDiscriminatorOrderSchemas() throws Exception {
		TemporaryFolder temporaryFolder = new TemporaryFolder();

		temporaryFolder.create();
		final File output = temporaryFolder.getRoot();

		final CodegenConfigurator configurator = new CodegenConfigurator().setLang("java")
				.setInputSpecURL("src/test/resources/3_0_0/discriminator_order_schemas.yaml")
				.setOutputDir(output.getAbsolutePath());

		final ClientOptInput clientOptInput = configurator.toClientOptInput();
		new DefaultGenerator().opts(clientOptInput).generate();

		File child = new File(output, "src/main/java/io/swagger/client/model/DSubSubSubBase.java");
		Assert.assertTrue(child.exists());
		temporaryFolder.delete();
	}

}
