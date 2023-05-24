package nl.sanderhautvast.contiguous;

public class NestedBean {
    private StringBean stringBean;

    public NestedBean() {
    }

    public NestedBean(StringBean stringBean) {
        this.stringBean = stringBean;
    }

    public StringBean getStringBean() {
        return stringBean;
    }

    public void setStringBean(StringBean stringBean) {
        this.stringBean = stringBean;
    }
}
