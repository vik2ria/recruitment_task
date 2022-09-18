package temperature_sensor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class TemperatureReader {

    private static double roundingDouble(Double doubleValueToRound) {
        double returnValue = Math.round(doubleValueToRound) /100.0;
        return returnValue; 
    }

    private static double convertADCToCelsius(double adcValue) {
        //rougly 41.39 messured in ADC per Celsius
        double adcPerCelsius = 41.39;
        //baseline 0C = 2048
        double baselineCelsius = 2048.00;
        double temperaturIncelsius;

        //Calculating the conversion and rounding to two decimals
        temperaturIncelsius = roundingDouble(((adcValue - baselineCelsius) / adcPerCelsius));

        return temperaturIncelsius;
    }

    private static void postJSONToApi(String jsonStringWithTemp) {
        try {
            URL successfulTempReading = new URL("http://localhost:5001/api/temperature");
            HttpURLConnection apiCon = (HttpURLConnection)successfulTempReading.openConnection();

            apiCon.setRequestMethod("POST");
            apiCon.setRequestProperty("Content-type", "application/json");
            apiCon.setRequestProperty("Accept", "application/json");
            apiCon.setDoOutput(true);

            try(OutputStream outputStream = apiCon.getOutputStream()) {
                byte[] input = jsonStringWithTemp.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader(apiCon.getInputStream(), "utf-8"))){
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
                
            }


        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void temperatureLastTwoMinutes() {

    }


    public static void getTemperature() {
        try {
            File measuredTemperature = new File("temperature_sensor\\temperature.txt");
            Scanner readMeasuredTemperature = new Scanner(measuredTemperature);
            
            LocalDateTime startTimeOfReadingOfValues = LocalDateTime.now().withNano(0);
            // TODO: change to 2min
            LocalDateTime endTimeOfReadingOfValues = startTimeOfReadingOfValues.plusSeconds(30);
            
            // set the variables to be a value that is unlikely to get passed from the sensore            
            double minTemperature = 5000;
            double maxTemperature = 5000;
            double avgTemperature = 0.0;
            int valueCounter = 0;

            //Hashmap to contain the readings
            

            while(readMeasuredTemperature.hasNextLine()){
                try { 
                    
                    String stringValueFromADC = readMeasuredTemperature.nextLine();
                    double doubleValueFromADC = Double.valueOf(stringValueFromADC);
                    double doubleValueInCelsius = convertADCToCelsius(doubleValueFromADC);

                    valueCounter ++;
                    
                    avgTemperature += doubleValueInCelsius;
                    
                    LocalDateTime currentDateTime = LocalDateTime.now().withNano(0);

                    if(endTimeOfReadingOfValues.equals(currentDateTime)) {
                        avgTemperature = avgTemperature / valueCounter;
                        String temperatureInJSON = "{time:{start:" + startTimeOfReadingOfValues +
                        ", end:" + endTimeOfReadingOfValues +
                        "}, min:" + minTemperature +", max:" + maxTemperature + ", avg:" + avgTemperature +"}";

                        postJSONToApi(temperatureInJSON);

                        System.out.println("min: " + minTemperature + "max: " + maxTemperature + "avg: " + roundingDouble(avgTemperature));
                        
                        //Reset variables
                        minTemperature = 5000;
                        maxTemperature = 5000;
                        avgTemperature = 0.0;
                        valueCounter = 0;
                        
                        //set start and end time for next interval
                        startTimeOfReadingOfValues = LocalDateTime.now().withNano(0);
                        // TODO: change to min
                        endTimeOfReadingOfValues = startTimeOfReadingOfValues.plusSeconds(30);
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
                    
                 } catch (InterruptedException e) {
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
    


