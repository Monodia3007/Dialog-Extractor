package eu.lilithmonodia.dialogextractor;

import eu.lilithmonodia.dialogextractor.data.MinecraftLog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.File;
import java.util.logging.Logger;

import static eu.lilithmonodia.dialogextractor.utils.FileUtils.*;
import static eu.lilithmonodia.dialogextractor.utils.LogUtils.logAction;

/**
 * The DialogExtractorController class handles the processing of dialog extraction from an input file.
 * It provides methods for uploading a file, extracting content, and downloading the processed content to an output file.
 */
public class DiaLogExtractorController {
    private static final Logger LOGGER = Logger.getLogger(DiaLogExtractorController.class.getName());

    @FXML
    private Button uploadButton;
    @FXML
    private Button extractButton;
    @FXML
    private Button downloadButton;
    @FXML
    private TextArea originalContentArea;
    @FXML
    private TextArea processedContentArea;
    @FXML
    private TextField uploadFilePath;
    @FXML
    private TextField downloadFilePath;
    @FXML
    private Pane dragAndDropOverlay;
    @FXML
    private VBox root;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        this.uploadButton.setOnAction(event -> uploadFile());
        this.extractButton.setOnAction(event -> extractContent());
        this.downloadButton.setOnAction(event -> downloadFile());
        this.downloadButton.setDisable(true);

        // Allow the upload button to accept drag dropping of files.
        Platform.runLater(this::initializeDragAndDropFunctionality);
    }

    private void initializeDragAndDropFunctionality() {
        Scene scene = root.getScene();

        scene.setOnDragOver(event -> {
            // If drag board has files.
            if (event.getDragboard().hasFiles()) {
                // Allow for both copying and moving, whatever user chooses.
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                // Show overlay
                dragAndDropOverlay.setVisible(true);
            }
            event.consume();
        });

        scene.setOnDragExited(event -> {
            // Hide overlay
            dragAndDropOverlay.setVisible(false);
            event.consume();
        });

        scene.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            // If this is a file drag event
            if (dragboard.hasFiles()) {
                File file = dragboard.getFiles().get(0); // Let's take the first file only
                uploadFilePath.setText(file.getAbsolutePath());
                processFile(file, originalContentArea);
                success = true;
            }
            // Hide overlay
            dragAndDropOverlay.setVisible(false);
            // let the source know whether the file was successfully transferred and used
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Uploads a file to a specified location.
     * <p>
     * This method retrieves the current window from the upload button's scene
     * and prompts the user to choose a file. If a file is selected, its absolute
     * path is displayed in the uploadFilePath text field and the file is processed.
     */
    private void uploadFile() {
        logAction(LOGGER, "Attempting to upload file ...");
        Window window = uploadButton.getScene().getWindow();
        File file = chooseFile(window, false);
        if (file != null) {
            uploadFilePath.setText(file.getAbsolutePath());
            processFile(file, originalContentArea);
        }
        logAction(LOGGER, "File upload finished successfully.");
    }

    /**
     * Downloads a file by writing the content from a text area to a specified output file.
     * The file path of the downloaded file will be displayed in a text field.
     */
    private void downloadFile() {
        logAction(LOGGER, "Attempting to download file ...");
        Window window = downloadButton.getScene().getWindow();
        File outFile = chooseFile(window, true);
        String outputText = processedContentArea.getText();
        if (outFile != null && !outputText.isEmpty()) {
            downloadFilePath.setText(outFile.getAbsolutePath());
            writeToFile(outFile, outputText);
        }
        logAction(LOGGER, "File download finished successfully.");
    }

    /**
     * Extracts content from the originalContentArea, processes it, and sets the processed content in the processedContentArea.
     * Sets the enabled/disabled state of the downloadButton depending on the extracted content.
     */
    private void extractContent() {
        logAction(LOGGER, "Attempting to extract content ...");
        String content = originalContentArea.getText();
        MinecraftLog minecraftLog = new MinecraftLog(content);
        String outputText = minecraftLog.extractDialogue().log();
        processedContentArea.setText(outputText);
        downloadButton.setDisable(outputText.isEmpty());
        logAction(LOGGER, "Content extraction finished successfully.");
    }
}