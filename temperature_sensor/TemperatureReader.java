package temperature_sensor;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TemperatureReader {

    public static void readTemperature() {
        try {
            File measuredTemperature = new File("temperature_sensor\\temperature.txt");
            Scanner readMeasuredTemperature = new Scanner(measuredTemperature);
            while(readMeasuredTemperature.hasNextLine()){
                String stringTemperature = readMeasuredTemperature.nextLine();
                double temperature = Double.valueOf(stringTemperature);
                System.out.println(temperature);
            }
            readMeasuredTemperature.close();
        } catch (FileNotFoundException e) {
            System.out.println("Ingen temperatur funnet");
            e.printStackTrace();
        }
    }
    
}
