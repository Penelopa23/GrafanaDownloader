package org.grafana.downloader;

import org.grafana.Application;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class GrafanaCreator {
    private static final int BUFFER_SIZE = 4096;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final List<GrafanaParameters> urlList = new ArrayList<>();
    private static final List<File> picFiles = new ArrayList<>();
    private static final Logger log = Logger.getLogger(Application.class.getName());
    private static String startTime;
    private static String endTime;

    public GrafanaCreator() {
    }

    public static void create() {
        readXMLfile();
        downloadFiles();
        zipFiles();
    }

    private static void readXMLfile() {
        Document doc = Application.getConfigDocument();
        doc.getDocumentElement().normalize();
        startTime = doc.getElementsByTagName("start").item(0).getTextContent();
        String duration = doc.getElementsByTagName("duration").item(0).getTextContent();
        endTime = dateAddOffset(startTime, duration);
        NodeList nodeListUrl = doc.getElementsByTagName("url");

        for (int i = 0; i < nodeListUrl.getLength(); ++i) {
            Node node = nodeListUrl.item(i);
            if (1 == node.getNodeType()) {
                Element e = (Element) node;
                GrafanaParameters parGrafana = new GrafanaParameters(getTagText(e, "domen"),
                        getTagText(e, "dashboardId"), getTagText(e, "panelId"),
                        getTagText(e, "orgId"), getTagText(e, "dataSource"),
                        getTagText(e, "measurement"), getTagText(e, "width"),
                        getTagText(e, "height"), getTagText(e, "tz"),
                        getTagText(e, "apiKey"), getTagText(e, "name"));
                urlList.add(parGrafana);
            }
        }
    }

    public static void downloadFiles() {
        String timeStart;
        String timeEnd;
        try {
            timeStart = String.valueOf(sdf.parse(String.valueOf(startTime)).getTime());
            timeEnd = String.valueOf(sdf.parse(String.valueOf(endTime)).getTime());
        } catch (ParseException var8) {
            throw new RuntimeException("incorrect date in args!\n");
        }

        File dirReport = new File(Application.getFolderPath() + File.separator);
        dirReport.mkdirs();

        for (GrafanaParameters p : urlList) {
            StringBuilder url = new StringBuilder();
            appendIfPresent("http://", p.domen(), url);
            appendIfPresent("/", p.dashboardId(), url);
            appendIfPresent("?orgId=", p.orgId(), url);
            appendIfPresent("&from=", timeStart, url);
            appendIfPresent("&to=", timeEnd, url);
            appendIfPresent("&var-DataSource=", p.dataSource(), url);
            appendIfPresent("&var-Measurement=", p.measurement(), url);
            appendIfPresent("&panelId=", p.panelId(), url);
            appendIfPresent("&width=", p.width(), url);
            appendIfPresent("&height=", p.height(), url);
            appendIfPresent("&tz=", p.tz(), url);
            log.info("URL request sent in Grafana: " + url + "\n");

            try {
                downloadFile(url.toString(), p.apiKey(), p.name());
            } catch (IOException var7) {
                log.warning("Critical error when screenshots uploading!\n" + var7 + "\n");
            }
        }
    }

    private static void zipFiles() {
        try {
            ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(Application.getFolderPath()
                    + File.separator + "screenshots" + sdf.parse(String.valueOf(startTime)).getTime()
                    + ".zip")));

            for (File file : picFiles) {
                FileInputStream fis = new FileInputStream(file);
                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);
                byte[] bytes = new byte[BUFFER_SIZE];

                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }

                fis.close();
            }

            zos.close();
            log.info("Screenshots archived\n");
        } catch (IOException var7) {
            log.warning("Archived failed\n" + var7+ "\n");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static void downloadFile(String fileURL, String apiKey, String filename) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) (new URL(fileURL)).openConnection();
        httpConn.setDoOutput(true);
        httpConn.setInstanceFollowRedirects(false);
        httpConn.setRequestMethod("GET");
        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpConn.setRequestProperty("charset", "utf-8");
        httpConn.setRequestProperty("Accept", "image/png");
        httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);
        httpConn.setUseCaches(false);
        if (httpConn.getResponseCode() == 200) {

            //TODO Check file size, it must be more than 3176 byte
            InputStream inputStream = httpConn.getInputStream();

            File file = new File(Application.getFolderPath() + File.separator + filename + ".png");
            FileOutputStream outputStream = new FileOutputStream(file);
            picFiles.add(file);
            byte[] buffer = new byte[4096];

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            log.info("File " + filename + " save in " + Application.getFolderPath()+ "\n");

            try {
                Thread.sleep(300L);
            } catch (InterruptedException var12) {
                log.warning("Interrupted\n" + var12+ "\n");
            }
        } else {
            log.warning(httpConn.getResponseMessage());
        }

        httpConn.disconnect();
    }

    private static void appendIfPresent(String prefix, String value, StringBuilder sb) {
        if (!value.isEmpty()) {
            sb.append(prefix).append(value);
        }
    }

    private static String getTagText(Element element, String tagName) {
        Node n = element.getElementsByTagName(tagName).item(0);
        return n == null ? "" : n.getTextContent();
    }

    private static String dateAddOffset(String date, String offset) {
        Date st;
        try {
            st = sdf.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(st);
            String[] fields = offset.split(":");
            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(fields[0]));
            cal.add(Calendar.MINUTE, Integer.parseInt(fields[1]));
            cal.add(Calendar.SECOND, Integer.parseInt(fields[2]));
            st = cal.getTime();
        } catch (ParseException var5) {
            throw new RuntimeException(var5.getMessage());
        }

        return sdf.format(st);
    }
}
