package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import java.util.List;

public class ProductsPage extends BasePage {

    @FindBy(className = "inventory_item")
    private List<WebElement> productItems;

    @FindBy(className = "shopping_cart_link")
    private WebElement cartIcon;

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    public void addProductToCart(int index) {
        if (index >= 0 && index < productItems.size()) {
            productItems.get(index).findElement(By.tagName("button")).click();
        }
    }

    public int getProductCount() {
        return productItems.size();
    }

    public void goToCart() {
        cartIcon.click();
    }
}