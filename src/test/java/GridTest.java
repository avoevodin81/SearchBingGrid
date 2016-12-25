import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class GridTest {
    static WebDriver driver;
    static JavascriptExecutor jse;
    static WebDriverWait wait;

    @BeforeTest
    @Parameters({"hub", "browser"})
    public void setup(String hub, String browser) throws MalformedURLException {
        if (browser.equalsIgnoreCase("firefox")) {
            log("Executing on FireFox");
            DesiredCapabilities cap = DesiredCapabilities.firefox();
            cap.setBrowserName("firefox");
            driver = new RemoteWebDriver(new URL(hub), cap);
        } else if (browser.equalsIgnoreCase("chrome")) {
            log("Executing on CHROME");
            DesiredCapabilities cap = DesiredCapabilities.chrome();
            cap.setBrowserName("chrome");
            driver = new RemoteWebDriver(new URL(hub), cap);
        } else if (browser.equalsIgnoreCase("android")) {
            log("Executing on Android");
            DesiredCapabilities cap = DesiredCapabilities.android();
            cap.setBrowserName("android");
            driver = new RemoteWebDriver(new URL(hub), cap);
        } else {
            throw new IllegalArgumentException("The Browser Type is Undefined");
        }
        //initialise the JavascriptExecutor object
        jse = (JavascriptExecutor) driver;
        //initialise the wait element
        wait = new WebDriverWait(driver, 10);

        Reporter.setEscapeHtml(false);
    }

    @Test
    public void validateMainElements() {
        log("open the main page");
        driver.navigate().to("https://www.bing.com/");
        wait.until(ExpectedConditions.titleIs("Bing"));

        log("check the logo");
        By logo = By.xpath("//*[@id= 'bLogoExp' or @class = 'hp_sw_logo hpcLogoWhite']");
        wait.until(ExpectedConditions.presenceOfElementLocated(logo));
        Assert.assertTrue(driver.findElements(logo).size() == 1, "The logo is not found!");

        log("check the input box");
        By inputBox = By.id("sb_form_q");
        Assert.assertTrue(driver.findElements(inputBox).size() == 1, "The input box is not found!");

        log("check the search button");
        By searchButton = By.xpath("//input[@type = 'submit']");
        Assert.assertTrue(driver.findElements(searchButton).size() == 1, "The search button is not found!");

        log("check that the input box is available for inputting");
        WebElement input = driver.findElement(inputBox);
        String test = "Test";
        input.sendKeys(test);
        Assert.assertEquals(driver.findElement(inputBox).getAttribute("value"), test, "The text in the input box is not correct!");

        log("check that the search button is worked");
        WebElement submitButton = driver.findElement(searchButton);
        submitButton.click();
        String title = test + " - Bing";
        wait.until(ExpectedConditions.titleIs(title));
        Assert.assertEquals(driver.getTitle(), title, "The title is not correct!");
    }

    @Test(dependsOnMethods = {"validateMainElements"}, dataProvider = "keySearchString")
    public void checkLoadingImages(String text) {
        log("open the main page");
        driver.navigate().to("https://www.bing.com/");
        wait.until(ExpectedConditions.titleIs("Bing"));

        log("create data for searching");
        String[] searchText = {text.substring(0, text.length() - 1), text.substring(text.length() - 1)};

        log("search with data");
        By inputBox = By.id("sb_form_q");
        WebElement input = driver.findElement(inputBox);

        log("fill the input field");
        input.sendKeys(searchText[0]);

        log("wait for the full text");
        By fullText = By.xpath("//*[@*= '" + text + "']//div[@class='sa_tm']");
        wait.until(ExpectedConditions.presenceOfElementLocated(fullText));
        Assert.assertTrue(driver.findElements(fullText).size() == 1, "The full text is not found!");
        WebElement automation = driver.findElement(fullText);
        //click the text link
        automation.click();

        log("save the first URL from results");
        By searchResult = By.className("b_algo");
        wait.until(ExpectedConditions.presenceOfElementLocated(searchResult));
        Assert.assertTrue(driver.findElements(searchResult).size() > 0, "The search results is not shown!");
        By urlAddress = By.className("b_attribution");
        String fullUrlAddress = driver.findElement(urlAddress).getText();
        log("the saved URL is - " + fullUrlAddress);
        String title = driver.getTitle();

        log("navigate to the first search result page");
        By navigate = By.xpath("//*[@class = 'b_algo']//a");
        WebElement navigateTo = driver.findElement(navigate);
        navigateTo.click();

        log("check the URL of the opened page");
        //wait, until URL is not changed
        log("the current URL is - " + driver.getCurrentUrl());
        wait.until(ExpectedConditions.not(ExpectedConditions.titleContains(title)));
        //the result with mobile browser always has exception, because the URL has "m", "https://en.m.wikipedia.org/wiki/Manual_testing" for example
        Assert.assertEquals(driver.getCurrentUrl(), fullUrlAddress, "The current URL address is not the same with the correct URL!");
    }

    @AfterTest
    public void tearDown() {
        //quit the google driver
        driver.quit();
    }

    private void log(String message) {
        Reporter.log(message + "<br>");
    }

    @DataProvider
    public Object[][] keySearchString() {
        //read the file
        ArrayList<String> words = new ArrayList<String>();
        Scanner scn = null;
        try {
            scn = new Scanner(new File("src/main/resources/words.txt"));
        } catch (FileNotFoundException e) {
            log(e.getMessage());
            e.printStackTrace();
        }
        while (scn.hasNext()) {
            words.add(scn.nextLine());
        }
        Object[][] result = new Object[words.size()][1];
        for (int i = 0; i < words.size(); i++) {
            result[i][0] = words.get(i);
        }
        scn.close();
        return result;
    }
}
