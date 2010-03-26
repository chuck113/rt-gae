package com.rt.web;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

public class AppEngineDataEntryClient {
    private static final File ROOT_FOLDER = new File("C:\\data\\projects\\rapAttack\\rapAttackGAE\\resources\\indexes");
    private static final String SERVLET_NAME = "DataInputServlet";
    private static final String PARAM_NAME = "artist";

    private void doUpdates(String targetHost) {
        File[] files = ROOT_FOLDER.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".ser");
            }
        });

        Arrays.sort(files, new Comparator<File>() {
            // smallest first
            public int compare(File o1, File o2) {
                return (int) (o1.length() - o2.length());
            }
        });

        // poke GAE to get an instance ready
        get("http://" + targetHost + "/" + SERVLET_NAME);

        for (File f : files) {
            String fileName = f.getName();
            String artistName = getAritstName(fileName);
            System.out.println("AppEngineDataEntryClient.doUpdates using artist name: " + artistName + " lenght is " + f.length());
            get("http://" + targetHost + "/" + SERVLET_NAME + "?" + PARAM_NAME + "=" + artistName);
        }
    }

    private void get(String urlString) {
        try {
            URL url = new URL(urlString);
            System.out.println("AppEngineDataEntryClient.doUpdates calling url: " + url);
            IOUtils.toString(url.openConnection().getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAritstName(String file) {
        System.out.println("AppEngineDataEntryClient.getAritstName file: " + file);
        return file.substring("hierarcy-index-".length(), file.length() - ".ser".length());
    }

    public static void main(String[] args) {
        String host = args.length == 0?"localhost:8080":args[0];
        new AppEngineDataEntryClient().doUpdates(host);
    }
}
