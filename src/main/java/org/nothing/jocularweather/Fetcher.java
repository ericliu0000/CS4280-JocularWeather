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
     * Returns environment variable with desired key
     *
     * @param key name of key
     * @return String environment variable value
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
     * Push location to database
     *
     * @param zipCode ZIP code or location
     * @param lon     longitude
     * @param lat     latitude
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

    /**
     * Returns saved locations in local file
     *
     * @return ArrayList of strings containing the locations
     */
    public static ArrayList<String> getSavedLocations() {
        try {
            return (ArrayList<String>) Files.readAllLines(Paths.get("src/main/resources/locationStorage.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Pull current city from user IP
     *
     * @return String city, blank if not found
     */
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
     * Get weather report from API
     *
     * @param zipCode desired ZIP code or city name to find location at
     * @return Report object representing unpacked JSON data
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
     * Remove ZIP from list of stored ZIP codes
     *
     * @param zip target ZIP code to remove
     * @return whether method succeeded
     */
    public boolean removeZipFromSaved(String zip) {
        if (!(zip.length() == 5)) {
            return false;
        }

        ArrayList<String> zips = getSavedLocations();
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
     * Save ZIP code to local file
     *
     * @param zip zip code to save
     * @return whether method succeeded
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

    /**
     * Get weather reports in bulk
     *
     * @param zips list of ZIP codes/locations to pull from
     * @return Report[] array of reports
     */
    public ArrayList<Report> getWeatherReports(ArrayList<String> zips) {
        ArrayList<Report> reports = new ArrayList<>();

        for (int i = 0; i < zips.size(); i++) {
            reports.set(i, getWeatherReport(zips.get(i)));
        }

        return reports;
    }
}