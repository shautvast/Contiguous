package com.github.shautvast.contiguous;

public class NestedBeanHandler extends CompoundTypeHandler{
    NestedBeanHandler(Class<NestedBean> type) {
        super(type);
    }

    NestedBeanHandler(Class<?> type, String propertyName) {
        super(type, propertyName);
    }
}
