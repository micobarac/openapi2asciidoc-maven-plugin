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
package com.logate.openapi2asciidoc.swagger2markup.internal.component;


import ch.netzwerg.paleo.StringColumn;
import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.SwaggerLabels;
import com.logate.openapi2asciidoc.swagger2markup.internal.adapter.PropertyAdapter;
import com.logate.openapi2asciidoc.swagger2markup.internal.resolver.DocumentResolver;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.BasicType;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.ObjectType;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.Type;
import com.logate.openapi2asciidoc.swagger2markup.internal.utils.ModelUtils;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupLanguage;
import com.logate.openapi2asciidoc.swagger2markup.model.SwaggerPathOperation;
import com.logate.openapi2asciidoc.swagger2markup.spi.MarkupComponent;
import com.logate.openapi2asciidoc.swagger2markup.spi.PathsDocumentExtension;
import io.swagger.models.Model;
import io.swagger.models.Response;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ch.netzwerg.paleo.ColumnIds.StringColumnId;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.InlineSchemaUtils.createInlineType;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ResponseComponent extends MarkupComponent<ResponseComponent.Parameters> {

    private final TableComponent tableComponent;
    private final Map<String, Model> definitions;
    private final DocumentResolver definitionDocumentResolver;

    ResponseComponent(Swagger2MarkupConverter.SwaggerContext context,
                      DocumentResolver definitionDocumentResolver) {
        super(context);
        this.definitions = context.getSchema().getDefinitions();
        this.definitionDocumentResolver = Validate.notNull(definitionDocumentResolver, "DocumentResolver must not be null");
        this.tableComponent = new TableComponent(context);
    }

    public static Parameters parameters(SwaggerPathOperation operation,
                                        int titleLevel,
                                        List<ObjectType> inlineDefinitions) {
        return new Parameters(operation, titleLevel, inlineDefinitions);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        SwaggerPathOperation operation = params.operation;
        Map<String, Response> responses = operation.getOperation().getResponses();

        MarkupDocBuilder responsesBuilder = copyMarkupDocBuilder(markupDocBuilder);
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_RESPONSES_BEGIN, responsesBuilder, operation));
        if (MapUtils.isNotEmpty(responses)) {
            StringColumn.Builder httpCodeColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(SwaggerLabels.HTTP_CODE_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "2");
            StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(SwaggerLabels.DESCRIPTION_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "14")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");
            StringColumn.Builder schemaColumnBuilder = StringColumn.builder(StringColumnId.of(labels.getLabel(SwaggerLabels.SCHEMA_COLUMN)))
                    .putMetaData(TableComponent.WIDTH_RATIO, "4")
                    .putMetaData(TableComponent.HEADER_COLUMN, "true");

            Map<String, Response> sortedResponses = toSortedMap(responses, config.getResponseOrdering());
            sortedResponses.forEach((String responseName, Response response) -> {
                String schemaContent = labels.getLabel(SwaggerLabels.NO_CONTENT);
                if (response.getResponseSchema() != null) {
                    Model model = response.getResponseSchema();
                    Type type = null;

                    if (model != null) {
                        type = ModelUtils.getType(model, definitions, definitionDocumentResolver);
                    } else {
                        type = new BasicType("string", responseName);
                    }

                    if (config.isInlineSchemaEnabled()) {
                        type = createInlineType(type, labels.getLabel(SwaggerLabels.RESPONSE) + " " + responseName, operation.getId() + " " + labels.getLabel(SwaggerLabels.RESPONSE) + " " + responseName, params.inlineDefinitions);
                    }

                    schemaContent = type.displaySchema(markupDocBuilder);
                }

                MarkupDocBuilder descriptionBuilder = copyMarkupDocBuilder(markupDocBuilder);

                descriptionBuilder.text(markupDescription(MarkupLanguage.valueOf(config.getSchemaMarkupLanguage().name()),
                        markupDocBuilder, response.getDescription()));

                Map<String, Property> headers = response.getHeaders();
                if (MapUtils.isNotEmpty(headers)) {
                    descriptionBuilder.newLine(true).boldText(labels.getLabel(SwaggerLabels.HEADERS_COLUMN)).text(COLON);
                    for (Map.Entry<String, Property> header : headers.entrySet()) {
                        descriptionBuilder.newLine(true);
                        Property headerProperty = header.getValue();
                        PropertyAdapter headerPropertyAdapter = new PropertyAdapter(headerProperty);
                        Type propertyType = headerPropertyAdapter.getType(definitionDocumentResolver);
                        String headerDescription = markupDescription(MarkupLanguage.valueOf(config.getSchemaMarkupLanguage().name()),
                                markupDocBuilder, headerProperty.getDescription());
                        Optional<Object> optionalDefaultValue = headerPropertyAdapter.getDefaultValue();

                        descriptionBuilder
                                .literalText(header.getKey())
                                .text(String.format(" (%s)", propertyType.displaySchema(markupDocBuilder)));

                        if (isNotBlank(headerDescription) || optionalDefaultValue.isPresent()) {
                            descriptionBuilder.text(COLON);

                            if (isNotBlank(headerDescription) && !headerDescription.endsWith("."))
                                headerDescription += ".";

                            descriptionBuilder.text(headerDescription);

                            optionalDefaultValue.ifPresent(o -> descriptionBuilder.text(" ")
                                    .boldText(labels.getLabel(SwaggerLabels.DEFAULT_COLUMN))
                                    .text(COLON).literalText(Json.pretty(o)));
                        }
                    }
                }

                httpCodeColumnBuilder.add(boldText(markupDocBuilder, responseName));
                descriptionColumnBuilder.add(descriptionBuilder.toString());
                schemaColumnBuilder.add(schemaContent);
            });

            responsesBuilder = tableComponent.apply(responsesBuilder, TableComponent.parameters(httpCodeColumnBuilder.build(),
                    descriptionColumnBuilder.build(),
                    schemaColumnBuilder.build()));
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_RESPONSES_END, responsesBuilder, operation));
        String responsesContent = responsesBuilder.toString();

        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_RESPONSES_BEFORE, markupDocBuilder, operation));
        if (isNotBlank(responsesContent)) {
            markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(SwaggerLabels.RESPONSES));
            markupDocBuilder.text(responsesContent);
        }
        applyPathsDocumentExtension(new PathsDocumentExtension.Context(PathsDocumentExtension.Position.OPERATION_RESPONSES_AFTER, markupDocBuilder, operation));
        return markupDocBuilder;
    }

    /**
     * Apply extension context to all OperationsContentExtension.
     *
     * @param context context
     */
    private void applyPathsDocumentExtension(PathsDocumentExtension.Context context) {
        extensionRegistry.getPathsDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    public static class Parameters {
        private final SwaggerPathOperation operation;
        private final int titleLevel;
        private final List<ObjectType> inlineDefinitions;

        public Parameters(SwaggerPathOperation operation,
                          int titleLevel,
                          List<ObjectType> inlineDefinitions) {

            this.operation = Validate.notNull(operation, "PathOperation must not be null");
            this.titleLevel = titleLevel;
            this.inlineDefinitions = Validate.notNull(inlineDefinitions, "InlineDefinitions must not be null");
        }
    }
}
