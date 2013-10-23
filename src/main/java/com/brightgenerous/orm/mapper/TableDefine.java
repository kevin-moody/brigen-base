package com.brightgenerous.orm.mapper;

import java.io.Serializable;
import java.util.Map;

import com.brightgenerous.commons.EqualsUtils;
import com.brightgenerous.commons.HashCodeUtils;
import com.brightgenerous.commons.ToStringUtils;
import com.brightgenerous.lang.Args;

public class TableDefine implements Serializable {

    private static final long serialVersionUID = 7210435864811345378L;

    private final TableMapper tableMapper;

    private final Map<String, TableMapper> propertyTableMappers;

    public TableDefine(TableMapper tableMapper) {
        this(tableMapper, null);
    }

    public TableDefine(TableMapper tableMapper, Map<String, TableMapper> propertyTableMappers) {
        Args.notNull(tableMapper, "tableMapper");

        this.tableMapper = tableMapper;
        this.propertyTableMappers = propertyTableMappers;
    }

    public TableMapper getTableMapper() {
        return tableMapper;
    }

    public Map<String, TableMapper> getPropertyTableMappers() {
        return propertyTableMappers;
    }

    @Override
    public int hashCode() {
        if (HashCodeUtils.useful()) {
            return HashCodeUtils.hashCodeAlt(null, this);
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (EqualsUtils.useful()) {
            return EqualsUtils.equalsAlt(null, this, obj);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        if (ToStringUtils.useful()) {
            return ToStringUtils.toStringAlt(this);
        }
        return super.toString();
    }
}
