package temperature_sensor;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class TemperatureReader {

    private static double convertADCToCelsius(double adcValue) {
        //rougly 41.39 messured in ADC per Celsius
        double adcPerCelsius = 41.39;
        //baseline 0C = 2048
        double baselineCelsius = 2048.00;
        double temperaturIncelsius;

        //Calculating the conversion and rounding to two decimals
        temperaturIncelsius = Math.round((((adcValue - baselineCelsius) / adcPerCelsius) *100.0)) / 100.0;

        return temperaturIncelsius;
    }

    private static void temperatureLastTwoMinutes() {

    }


    public static void getTemperature() {
        try {
            File measuredTemperature = new File("temperature_sensor\\temperature.txt");
            Scanner readMeasuredTemperature = new Scanner(measuredTemperature);
            
            LocalDateTime startTimeOfReadingOfValues = LocalDateTime.now().withNano(0);
            // TODO: change to 2min
            LocalDateTime endTimeOfReadingOfValues = startTimeOfReadingOfValues.plusSeconds(15);
            
            // set the variabels to be a value that is unlikely to get passed from the sensore            
            double minTemperature = 5000;
            double maxTemperature = 5000;
            double avgTemperature = 0.0;
            int valueCounter = 0;

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
                        System.out.println("min: " + minTemperature + "max: " + maxTemperature + "avg: " + (avgTemperature/valueCounter));
                        startTimeOfReadingOfValues = LocalDateTime.now().withNano(0);
                        // TODO: change to min
                        endTimeOfReadingOfValues = startTimeOfReadingOfValues.plusSeconds(15);
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
    


