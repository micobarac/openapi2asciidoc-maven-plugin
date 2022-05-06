package com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentImpl extends StructuralNodeImpl implements Document {

    public DocumentImpl() {
        this(null);
    }

    public DocumentImpl(StructuralNode parent) {
        this(parent, "document", "");
    }

    public DocumentImpl(StructuralNode parent, String context, Object content) {
        this(parent, context, new HashMap<>(), new ArrayList<>(), content, new ArrayList<>(), "", new ArrayList<>());
    }

    public DocumentImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                        Object content, List<StructuralNode> blocks, String contentModel,
                        List<String> subs) {
        this(parent, context, attributes, roles, content, blocks, null != parent ? parent.getLevel() + 1 : 0, contentModel, subs);
    }

    public DocumentImpl(StructuralNode parent, String context, Map<String, Object> attributes, List<String> roles,
                        Object content, List<StructuralNode> blocks, Integer level, String contentModel,
                        List<String> subs) {
        super(parent, context, attributes, roles, content, blocks, level, contentModel, subs);
    }

    @Override
    public boolean isBasebackend(String backend) {
        return isAttribute("basebackend", backend);
    }

    @Override
    @Deprecated
    public boolean basebackend(String backend) {
        return isBasebackend(backend);
    }

    @Override
    public Map<Object, Object> getOptions() {
        return null;
    }

    @Override
    public Title getStructuredDoctitle() {
        return (Title) getOptions().get("doctitle");
    }

    @Override
    public String getDoctitle() {
        return getTitle();
    }

    @Override
    @Deprecated
    public String doctitle() {
        return getDoctitle();
    }

    @Override
    public List<Author> getAuthors() {
        return null;
    }

    @Override
    public int getAndIncrementCounter(String name) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public int getAndIncrementCounter(String name, int initialValue) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public boolean isSourcemap() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public void setSourcemap(boolean state) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public Catalog getCatalog() {
        return null;
    }

    @Override
    public RevisionInfo getRevisionInfo() {
        return null;
    }
}
