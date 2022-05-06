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
package com.logate.openapi2asciidoc.openapi2markup.internal.component;

import com.logate.openapi2asciidoc.openapi2markup.OpenAPI2MarkupConverter;
import com.logate.openapi2asciidoc.openapi2markup.extension.MarkupComponent;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.TableImpl;
import com.logate.openapi2asciidoc.swagger2markup.core.Schema2MarkupProperties;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import java.util.*;
import java.util.stream.Collectors;

import static com.logate.openapi2asciidoc.openapi2markup.config.OpenAPILabels.*;
import static com.logate.openapi2asciidoc.openapi2markup.internal.helper.OpenApiHelpers.*;
import static com.logate.openapi2asciidoc.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;

public class ParametersComponent extends MarkupComponent<StructuralNode, ParametersComponent.Parameters, StructuralNode> {

    private final SchemaComponent schemaComponent;

    public ParametersComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.schemaComponent = new SchemaComponent(context);
    }

    public static Parameters parameters(Map<String, Parameter> parameters) {
        return new Parameters(parameters);
    }

    public static Parameters parameters(List<Parameter> parameters) {
        if(null == parameters) {
            return new Parameters(new HashMap<>());
        }
        return new Parameters(parameters.stream().collect(Collectors.toMap(Parameter::getName, parameter -> parameter)));
    }

    public StructuralNode apply(StructuralNode parent, List<Parameter> params) {
        return apply(parent, parameters(params));
    }

    public StructuralNode apply(StructuralNode parent, Map<String, Parameter> params) {
        return apply(parent, parameters(params));
    }

    @Override
    public StructuralNode apply(StructuralNode parent, Parameters componentParameters) {
        Map<String, Parameter> parameters = componentParameters.parameters;
        if (null == parameters || parameters.isEmpty()) return parent;

        TableImpl pathParametersTable = new TableImpl(parent, new HashMap<>(), new ArrayList<>());
        pathParametersTable.setOption("header");
        pathParametersTable.setAttribute("caption", "", true);
        pathParametersTable.setAttribute("cols", ".^2a,.^3a,.^10a,.^5a", true);
        pathParametersTable.setTitle(labels.getLabel(TABLE_TITLE_PARAMETERS));
        pathParametersTable.setHeaderRow(
                labels.getLabel(TABLE_HEADER_TYPE),
                labels.getLabel(TABLE_HEADER_NAME),
                labels.getLabel(TABLE_HEADER_DESCRIPTION),
                labels.getLabel(TABLE_HEADER_SCHEMA));

        parameters.forEach((alt, parameter) ->
                pathParametersTable.addRow(
                        generateInnerDoc(pathParametersTable, boldUnconstrained(parameter.getIn()), alt),
                        getParameterNameDocument(pathParametersTable, parameter),
                        generateInnerDoc(pathParametersTable, Optional.ofNullable(parameter.getDescription()).orElse("")),
                        generateInnerDoc(pathParametersTable, getSchemaTypeAsString(parameter.getSchema()))
                ));
        parent.append(pathParametersTable);

        return parent;
    }

    private Document getParameterNameDocument(Table table, Parameter parameter) {
        String documentContent = boldUnconstrained(parameter.getName()) + " +" + LINE_SEPARATOR + requiredIndicator(parameter.getRequired(),
                labels.getLabel(LABEL_REQUIRED), labels.getLabel(LABEL_OPTIONAL));
        return generateInnerDoc(table, documentContent);
    }

    public static class Parameters {

        private final Map<String, Parameter> parameters;

        public Parameters(Map<String, Parameter> parameters) {
            this.parameters = parameters;
        }
    }
}
