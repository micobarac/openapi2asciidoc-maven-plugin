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
package com.logate.openapi2asciidoc.openapi2markup;

import com.logate.openapi2asciidoc.swagger2markup.core.Schema2MarkupProperties;
import org.apache.commons.configuration2.Configuration;

import java.util.Map;
import java.util.Properties;

public class OpenAPI2MarkupProperties extends Schema2MarkupProperties {

    public OpenAPI2MarkupProperties(Properties properties) {
        super(properties);
    }

    public OpenAPI2MarkupProperties(Map<String, String> map) {
        super(map);
    }

    public OpenAPI2MarkupProperties(Configuration configuration) {
        super(configuration);
    }
}
