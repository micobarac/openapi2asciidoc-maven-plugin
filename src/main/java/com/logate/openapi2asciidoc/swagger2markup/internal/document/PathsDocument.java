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
package com.logate.openapi2asciidoc.swagger2markup.internal.document;

import com.google.common.collect.Multimap;
import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConfig;
import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.SwaggerLabels;
import com.logate.openapi2asciidoc.swagger2markup.core.GroupBy;
import com.logate.openapi2asciidoc.swagger2markup.core.model.PathOperation;
import com.logate.openapi2asciidoc.swagger2markup.internal.component.PathOperationComponent;
import com.logate.openapi2asciidoc.swagger2markup.internal.resolver.DefinitionDocumentResolverFromOperation;
import com.logate.openapi2asciidoc.swagger2markup.internal.resolver.OperationDocumentNameResolver;
import com.logate.openapi2asciidoc.swagger2markup.internal.resolver.OperationDocumentResolverDefault;
import com.logate.openapi2asciidoc.swagger2markup.internal.resolver.SecurityDocumentResolver;
import com.logate.openapi2asciidoc.swagger2markup.internal.utils.PathUtils;
import com.logate.openapi2asciidoc.swagger2markup.internal.utils.RegexUtils;
import com.logate.openapi2asciidoc.swagger2markup.internal.utils.TagUtils;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import com.logate.openapi2asciidoc.swagger2markup.model.SwaggerPathOperation;
import com.logate.openapi2asciidoc.swagger2markup.spi.MarkupComponent;
import com.logate.openapi2asciidoc.swagger2markup.spi.PathsDocumentExtension;
import com.logate.openapi2asciidoc.swagger2markup.utils.IOUtils;
import io.swagger.models.Path;
import io.swagger.models.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.WordUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.crossReference;

/**
 * @author Robert Winkler
 */
public class PathsDocument extends MarkupComponent<PathsDocument.Parameters> {

    private static final String PATHS_ANCHOR = "paths";
    private final PathOperationComponent pathOperationComponent;
    private final OperationDocumentNameResolver operationDocumentNameResolver;
    private final OperationDocumentResolverDefault operationDocumentResolverDefault;

    public PathsDocument(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
        this.pathOperationComponent = new PathOperationComponent(context,
                new DefinitionDocumentResolverFromOperation(context),
                new SecurityDocumentResolver(context));
        this.operationDocumentNameResolver = new OperationDocumentNameResolver(context);
        this.operationDocumentResolverDefault = new OperationDocumentResolverDefault(context);

        if (logger.isDebugEnabled()) {
            if (config.isGeneratedExamplesEnabled()) {
                logger.debug("Generate examples is enabled.");
            } else {
                logger.debug("Generate examples is disabled.");
            }

            if (config.isSeparatedOperationsEnabled()) {
                logger.debug("Create separated operation files is enabled.");
            } else {
                logger.debug("Create separated operation files is disabled.");
            }
        }
    }

    public static Parameters parameters(Map<String, Path> paths) {
        return new Parameters(paths);
    }

