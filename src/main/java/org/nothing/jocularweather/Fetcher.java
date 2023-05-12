package org.nothing.jocularweather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Fetcher {
    private static final String API_KEY = getEnv("API_KEY");
    private static final String BASE_URL = getEnv("BASE_URL");

    public Fetcher() {
    }

    /**
     * @param key
     * @return
     */
    public static String getEnv(String key) {
        ArrayList<String> constants;

        try {
            constants = (ArrayList<String>) Files.readAllLines(Path.of(".env"));
            for (String constant : constants) {
                String[] parts = constant.split("=");
                if (parts[0].equals(key)) {
                    return parts[1];
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Environment variables not found");
        } catch (IOException e) {
            throw new RuntimeException(".env file could not be read");
        }

        return "";
    }

    /**
     * @param zipCode
     */
    public static void pushToDB(String zipCode, double lon, double lat) {
        String useURL = "https://98q0kalf91.execute-api.us-east-1.amazonaws.com/pushdb?zip=" + zipCode + "&lon=" + lon + "&lat=" + lat;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(useURL).toURL().openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String[] getSavedLocations() {
        String origStr = null;
        try {
            origStr = Files.readString(Paths.get("src/main/resources/locationStorage.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        return origStr.split("\n");
    }

    public String getCurrentCity() {
        String cityURL = "https://98q0kalf91.execute-api.us-east-1.amazonaws.com/ip";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(cityURL).toURL().openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return content.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * @param zipCode
     * @return
     */
    public Report getWeatherReport(String zipCode) {
        // TODO fix this !
        StringBuilder content = new StringBuilder();
        String combinedURL = BASE_URL + "?appid=" + API_KEY + "&zip=" + zipCode + "&units=imperial";

        if (!Main.isNumeric(zipCode)) {
            combinedURL = BASE_URL + "?appid=" + API_KEY + "&q=" + zipCode + "&units=imperial";
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(combinedURL).toURL().openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Report processedReport;

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

        try {
            processedReport = mapper.readValue(content.toString(), Report.class);

            pushToDB(zipCode, processedReport.coord().lon(), processedReport.coord().lat());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("THE REPORT IS NOT SET UP CORRECTLY!");
        }

        return processedReport;
    }

    /**
     * @param zip
     * @return
     */
    public boolean removeZipFromSaved(String zip) {
        if (!(zip.length() == 5)) {
            return false; // the zipcode length must be 5
        }

        String[] zips = getSavedLocations();
        StringBuilder newZips = new StringBuilder();

        for (String z : zips) {
            if (!z.equals(zip)) {
                newZips.append(z).append("\n");
            }
        }

        try {
            Files.writeString(Paths.get("src/main/resources/locationStorage.txt"), newZips.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * @param zip
     * @return
     */
    public boolean addZipToSaved(String zip) {
        if (!(zip.length() == 5)) {
            return false; // the zipcode length must be 5
        }

        // String[] zips = getSavedLocations();
        // String newZips = String.join("\n", zips);

        // // for (String z : zips) {
        // //     newZips += z + "\n";
        // // }

        // newZips += zip + "\n";

        try (FileWriter writer = new FileWriter("src/main/resources/locationStorage.txt")) {
            // Files.writeString(Paths.get("src/main/resources/locationStorage.txt"), newZips);
            writer.append(zip);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Report[] getWeatherReports(String[] zips) {
        // for saved locations
        Report[] reports = new Report[zips.length];

        for (int i = 0; i < zips.length; i++) {
            reports[i] = getWeatherReport(zips[i]);
        }

        return reports;
    }
}