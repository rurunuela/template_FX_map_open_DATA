package Hubo.com;

import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.Circle;
import com.lynden.gmapsfx.shapes.CircleOptions;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import netscape.javascript.JSObject;
/**
 * Created by richard on 28/07/2016.
 */
public class MapManager implements MapComponentInitializedListener {
    GoogleMap map;
    private GoogleMapView mapView;
    ArrayList<Marker> listeM;

    @Override

    public void mapInitialized() {


        //Set the initial properties of the map.
        MapOptions mapOptions = new MapOptions();

        mapOptions.center(new LatLong(47.2160584002019,-1.55784409350264))
                // .mapType(MapType.ROADMAP)
                .overviewMapControl(true)
                        //.panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                        //.streetViewControl(false)
                .zoomControl(false)
                .zoom(16)

        ;

        map = mapView.createMap(mapOptions);

        //Add a marker to the map
        //MarkerOptions markerOptions = new MarkerOptions();

        /*markerOptions.position( new LatLong(47.6, -122.3) )
                .visible(Boolean.TRUE)
                .title("My Marker");

        Marker marker = new Marker( markerOptions );

        map.addMarker(marker);*/




        //Add a marker



        OpenDataManager openDataManager = new OpenDataManager();
        listeM = openDataManager.getListeMarker();
        Iterator imap = listeM.iterator();
        while (imap.hasNext()) {
            Marker marker = (Marker) imap.next();
            map.addMarker(marker);
        }







    }

    public void setMapView(GoogleMapView mapView) {
        this.mapView = mapView;
    }
}
