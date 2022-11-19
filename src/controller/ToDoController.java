package controller;

import db.DBConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tm.ToDoTM;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ToDoController {

    public Label lblBanner;
    public Label lblUserID;
    public AnchorPane root;
    public Pane subRoot;
    public TextField txtDescription;
    public ListView<ToDoTM> lstToDo;
    public TextField txtSelectedToDo;
    public Button btnUpdate;
    public Button btnDelete;
    public static String setID;

    public void initialize(){
        lblBanner.setText("Hi "+LoginFormController.loginUserName+", Welcome to To Do List");
        lblUserID.setText(LoginFormController.loginUserID);
        subRoot.setVisible(false);
        loadList();
        setDisableCommon(true);
        lstToDo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoTM>() {
            @Override
            public void changed(ObservableValue<? extends ToDoTM> observable, ToDoTM oldValue, ToDoTM newValue) {
                setDisableCommon(false);
                subRoot.setVisible(false);
                ToDoTM selectedItem = lstToDo.getSelectionModel().getSelectedItem();
                if(selectedItem == null){
                    return;
                }
                txtSelectedToDo.setText(selectedItem.getDescription());
                txtSelectedToDo.requestFocus();
                setID = selectedItem.getId();
            }
        });
    }

    public void setDisableCommon(boolean isDisable){
        txtSelectedToDo.setDisable(isDisable);
        btnDelete.setDisable(isDisable);
        btnUpdate.setDisable(isDisable);
    }

    public void btnLogOutOnAction(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to log out?", ButtonType.YES,ButtonType.NO );
        Optional<ButtonType> buttonType = alert.showAndWait();

        if(buttonType.get().equals(ButtonType.YES)){
            Parent parent = FXMLLoader.load(this.getClass().getResource("../view/LoginForm.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
        }
    }

    public void btnAddNewToDoOnAction(ActionEvent actionEvent) {
        lstToDo.getSelectionModel().clearSelection();
        subRoot.setVisible(true);
        txtDescription.requestFocus();
        setDisableCommon(true);
        txtSelectedToDo.clear();
    }

    public void btnAddToListOnAction(ActionEvent actionEvent) {
        String id = autoGenerateID();
        String description = txtDescription.getText();
        String userID = lblUserID.getText();

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("insert into todo values(?,?,?)");
            preparedStatement.setObject(1,id);
            preparedStatement.setObject(2,description);
            preparedStatement.setObject(3,userID);
            int i = preparedStatement.executeUpdate();

            if(i>0){
                txtDescription.clear();
                subRoot.setVisible(false);
            }
            loadList();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String autoGenerateID() {
        String todoID = null;
        Connection connection = DBConnection.getInstance().getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select id from todo order by id desc limit 1");
            boolean isExit = resultSet.next();
            if(isExit) {
                String userID = resultSet.getString(1);
                userID = userID.substring(1,4);
                int intID = Integer.parseInt(userID);
                intID++;
                if(intID<10){
                    todoID = "T00" + intID;
                }else if(intID<100){
                    todoID = "T0" + intID;
                }else{
                    todoID = "T" + intID;
                }
            }else{
                todoID = "T001";
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return todoID;
    }

    public void loadList(){
        ObservableList<ToDoTM> items = lstToDo.getItems();
        items.clear();

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from todo where user_id = ?");
            preparedStatement.setObject(1,lblUserID.getText());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                String id = resultSet.getString(1);
                String description = resultSet.getString(2);
                String user_id = resultSet.getString(3);
                items.add(new ToDoTM(id, description, user_id));
            }
            lstToDo.refresh();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void btnUpdateOnAction(ActionEvent actionEvent) {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("update todo set description=? where id=?");
            preparedStatement.setObject(1,txtSelectedToDo.getText());
            preparedStatement.setObject(2,setID);
            preparedStatement.executeUpdate();
            loadList();
            txtSelectedToDo.clear();
            setDisableCommon(true);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to delete this todo..?",ButtonType.YES,ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();

        if(buttonType.get().equals(ButtonType.YES)) {
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("delete from todo where id=?");
                preparedStatement.setObject(1, setID);
                preparedStatement.executeUpdate();
                loadList();
                txtSelectedToDo.clear();
                setDisableCommon(true);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
