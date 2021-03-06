package top.zwx.crm.manage.controller;

import cn.hutool.db.Entity;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import top.zwx.crm.manage.Main;
import top.zwx.crm.manage.dao.ClueDAO;
import top.zwx.crm.manage.dao.ClueSourceCategoryDAO;
import top.zwx.crm.manage.entity.CluePool;
import top.zwx.crm.manage.entity.ClueSourceCategory;
import top.zwx.crm.manage.entity.Clue;
import top.zwx.crm.manage.util.ComponentUtil;
import top.zwx.crm.manage.util.DaoFactory;
import top.zwx.crm.manage.util.ExcelExport;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * ClueController
 * 线索
 * @author mqxu
 */
public class ClueController implements Initializable {
    
    /**
     * 布局文件中的表格视图对象，用来显示数据库中读取的所有线索信息
     */
    @FXML
    private TableView<Clue> clueTable;

    /**
     * 布局文件中的下拉框组件对象，用来显示数据库中读取的所有线索来源类别
     */
    @FXML
    private ComboBox<ClueSourceCategory> categoryComboBox;

    /**
     * 布局文件中的输入文本框对象，用来输入搜索关键词
     */
    @FXML
    private TextField keywordsField;

    /**
     * 线索模型数据集合，可以实时相应数据变化，无需刷新
     */
    private final ObservableList<Clue> clueData = FXCollections.observableArrayList();

    /**
     * 线索来源类型模型数据集合
     */
    private final ObservableList<ClueSourceCategory> categoryData = FXCollections.observableArrayList();

    /**
     * 线索DAO对象，从DAO工厂通过静态方法获得
     */
    private final ClueDAO clueDAO = DaoFactory.getclueDAOInstance();

    /**
     * 线索来源类别DAO对象
     */
    private final ClueSourceCategoryDAO categoryDAO = DaoFactory.getClueSourceCategoryDAOInstance();

    /**
     * 线索实体集合，存放数据库Clue表各种查询的结果
     */
    private List<Entity> clueList = null;

    /**
     * 类别实体集合，存放数据库类别表查询结果
     */
    private List<Entity> categoryList = null;
    
    /**
     * 表格中的删除行
     */
    private final TableColumn<Clue, Clue> delCol = new TableColumn<>("操作");

