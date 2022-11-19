package controller;

import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class CreateNewAccountController {
    public Label lblPasswordNotMatched1;
    public Label lblPasswordNotMatched2;
    public PasswordField txtNewPassword;
    public PasswordField txtConfirmPassword;
    public TextField txtEmail;
    public TextField txtUserName;
    public Label lblUserID;
    public AnchorPane root;

    public void initialize(){
        setLabelVisibility(false);
        setDisableCommon(true);
    }

    public void btnRegisterOnAction(ActionEvent actionEvent) {
        register();
    }

    public void setDisableCommon(boolean isDisable){
        txtUserName.setDisable(isDisable);
        txtEmail.setDisable(isDisable);
        txtNewPassword.setDisable(isDisable);
        txtConfirmPassword.setDisable(isDisable);
    }

    public void setBorderColor(String color){
        txtNewPassword.setStyle("-fx-border-color:"+ color);
        txtConfirmPassword.setStyle("-fx-border-color:"+ color);
    }

    public void setLabelVisibility(boolean isVisible){
        lblPasswordNotMatched1.setVisible(isVisible);
        lblPasswordNotMatched2.setVisible(isVisible);
    }

    public void btnAddUserOnAction(ActionEvent actionEvent) {
        setDisableCommon(false);
        txtUserName.requestFocus();
        autoGenerateUserID();
    }

    public void autoGenerateUserID() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select id from user order by id desc limit 1");
            boolean isExit = resultSet.next();
            if(isExit) {
                String userID = resultSet.getString(1);
                userID = userID.substring(1,4);
                int intID = Integer.parseInt(userID);
                intID++;
                if(intID<10){
                    lblUserID.setText("U00"+intID);
                }else if(intID<100){
                    lblUserID.setText("U0"+intID);
                }else{
                    lblUserID.setText("U"+intID);
                }
            }else{
                lblUserID.setText("U001");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void txtConfirmPasswordOnAction(ActionEvent actionEvent) {
        register();
    }

    public void register(){
        if(txtUserName.getText().isEmpty()){
            txtUserName.requestFocus();
        }else if(txtEmail.getText().isEmpty()){
            txtEmail.requestFocus();
        }else if(txtNewPassword.getText().isEmpty()){
            txtNewPassword.requestFocus();
        }else if(txtConfirmPassword.getText().isEmpty()){
            txtConfirmPassword.requestFocus();
        }else {
            String password = txtNewPassword.getText();
            String confirmPassword = txtConfirmPassword.getText();

            if (password.equals(confirmPassword)) {
                setBorderColor("transparent");
                setLabelVisibility(false);
                String id = lblUserID.getText();
                String userName = txtUserName.getText();
                String email = txtEmail.getText();
                Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = connection.prepareStatement("insert into user values(?,?,?,?)");
                    preparedStatement.setObject(1, id);
                    preparedStatement.setObject(2, userName);
                    preparedStatement.setObject(3, email);
                    preparedStatement.setObject(4, confirmPassword);
                    int i = preparedStatement.executeUpdate();
                    System.out.println(i);

                    if (i > 0) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Success..!");
                        alert.showAndWait();
                        Parent parent = FXMLLoader.load(this.getClass().getResource("../view/LoginForm.fxml"));
                        Scene scene = new Scene(parent);
                        Stage stage = (Stage) root.getScene().getWindow();
                        stage.setScene(scene);
                        stage.setTitle("Login");
                        stage.centerOnScreen();
                    }
                } catch (SQLException | IOException throwables) {
                    throwables.printStackTrace();
                }
            } else {
                setBorderColor("red");
                setLabelVisibility(true);
                txtConfirmPassword.clear();
                txtNewPassword.clear();
                txtNewPassword.requestFocus();
            }
        }
    }
}
