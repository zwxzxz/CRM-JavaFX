package top.zwx.crm.manage.controller;

import cn.hutool.db.Entity;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import top.zwx.crm.manage.dao.HighSeasPoolGradeCategoryDAO;
import top.zwx.crm.manage.entity.HighSeasPoolGradeCategory;
import top.zwx.crm.manage.util.ComponentUtil;
import top.zwx.crm.manage.util.DaoFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * HighSeasPoolGradeCategoryController 公海池来源类别控制器
 *
 * @author zwx
 */
public class HighSeasPoolGradeCategoryController implements Initializable {
    /**
     * 获得布局文件中的表格对象
     */
    @FXML
    private TableView<HighSeasPoolGradeCategory> gradecategoryTable;

    /**
     * 定义ObservableList数据集合
     */
    private final ObservableList<HighSeasPoolGradeCategory> gradecategoryData = FXCollections.observableArrayList();

    /**
     * 通过工厂类获得HighSeasPoolGradeCategoryDAO的实例
     */
    private final HighSeasPoolGradeCategoryDAO gradecategoryDAO = DaoFactory.getHighSeasPoolGradeCategoryDAOInstance();

    /**
     * 定义实体集合，用来存放数据库查询结果
     */
    private List<Entity> entityList = null;

    private final TableColumn<HighSeasPoolGradeCategory, HighSeasPoolGradeCategory> delCol = new TableColumn<>("操作");


    @Override
    public void initialize(URL location, ResourceBundle regrades) {
        //水平方向不显示滚动条
        gradecategoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        //在表格最后加入删除按钮
        delCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        delCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = ComponentUtil.getButton("删除", "warning-theme");

            @Override
            protected void updateItem(HighSeasPoolGradeCategory gradecategory, boolean empty) {
                super.updateItem(gradecategory, empty);
                if (gradecategory == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(deleteButton);
                //点击删除按钮，需要将这一行从表格移除，同时从底层数据库真正删除
                deleteButton.setOnAction(event -> {
                    //删除操作之前，弹出确认对话框，点击确认按钮才删除
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("确认对话框");
                    alert.setHeaderText("请确认");
                    alert.setContentText("确定要删除这行记录吗?");
                    Optional<ButtonType> result = alert.showAndWait();
                    //点击了确认按钮，执行删除操作，同时移除一行模型数据
                    if (result.get() == ButtonType.OK) {
                        gradecategoryData.remove(gradecategory);
                        try {
                            gradecategoryDAO.deleteById(gradecategory.getId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        //删除列加入表格
        gradecategoryTable.getColumns().add(delCol);
        try {
            entityList = gradecategoryDAO.selectAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showCategoryData(entityList);
    }

    public void addCategory() {
        //创建一个输入对话框
        TextInputDialog dialog = new TextInputDialog("新类别");
        dialog.setTitle("客户等级类别");
        dialog.setHeaderText("新增类别");
        dialog.setContentText("请输入客户等级名称:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> System.out.println("你的输入： " + name));
        //确认输入了内容
        if (result.isPresent()) {
            //获得输入的内容
            String name = result.get();
            //创建一个Type对象，插入数据库，并返回主键
            HighSeasPoolGradeCategory gradecategory = new HighSeasPoolGradeCategory();
            gradecategory.setGradename(name);
            long id = 0;
            try {
                id = gradecategoryDAO.insert(gradecategory);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            gradecategory.setId(id);
            //加入ObservableList，刷新模型视图，不用重新查询数据库也可以立刻看到结果
            gradecategoryData.add(gradecategory);
        }
    }

    private void showCategoryData(List<Entity> entityList) {
        //遍历实体集合
        for (Entity entity : entityList) {
            //取出属性，创建Type的对象
            HighSeasPoolGradeCategory gradecategory = new HighSeasPoolGradeCategory();
            gradecategory.setId(entity.getInt("id"));
            gradecategory.setGradename(entity.getStr("gradename"));
            //加入ObservableList模型数据集合
            gradecategoryData.add(gradecategory);
        }
        gradecategoryTable.setItems(gradecategoryData);
    }

}