    /**
     * 初始化方法，通过调用对线索池表格和列表下拉框的两个封装方法，实现数据初始化
     *
     * @param location  location
     * @param resources resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
        initComBox();
    }

    /**
     * 表格初始化方法
     */
    private void initTable() {
        //水平方向不显示滚动条，表格的列宽会均匀分布
        clueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        //1.调用底层查询所有线索
        try {
            clueList = clueDAO.selectAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //将实体集合作为参数，调用显示数据的方法，可以在界面的表格中显示线索模型集合的值
        showclueData(clueList);

        //3.删除列的相关设置
        delCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        delCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = ComponentUtil.getButton("删除", "warning-theme");

            @Override
            protected void updateItem(Clue clue, boolean empty) {
                super.updateItem(clue, empty);
                if (clue == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(deleteButton);
                //点击删除按钮，需要将这一行从表格移除，同时从底层数据库真正删除
                deleteButton.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("确认对话框");
                    alert.setHeaderText("客户：" + clue.getName());
                    alert.setContentText("确定要删除这行记录吗?");
                    Optional<ButtonType> result = alert.showAndWait();
                    //点击了确认按钮，执行删除操作，同时移除一行模型数据
                    if (result.get() == ButtonType.OK) {
                        clueData.remove(clue);
                        try {
                            clueDAO.deleteById(clue.getId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        //将删除列加入表格
        clueTable.getColumns().add(delCol);


        //4.线索表格双击事件,双击弹出显示线索详情的界面
        clueTable.setRowFactory(tv -> {
            TableRow<Clue> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                //判断鼠标双击了一行
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    //获得该行的ID属性
                    long id = row.getItem().getId();
                    CluePool cluePool = new CluePool();
                    try {
                        //通过ID到数据库查询到该线索对象的完整信息，因为表格中没有显示全，
                        // 但是在详情界面需要这些数据，如果不查出完整信息，会有空值异常
                        Entity entity = clueDAO.getById(id);
                        cluePool.setId(entity.getLong("id"));
                        cluePool.setName(entity.getStr("name"));
                        cluePool.setCompany(entity.getStr("company"));
                        cluePool.setSource(entity.getStr("source"));
                        cluePool.setDetailed(entity.getStr("detailed"));
                        cluePool.setPool(entity.getStr("pool"));
                        DatePicker datePicker = new DatePicker();
                        datePicker.setValue(LocalDate.now());
                        LocalDate createDate = datePicker.getValue();
                        cluePool.setCreatetime(createDate);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    //创建一个新的线索详情界面窗口
                    Stage customerInfoStage = new Stage();
                    customerInfoStage.setTitle("线索详情界面");
                    //用VBox显示具体线索信息
                    VBox vBox = new VBox();
                    vBox.setSpacing(10);
                    vBox.setAlignment(Pos.CENTER);
                    vBox.setPrefSize(600, 400);
                    vBox.setPadding(new Insets(10, 10, 10, 10));
                    Label nameLabel = new Label("姓名：" + cluePool.getName());
                    nameLabel.getStyleClass().add("font-title");
                    Label authorLabel = new Label("公司：" + cluePool.getCompany());
                    Label priceLabel = new Label("线索来源：" + cluePool.getSource());
                    Label summaryLabel = new Label("线索详细：" + cluePool.getDetailed());
                    summaryLabel.setPrefWidth(400);
                    summaryLabel.setWrapText(true);
                    summaryLabel.getStyleClass().add("box");
                    vBox.getChildren().addAll(nameLabel, authorLabel, priceLabel, summaryLabel);
                    Scene scene = new Scene(vBox, 640, 480);
                    //因为是一个新的窗口，需要重新读入一下样式表，这个界面就可以使用style.css样式表中的样式了
                    scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("css/style.css")).toExternalForm());
                    customerInfoStage.setScene(scene);
                    customerInfoStage.show();
                }
            });
            return row;
        });
    }

    /**
     * 下拉框初始化方法
     */
    private void initComBox() {
        //1.到数据库查询所有的类别
        try {
            categoryList = categoryDAO.selectAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //2.遍历categoryList集合，将其加入categoryData模型数据集合
        for (Entity entity : categoryList) {
            ClueSourceCategory category = new ClueSourceCategory();
            category.setId(entity.getLong("id"));
            category.setSourcename(entity.getStr("sourcename"));
            categoryData.add(category);
        }
        categoryComboBox.setItems(categoryData);

        //4.下拉框选择事件监听，根据选择不同的类别，图书表格中会过滤出该类别的线索
        categoryComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
                    System.out.println(newValue.getId() + "," + newValue.getSourcename());
                    //移除掉之前的数据
                    clueTable.getItems().removeAll(clueData);
                    try {
                        //根据选中的类别查询该类别所有线索
                        clueList = clueDAO.selectByCategoryId(newValue.getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    //重新显示数据
                    showclueData(clueList);
                }
        );
    }

    /**
     * 显示线索表格数据的方法
     *
     * @param clueList 线索列表
     */
    private void showclueData(List<Entity> clueList) {
        for (Entity entity : clueList) {
            Clue clue = new Clue();
            clue.setId(entity.getLong("id"));
            clue.setName(entity.getStr("name"));
            clue.setCompany(entity.getStr("company"));
            clue.setSource(entity.getStr("source"));
            clue.setDetailed(entity.getStr("detailed"));
            clue.setPool(entity.getStr("pool"));
            clue.setCreatetime(entity.getDate("createtime").toString());
            clueData.add(clue);
        }
        clueTable.setItems(clueData);
    }

    /**
     * 根据关键词搜索方法
     */
    public void search() {
        clueTable.getItems().removeAll(clueData);
        //获得输入的查询关键字
        String keywords = keywordsField.getText().trim();
        try {
            clueList = clueDAO.selectByKeywords(keywords);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showclueData(clueList);
    }

    /**
     * 数据导出方法，采用hutool提供的工具类
     */
    public void export() {
        ExcelExport.export1(clueList);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示信息");
        alert.setHeaderText("线索已导出!");
        alert.showAndWait();
    }
}
