package org.nothing.jocularweather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Class for getting and putting information to weather, storage, and location APIs.
 *
 * @author Ganning Xu
 * @author Eric Liu
 */
public class Fetcher {
    private static final String CITY_URL = "https://98q0kalf91.execute-api.us-east-1.amazonaws.com/ip";
    private static final String API_KEY = getEnv("API_KEY");
    private static final String BASE_URL = getEnv("BASE_URL");

    /**
     * Returns environment variable with desired key
     *
     * @param key name of key
     * @return String environment variable value
     */
    public static String getEnv(String key) {
        Logger.print(MessageType.JW_INFO, "Loading environment variable " + key + " from .env file");
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
            Logger.print(MessageType.JW_ERROR, "File .env not found");
            throw new RuntimeException("Environment variables not found");
        } catch (IOException e) {
            Logger.print(MessageType.JW_ERROR, "Environment variable" + key + "could not be read");
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
        String dbUrl = "https://98q0kalf91.execute-api.us-east-1.amazonaws.com/pushdb?zip=" + zipCode + "&lon=" + lon + "&lat=" + lat;

        HttpURLConnection connection;
        try {
            Logger.print(MessageType.JW_INFO, "Opening connection to database");

            // Connect to API for data storage
            connection = (HttpURLConnection) new URI(dbUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
        } catch (IOException | URISyntaxException e) {
            Logger.print(MessageType.JW_ERROR, "Unable to connect to database");
            throw new RuntimeException(e);
        }

        // Push information
        try (BufferedReader ignored = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            Logger.print(MessageType.JW_INFO, "Pushing " + zipCode + " to database. ");
        } catch (Exception e) {
            Logger.print(MessageType.JW_ERROR, "Could not push " + zipCode + " to database");
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
            Logger.print(MessageType.JW_INFO, "Loading saved locations from local file");
            return (ArrayList<String>) Files.readAllLines(Paths.get("src/main/resources/locationStorage.txt"));
        } catch (IOException e) {
            Logger.print(MessageType.JW_ERROR, "Could not load saved locations from local file");
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether a string is not possibly a valid United States ZIP Code
     *
     * @param str any string
     * @return boolean whether the string is only five digits
     */
    public static boolean isNotZip(String str) {
        try {
            Double.parseDouble(str);
            return str.length() != 5;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Pull current city from user IP
     *
     * @return String city, blank if not found
     */
    public String getCurrentCity() {
        HttpURLConnection connection;
        try {
            // Pull city from API
            Logger.print(MessageType.JW_INFO, "Requesting current city from API");
            connection = (HttpURLConnection) new URI(CITY_URL).toURL().openConnection();
            connection.setRequestMethod("GET");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return "";
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            // Attempt to resolve output
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
        Logger.print(MessageType.JW_INFO, "Trying to get weather report for " + zipCode + " from API");
        StringBuilder content = new StringBuilder();
        String combinedURL;

        // Search by city if a ZIP code was not added
        if (isNotZip(zipCode)) {
            // replace all " " with "%20" for URL
            zipCode = zipCode.replaceAll(" ", "%20");
            combinedURL = BASE_URL + "?appid=" + API_KEY + "&q=" + zipCode + "&units=imperial";
        } else {
            combinedURL = BASE_URL + "?appid=" + API_KEY + "&zip=" + zipCode + "&units=imperial";
        }

        // Attempt to open connection with API
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URI(combinedURL).toURL().openConnection();
            connection.setRequestMethod("GET");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        // Pull data from API
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            // Move JSON into Report record
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
            Report processedReport = mapper.readValue(content.toString(), Report.class);

            // Load location into database
            pushToDB(zipCode, processedReport.coord().lon(), processedReport.coord().lat());

            return processedReport;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Report fields in JSON do not match Report object.");
        }
    }

    /**
     * Remove ZIP from list of stored ZIP codes
     *
     * @param zip target ZIP code to remove
     * @return whether method succeeded
     */
    public boolean removeZipFromSaved(String zip) {
        Logger.print(MessageType.JW_INFO, "Removing " + zip + " from saved locations");
        if (isNotZip(zip)) {
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
        Logger.print(MessageType.JW_INFO, "Adding " + zip + " to saved locations");
        if (isNotZip(zip)) {
            return false;
        }

        try (FileWriter writer = new FileWriter("src/main/resources/locationStorage.txt")) {
            writer.append(zip);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save ZIP code to local file
     *
     * @param zip zip code to check if it's in locationStorage.txt
     * @return whether zip code is in saved locations file
     */
    public boolean isInSavedZips(String zip) {
        try {
            ArrayList<String> zips = getSavedLocations();
            for (String z : zips) {
                if (z.equals(zip)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get weather reports in bulk
     *
     * @param zips list of ZIP codes/locations to pull from
     * @return Report[] array of reports
     */
    public TreeMap<String, Report> getWeatherReports(ArrayList<String> zips) {
        TreeMap<String, Report> reports = new TreeMap<>();
        // ArrayList<Report> reports = new ArrayList<>();

        for (String zip : zips) {
            reports.put(zip, getWeatherReport(zip));
        }

        return reports;
    }
}