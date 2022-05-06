/*
 * Copyright 2017 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.logate.openapi2asciidoc.swagger2markup;

import com.logate.openapi2asciidoc.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import com.logate.openapi2asciidoc.swagger2markup.builder.Swagger2MarkupExtensionRegistryBuilder;
import com.logate.openapi2asciidoc.swagger2markup.core.AbstractSchema2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.core.Labels;
import com.logate.openapi2asciidoc.swagger2markup.core.utils.URIUtils;
import com.logate.openapi2asciidoc.swagger2markup.internal.document.DefinitionsDocument;
import com.logate.openapi2asciidoc.swagger2markup.internal.document.OverviewDocument;
import com.logate.openapi2asciidoc.swagger2markup.internal.document.PathsDocument;
import com.logate.openapi2asciidoc.swagger2markup.internal.document.SecurityDocument;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.LineSeparator;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilders;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupLanguage;
import com.logate.openapi2asciidoc.swagger2markup.spi.Swagger2MarkupExtensionRegistry;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


/**
 * @author Robert Winkler
 */
public class Swagger2MarkupConverter extends AbstractSchema2MarkupConverter<Swagger> {
    private final OverviewDocument overviewDocument;
    private final PathsDocument pathsDocument;
    private final DefinitionsDocument definitionsDocument;
    private final SecurityDocument securityDocument;
    private final SwaggerContext swaggerContext;

