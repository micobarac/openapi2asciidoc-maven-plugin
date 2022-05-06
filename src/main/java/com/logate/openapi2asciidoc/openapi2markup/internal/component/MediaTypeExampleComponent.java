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
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import com.logate.openapi2asciidoc.swagger2markup.adoc.converter.internal.Delimiters;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.StructuralNode;

import static com.logate.openapi2asciidoc.openapi2markup.config.OpenAPILabels.LABEL_EXAMPLE;

public class MediaTypeExampleComponent extends MarkupComponent<StructuralNode, MediaTypeExampleComponent.Parameters, StructuralNode> {

    public MediaTypeExampleComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
    }

    public static Parameters parameters(Object example) {
        return new Parameters(example);
    }

    public StructuralNode apply(StructuralNode node, Object example) {
        return apply(node, parameters(example));
    }

    @Override
    public StructuralNode apply(StructuralNode node, Parameters parameters) {
        Object example = parameters.example;
        if (example == null || StringUtils.isBlank(example.toString())) return node;

        ParagraphBlockImpl sourceBlock = new ParagraphBlockImpl(node);
        sourceBlock.setTitle(labels.getLabel(LABEL_EXAMPLE));
        sourceBlock.setAttribute("style", "source", true);
        sourceBlock.setSource(Delimiters.DELIMITER_BLOCK + Delimiters.LINE_SEPARATOR + example + Delimiters.LINE_SEPARATOR + Delimiters.DELIMITER_BLOCK);
        node.append(sourceBlock);

        return node;
    }

    public static class Parameters {

        private final Object example;

        public Parameters(Object example) {
            this.example = example;
        }
    }
}
