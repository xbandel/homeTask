package com.test;

import com.google.common.base.Function;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class taskTest {

	public static final String HTTPS_SHOP_POLYMER = "https://shop.polymer-project.org/";
	private static WebDriver driver;
	private final WebDriverWait wait = new WebDriverWait(driver, 5000);

	@BeforeClass
	public static void setUp(){
		 System.setProperty("webdriver.chrome.driver", "/home/xeniya/tmp/chromedriver");
		driver = new ChromeDriver();
        driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
	}
	

	 private void clickTab(String href){
		 final WebElement shopAppRoot = getShadowRoot("\\shop-app");
		 WebElement click = shopAppRoot.findElement(By.cssSelector(String.format("shop-tab > a[href='/list/%s']", href)));
		 click.click();

		 wait.until(new Function<WebDriver, Boolean>() {
			 public Boolean apply(WebDriver driver) {
				 return shopAppRoot.findElements(By.cssSelector("shop-list")).size() > 0;
			 }
		 });
	 }

	 private  void validateList(String jsonUrl, String href) throws IOException {
		final Object doc = getJsonList(jsonUrl);
			driver.get(HTTPS_SHOP_POLYMER);

			wait.until(new Function<WebDriver, Boolean>() {
				public Boolean apply(WebDriver driver) {
					return getShadowRoot("\\shop-app") != null;
				}
			});

			final WebElement shopAppRoot = getShadowRoot("\\shop-app");

			clickTab(href);

		//wait
		long len = ((Integer)JsonPath.read(doc, "$.length()")).longValue();

		final WebElement shopList = shopAppRoot.findElement(By.cssSelector("shop-list"));
		assertNotNull(shopList);
		wait.until(new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				return getShadowRoot(shopList)!= null;
			}
		});

		WebElement shopListRoot = getShadowRoot(shopList);
		assertNotNull(shopListRoot);


		for(int i =0 ; i< len; ++i){

			String title = JsonPath.read(doc, String.format("$[%s].title", i));
			Double price = JsonPath.read(doc, String.format("$[%s].price", i));

			String name = "/detail/" + href + "/"  + JsonPath.read(doc, "$[" +i +"].name");

			WebElement item = shopListRoot.findElement(By.cssSelector(String.format("ul > li  > a[href=\"%s\"] > shop-list-item", name)));
			WebElement itemRoot = getShadowRoot(item);

			WebElement webTitle = itemRoot.findElement(By.cssSelector("div"));
			WebElement webPrice = itemRoot.findElement(By.cssSelector("span"));

			assertEquals(webTitle.getText(), title);
			assertEquals(Double.valueOf(webPrice.getText().substring(1)), price);
		}
	}

	@Test
	public void testMenOutwear() throws IOException{
		validateList("https://shop.polymer-project.org/data/mens_outerwear.json", "mens_outerwear");
	}

	@Test
	public void testWomenOutwear() throws IOException{
		validateList("https://shop.polymer-project.org/data/ladies_outerwear.json", "ladies_outerwear");
	}


	@Test
	public void testCartCheckout() throws IOException{


		addToCart("ladies_outerwear", "Ladies+Colorblock+Wind+Jacket");
		addToCart("mens_tshirts", "Inbox+-+Subtle+Actions+T-Shirt");
		final WebElement shopAppRoot = getShadowRoot("\\shop-app");
		WebElement el = shopAppRoot.findElement(By.cssSelector("shop-cart-modal"));
		WebElement gotoCartAnchor  = getShadowRoot(el).findElement(By.cssSelector("shop-button"));
		assertNotNull(gotoCartAnchor);
		  wait.until(ExpectedConditions.elementToBeClickable(gotoCartAnchor));
		gotoCartAnchor.click();


		WebElement e1 = shopAppRoot.findElement(By.cssSelector("iron-pages > shop-cart"));
		WebElement checkoutBtn = getShadowRoot(e1).findElement(By.cssSelector("shop-button > a"));
		assertNotNull(checkoutBtn);
		checkoutBtn.click();


		WebElement e2 = shopAppRoot.findElement(By.cssSelector("iron-pages > shop-checkout"));
		WebElement form = getShadowRoot(e2).findElement(By.cssSelector("form[id=\"checkoutForm\"]"));

		form.findElement(By.cssSelector("input[id=\"accountEmail\"]")).sendKeys("test@mail.com");
		form.findElement(By.cssSelector("input[id=\"ccName\"]")).sendKeys("Foo Bar");
		form.findElement(By.cssSelector("input[id=\"accountPhone\"]")).sendKeys("123456789123");
		form.findElement(By.cssSelector("input[id=\"ccNumber\"]")).sendKeys("1234123412341234");

		form.findElement(By.cssSelector("input[id=\"ccCVV\"]")).sendKeys("123");

		form.findElement(By.cssSelector("input[id=\"shipAddress\"]")).sendKeys("address");
		form.findElement(By.cssSelector("input[id=\"shipCity\"]")).sendKeys("city");
		form.findElement(By.cssSelector("input[id=\"shipState\"]")).sendKeys("state");
		form.findElement(By.cssSelector("input[id=\"shipZip\"]")).sendKeys("12345");


		form.findElement(By.cssSelector("shop-button > input")).click();

		wait.until(new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				return driver.getCurrentUrl().equalsIgnoreCase("https://shop.polymer-project.org/checkout/success");
			}
		});
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("https://shop.polymer-project.org/checkout/success"));
	}


	private void addToCart(final String category, final String item){
		driver.get(HTTPS_SHOP_POLYMER);



		wait.until(new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				return getShadowRoot("\\shop-app") != null;
			}
		});

		final WebElement shopAppRoot = getShadowRoot("\\shop-app");

		clickTab(category);

		final WebElement shopList = shopAppRoot.findElement(By.cssSelector("shop-list"));
		assertNotNull(shopList);
		wait.until(new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				return getShadowRoot(shopList)!= null;
			}
		});

		WebElement shopListRoot = getShadowRoot(shopList);
		assertNotNull(shopListRoot);

		WebElement anchorToClick = shopListRoot.
				findElement(By.cssSelector("ul > li > a[href=\"/detail/" + category +"/" + item +"\"]"));

		anchorToClick.click();
		wait.until(new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				return driver.getCurrentUrl().contains(item);
			}
		});

		WebElement shopAppShadow = getShadowRoot("\\shop-app");

		final WebElement ironSelected = shopAppShadow.findElement(By.cssSelector("shop-detail"));
		wait.until(new Function<WebDriver, Boolean>() {
			public Boolean apply(WebDriver driver) {
				return getShadowRoot(ironSelected) !=null;
			}
		});

		WebElement ironSelectedShadow = getShadowRoot(ironSelected);
		assertNotNull(ironSelectedShadow);
		WebElement  addToCard = ironSelectedShadow.findElement(By.cssSelector("shop-button"));
		assertNotNull(addToCard);
		addToCard.click();
	}


	private WebElement getShadowRoot(String selector){
		 final JavascriptExecutor js =(JavascriptExecutor)driver;
		return  (WebElement)js.executeScript("return document.querySelector(arguments[0]).shadowRoot", selector);
	
	}
	private WebElement getShadowRoot(WebElement el){
		 final JavascriptExecutor js =(JavascriptExecutor)driver;
		return  (WebElement)js.executeScript("return arguments[0].shadowRoot", el);
	
	}
	
	private Object getJsonList(String urlString)throws IOException{
		 URL url = new URL(urlString);
	     InputStreamReader reader = new InputStreamReader(url.openStream());
	     String json = IOUtils.toString(reader);   
	     reader.close();
	     return  Configuration.defaultConfiguration().jsonProvider().parse(json);
	}
	
	@AfterClass
	public static void tearDown(){
		driver.quit();
	}
	

	

}
