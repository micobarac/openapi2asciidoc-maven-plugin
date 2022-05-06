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

package com.logate.openapi2asciidoc.openapi2markup.extension;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * OpenAPIModelExtension extension point can be used to preprocess the Swagger model.
 */
public abstract class OpenAPIModelExtension extends AbstractExtension {

    public abstract void apply(OpenAPI openAPI);

}
