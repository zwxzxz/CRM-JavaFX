
package top.zwx.crm.manage.entity;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * 公海池来源类别实体类
 *
 * @author zwx
 */
public class HighSeasPoolSourceCategory {
    /**
     * 以JavaFX属性绑定的形式，定义和数据表字段id和type_name对应的属性，注意命名规范
     */
    private final SimpleLongProperty id = new SimpleLongProperty();
    private final SimpleStringProperty sourcename = new SimpleStringProperty("");

    public HighSeasPoolSourceCategory() {
    }

    public HighSeasPoolSourceCategory(long id, String sourcename) {
        setId(id);
        setSourcename(sourcename);
    }

    public long getId() {
        return id.get();
    }

    public SimpleLongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public String getSourcename() {
        return sourcename.get();
    }

    public SimpleStringProperty sourcenameProperty() {
        return sourcename;
    }

    public void setSourcename(String sourcename) {
        this.sourcename.set(sourcename);
    }

    @Override
    public String toString() {
        return sourcename.get();
    }
}
