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
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.DocumentImpl;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import com.logate.openapi2asciidoc.swagger2markup.core.Schema2MarkupProperties;
import io.swagger.v3.oas.models.links.Link;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.Map;

import static com.logate.openapi2asciidoc.openapi2markup.config.OpenAPILabels.*;
import static com.logate.openapi2asciidoc.openapi2markup.internal.helper.OpenApiHelpers.italicUnconstrained;

public class LinkComponent extends MarkupComponent<StructuralNode, LinkComponent.Parameters, StructuralNode> {

    public LinkComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
    }

    public static Parameters parameters(Map<String, Link> links) {
        return new Parameters(links);
    }

    public Document apply(StructuralNode parent, Map<String, Link> links) {
        return apply(parent, parameters(links));
    }

    @Override
    public Document apply(StructuralNode parent, Parameters parameters) {
        DocumentImpl linksDocument = new DocumentImpl(parent);
        ParagraphBlockImpl linkParagraph = new ParagraphBlockImpl(linksDocument);

        Map<String, Link> links = parameters.links;
        if (null == links || links.isEmpty()) {
            linkParagraph.setSource(labels.getLabel(LABEL_NO_LINKS));
        } else {
            StringBuilder sb = new StringBuilder();
            links.forEach((name, link) -> {
                sb.append(name).append(" +").append(Schema2MarkupProperties.LINE_SEPARATOR);
                sb.append(italicUnconstrained(labels.getLabel(LABEL_OPERATION))).append(' ')
                        .append(italicUnconstrained(link.getOperationId())).append(" +").append(Schema2MarkupProperties.LINE_SEPARATOR);
                Map<String, String> linkParameters = link.getParameters();
                if (null != linkParameters && !linkParameters.isEmpty()) {
                    sb.append(italicUnconstrained(labels.getLabel(LABEL_PARAMETERS))).append(" {").append(" +").append(Schema2MarkupProperties.LINE_SEPARATOR);
                    linkParameters.forEach((param, value) ->
                            sb.append('"').append(param).append("\": \"").append(value).append('"').append(" +").append(Schema2MarkupProperties.LINE_SEPARATOR)
                    );
                    sb.append('}').append(" +").append(Schema2MarkupProperties.LINE_SEPARATOR);
                }
            });
            linkParagraph.setSource(sb.toString());
        }
        linksDocument.append(linkParagraph);
        return linksDocument;
    }

    public static class Parameters {
        private final Map<String, Link> links;

        public Parameters(Map<String, Link> links) {
            this.links = links;
        }
    }
}