    public Swagger2MarkupConverter(SwaggerContext swaggerContext) {
        super(swaggerContext);
        this.swaggerContext = swaggerContext;
        this.overviewDocument = new OverviewDocument(swaggerContext);
        this.pathsDocument = new PathsDocument(swaggerContext);
        this.definitionsDocument = new DefinitionsDocument(swaggerContext);
        this.securityDocument = new SecurityDocument(swaggerContext);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a URI.
     *
     * @param swaggerUri the URI
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(URI swaggerUri) {
        Validate.notNull(swaggerUri, "swaggerUri must not be null");
        String scheme = swaggerUri.getScheme();
        if (scheme != null && swaggerUri.getScheme().startsWith("http")) {
            try {
                return from(swaggerUri.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to convert URI to URL", e);
            }
        } else if (scheme != null && swaggerUri.getScheme().startsWith("file")) {
            return from(Paths.get(swaggerUri));
        } else {
            return from(URIUtils.convertUriWithoutSchemeToFileScheme(swaggerUri));
        }
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder using a remote URL.
     *
     * @param swaggerURL the remote URL
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(URL swaggerURL) {
        Validate.notNull(swaggerURL, "swaggerURL must not be null");
        return new Builder(swaggerURL);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder using a local Path.
     *
     * @param swaggerPath the local Path
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Path swaggerPath) {
        Validate.notNull(swaggerPath, "swaggerPath must not be null");
        if (Files.notExists(swaggerPath)) {
            throw new IllegalArgumentException(String.format("swaggerPath does not exist: %s", swaggerPath));
        }
        try {
            if (Files.isHidden(swaggerPath)) {
                throw new IllegalArgumentException("swaggerPath must not be a hidden file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to check if swaggerPath is a hidden file", e);
        }
        return new Builder(swaggerPath);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger model.
     *
     * @param swagger the Swagger source.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Swagger swagger) {
        Validate.notNull(swagger, "swagger must not be null");
        return new Builder(swagger);
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger YAML or JSON String.
     *
     * @param swaggerString the Swagger YAML or JSON String.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(String swaggerString) {
        Validate.notEmpty(swaggerString, "swaggerString must not be null");
        return from(new StringReader(swaggerString));
    }

    /**
     * Creates a Swagger2MarkupConverter.Builder from a given Swagger YAML or JSON reader.
     *
     * @param swaggerReader the Swagger YAML or JSON reader.
     * @return a Swagger2MarkupConverter
     */
    public static Builder from(Reader swaggerReader) {
        Validate.notNull(swaggerReader, "swaggerReader must not be null");
        Swagger swagger;
        try {
            swagger = new SwaggerParser().parse(IOUtils.toString(swaggerReader));
        } catch (IOException e) {
            throw new RuntimeException("Swagger source can not be parsed", e);
        }
        if (swagger == null)
            throw new IllegalArgumentException("Swagger source is in a wrong format");

        return new Builder(swagger);
    }

    /**
     * Returns the global Context
     *
     * @return the global Context
     */
    public SwaggerContext getContext() {
        return swaggerContext;
    }

    /**
     * Converts the Swagger specification into the given {@code outputDirectory}.
     *
     * @param outputDirectory the output directory path
     */
    public void toFolder(Path outputDirectory) {
        Validate.notNull(outputDirectory, "outputDirectory must not be null");

        swaggerContext.setOutputPath(outputDirectory);

        applyOverviewDocument()
                .writeToFile(outputDirectory.resolve(swaggerContext.config.getOverviewDocument()), StandardCharsets.UTF_8);
        applyPathsDocument()
                .writeToFile(outputDirectory.resolve(swaggerContext.config.getPathsDocument()), StandardCharsets.UTF_8);
        applyDefinitionsDocument()
                .writeToFile(outputDirectory.resolve(swaggerContext.config.getDefinitionsDocument()), StandardCharsets.UTF_8);
        applySecurityDocument()
                .writeToFile(outputDirectory.resolve(swaggerContext.config.getSecurityDocument()), StandardCharsets.UTF_8);
    }

    private MarkupDocBuilder applyOverviewDocument() {
        return overviewDocument.apply(
                swaggerContext.createMarkupDocBuilder(),
                OverviewDocument.parameters(swaggerContext.getSchema()));
    }

    private MarkupDocBuilder applyPathsDocument() {
        return pathsDocument.apply(
                swaggerContext.createMarkupDocBuilder(),
                PathsDocument.parameters(swaggerContext.getSchema().getPaths()));
    }

    private MarkupDocBuilder applyDefinitionsDocument() {
        return definitionsDocument.apply(
                swaggerContext.createMarkupDocBuilder(),
                DefinitionsDocument.parameters(swaggerContext.getSchema().getDefinitions()));
    }

    private MarkupDocBuilder applySecurityDocument() {
        return securityDocument.apply(
                swaggerContext.createMarkupDocBuilder(),
                SecurityDocument.parameters(swaggerContext.getSchema().getSecurityDefinitions()));
    }

    /**
     * Converts the Swagger specification into the {@code outputPath} which can be either a directory (e.g /tmp) or a file without extension (e.g /tmp/swagger).
     * Internally the method invokes either {@code toFolder} or {@code toFile}. If the {@code outputPath} is a directory, the directory must exist.
     * Otherwise it cannot be determined if the {@code outputPath} is a directory or not.
     *
     * @param outputPath the output path
     */
    public void toPath(Path outputPath) {
        Validate.notNull(outputPath, "outputPath must not be null");
        if (Files.isDirectory(outputPath)) {
            toFolder(outputPath);
        } else {
            toFile(outputPath);
        }
    }

    /**
     * Converts the Swagger specification the given {@code outputFile}.<br>
     * An extension identifying the markup language will be automatically added to file name.
     *
     * @param outputFile the output file
     */
    public void toFile(Path outputFile) {
        Validate.notNull(outputFile, "outputFile must not be null");

        applyOverviewDocument().writeToFile(outputFile, StandardCharsets.UTF_8);
        applyPathsDocument().writeToFile(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        applyDefinitionsDocument().writeToFile(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        applySecurityDocument().writeToFile(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    /**
     * Converts the Swagger specification the given {@code outputFile}.
     *
     * @param outputFile the output file
     */
    public void toFileWithoutExtension(Path outputFile) {
        Validate.notNull(outputFile, "outputFile must not be null");

        applyOverviewDocument().writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8);
        applyPathsDocument().writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        applyDefinitionsDocument().writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        applySecurityDocument().writeToFileWithoutExtension(outputFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    /**
     * Builds the document returns it as a String.
     *
     * @return the document as a String
     */
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(applyOverviewDocument().toString());
        sb.append(applyPathsDocument().toString());
        sb.append(applyDefinitionsDocument().toString());
        sb.append(applySecurityDocument().toString());
        return sb.toString();
    }

    public static class Builder {
        private final Swagger swagger;
        private final URI swaggerLocation;
        private Swagger2MarkupConfig config;
        private Swagger2MarkupExtensionRegistry extensionRegistry;

        /**
         * Creates a Builder from a remote URL.
         *
         * @param swaggerUrl the remote URL
         */
        Builder(URL swaggerUrl) {
            try {
                this.swaggerLocation = swaggerUrl.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("swaggerURL is in a wrong format", e);
            }
            this.swagger = readSwagger(swaggerUrl.toString());
        }

        /**
         * Creates a Builder from a local Path.
         *
         * @param swaggerPath the local Path
         */
        Builder(Path swaggerPath) {
            this.swaggerLocation = swaggerPath.toAbsolutePath().toUri();
            this.swagger = readSwagger(swaggerPath.toString());
        }

        /**
         * Creates a Builder using a given Swagger model.
         *
         * @param swagger the Swagger source.
         */
        Builder(Swagger swagger) {
            this.swagger = swagger;
            this.swaggerLocation = null;
        }

        /**
         * Uses the SwaggerParser to read the Swagger source.
         *
         * @param swaggerLocation the location of the Swagger source
         * @return the Swagger model
         */
        private Swagger readSwagger(String swaggerLocation) {
            Swagger swagger = new SwaggerParser().read(swaggerLocation);
            if (swagger == null) {
                throw new IllegalArgumentException("Failed to read the Swagger source");
            }
            return swagger;
        }

        public Builder withConfig(Swagger2MarkupConfig config) {
            Validate.notNull(config, "config must not be null");
            this.config = config;
            return this;
        }

        public Builder withExtensionRegistry(Swagger2MarkupExtensionRegistry registry) {
            Validate.notNull(registry, "registry must not be null");
            this.extensionRegistry = registry;
            return this;
        }

        public Swagger2MarkupConverter build() {
            if (config == null)
                config = new Swagger2MarkupConfigBuilder().build();

            if (extensionRegistry == null)
                extensionRegistry = new Swagger2MarkupExtensionRegistryBuilder().build();
            SwaggerLabels swaggerLabels = new SwaggerLabels(config);
            SwaggerContext context = new SwaggerContext(config, extensionRegistry, swagger, swaggerLocation, swaggerLabels);

            initExtensions(context);

            applySwaggerExtensions(context);

            return new Swagger2MarkupConverter(context);
        }

        private void initExtensions(SwaggerContext context) {
            extensionRegistry.getSwaggerModelExtensions().forEach(extension -> extension.setGlobalContext(context));
            extensionRegistry.getOverviewDocumentExtensions().forEach(extension -> extension.setGlobalContext(context));
            extensionRegistry.getDefinitionsDocumentExtensions().forEach(extension -> extension.setGlobalContext(context));
            extensionRegistry.getPathsDocumentExtensions().forEach(extension -> extension.setGlobalContext(context));
            extensionRegistry.getSecurityDocumentExtensions().forEach(extension -> extension.setGlobalContext(context));
        }

        private void applySwaggerExtensions(SwaggerContext context) {
            extensionRegistry.getSwaggerModelExtensions().forEach(extension -> extension.apply(context.getSchema()));
        }
    }

    public static class SwaggerContext extends Context<Swagger> {
        private Swagger2MarkupConfig config;
        private Swagger2MarkupExtensionRegistry extensionRegistry;

        public SwaggerContext(Swagger2MarkupConfig config,
                              Swagger2MarkupExtensionRegistry extensionRegistry,
                              Swagger schema, URI swaggerLocation, Labels labels) {
            super(config, extensionRegistry, schema, swaggerLocation, labels);
            this.config = config;
            this.extensionRegistry = extensionRegistry;
        }

        @Override
        public Swagger2MarkupConfig getConfig() {
            return config;
        }

        @Override
        public Swagger2MarkupExtensionRegistry getExtensionRegistry() {
            return extensionRegistry;
        }

        public MarkupDocBuilder createMarkupDocBuilder() {
            MarkupLanguage markupLanguage = null;
            if (config.getMarkupLanguage() != null) {
                markupLanguage = MarkupLanguage.valueOf(config.getMarkupLanguage().name());
            }
            LineSeparator lineSeparator = null;
            if (config.getLineSeparator() != null) {
                lineSeparator = LineSeparator.valueOf(config.getLineSeparator().name());
            }
            return MarkupDocBuilders.documentBuilder(markupLanguage, lineSeparator,
                    config.getAsciidocPegdownTimeoutMillis()).withAnchorPrefix(config.getAnchorPrefix());
        }
    }

}
