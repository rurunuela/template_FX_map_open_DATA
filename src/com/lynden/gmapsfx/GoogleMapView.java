/*
 * Copyright 2014 Lynden, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lynden.gmapsfx;

import com.lynden.gmapsfx.javascript.JavaFxWebEngine;
import com.lynden.gmapsfx.javascript.JavascriptRuntime;
import com.lynden.gmapsfx.javascript.event.MapStateEventType;
import com.lynden.gmapsfx.javascript.object.DirectionsPane;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 *
 * @author Rob Terpilowski
 */
public class GoogleMapView extends AnchorPane {

    protected static final String GOOGLE_MAPS_API_LINK = "https://maps.googleapis.com/maps/api/js?v=3.exp";

    protected final String language;
    protected final String key;
    protected WebView webview;
    protected JavaFxWebEngine webengine;
    protected boolean initialized = false;
    protected final CyclicBarrier barrier = new CyclicBarrier(2);
    protected final List<MapComponentInitializedListener> mapInitializedListeners = new ArrayList<>();
    protected final List<MapReadyListener> mapReadyListeners = new ArrayList<>();
    protected GoogleMap map;
    protected DirectionsPane direc;

    public GoogleMapView() {
        this(false);
    }

    public GoogleMapView(boolean debug) {
        this(null, debug);
    }

    /**
     * Allows for the creation of the map using external resources from another
     * jar for the html page and markers. The map html page must be sourced from
     * the jar containing any marker images for those to function.
     * <p>
     * The html page is, at it's simplest:
     * {@code 
	 * <!DOCTYPE html>
     * <html>
     *   <head>
     *     <meta name="viewport" content="initial-scale=1.0, user-scalable=no">
     *     <meta charset="utf-8">
     *     <title>My Map</title>
     *     <style>
     *     html, body, #map-canvas {
     *       height: 100%;
     *       margin: 0px;
     *       padding: 0px
     *     }
     * </style>
     * <script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
     * </head>
     * <body>
     * <div id="map-canvas"></div>
     * </body>
     * </html>
     * }
     * <p>
     * If you store this file in your project jar, under
     * my.gmapsfx.project.resources as mymap.html then you should call using
     * "/my/gmapsfx/project/resources/mymap.html" for the mapResourcePath.
     * <p>
     * Your marker images should be stored in the same folder as, or below the
     * map file. You then reference them using relative notation. If you put
     * them in a subpackage "markers" you would create your MarkerOptions object
     * as follows:
     * {@code
	 * myMarkerOptions.position(myLatLong)
     *     .title("My Marker")
     *     .icon("markers/mymarker.png")
     *     .visible(true);
     * }
     *
     * @param mapResourcePath
     */
    public GoogleMapView(String mapResourcePath) {
        this(mapResourcePath, false);
    }

    /**
     * Creates a new map view and specifies if the FireBug pane should be
     * displayed in the WebView
     *
     * @param mapResourcePath
     * @param debug true if the FireBug pane should be displayed in the WebView.
     */
    public GoogleMapView(String mapResourcePath, boolean debug) {
        this(mapResourcePath, null, null, debug);
    }
    
    /**
     * Creates a new map view and specifies the display language and API key.
     *
     * @param language map display language, null for default
     * @param key Google Maps API key or null
     */
    public GoogleMapView(String language, String key) {
        this(null, language, key, false);
    }

    /**
     * Creates a new map view and specifies the display language and API key.
     *
     * @param mapResourcePath
     * @param language map display language, null for default
     * @param key Google Maps API key or null
     * @param debug true if the FireBug pane should be displayed in the WebView.
     */
    public GoogleMapView(String mapResourcePath, String language, String key, boolean debug) {
        this.language = language;
        this.key = key;

        String htmlFile;
        if (mapResourcePath == null) {
            if (debug) {
                htmlFile = "/html/maps-debug.html";
            } else {
                htmlFile = "/html/maps.html";
            }
        } else {
            htmlFile = mapResourcePath;
        }

        String htmlText;
        try {
            StringBuilder text = loadText(htmlFile);
            text = modifyHtml(text);

            htmlText = text.toString();
        } catch (Exception e) {
            String exception = e.getClass().getSimpleName();
            if (e.getMessage() != null)
                exception += ": " + e.getMessage();

            throw new IllegalStateException("Couldn't load map file '" + htmlFile + "': " + exception);
        }
        
        String htmlText1 = htmlText;
        if(language != null){
            String lang = "https://maps.googleapis.com/maps/api/js?v=3.exp&language="+language;
            htmlText1 = htmlText.replace("https://maps.googleapis.com/maps/api/js?v=3.exp",
                lang);
        }
        String htmlText2 = htmlText1;
        
        
        CountDownLatch latch = new CountDownLatch(1);
        Runnable initWebView = () -> {
            try {
                webview = new WebView();
                webengine = new JavaFxWebEngine(webview.getEngine());
                JavascriptRuntime.setDefaultWebEngine(webengine);

                setTopAnchor(webview, 0.0);
                setLeftAnchor(webview, 0.0);
                setBottomAnchor(webview, 0.0);
                setRightAnchor(webview, 0.0);
                getChildren().add(webview);

                webview.widthProperty().addListener(e -> mapResized());
                webview.heightProperty().addListener(e -> mapResized());

                webview.widthProperty().addListener(e -> mapResized());
                webview.heightProperty().addListener(e -> mapResized());

                webengine.getLoadWorker().stateProperty().addListener(
                        new ChangeListener<Worker.State>() {
                            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                                if (newState == Worker.State.SUCCEEDED) {
                                    setInitialized(true);
                                    fireMapInitializedListeners();

                                }
                            }
                        });
                webengine.loadContent(htmlText2);
            } finally {
                latch.countDown();
            }
        };

