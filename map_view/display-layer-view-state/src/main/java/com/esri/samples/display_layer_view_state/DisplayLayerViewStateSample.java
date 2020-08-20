/*
 * Copyright 2017 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.esri.samples.display_layer_view_state;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.esri.arcgisruntime.loadable.LoadStatus;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.LayerViewStatus;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

public class DisplayLayerViewStateSample extends Application {

  private MapView mapView;
  private FeatureLayer featureLayer;

  @Override
  public void start(Stage stage) {

    try {
      // create the stack pane and application scene
      StackPane stackPane = new StackPane();
      Scene scene = new Scene(stackPane);
      scene.getStylesheets().add(getClass().getResource("/display_layer_view_state/style.css").toExternalForm());

      // set a title, size, and add the scene to the stage
      stage.setTitle("Display Layer View State Sample");
      stage.setWidth(800);
      stage.setHeight(700);
      stage.setScene(scene);
      stage.show();

      // create an ArcGISMap with the topographic basemap
      ArcGISMap map = new ArcGISMap(Basemap.createTopographic());

      // create a map view and set the ArcGISMap to it
      mapView = new MapView();
      mapView.setMap(map);

      // set the initial viewpoint for the map view
      mapView.setViewpoint(new Viewpoint(new Point(-11e6, 45e5, SpatialReferences.getWebMercator()), 40000000));

      // create a label to display the view status
      Label layerViewStatusLabel = new Label("Loading feature layer...");
      
      // set up checkbox UI
      CheckBox visibilityCheckBox = new CheckBox();
      visibilityCheckBox.setText("Layer active ");
      visibilityCheckBox.setSelected(true);
      visibilityCheckBox.setDisable(true);
      visibilityCheckBox.setTextFill(Color.WHITE);

      // create a new feature layer from a portal item
      final PortalItem portalItem = new PortalItem(new Portal("https://runtime.maps.arcgis.com/"),
        "b8f4033069f141729ffb298b7418b653");
      featureLayer = new FeatureLayer(portalItem, 0);
      //  set a minimum and a maximum scale for the visibility of the feature layer
      featureLayer.setMinScale(40000000);
      featureLayer.setMaxScale(4000000);
      // add the feature layer to the map
      map.getOperationalLayers().add(featureLayer);

      featureLayer.addDoneLoadingListener(() -> {
        if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
          // if the feature layer has loaded, create a listener that fires every time the layer's view status changes

          mapView.addLayerViewStateChangedListener(statusChangeEvent -> {
            // check the layer whose state has changed is the feature layer
            if (statusChangeEvent.getLayer() == featureLayer) {

              // get the layer's view status and display the status
              EnumSet<LayerViewStatus> layerViewStatus = statusChangeEvent.getLayerViewStatus();
              List<String> stringList = new ArrayList<>(layerViewStatus.size());
              for (Enum<LayerViewStatus> enumName : layerViewStatus) {
                String enumNameTidied = enumName.toString().toLowerCase().replace("_", " ");
                stringList.add(enumNameTidied);
              }
              layerViewStatusLabel.setText("Current view status: " + String.join(", ", stringList));

              // enable UI interaction once feature layer has loaded
              visibilityCheckBox.setDisable(false);
              // create a listener for clicks on the checkbox
              visibilityCheckBox.setOnAction(event -> featureLayer.setVisible(visibilityCheckBox.isSelected()));

              // show an alert if a warning is detected from the state change
              ArcGISRuntimeException statusError = statusChangeEvent.getError();
              if (statusError != null) {
                new Alert(Alert.AlertType.ERROR, "Unable to update layer view status " +
                  statusChangeEvent.getError()).show();
              }
            }
          });

        } else {
          new Alert(Alert.AlertType.ERROR, "Feature layer failed to load").show();
        }
      });

      // create a control panel and add the label and button
      VBox controlsVBox = new VBox(6);
      controlsVBox.setAlignment(Pos.TOP_LEFT);
      controlsVBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(0,0,0,0.5)"), CornerRadii.EMPTY,
        Insets.EMPTY)));
      controlsVBox.setPadding(new Insets(10.0));
      controlsVBox.setMaxSize(300, 80);
      controlsVBox.getStyleClass().add("panel-region");
      controlsVBox.getChildren().addAll(layerViewStatusLabel, visibilityCheckBox);

      // add the map view and control panel to the stack pane
      stackPane.getChildren().addAll(mapView, controlsVBox);
      StackPane.setAlignment(controlsVBox, Pos.TOP_LEFT);
      StackPane.setMargin(controlsVBox, new Insets(10, 0, 0, 10));
    } catch (Exception e) {
      // on any error, display the stack trace
      e.printStackTrace();
    }
  }

  /**
   * Stops and releases all resources used in application.
   */
  @Override
  public void stop() {

    if (mapView != null) {
      mapView.dispose();
    }
  }

  /**
   * Opens and runs application.
   *
   * @param args arguments passed to this application
   */
  public static void main(String[] args) {

    Application.launch(args);
  }
}
