package org.grafana;

import org.apache.commons.cli.*;
import org.grafana.downloader.GrafanaCreator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;


public class Application {
    private static final Logger log = Logger.getLogger(Application.class.getName());
    private static final Options options = new Options();
    private static CommandLine cl;
    private static Document configDocument;
    private static Path folderPath;

    public Application() {
    }
    public static void main(String[] args) {

        initializeOptions(args);

        try {
            Path confPath = Paths.get(cl.getOptionValue('c'));
            folderPath = Paths.get(cl.getOptionValue('f'));
            log.info("Reading config file: " + confPath + "\n");
            if (!confPath.toFile().exists()) {
                log.warning("Configuration file doesn't find!\n");
                System.exit(1);
            }
            configDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(confPath.toString());
        } catch (IOException | ParserConfigurationException | SAXException var5) {
            log.warning("Invalid configuration xml!\n" + var5 + "\n");
            System.exit(1);
        }

        if (cl.hasOption('g')) {
            log.info("Downloading screenshots\n");

            try {
                GrafanaCreator.create();
                log.info("Screenshots downloaded!\n");
            } catch (RuntimeException var3) {
                log.warning("Screenshots downloaded error!\n" + var3+ "\n");
            }
        }
    }


    private static void initializeOptions(String[] args) {
        Option configOption = Option.builder("c").longOpt("config").hasArg().required().desc("Path " +
                "to configuration .xml file.\n Required").build();
        Option folderOption = Option.builder("f").longOpt("folder").hasArg().required().desc("Path " +
                "to results folder.\n Required").build();
        Option grafanaOption = Option.builder("g").longOpt("grafana").hasArg().optionalArg(true).desc(
                "Indicates the need download screenshots from grafana").build();
        options.addOption(configOption).addOption(folderOption).addOption(grafanaOption);

        try {
            cl = (new DefaultParser()).parse(options, args);
        } catch (ParseException var8) {
            log.warning(var8.toString());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Check options", options);
            System.exit(1);
        }

        log.info("Arguments initialized\n");
    }

    public static Document getConfigDocument() {
        return configDocument;
    }

    public static Path getFolderPath() {
        return folderPath;
    }
}
