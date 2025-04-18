package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class TestUtils {
    public static WebDriver setupDriver(String browser) {
        WebDriver driver;

        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                break;
            case "chrome":
                // 需外网VPN
                // WebDriverManager.chromedriver().clearDriverCache().setup();
                // ChromeOptions options = new ChromeOptions();
                // options.addArguments("--remote-allow-origins=*");
                // driver = new ChromeDriver(options);

                // 手动指定ChromeDriver
                // 使用本地driver
                System.setProperty("webdriver.chrome.driver", "D:/code/python/学习/saucedemo/drivers/chromedriver.exe");

                ChromeOptions options = new ChromeOptions();
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--start-maximized");

                driver = new ChromeDriver(options);
                break;

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        driver.manage().window().maximize();
        return driver;
    }
}