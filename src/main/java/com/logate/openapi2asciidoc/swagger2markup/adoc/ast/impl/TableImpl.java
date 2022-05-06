package com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.*;

public class TableImpl extends StructuralNodeImpl implements Table {
    public static final String OPTION_UNBREAKABLE = "unbreakable";
    public static final String OPTION_BREAKABLE = "breakable";
    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String CONTEXT = "table";
    private static final String FRAME_ATTR = "frame";
    private static final String GRID_ATTR = "grid";
    private RowList headerRows;
    private RowList bodyRows;
    private RowList footerRows;

    private List<Column> columns = new ArrayList<>();

    public TableImpl(StructuralNode parent) {
        this(parent, new HashMap<>(), new ArrayList<>());
    }

    public TableImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles) {
        this(parent, attributes, roles, calculateLevel(parent));
    }

    public TableImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles, Integer level) {
        this(parent, attributes, roles, null, new ArrayList<>(), level, "", new ArrayList<>());
    }

    public TableImpl(StructuralNode parent, Map<String, Object> attributes, List<String> roles,
                     Object content, List<StructuralNode> blocks, Integer level, String contentModel, List<String> subs) {
        super(parent, CONTEXT, attributes, roles, content, blocks, level, contentModel, subs);
        this.headerRows = new RowList(new ArrayList<>());
        this.bodyRows = new RowList(new ArrayList<>());
        this.footerRows = new RowList(new ArrayList<>());
    }

    @Override
    public boolean hasHeaderOption() {
        return isOption("header");
    }

    @Override
    public String getFrame() {
        return (String) getAttribute(FRAME_ATTR, "all");
    }

    @Override
    public void setFrame(String frame) {
        setAttribute(FRAME_ATTR, frame, true);
    }

    @Override
    public String getGrid() {
        return (String) getAttribute(GRID_ATTR, "all");
    }

    @Override
    public void setGrid(String grid) {
        setAttribute(GRID_ATTR, grid, true);
    }

    @Override
    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public List<Row> getHeader() {
        return headerRows;
    }

    public void setHeaderRow(Row row) {
        headerRows.clear();
        headerRows.add(row);
        scanRowForColumns(row);
    }

    public void setHeaderRow(List<Cell> cells) {
        setHeaderRow(new RowImpl(cells));
    }

    public void setHeaderRow(String... documentContents) {
        headerRows.clear();
        headerRows.add(generateRow(documentContents));
    }

    public RowImpl generateRow(Document... innerDocs) {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < innerDocs.length; i++) {

            Column column = null;
            try {
                column = columns.get(i);
            } catch (Exception ignored) {
            }

            if (null == column) {
                ColumnImpl newColumn = new ColumnImpl(this);
                newColumn.setColumnNumber(i + 1);
                column = newColumn;
                addColumnAt(column, i);
            }
            cells.add(new CellImpl(column, innerDocs[i]));

        }
        return new RowImpl(cells);
    }

    public RowImpl generateRow(String... documentContents) {
        Document[] documents = Arrays.stream(documentContents).map(documentContent -> {
            Document innerDoc = new DocumentImpl();
            Block paragraph = new ParagraphBlockImpl(innerDoc);
            paragraph.setSource(documentContent);
            innerDoc.append(paragraph);
            return innerDoc;
        }).toArray(Document[]::new);
        return generateRow(documents);
    }

    @Override
    public List<Row> getBody() {
        return bodyRows;
    }

    public void setBodyRows(List<Row> rows) {
        bodyRows.clear();
        bodyRows.addAll(rows);
        bodyRows.forEach(this::scanRowForColumns);
    }

    public void addRow(Row row) {
        bodyRows.add(row);
        scanRowForColumns(row);
    }

    public void addRow(List<Cell> cells) {
        bodyRows.add(new RowImpl(cells));
    }

    public RowImpl addRow(Document... documentContents) {
        RowImpl row = generateRow(documentContents);
        bodyRows.add(row);
        return row;
    }

    public RowImpl addRow(String... documentContents) {
        RowImpl row = generateRow(documentContents);
        bodyRows.add(row);
        return row;
    }

    @Override
    public List<Row> getFooter() {
        return footerRows;
    }

    public void setFooterRow(Row row) {
        footerRows.clear();
        footerRows.add(row);
        scanRowForColumns(row);
    }

    public void setFooterRow(String... documentContents) {
        footerRows.clear();
        footerRows.add(generateRow(documentContents));
    }

    private void scanRowForColumns(Row row) {
        row.getCells().forEach(cell -> {
            Column column = cell.getColumn();
            int i = column.getColumnNumber() - 1;
            addColumnAt(column, i);
        });
    }

    private void addColumnAt(Column column, int i) {
        if (columns.size() >= i) {
            columns.add(i, column);
        } else {
            while (columns.size() < i) {
                columns.add(columns.size(), null);
            }
            columns.add(column);
        }
    }

    public void setFooterRow(List<Cell> cells) {
        setFooterRow(new RowImpl(cells));
    }

    class RowList extends AbstractList<Row> {

        private final List<Row> rubyArray;

        private RowList(List<Row> rubyArray) {
            this.rubyArray = rubyArray;
        }

        @Override
        public int size() {
            return rubyArray.size();
        }

        @Override
        public boolean isEmpty() {
            return rubyArray.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return rubyArray.contains(o);
        }

        @Override
        public boolean add(Row row) {
            boolean changed = false;
            try {
                changed = rubyArray.add(row);
                setAttribute("rowcount", size(), true);
            } catch (Exception e) {
                logger.debug("Couldn't add row", e);
            }
            return changed;
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof RowImpl)) {
                return false;
            }
            try {
                boolean changed = rubyArray.remove(o);
                setAttribute("rowcount", size(), true);
                return changed;
            } catch (Exception e) {
                logger.debug("Couldn't add row", e);
                return false;
            }
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            rubyArray.clear();
            setAttribute("rowcount", size(), true);
        }

        @Override
        public Row get(int index) {
            return rubyArray.get(index);
        }

        @Override
        public Row set(int index, Row element) {
            Row oldRow = get(index);
            rubyArray.set(index, element);
            return oldRow;
        }

        @Override
        public void add(int index, Row element) {
            rubyArray.add(index, element);
            setAttribute("rowcount", size(), true);
        }

        @Override
        public Row remove(int index) {
            Row removed = rubyArray.remove(index);
            setAttribute("rowcount", size(), true);
            return removed;
        }

        @Override
        public int indexOf(Object o) {
            if (!(o instanceof RowImpl)) {
                return -1;
            }
            return rubyArray.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            if (!(o instanceof RowImpl)) {
                return -1;
            }
            return rubyArray.lastIndexOf(o);
        }
    }

    protected static Integer calculateLevel(StructuralNode parent) {
        int level = 1;
        if (parent instanceof Table)
            level = parent.getLevel() + 1;
        return level;
    }
}
