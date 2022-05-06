package com.logate.openapi2asciidoc.swagger2markup.adoc.converter.internal;

import org.asciidoctor.ast.Block;

import java.util.List;

public class BlockListingNode extends ParagraphAttributes {

    final private Block node;

    public BlockListingNode(Block node) {
        super(node);
        this.node = node;
    }

    @Override
    public String processAsciiDocContent() {
        StringBuilder sb = new StringBuilder();
        attrsToString(sb, attrs);
        sb.append(Delimiters.LINE_SEPARATOR).append(Delimiters.DELIMITER_BLOCK).append(Delimiters.LINE_SEPARATOR).append(node.getSource()).append(Delimiters.LINE_SEPARATOR).append(Delimiters.DELIMITER_BLOCK).append(Delimiters.LINE_SEPARATOR);
        return sb.toString();
    }

    void attrsToString(StringBuilder sb, List<String> list) {
        if (!list.isEmpty()) {
            sb.append(Delimiters.ATTRIBUTES_BEGIN).append(String.join(",", list)).append(Delimiters.ATTRIBUTES_END).append(Delimiters.LINE_SEPARATOR);
        }
    }
}
