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

package com.logate.openapi2asciidoc.swagger2markup.internal.resolver;

import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConfig;
import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.core.model.PathOperation;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import io.vavr.Function1;

/**
 * A functor to return the document part of an inter-document cross-references, depending on the context.
 */
public abstract class OperationDocumentResolver implements Function1<PathOperation, String> {

    Swagger2MarkupConverter.Context context;
    MarkupDocBuilder markupDocBuilder;
    Swagger2MarkupConfig config;

    public OperationDocumentResolver(Swagger2MarkupConverter.SwaggerContext context) {
        this.context = context;
        this.markupDocBuilder = context.createMarkupDocBuilder();
        this.config = context.getConfig();
    }
}