package temperature_sensor;
import java.io.File;
import java.io.FileNotFoundException;
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

    private static 


    public static void getTemperature() {
        try {
            File measuredTemperature = new File("temperature_sensor\\temperature.txt");
            Scanner readMeasuredTemperature = new Scanner(measuredTemperature);
            while(readMeasuredTemperature.hasNextLine()){
                try { 
                String stringValueFromADC = readMeasuredTemperature.nextLine();
                double doubleValueFromADC = Double.valueOf(stringValueFromADC);
                
                System.out.println(convertADCToCelsius(doubleValueFromADC));
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
