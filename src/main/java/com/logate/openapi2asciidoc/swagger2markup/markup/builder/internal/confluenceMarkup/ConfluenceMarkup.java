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
package com.logate.openapi2asciidoc.swagger2markup.markup.builder.internal.confluenceMarkup;

import com.logate.openapi2asciidoc.swagger2markup.markup.builder.internal.Markup;

/**
 * @author Robert Winkler
 */
public enum ConfluenceMarkup implements Markup {
    TABLE_COLUMN_DELIMITER("|"),
    LITERAL("{noformat}"),
    BOLD("*"),
    ITALIC("_"),
    LIST_ENTRY("* "),
    ANCHOR_START("{anchor:"),
    ANCHOR_END("}"),
    SPACE_ESCAPE("_"),
    LINE_BREAK("\\\\ ");

    private final String markup;

    /**
     * @param markup AsciiDoc markup
     */
    ConfluenceMarkup(final String markup) {
        this.markup = markup;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return markup;
    }
}
