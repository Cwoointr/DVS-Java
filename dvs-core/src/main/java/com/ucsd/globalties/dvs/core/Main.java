package com.ucsd.globalties.dvs.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ucsd.globalties.dvs.core.ui.RootViewController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * The entry point of the program.
 * Loads OpenCV library, resources, and launches the JavaFX UI.
 * @author Rahul
 *
 */
public class Main extends Application {
  // OpenCV constants for face/eye detection
  private static final String HAAR_FACE = "/haarcascade_frontalface_alt.xml";
  private static final String HAAR_EYE = "/haarcascade_eye.xml";

  // Mostly front end constants.
  // TODO move to constants class or something
  public static final String[] sceneLabels = { "Name", "Date of Birth", "Gender", "Ethnicity", "Language",
      "Room Number", "School", "Screening Comment" };
  public static final String inputScreenID = "inputGrid";
  public static final String inputScreenFile = "/views/input_grid.fxml";
  public static final String photoGridID = "photoGrid";
  public static final String photoGridFile = "/views/photo_grid.fxml";
  public static final String detectGridID = "detectGrid";
  public static final String detectGridFile = "/views/detect_grid.fxml";
  public static final String resultGridID = "resultGrid";
  public static final String resultGridFile = "/views/result_grid.fxml";

  // The paths of the face/eye detection resource fields
  public static String HAAR_FACE_PATH;
  public static String HAAR_EYE_PATH;

  private static int BUFFER_SIZE = Short.MAX_VALUE;
  private static Controller controller;
  public static final String OUTPUT_FILE = "output/";

  public static void main(String[] args) {
    (new File("output")).mkdir(); // create temporary output folder
    controller = new Controller();
    loadLibraryComponents();
    launch(args);
  }

  /**
   * Load the OpenCV resources used for face and eye detection.
   */
  public static void loadLibraryComponents() {
    // load OpenCV constants
    // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    loadOpencvLibrary();
    // Workaround for "/" thing, load as file then get filepath
    File fp = new File(Main.class.getResource(HAAR_FACE).getFile());
    File ep = new File(Main.class.getResource(HAAR_EYE).getFile());
    HAAR_FACE_PATH = fp.getAbsolutePath();
    HAAR_EYE_PATH = ep.getAbsolutePath();
  }

  /**
   * Load the OpenCV native library appropriate for this operating system. The
   * library file is stored in the running directory.
   */
  private static void loadOpencvLibrary() {
    try {
      InputStream in = null;
      File fileOut = null;
      String osName = System.getProperty("os.name");
      log.info(osName);
      if (osName.startsWith("Windows")) {
        int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
        if (bitness == 32) {
          log.info("32 bit detected");
          in = Main.class.getResourceAsStream("/opencv/opencv_java249_x86.dll");
        } else if (bitness == 64) {
          log.info("64 bit detected");
          in = Main.class.getResourceAsStream("/opencv/opencv_java249_x64.dll");
        } else {
          log.info("Unknown bit detected - trying with 32 bit");
          in = Main.class.getResourceAsStream("/opencv/opencv_java249_x86.dll");

        }
        fileOut = File.createTempFile("lib", ".dll");
      } else if (osName.equals("Mac OS X")) {
        log.info("Mac os detected");
        in = Main.class.getResourceAsStream("/opencv/libopencv_java249.dylib");
        fileOut = File.createTempFile("lib", ".dylib");
      }

      OutputStream out = new BufferedOutputStream(new FileOutputStream(fileOut), BUFFER_SIZE);
      int b = 0;

      while ((b = in.read()) >= 0) {
        out.write(b);
      }
      out.flush();
      in.close();
      out.close();
      System.load(fileOut.toString());
    } catch (Exception e) {
      throw new RuntimeException("Failed to load opencv native library", e);
    }
  }

  /**
   * Launch the JavaFX UI
   */
  @Override
  public void start(Stage stage) throws Exception {

    try {
      stage.setTitle("Digital Vision Screening");
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
      VBox vbox = (VBox) loader.load();
      RootViewController rootViewController = loader.getController();
      rootViewController.setController(controller);
      stage.setScene(new Scene(vbox));
      stage.show();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
