package org.grafana.report;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.grafana.Application;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.STHighlightColorImpl;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public final class ReportCreator {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private static final Logger log = Logger.getLogger(Application.class.getName());
    private static String testName;
    private static String realTestStart;
    private static String realTestDuration;
    private static String testStart;
    private static XWPFDocument document;
    private static boolean corrupted = false;


    public ReportCreator() {}

    public static void create() {
        readXMLfile(Application.getConfigDocument());
        document = new XWPFDocument();
        addTestName();
        addCommonText("Дата теста: " + realTestStart + " Длительность: " + realTestDuration);
        addCommonText(testName + " прошёл успешно.");
        addCommonText("Подробные данные по временам отклика по всем операциям можно посмотреть в прикреплённом ниже файле");
        addCommonText("На графике представлена средняя интенсивность операций: ");
        addPic("test.png");
        addNewPageHeader("Утилизация ЦПУ: ");
        addSmallText("Примечание: время на графиках смещено относительно Московского на -3 часа (UTC).");
        addPic("test.png");
        addPicInfo("Сервера приложений CPU, %");
        addPic("test.png");
        addPicInfo("БД ORACLE CPU, %");
        addNewPageHeader("Утилизация оперативной памяти: ");
        addPic("test.png");
        addPicInfo("Сервера приложений RAM, %");
        addPic("test.png");
        addPicInfo("БД ORACLE RAM, %");
        addNewPageHeader("JVM Metrics:");
        addPic("test.png");
        addPicInfo("Сервера приложений Heap Memory used, Mb");
        addPic("test.png");
        addPicInfo("Сервера приложений Non-Heap memory used, Mb");
        addNewPageHeader("Incoming traffic: ");
        addPic("test.png");
        addPicInfo("Сервера приложений Входящий траффик, Mb");
        addPic("test.png");
        addPicInfo("DB Oracle incoming traffic, Mb");
        addPic("test.png");
        addPicInfo("Service servers  outgoing traffic, Mb");
        addPic("test.png");
        addPicInfo("DB Oracle outgoing traffic, Mb");
        writeFile();
    }

    private static void addTestName() {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setText(testName);
        run.setFontFamily("Arial");
        run.setFontSize(18);
        run.setBold(true);
    }

    private static void addCommonText(String text) {
        XWPFRun run = document.createParagraph().createRun();
        run.setText(text);
        run.setFontFamily("Arial");
        run.setFontSize(18);
        run.setBold(true);
    }

    private static void addSmallText(String text) {
        XWPFRun run = document.createParagraph().createRun();
        run.setText(text);
        run.setFontFamily("Calibri");
        run.setFontSize(11);
    }

    private static void addNewPageHeader(String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setPageBreak(true);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("Arial");
        run.setFontSize(14);
        run.setBold(true);
    }

    private static void addPic(String filename) {
        try {
            XWPFRun run = document.createParagraph().createRun();
            run.addPicture(Files.newInputStream(Paths.get(Application.getFolderPath() +
                            File.separator + filename)), 6, filename, Units.toEMU(450.0D),
                    Units.toEMU(300.0D));
        } catch (InvalidFormatException | IOException var2) {
            corrupted = true;
            addCorruptedPicText();
            log.warning("Error screenshot download: " + filename + "\n" + var2);
        }
    }

    private static void addCorruptedPicText() {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setText("MISSING PIC");
        run.setFontFamily("Arial");
        run.setTextHighlightColor(STHighlightColorImpl.RED.toString());
        run.setFontSize(24);
        run.setBold(true);
        run.addCarriageReturn();
    }

    private static void addPicInfo(String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("Arial");
        run.setFontSize(12);
        run.setBold(true);
        run.addCarriageReturn();
    }

    private static void writeFile() {
        try {
            String reportName = (corrupted ? "CORRUPTED" : "") + "Заключение по тесту " + testName +
                    sdf.parse(String.valueOf(testStart)).getTime() + ".docx";
            log.info("File save with name: " + reportName);
            String reportPath = Application.getFolderPath() + File.separator + reportName;
            FileOutputStream os = new FileOutputStream(reportPath);

            document.write(os);
            os.close();
            if (corrupted) {
                log.warning("Error generation report");
            }
        } catch (IOException | ParseException var3) {
            log.warning("Failed to write report file");
        }
    }

    private static void readXMLfile(Document doc) {
        testName = doc.getElementsByTagName("testName").item(0).getTextContent();
        realTestStart = doc.getElementsByTagName("realStart").item(0).getTextContent();
        realTestDuration = doc.getElementsByTagName("realDuration").item(0).getTextContent();
        testStart = doc.getElementsByTagName("start").item(0).getTextContent();
    }
}

