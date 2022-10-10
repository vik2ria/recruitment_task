package temperature_sensor;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Scanner;

// jar downloaded from: https://search.maven.org/artifact/org.json/json/20220924/bundle
import org.json.*;


public class TemperatureReader {


    private static double roundingDouble(Double doubleValueToRound) {
        double returnValue = Math.round(doubleValueToRound) /100.0;
        return returnValue;
    }

    private static double convertADCToCelsius(double adcValue) {
        //roughly 41.39 measured in ADC per Celsius
        double adcPerCelsius = 41.39;
        //baseline 0C = 2048
        double baselineCelsius = 2048.00;
        double temperatureInCelsius;

        //Calculating the conversion and rounding to two decimals
        temperatureInCelsius = roundingDouble(((adcValue - baselineCelsius) / adcPerCelsius));

        return temperatureInCelsius;
    }

    private static int postJSONToApi(JSONObject jsonTempObject, JSONArray failureToConnectArray, int lastPOSTStatusFlag) {
        if(lastPOSTStatusFlag == 0){
            try {
                URL successfulTempReading = new URL("http://localhost:5000/api/temperature");
                HttpURLConnection apiCon = (HttpURLConnection)successfulTempReading.openConnection();

                apiCon.setRequestMethod("POST");
                apiCon.setRequestProperty("Content-type", "application/json");
                apiCon.setRequestProperty("Accept", "application/json");
                apiCon.setDoOutput(true);

                try(OutputStream outputStream = apiCon.getOutputStream()) {
                    String jsonStringWithTemp = jsonTempObject.toString();
                    byte[] input = jsonStringWithTemp.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                }

                int responseCode = apiCon.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Målingen er lagret");
                } else if (responseCode == 500) {
                    failureToConnectArray.put(jsonTempObject);
                    lastPOSTStatusFlag = 1;
                    System.out.println("Målingen ble ikke lagret, sendes sammen med neste intervall");
                }



        }catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if(lastPOSTStatusFlag == 1) {
            try{
                failureToConnectArray.put(jsonTempObject);
                URL failedTempReading = new URL("http://localhost:5000/api/temperature/missing");
                HttpURLConnection apiMissingCon = (HttpURLConnection)failedTempReading.openConnection();

                apiMissingCon.setRequestMethod("POST");
                apiMissingCon.setRequestProperty("Content-type", "application/json");
                apiMissingCon.setRequestProperty("Accept", "application/json");
                apiMissingCon.setDoOutput(true);

                try(OutputStream outputStream = apiMissingCon.getOutputStream()) {
                    String jsonStringWithTemp = failureToConnectArray.toString();
                    byte[] input = jsonStringWithTemp.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                }
                int responseFromMissingPOST = apiMissingCon.getResponseCode();
                if(responseFromMissingPOST == HttpURLConnection.HTTP_OK){
                    lastPOSTStatusFlag = 0;
                    System.out.println("målingen ble sendt til missing");
                } else if(responseFromMissingPOST== 400) {
                    failureToConnectArray.iterator().forEachRemaining(System.out::println);
                }

            } catch (IOException e){
                e.printStackTrace();
            }
        }

        return lastPOSTStatusFlag;
    }

    public static void getTemperature() {
        JSONArray failedPOSTArray = new JSONArray();
        int lastPOSTStatusFlag = 0;
        try {
            File measuredTemperature = new File("src/main/java/temperature_sensor/temperature.txt");
            Scanner readMeasuredTemperature = new Scanner(measuredTemperature);

            LocalDateTime startTimeOfReadingOfValues = LocalDateTime.now().withNano(0);
            // TODO: change to 2min
            LocalDateTime endTimeOfReadingOfValues = startTimeOfReadingOfValues.plusSeconds(10);

            // set the variables to be a value that is unlikely to get passed from the sensore
            double minTemperature = 5000;
            double maxTemperature = 5000;
            double avgTemperature = 0.0;
            int valueCounter = 0;

            //Hashmap to contain the readings


            while(readMeasuredTemperature.hasNextLine()){
                try {

                    String stringValueFromADC = readMeasuredTemperature.nextLine();
                    double doubleValueFromADC = Double.parseDouble(stringValueFromADC);
                    double doubleValueInCelsius = convertADCToCelsius(doubleValueFromADC);

                    valueCounter ++;

                    avgTemperature += doubleValueInCelsius;

                    LocalDateTime currentDateTime = LocalDateTime.now().withNano(0);

                    if(endTimeOfReadingOfValues.equals(currentDateTime)) {
                        // calculate avg temperature
                        avgTemperature = roundingDouble(avgTemperature / valueCounter);

                        //hashmap to give to JSONObject
                        HashMap<String, LocalDateTime> startAndEnTime = new HashMap<>();
                        startAndEnTime.put("start", startTimeOfReadingOfValues);
                        startAndEnTime.put("end", endTimeOfReadingOfValues);
                        //JSONObject
                        JSONObject jsonTemperatureReading = new JSONObject();
                        jsonTemperatureReading.put("time", startAndEnTime);
                        jsonTemperatureReading.put("min", minTemperature);
                        jsonTemperatureReading.put("max", maxTemperature);
                        jsonTemperatureReading.put("avg", avgTemperature);

                        lastPOSTStatusFlag = postJSONToApi(jsonTemperatureReading, failedPOSTArray, lastPOSTStatusFlag);

                        //Reset variables
                        minTemperature = 5000;
                        maxTemperature = 5000;
                        avgTemperature = 0.0;
                        valueCounter = 0;

                        //set start and end time for next interval
                        startTimeOfReadingOfValues = LocalDateTime.now().withNano(0);
                        // TODO: change to min
                        endTimeOfReadingOfValues = startTimeOfReadingOfValues.plusSeconds(10);
                    }

                    if(minTemperature == 5000 && maxTemperature == 5000) {
                        minTemperature = doubleValueInCelsius;
                        maxTemperature = doubleValueInCelsius;
                    }

                    if(minTemperature > doubleValueInCelsius){
                        minTemperature = doubleValueInCelsius;
                    }

                    if(maxTemperature < doubleValueInCelsius) {
                        maxTemperature = doubleValueInCelsius;
                    }


                    Thread.sleep(100);

                } catch (InterruptedException | JSONException e) {
                    // TODO: handle exception
                }

            }
            readMeasuredTemperature.close();
        } catch (FileNotFoundException e) {
            System.out.println("Ingen temperatur funnet");
            e.printStackTrace();
        }
    }

}



