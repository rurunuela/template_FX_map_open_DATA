package Hubo.com;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import com.lynden.gmapsfx.GoogleMapView;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.event.ChangeEvent;
import javafx.beans.value.ChangeListener;
public class Programme extends Application   {


    private GoogleMapView mapView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("#Template FX MAP OPEN_DATA");
        Scene scene = new Scene(new VBox(), 700, 550);
        scene.setFill(Color.OLDLACE);



        SplitPane sp = new SplitPane();
        sp.setPrefWidth(scene.getWidth());
        sp.setPrefHeight(scene.getHeight());
        ListView<agent> l1 = new ListView<agent>();
        ObservableList<agent> data = FXCollections.observableArrayList(new agent(),new agent());
                // ListView l2 = new ListView();
        l1.setItems(data);


        //javafx.scene.canvas.Canvas cnv = new Canvas();
        //cnv.setWidth(400);
        mapView = new GoogleMapView();
        MapManager myMapManage = new MapManager();
        myMapManage.setMapView(mapView);
        mapView.addMapInializedListener(myMapManage);
        //sp.getItems().addAll(l1, l2, l3 );
        //sp.getItems().addAll(l1, cnv);
        sp.getItems().addAll(l1, mapView);
        sp.setDividerPositions(0.3f);


        ((VBox) scene.getRoot()).getChildren().addAll(sp);

        SplitPane.setResizableWithParent(sp, Boolean.TRUE);


                stage.setScene(scene);
        stage.show();
    }



}