package com.vakya.flight;



import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FlightPriceComparator {

    private static final String CLEARTRIP_URL = "https://www.cleartrip.com/flights";
    private static final String PAYTM_URL = "https://tickets.paytm.com/flights";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter travel date (YYYY/MM/DD): ");
        String travelDate = scanner.nextLine();

        try {
            // Fetch flights from both websites
            List<String[]> cleartripFlights = fetchCleartripFlights(travelDate);
            List<String[]> paytmFlights = fetchPaytmFlights(travelDate);

            // Write to CSV
            writeToCsv(cleartripFlights, paytmFlights);
            System.out.println("Flight prices have been compared and saved to flights.csv.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static List<String[]> fetchCleartripFlights(String date) {
        // Setup WebDriver
        //WebDriverManager.firefoxdriver().setup();
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Surya\\Downloads\\geckodriver-v0.35.0-win64\\geckodriver.exe");

        WebDriver driver = new FirefoxDriver();
        driver.get(CLEARTRIP_URL);

        // Initialize wait with Duration
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Set timeout to 20 seconds

        // Input flight details
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='From']"))).sendKeys("Bangalore");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='To']"))).sendKeys("Delhi");

            // Wait for suggestions and click the first one
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[@data-testid='react-autosuggest-option']"))).click();

            // Select date
            WebElement dateInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Departure']")));
            dateInput.click();
            dateInput.clear();
            dateInput.sendKeys(date);
            dateInput.sendKeys(Keys.ENTER); // Submit date

            // Wait for flights to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-testid='flight-card']")));

            // Scrape flight data
            List<WebElement> flights = driver.findElements(By.xpath("//div[@data-testid='flight-card']"));
            List<String[]> flightDetails = new ArrayList<>();

            for (WebElement flight : flights) {
                String operator = flight.findElement(By.className("airlineName")).getText();
                String flightNumber = flight.findElement(By.className("flightNumber")).getText();
                String price = flight.findElement(By.className("price")).getText();
                flightDetails.add(new String[]{operator, flightNumber, price});
            }
            return flightDetails;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Ensure the driver is closed
        }
        return new ArrayList<>(); // Return an empty list in case of failure
    }

    private static List<String[]> fetchPaytmFlights(String date) {
        // Setup WebDriver
        //WebDriverManager.firefoxdriver().setup();
        System.setProperty("webdriver.gecko.driver", "C:\\Users\\Surya\\Downloads\\geckodriver-v0.35.0-win64\\geckodriver.exe");

        WebDriver driver = new FirefoxDriver();
        driver.get(PAYTM_URL);

        // Initialize wait with Duration
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Set timeout to 20 seconds

        // Input flight details
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='From']"))).sendKeys("Bangalore");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='To']"))).sendKeys("Delhi");

            // Wait for suggestions and click the first one
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(@class, 'react-autosuggest__suggestion')]"))).click();

            // Click on the departure date input
            WebElement dateInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Departure']")));
            dateInput.click();
            dateInput.clear();
            dateInput.sendKeys(date);
            dateInput.sendKeys(Keys.ENTER); // Submit date

            // Wait for the flights to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='flight-list']")));

            // Scrape flight data
            List<WebElement> flights = driver.findElements(By.xpath("//div[@class='flight-list']//div[contains(@class, 'flight-card')]"));
            List<String[]> flightDetails = new ArrayList<>();

            for (WebElement flight : flights) {
                String operator = flight.findElement(By.className("airline-name")).getText();
                String flightNumber = flight.findElement(By.className("flight-number")).getText();
                String price = flight.findElement(By.className("price")).getText();
                flightDetails.add(new String[]{operator, flightNumber, price});
            }
            return flightDetails;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Ensure the driver is closed
        }
        return new ArrayList<>(); // Return an empty list in case of failure
    }

    private static void writeToCsv(List<String[]> cleartripFlights, List<String[]> paytmFlights) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("flights.csv"))) {
            writer.write("Flight Operator,Flight Number,Price on Cleartrip,Price on Paytm\n");

            int maxLength = Math.max(cleartripFlights.size(), paytmFlights.size());

            for (int i = 0; i < maxLength; i++) {
                String[] cleartripFlight = i < cleartripFlights.size() ? cleartripFlights.get(i) : new String[]{"N/A", "N/A", "N/A"};
                String paytmPrice = i < paytmFlights.size() ? paytmFlights.get(i)[2] : "N/A";
                writer.write(String.join(",", cleartripFlight[0], cleartripFlight[1], cleartripFlight[2], paytmPrice) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

