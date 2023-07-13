package cn.knet.suggest.editor;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * 字符串过滤器
 * @author xuxiannian
 *
 */
public class StringPropertyEditor extends PropertyEditorSupport{
    private final String charsToDelete;

    private final boolean emptyAsNull;

    /**
     *
     * @param emptyAsNull
     */
    public StringPropertyEditor(final boolean emptyAsNull) {
        this.charsToDelete = null;
        this.emptyAsNull = emptyAsNull;
    }

    /**
     *
     * @param charsToDelete
     * @param emptyAsNull
     */
    public StringPropertyEditor(final String charsToDelete, final boolean emptyAsNull) {
        this.charsToDelete = charsToDelete;
        this.emptyAsNull = emptyAsNull;
    }


    @Override
    public void setAsText(String text) {
        if (text == null) {
            setValue(null);
        }else {
            String value = text.trim();
            if (this.charsToDelete != null) {
                value = StringUtils.deleteAny(value, this.charsToDelete);
            }
            if (this.emptyAsNull && "".equals(value)) {
                setValue(null);
            }else {
                setValue(value);
            }
        }
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        if(value == null) {
            return "";
        }
        return  value.toString() ;
    }


}