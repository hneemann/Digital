package de.neemann.digital.cli;

import java.awt.Cursor;
import java.awt.Desktop;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog2.VerilogGenerator;
import de.neemann.digital.lang.Lang;

/**
 * The Send To Jutge of Digital
 * <p>
 * Created by Miquel Torner on 12.12.2024.
 */
public class SendToJutge {

    private final ElementLibrary library;
    private final CircuitComponent circuitComponent;
    private final String endpoint = "https://api.jutge.org/api";
    private final String boundary = "----WebkitFormBoundary" + System.currentTimeMillis();
    private final String compiler_id = "Circuits";

    /**
     * Creates...
     *
     * @param circuitComponent circuit component
     * @param library          library
     */
    public SendToJutge(CircuitComponent circuitComponent, ElementLibrary library) {
        this.circuitComponent = circuitComponent;
        this.library = library;
    }

    public String login(String email, String password) {
        String token;
        String expiration;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            String inputJson = String.format(
                    "{\"func\":\"auth.login\", \"input\":{\"email\":\"%s\", \"password\":\"%s\"}}",
                    email, password);
            String payload = "data=" + inputJson; // Wrap in the 'data=' prefix

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // 200
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }

                    String responseBody = response.toString();
                    int jsonStart = responseBody.indexOf("{\"output\":");
                    int jsonEnd = responseBody.lastIndexOf("}") + 1;
                    if (jsonStart != -1 && jsonEnd != -1) {
                        String jsonPart = responseBody.substring(jsonStart, jsonEnd);
                        try {
                            JSONParser parser = new JSONParser();
                            JSONObject jsonObject = (JSONObject) parser.parse(jsonPart);
                            JSONObject output = (JSONObject) jsonObject.get("output");

                            token = (String) output.get("token");
                            expiration = (String) output.get("expiration");

                        } catch (Exception e) {
                            e.printStackTrace();
                            // JOptionPane.showMessageDialog(circuitComponent, "Error evaluating Jutge's response: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    } else {
                        // JOptionPane.showMessageDialog(circuitComponent, "Incorrect credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.println(responseBody);
                        return null;
                    }
                }
            } else {
                // JOptionPane.showMessageDialog(circuitComponent, "Jutge's server returned code " + Integer.toString(responseCode) + ".", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // JOptionPane.showMessageDialog(circuitComponent, "Error on API call:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        return token;
    }

    /**
     * Returns the user credentials
     *
     * @param token   user token
     * @param problem problem ID
     * 
     */
    public void sendProblem(String token, String problem_id, String topModule, String anotations) {

        // System.out.println("Token: " + token);
        // System.out.println("Problem: " + problem_id);
        // System.out.println("TopModule: " + topModule);
        // System.out.println("Anotations: " + anotations);

        // Check model for errors
        try {
            new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);
        } catch (PinException | NodeException | ElementNotFoundException error) {
            JOptionPane.showMessageDialog(circuitComponent, Lang.get("msg_modelHasErrors"), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Construct the file name
        ElementAttributes settings = Settings.getInstance().getAttributes();
        File oldExportDirectory = settings.getFile("exportDirectory");
        File outputFile = new File(topModule + ".v");
        File exportDirectory = new File("/tmp/");

        if (exportDirectory != null) {
            outputFile = new File(exportDirectory, topModule + ".v");
        }

        settings.setFile("exportDirectory", outputFile.getParentFile());

        try (VerilogGenerator vlog = new VerilogGenerator(library, new CodePrinter(outputFile))) {
            vlog.export(circuitComponent.getCircuit());
        } catch (IOException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(circuitComponent, "Error while exporting. Check your circuit and try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        byte[] verilogProgram = null;
        try {
            verilogProgram = Files.readAllBytes(outputFile.toPath());
        } catch (IOException e1) {
            System.out.println("Error on reading verilog file");
            System.out.println(outputFile.toPath());
            return;
        }

        try {
            // Open connection
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setDoOutput(true);

            // Prepare the multipart body
            try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
                // Add form data for "data"
                String inputJson = String.format(
                        "{\"func\":\"student.submissions.submit\", \"input\":{\"problem_id\":\"%s\", \"compiler_id\":\"%s\", \"annotation\":\"%s\"}, \"meta\":{\"token\":\"%s\"}}",
                        problem_id, compiler_id, anotations, token);
                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"data\"\r\n\r\n");
                os.writeBytes(inputJson + "\r\n");

                // Add file part
                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"program.v\"\r\n");
                os.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                os.write(verilogProgram);
                os.writeBytes("\r\n");

                // End boundary
                os.writeBytes("--" + boundary + "--\r\n");
                os.flush();
            }

            // Get response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read and parse response
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200
                System.out.println("Request succeeded!");
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    System.out.println("Response: " + response);
                }
            } else {
                try (Scanner scanner = new Scanner(connection.getErrorStream())) {
                    StringBuilder errorResponse = new StringBuilder();
                    while (scanner.hasNext()) {
                        errorResponse.append(scanner.nextLine());
                    }
                    System.err.println("Error Response: " + errorResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JEditorPane editorPane = new JEditorPane("text/html",
                "<html>See the correction <a href='https://jutge.org/problems/" + problem_id
                        + "/submissions'>here</a>.</html>");
        editorPane.setEditable(false);
        editorPane.setSelectionColor(null);
        editorPane.setOpaque(false);
        editorPane.setCursor(new Cursor(Cursor.HAND_CURSOR));

        editorPane.setHighlighter(null);
        editorPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        editorPane.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                try {
                    Desktop.getDesktop().browse(new URI(e.getURL().toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JOptionPane.showMessageDialog(null, editorPane, "Alert", JOptionPane.INFORMATION_MESSAGE);

        settings.setFile("exportDirectory", oldExportDirectory);
    }
}