    /**
     * Builds the paths MarkupDocument.
     *
     * @return the paths MarkupDocument
     */
    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        Map<String, Path> paths = params.paths;
        if (MapUtils.isNotEmpty(paths)) {
            applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.DOCUMENT_BEFORE, markupDocBuilder));
            buildPathsTitle(markupDocBuilder);
            applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.DOCUMENT_BEGIN, markupDocBuilder));
            buildsPathsSection(markupDocBuilder, paths);
            applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.DOCUMENT_END, markupDocBuilder));
            applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.DOCUMENT_AFTER, markupDocBuilder));
        }
        return markupDocBuilder;
    }

    /**
     * Builds the paths section. Groups the paths either as-is, by tags or using regex.
     *
     * @param paths the Swagger paths
     */
    private void buildsPathsSection(MarkupDocBuilder markupDocBuilder, Map<String, Path> paths) {
        List<SwaggerPathOperation> pathOperations = PathUtils.toPathOperationsList(paths, getHostname(), getBasePath(), config.getOperationOrdering());
        logger.info("----- Path operations: -----");
        logger.info(pathOperations.toString());
        logger.info("----- Paths grouped by: -----");
        logger.info(config.getPathsGroupedBy().toString());

        if (CollectionUtils.isNotEmpty(pathOperations)) {
            if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
                pathOperations.forEach(operation -> buildOperation(markupDocBuilder, operation, config));
            } else if (config.getPathsGroupedBy() == GroupBy.TAGS) {
                Validate.notEmpty(context.getSchema().getTags(), "Tags must not be empty, when operations are grouped by tags");
                // Group operations by tag
                Multimap<String, SwaggerPathOperation> operationsGroupedByTag = TagUtils.groupOperationsByTag(pathOperations, config.getOperationOrdering());

                logger.info("----- Operations grouped by tag: -----");
                logger.info(operationsGroupedByTag.toString());

                Map<String, Tag> tagsMap = TagUtils.toSortedMap(context.getSchema().getTags(), config.getTagOrdering());

                logger.info("----- Tags map: -----");
                logger.info(tagsMap.toString());

                tagsMap.forEach((String tagName, Tag tag) -> {
                    markupDocBuilder.sectionTitleWithAnchorLevel2(WordUtils.capitalize(tagName), tagName + "_resource");
                    String description = tag.getDescription();
                    if (StringUtils.isNotBlank(description)) {
                        markupDocBuilder.paragraph(description);
                    }
                    operationsGroupedByTag.get(tagName).forEach(operation -> buildOperation(markupDocBuilder, operation, config));

                });
            } else if (config.getPathsGroupedBy() == GroupBy.REGEX) {
                Validate.notNull(config.getHeaderPattern(), "Header regex pattern must not be empty when operations are grouped using regex");

                Pattern headerPattern = config.getHeaderPattern();
                Multimap<String, SwaggerPathOperation> operationsGroupedByRegex = RegexUtils.groupOperationsByRegex(pathOperations, headerPattern);
                Set<String> keys = operationsGroupedByRegex.keySet();
                String[] sortedHeaders = RegexUtils.toSortedArray(keys);

                for (String header : sortedHeaders) {
                    markupDocBuilder.sectionTitleWithAnchorLevel2(WordUtils.capitalize(header), header + "_resource");
                    operationsGroupedByRegex.get(header).forEach(operation -> buildOperation(markupDocBuilder, operation, config));
                }
            }
        }
    }

    /**
     * Builds the path title depending on the operationsGroupedBy configuration setting.
     */
    private void buildPathsTitle(MarkupDocBuilder markupDocBuilder) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            buildPathsTitle(markupDocBuilder, labels.getLabel(SwaggerLabels.PATHS));
        } else if (config.getPathsGroupedBy() == GroupBy.REGEX) {
            buildPathsTitle(markupDocBuilder, labels.getLabel(SwaggerLabels.OPERATIONS));
        } else {
            buildPathsTitle(markupDocBuilder, labels.getLabel(SwaggerLabels.RESOURCES));
        }
    }

    /**
     * Returns the hostname which should be prepended to the relative path
     *
     * @return either the relative or the full path
     */
    private String getHostname() {
        if (config.isHostnameEnabled()) {
            return StringUtils.defaultString(context.getSchema().getHost());
        }
        return "";
    }

    /**
     * Returns the basePath which should be prepended to the relative path
     *
     * @return either the relative or the full path
     */
    private String getBasePath() {
        if (config.isBasePathPrefixEnabled()) {
            return StringUtils.defaultString(context.getSchema().getBasePath());
        }
        return "";
    }

    private void buildPathsTitle(MarkupDocBuilder markupDocBuilder, String title) {
        markupDocBuilder.sectionTitleWithAnchorLevel1(title, PATHS_ANCHOR);
    }

    /**
     * Apply extension context to all OperationsContentExtension.
     *
     * @param context context
     */
    private void applyPathsDocumentExtension(PathsDocumentExtension.Context context) {
        extensionRegistry.getPathsDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    /**
     * Builds a path operation depending on generation mode.
     *
     * @param operation operation
     */
    private void buildOperation(MarkupDocBuilder markupDocBuilder, SwaggerPathOperation operation, Swagger2MarkupConfig config) {
        if (config.isSeparatedOperationsEnabled()) {
            MarkupDocBuilder pathDocBuilder = copyMarkupDocBuilder(markupDocBuilder);
            applyPathOperationComponent(pathDocBuilder, operation);
            java.nio.file.Path operationFile = context.getOutputPath().resolve(operationDocumentNameResolver.apply(operation));
            pathDocBuilder.writeToFileWithoutExtension(operationFile, StandardCharsets.UTF_8);
            if (logger.isDebugEnabled()) {
                logger.debug("Separate operation file produced : '{}'", operationFile);
            }
            buildOperationRef(markupDocBuilder, operation);

        } else {
            applyPathOperationComponent(markupDocBuilder, operation);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Operation processed : '{}' (normalized id = '{}')", operation, IOUtils.normalizeName(operation.getId()));
        }
    }

    /**
     * Builds a path operation.
     *
     * @param markupDocBuilder the docbuilder do use for output
     * @param operation        the Swagger Operation
     */
    private void applyPathOperationComponent(MarkupDocBuilder markupDocBuilder, SwaggerPathOperation operation) {
        if (operation != null) {
            pathOperationComponent.apply(markupDocBuilder, PathOperationComponent.parameters(operation));
        }
    }

    /**
     * Builds a cross-reference to a separated operation file
     *
     * @param markupDocBuilder the markupDocBuilder do use for output
     * @param operation        the Swagger Operation
     */
    private void buildOperationRef(MarkupDocBuilder markupDocBuilder, PathOperation operation) {
        buildOperationTitle(markupDocBuilder, crossReference(markupDocBuilder, operationDocumentResolverDefault.apply(operation),
                operation.getId(), operation.getTitle()), "ref-" + operation.getId());
    }

    /**
     * Adds a operation title to the document.
     *
     * @param title  the operation title
     * @param anchor optional anchor (null => auto-generate from title)
     */
    private void buildOperationTitle(MarkupDocBuilder markupDocBuilder, String title, String anchor) {
        if (config.getPathsGroupedBy() == GroupBy.AS_IS) {
            markupDocBuilder.sectionTitleWithAnchorLevel2(title, anchor);
        } else {
            markupDocBuilder.sectionTitleWithAnchorLevel3(title, anchor);
        }
    }

    public static class Parameters {
        private final Map<String, Path> paths;

        public Parameters(Map<String, Path> paths) {
            this.paths = paths;
        }
    }
}
