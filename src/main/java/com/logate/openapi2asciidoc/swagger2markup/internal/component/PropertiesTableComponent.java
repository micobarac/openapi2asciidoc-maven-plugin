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

import ch.netzwerg.paleo.ColumnIds;
import ch.netzwerg.paleo.StringColumn;
import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.SwaggerLabels;
import com.logate.openapi2asciidoc.swagger2markup.internal.adapter.PropertyAdapter;
import com.logate.openapi2asciidoc.swagger2markup.internal.resolver.DocumentResolver;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.ObjectType;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.RefType;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.Type;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupLanguage;
import com.logate.openapi2asciidoc.swagger2markup.spi.MarkupComponent;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.InlineSchemaUtils.createInlineType;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MapUtils.toSortedMap;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.markupDescription;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PropertiesTableComponent extends MarkupComponent<PropertiesTableComponent.Parameters> {


    private final DocumentResolver definitionDocumentResolver;
    private final TableComponent tableComponent;

    /**
     * Build a generic property table
     *
     * @param definitionDocumentResolver definition document resolver to apply to property type cross-reference
     */
    PropertiesTableComponent(Swagger2MarkupConverter.SwaggerContext context,
                             DocumentResolver definitionDocumentResolver) {
        super(context);
        this.definitionDocumentResolver = definitionDocumentResolver;
        this.tableComponent = new TableComponent(context);
    }

    public static Parameters parameters(Map<String, Property> properties,
      String parameterName,
      List<ObjectType> inlineDefinitions) {
        return new Parameters(properties, parameterName, inlineDefinitions);
    }

    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        //TODO: This method is too complex, split it up in smaller methods to increase readability
        StringColumn.Builder nameColumnBuilder = StringColumn.builder(ColumnIds.StringColumnId.of(labels.getLabel(SwaggerLabels.NAME_COLUMN)))
          .putMetaData(TableComponent.WIDTH_RATIO, "3");

        StringColumn.Builder descriptionColumnBuilder = StringColumn.builder(ColumnIds.StringColumnId.of(labels.getLabel(SwaggerLabels.DESCRIPTION_COLUMN)))
          .putMetaData(TableComponent.WIDTH_RATIO, "11")
          .putMetaData(TableComponent.HEADER_COLUMN, "true");

        StringColumn.Builder schemaColumnBuilder = StringColumn.builder(ColumnIds.StringColumnId.of(labels.getLabel(SwaggerLabels.SCHEMA_COLUMN)))
          .putMetaData(TableComponent.WIDTH_RATIO, "4")
          .putMetaData(TableComponent.HEADER_COLUMN, "true");

        Map<String, Property> properties = params.properties;
        if (MapUtils.isNotEmpty(properties)) {
            Map<String, Property> sortedProperties = toSortedMap(properties, config.getPropertyOrdering());
            sortedProperties.forEach((String propertyName, Property property) -> {
                PropertyAdapter propertyAdapter = new PropertyAdapter(property);
                Type propertyType = propertyAdapter.getType(definitionDocumentResolver);

                if (config.isInlineSchemaEnabled()) {
                    propertyType = createInlineType(propertyType, propertyName, params.parameterName + " " + propertyName, params.inlineDefinitions);
                }

                Optional<Object> optionalExample = propertyAdapter.getExample(config.isGeneratedExamplesEnabled(), markupDocBuilder);
                Optional<Object> optionalDefaultValue = propertyAdapter.getDefaultValue();
                Optional<Integer> optionalMaxLength = propertyAdapter.getMaxlength();
                Optional<Integer> optionalMinLength = propertyAdapter.getMinlength();
                Optional<String> optionalPattern = propertyAdapter.getPattern();

                Optional<BigDecimal> optionalMinValue = propertyAdapter.getMin();
                boolean exclusiveMin = propertyAdapter.getExclusiveMin();
                Optional<BigDecimal> optionalMaxValue = propertyAdapter.getMax();
                boolean exclusiveMax = propertyAdapter.getExclusiveMax();

                MarkupDocBuilder propertyNameContent = copyMarkupDocBuilder(markupDocBuilder);
                propertyNameContent.boldTextLine(propertyName, true);
                if (property.getRequired())
                    propertyNameContent.italicText(labels.getLabel(SwaggerLabels.FLAGS_REQUIRED).toLowerCase());
                else
                    propertyNameContent.italicText(labels.getLabel(SwaggerLabels.FLAGS_OPTIONAL).toLowerCase());
                if (propertyAdapter.getReadOnly()) {
                    propertyNameContent.newLine(true);
                    propertyNameContent.italicText(labels.getLabel(SwaggerLabels.FLAGS_READ_ONLY).toLowerCase());
                }

                MarkupDocBuilder descriptionContent = copyMarkupDocBuilder(markupDocBuilder);
                String description = markupDescription(MarkupLanguage.valueOf(config.getSchemaMarkupLanguage().name()),
                        markupDocBuilder, property.getDescription());
                if (isNotBlank(description))
                    descriptionContent.text(description);

                if (optionalDefaultValue.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }
                    descriptionContent.boldText(labels.getLabel(SwaggerLabels.DEFAULT_COLUMN)).text(COLON).literalText(Json.pretty(optionalDefaultValue.get()));
                }

                if (optionalMinLength.isPresent() && optionalMaxLength.isPresent()) {
                    // combination of minlength/maxlength
                    Integer minLength = optionalMinLength.get();
                    Integer maxLength = optionalMaxLength.get();

                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }

                    String lengthRange = minLength + " - " + maxLength;
                    if (minLength.equals(maxLength)) {
                        lengthRange = minLength.toString();
                    }

                    descriptionContent.boldText(labels.getLabel(SwaggerLabels.LENGTH_COLUMN)).text(COLON).literalText(lengthRange);

                } else {
                    if (optionalMinLength.isPresent()) {
                        if (isNotBlank(descriptionContent.toString())) {
                            descriptionContent.newLine(true);
                        }
                        descriptionContent.boldText(labels.getLabel(SwaggerLabels.MINLENGTH_COLUMN)).text(COLON).literalText(optionalMinLength.get().toString());
                    }

                    if (optionalMaxLength.isPresent()) {
                        if (isNotBlank(descriptionContent.toString())) {
                            descriptionContent.newLine(true);
                        }
                        descriptionContent.boldText(labels.getLabel(SwaggerLabels.MAXLENGTH_COLUMN)).text(COLON).literalText(optionalMaxLength.get().toString());
                    }
                }

                if (optionalPattern.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }
                    descriptionContent.boldText(labels.getLabel(SwaggerLabels.PATTERN_COLUMN)).text(COLON).literalText(Json.pretty(optionalPattern.get()));
                }

                DecimalFormat numberFormatter = new DecimalFormat("#.##",
                  DecimalFormatSymbols.getInstance(config.getLanguage().toLocale()));

                if (optionalMinValue.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }
                    String minValueColumn = exclusiveMin ? labels.getLabel(SwaggerLabels.MINVALUE_EXCLUSIVE_COLUMN) : labels.getLabel(SwaggerLabels.MINVALUE_COLUMN);
                    descriptionContent.boldText(minValueColumn).text(COLON).literalText(numberFormatter.format(optionalMinValue.get()));
                }

                if (optionalMaxValue.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }
                    String maxValueColumn = exclusiveMax ? labels.getLabel(SwaggerLabels.MAXVALUE_EXCLUSIVE_COLUMN) : labels.getLabel(SwaggerLabels.MAXVALUE_COLUMN);
                    descriptionContent.boldText(maxValueColumn).text(COLON).literalText(numberFormatter.format(optionalMaxValue.get()));
                }

                if (optionalExample.isPresent()) {
                    if (isNotBlank(descriptionContent.toString())) {
                        descriptionContent.newLine(true);
                    }

                    if(propertyType instanceof RefType && isReferenceLink(optionalExample.get().toString())) {
                        descriptionContent.boldText(labels.getLabel(SwaggerLabels.EXAMPLE_COLUMN)).text(COLON).crossReference(optionalExample.get().toString());
                    } else {
                        descriptionContent.boldText(labels.getLabel(SwaggerLabels.EXAMPLE_COLUMN)).text(COLON).literalText(Json.pretty(optionalExample.get()));
                    }
                }

                nameColumnBuilder.add(propertyNameContent.toString());
                descriptionColumnBuilder.add(descriptionContent.toString());
                schemaColumnBuilder.add(propertyType.displaySchema(markupDocBuilder));
            });
        }

        return tableComponent.apply(markupDocBuilder, TableComponent.parameters(
          nameColumnBuilder.build(),
          descriptionColumnBuilder.build(),
          schemaColumnBuilder.build()));
    }

    /*
     * Check if a string is a link to a reference, format <<_referenceClass>>
      *
     * @param possibleAnchor String to check
     * @return true if the string is a link to an anchor, false otherwise
     */
    private boolean isReferenceLink(String possibleAnchor) {
        return possibleAnchor.startsWith("<<_") && possibleAnchor.endsWith(">>");
    }

    public static class Parameters {
        private final Map<String, Property> properties;
        private final String parameterName;
        private final List<ObjectType> inlineDefinitions;

        public Parameters(Map<String, Property> properties,
          String parameterName,
          List<ObjectType> inlineDefinitions) {

            this.properties = Validate.notNull(properties, "Properties must not be null");
            this.parameterName = Validate.notBlank(parameterName, "ParameterName must not be blank");
            this.inlineDefinitions = Validate.notNull(inlineDefinitions, "InlineDefinitions must not be null");
        }
    }
}
