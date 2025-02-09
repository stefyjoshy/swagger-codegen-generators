package io.swagger.codegen.v3.generators.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import io.swagger.codegen.v3.VendorExtendable;

import java.io.IOException;

import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public abstract class NoneExtensionHelper implements Helper<VendorExtendable> {

	public abstract String getPreffix();

	@Override
	public Object apply(VendorExtendable vendor, Options options) throws IOException {
		final Options.Buffer buffer = options.buffer();
		if (vendor == null) {
			buffer.append(options.fn());
			return buffer;
		}
		final String param = options.param(0);
		String extension = getPreffix() + param;

		if (!getBooleanValue(vendor, extension)) {
			buffer.append(options.fn());
		}
		else {
			buffer.append(options.inverse());
		}
		return buffer;
	}

}
