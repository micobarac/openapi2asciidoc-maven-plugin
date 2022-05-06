package com.logate.openapi2asciidoc.swagger2markup.adoc.converter.internal;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;

public class DelimitedBlockNode extends ParagraphAttributes {

    public DelimitedBlockNode(StructuralNode node) {
        super(node);
    }

    @Override
    public void processPositionalAttributes() {
        String source = pop("1", "style");
        StringBuilder options = new StringBuilder();
        List<String> toRemove = new ArrayList<>();
        attributes.forEach((k, v) -> {
            if (k.endsWith(OPTION_SUFFIX)) {
                toRemove.add(k);
                options.append('%').append(k.replace(OPTION_SUFFIX, ""));
            }
        });
        toRemove.forEach(attributes::remove);
        source += options.toString();

        if (StringUtils.isNotBlank(source)) {
            attrs.add(source);
        }
        super.processPositionalAttributes();
    }

    @Override
    public String processAsciiDocContent() {
        StringBuilder sb = new StringBuilder();
        if (!attrs.isEmpty()) {
            sb.append(Delimiters.ATTRIBUTES_BEGIN).append(String.join(",", attrs)).append(Delimiters.ATTRIBUTES_END).append(Delimiters.LINE_SEPARATOR);
        }
        return sb.toString();
    }
}