        if (Platform.isFxApplicationThread()) {
            initWebView.run();
        } else {
            Platform.runLater(initWebView);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected StringBuilder loadText(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path), "UTF-8"))) {
            reader.lines().forEach(sb::append);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return sb;
    }

    protected StringBuilder modifyHtml(StringBuilder text) {
        int index = getGoogleMapsApiParamsIndex(text);
        if (index != -1) {
            if (language != null)
                text.insert(index, "&language=" + language);
            if (key != null)
                text.insert(index, "&key=" + key);
        }

        return text;
    }

    protected static int getGoogleMapsApiParamsIndex(StringBuilder text) {
        int index = text.indexOf(GOOGLE_MAPS_API_LINK);
        if (index != -1)
            index += GOOGLE_MAPS_API_LINK.length();
        return index;
    }

    private void mapResized() {
        if (initialized) {
            //map.triggerResized();
//            System.out.println("GoogleMapView.mapResized: triggering resize event");
            webengine.executeScript("google.maps.event.trigger(" + map.getVariableName() + ", 'resize')");
//            System.out.println("GoogleMapView.mapResized: triggering resize event done");
        }
    }

    public void setZoom(int zoom) {
        checkInitialized();
        map.setZoom(zoom);
    }

    public void setCenter(double latitude, double longitude) {
        checkInitialized();
        LatLong latLong = new LatLong(latitude, longitude);
        map.setCenter(latLong);
    }

    public GoogleMap getMap() {
        checkInitialized();
        return map;
    }

    public GoogleMap createMap(MapOptions mapOptions) {
        return createMap(mapOptions,false);
    }

    public GoogleMap createMap() {
        return createMap(null, false);
    }
    
    public GoogleMap createMap(boolean withDirectionsPanel) {
        return createMap(null, withDirectionsPanel);
    }
    
    public GoogleMap createMap(MapOptions mapOptions, boolean withDirectionsPanel ) {
        checkInitialized();
        if( mapOptions != null ) {
            map = new GoogleMap(mapOptions);
        } else {
            map = new GoogleMap();
        }
        
        direc = new DirectionsPane();
        if( withDirectionsPanel ) {
            map.showDirectionsPane();
        }
        
        map.addStateEventHandler(MapStateEventType.projection_changed, () -> {
            if (map.getProjection() != null) {
                mapResized();
                fireMapReadyListeners();
            }
        });

        return map;        
    }

    public DirectionsPane getDirec() {
        return direc;
    }
    
    public void addMapInializedListener(MapComponentInitializedListener listener) {
        synchronized (mapInitializedListeners) {
            mapInitializedListeners.add(listener);
        }
    }

    public void removeMapInitializedListener(MapComponentInitializedListener listener) {
        synchronized (mapInitializedListeners) {
            mapInitializedListeners.remove(listener);
        }
    }

    public void addMapReadyListener(MapReadyListener listener) {
        synchronized (mapReadyListeners) {
            mapReadyListeners.add(listener);
        }
    }

    public void removeReadyListener(MapReadyListener listener) {
        synchronized (mapReadyListeners) {
            mapReadyListeners.remove(listener);
        }
    }

    public Point2D fromLatLngToPoint(LatLong loc) {
        checkInitialized();
        return map.fromLatLngToPoint(loc);
    }

    public void panBy(double x, double y) {
        checkInitialized();
        map.panBy(x, y);
    }

    protected void init() {

    }

    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    protected void fireMapInitializedListeners() {
        synchronized (mapInitializedListeners) {
            for (MapComponentInitializedListener listener : mapInitializedListeners) {
                listener.mapInitialized();
            }
        }
    }

    protected void fireMapReadyListeners() {
        synchronized (mapReadyListeners) {
            for (MapReadyListener listener : mapReadyListeners) {
                listener.mapReady();
            }
        }
    }

    protected JSObject executeJavascript(String function) {
        Object returnObject = webengine.executeScript(function);
        return (JSObject) returnObject;
    }

    protected String getJavascriptMethod(String methodName, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName).append("(");
        for (Object arg : args) {
            sb.append(arg).append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");

        return sb.toString();
    }

    protected void checkInitialized() {
        if (!initialized) {
            throw new MapNotInitializedException();
        }
    }

    public class JSListener {

        public void log(String text) {
            System.out.println(text);
        }
    }

    public WebView getWebview() {
        return webview;
    }
    
}
