/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package com.logate.openapi2asciidoc.swagger2markup.markup.builder;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robert Winkler
 */
public enum MarkupLanguage {
    ASCIIDOC(".adoc,.asciidoc"),
    MARKDOWN(".md,.markdown"),
    CONFLUENCE_MARKUP(".txt");

    private final String fileNameExtensions;

    /**
     * @param fileNameExtensions file name suffix
     */
    private MarkupLanguage(final String fileNameExtensions) {
        this.fileNameExtensions = fileNameExtensions;
    }

    public List<String> getFileNameExtensions() {
        return Arrays.asList(fileNameExtensions.split(","));
    }
}
