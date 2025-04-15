package com.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CsvMerger {
    private static final Logger logger = LogManager.getLogger(CsvMerger.class);
    private static final String INPUT_DIR = "C:\\Users\\GADDAM DHRUV ADHRITH\\Desktop\\Srinivas\\in\\";
    private static final String OUTPUT_DIR = "C:\\Users\\GADDAM DHRUV ADHRITH\\Desktop\\Srinivas\\out\\";
    private static final String FILE1 = "InstrumentDetails.csv";
    private static final String FILE2 = "PositionDetails.csv";
    private static final String OUTPUT_FILE = "PositionReport.csv";
    private static DatabaseManager dbManager ;

    public CsvMerger(DatabaseManager dbManager) {
        this.dbManager = dbManager;
       // this.emailNotifier = emailNotifier;
    }

    public static void main(String[] args) {
        try {
            logger.info("Application started.");
            Map<String, String> dataFromCsv1 = readCsvFile(INPUT_DIR + FILE1);
            Map<String, String> dataFromCsv2 = readCsvFile(INPUT_DIR + FILE2);

            logger.debug("Data from CSV 1: {}", dataFromCsv1);
            logger.debug("Data from CSV 2: {}", dataFromCsv2);


            // Insert the InstrumentDetails and Positiondetails records into database
            dbRecordInsertion(dataFromCsv1, dataFromCsv2);

            // Process data (Example: merge data)
            Map<String, String> processedData = processData(dataFromCsv1, dataFromCsv2);



            // Generate output CSV
            generateCsvOutput(processedData, OUTPUT_DIR + OUTPUT_FILE);

            // Send email notification
           //sendEmailNotification("Processing completed successfully", "Your CSV files have been processed and output generated.");

            logger.info("Application finished successfully.");
        } catch (Exception e) {
            logger.error("Error occurred during processing.", e);
            sendEmailNotification("Processing failed", "An error occurred while processing the CSV files.");
        }
    }

    private static Map<String, String> readCsvFile(String filePath) throws IOException {
        Map<String, String> dataMap = new HashMap<>();
        FileReader fileReader = new FileReader(filePath);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(fileReader);

        for (CSVRecord record : records) {
            String key = record.get(0);
            String value = record.get(1);
            dataMap.put(key, value);
        }

        return dataMap;
    }

    public static void dbRecordInsertion(Map<String, String> data1, Map<String, String> data2) {

        try {
            logger.info("Starting to process files: {} and {}", data1, data2);
           // dbManager = DatabaseManager
            // Store in database
            dbManager.storeData("InstrumentDetails", data1);
            dbManager.storeData("PositionDetails", data2);

        } catch (Exception e) {
            logger.error("Error processing files", e);

        }
    }
    private static Map<String, String> processData(Map<String, String> data1, Map<String, String> data2) {
        Map<String, String> mergedData = new LinkedHashMap<>();

        mergedData.put("ID", String.valueOf(UUID.randomUUID()));
        mergedData.put("PositionID", data2.get("ID"));
        mergedData.put("ISIN", data1.get("ISIN"));
        mergedData.put("Quantity", data2.get("Quantity"));
        Float value = Integer.parseInt(data2.get("Quantity").trim()) * Float.parseFloat(data1.get("Unit Price").trim());
       mergedData.put("TotalPrice", String.valueOf(value));
    return mergedData;
    }

    private static void generateCsvOutput(Map<String, String> data, String outputFilePath) throws IOException {
        // Delete the file if it exists
        File file = new File(OUTPUT_DIR+OUTPUT_FILE);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Existing file deleted.");
            } else {
                System.out.println("Failed to delete the existing file.");
            }
        }
        try(FileWriter fileWriter = new FileWriter(outputFilePath)) {

            fileWriter.append("Key,Value\n");

            for (Map.Entry<String, String> entry : data.entrySet()) {
                fileWriter.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
            }

            fileWriter.flush();
            fileWriter.close();
            logger.info("Output file generated: {}", outputFilePath);
        }catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private static void sendEmailNotification(String subject, String body) {
        String to = "omnetestingdiv@gmail.com";  // Replace with actual recipient email
        String from = "bsrinu2584@gmail.com";  // Replace with your Gmail address
        final String username = "bsrinu2584@gmail.com"; // Replace with your Gmail username
        final String password = "Covid19@2020"; // Replace with your Gmail password

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            logger.info("Email notification sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Error occurred while sending email notification.", e);
        }
    }
}
