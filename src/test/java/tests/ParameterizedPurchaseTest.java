package tests;

import io.qameta.allure.Attachment;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.*;
import utils.TestUtils;
import utils.ScreenshotUtils;
import org.openqa.selenium.WebDriver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ParameterizedPurchaseTest {
    private WebDriver driver;
    private LoginPage loginPage;
    private ProductsPage productsPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;

    private String firstName;
    private String lastName;
    private String postalCode;
    private int productCount;

    @Parameters({ "browser", "productCount", "firstName", "lastName", "postalCode" })
    @BeforeClass
    public void setup(String browser, String productCount, String firstName, String lastName, String postalCode) {
        System.out.println("======== 测试参数 ========");
        System.out.println("Browser: " + browser);
        System.out.println("Product Count: " + productCount);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Postal Code: " + postalCode);
        this.productCount = Integer.parseInt(productCount);
        this.firstName = firstName;
        this.lastName = lastName;
        this.postalCode = postalCode;

        driver = TestUtils.setupDriver(browser);
        driver.get("https://www.saucedemo.com/");
        loginPage = new LoginPage(driver);
    }

    @Test(priority = 1)
    public void testLogin() throws IOException {
        loginPage.login("standard_user", "secret_sauce");
        productsPage = new ProductsPage(driver);
        // 登录后截图
        attachScreenshotToReport("login_success");
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory.html"));

    }

    @Test(priority = 2, dependsOnMethods = "testLogin")
    public void testAddProductsToCart() throws IOException {
        // 添加指定数量的商品到购物车
        for (int i = 0; i < productCount && i < productsPage.getProductCount(); i++) {
            productsPage.addProductToCart(i);
        }
        productsPage.goToCart();
        cartPage = new CartPage(driver);
        // 购物车页面截图
        attachScreenshotToReport("cart_page");
        Assert.assertTrue(driver.getCurrentUrl().contains("cart.html"));

    }

    @Test(priority = 3, dependsOnMethods = "testAddProductsToCart")
    public void testCheckoutProcess() throws IOException {
        cartPage.proceedToCheckout();
        checkoutPage = new CheckoutPage(driver);
        checkoutPage.fillCheckoutInfo(firstName, lastName, postalCode);

        // 填写信息后截图
        attachScreenshotToReport("checkout_info_filled");

        checkoutPage.completePurchase();
        // 订单确认页面截图
        attachScreenshotToReport("order_confirmation");

        Assert.assertEquals(checkoutPage.getConfirmationMessage(), "Thank you for your");

    }

    // Allure附件方法，将截图附加到测试报告
    @Attachment(value = "Screenshot: {0}", type = "image/png")
    private byte[] attachScreenshotToReport(String screenshotName) throws IOException {
        String screenshotPath = ScreenshotUtils.takeScreenshot(driver, screenshotName);
        return Files.readAllBytes(Paths.get(screenshotPath));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}