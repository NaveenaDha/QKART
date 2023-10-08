package QKART_TESTNG;

import QKART_TESTNG.pages.Checkout;
import QKART_TESTNG.pages.Home;
import QKART_TESTNG.pages.Login;
import QKART_TESTNG.pages.Register;
import QKART_TESTNG.pages.SearchResult;

import static org.testng.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import org.testng.annotations.Test;
@Listeners(ListenerClass.class)
public class QKART_Tests {

    static RemoteWebDriver driver;
    public static String lastGeneratedUserName;

     @BeforeSuite(alwaysRun = true)
    public static void createDriver() throws MalformedURLException {
        // Launch Browser using Zalenium
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(BrowserType.CHROME);
        driver = new RemoteWebDriver(new URL("http://localhost:8082/wd/hub"), capabilities);
        System.out.println("createDriver()");
    }

    /*
     * Testcase01: Verify a new user can successfully register
     */
        @Test(priority = 1, groups = {"Sanity"})
        @Parameters({"TC1_Username", "TC1_Password"})
        public void TestCase01(@Optional ("testUser") String TC1_Username, @Optional ("abc@123") String TC1_Password) throws InterruptedException {
        Boolean status;
        logStatus("Start TestCase", "Test Case 1: Verify User Registration", "DONE");
        //  takeScreenshot(driver, "StartTestCase", "TestCase1");

        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser(TC1_Username, TC1_Password, true);
        assertTrue(status, "Failed to register new user");

        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Visit the login page and login with the previuosly registered user
        Login login = new Login(driver);
        login.navigateToLoginPage();
         status = login.PerformLogin(lastGeneratedUserName, TC1_Password);
        logStatus("Test Step", "User Perform Login: ", status ? "PASS" : "FAIL");
        assertTrue(status, "Failed to login with registered user");

        // Visit the home page and log out the logged in user
        Home home = new Home(driver);
        status = home.PerformLogout();
        assertTrue(status, "Failed to LOGOUT");

        logStatus("End TestCase", "Test Case 1: Verify user Registration : ", status
          ? "PASS" : "FAIL");
        //  takeScreenshot(driver, "EndTestCase", "TestCase1");
    }
/*
     * Verify that an existing user is not allowed to re-register on QKart
     */
    @Test(priority = 2, groups = {"Sanity"})
    @Parameters({"TC1_Username", "TC1_Password"})
    public void TestCase02(@Optional ("testUser") String TC1_Username, @Optional ("abc@123") String TC1_Password) throws InterruptedException {
        Boolean status;
        logStatus("Start Testcase", "Test Case 2: Verify User Registration with an existing username ", "DONE");

        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser(TC1_Username, TC1_Password, true);
        logStatus("Test Step", "User Registration : ", status ? "PASS" : "FAIL");
        
        Assert.assertTrue(status);

        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Visit the Registration page and try to register using the previously
        // registered user's credentials
        registration.navigateToRegisterPage();
        status = registration.registerUser(lastGeneratedUserName, TC1_Password, false);

        // If status is true, then registration succeeded, else registration has
        // failed. In this case registration failure means Success
        logStatus("End TestCase", "Test Case 2: Verify user Registration : ", status ? "FAIL" : "PASS");
        // return !status;
        Assert.assertFalse(status);
    }

    /*
     * Verify the functinality of the search text box
     */
    @Test(priority = 3, groups = {"Sanity"})
    @Parameters("TC5_ProductNameToSearchFor")
    public void TestCase03(String TC5_ProductNameToSearchFor) throws InterruptedException {
        logStatus("TestCase 3", "Start test case : Verify functionality of search box ", "DONE");
        boolean status;

        // Visit the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Search for the "yonex" product
        status = homePage.searchForProduct(TC5_ProductNameToSearchFor);
        Assert.assertTrue("Test Case Failure. Unable to search for given product", status);

        // Fetch the search results
        List<WebElement> searchResults = homePage.getSearchResults();
        Assert.assertTrue("Test Case Failure. Unable to search for given product", searchResults.size()>0);

        for (WebElement webElement : searchResults) {
            // Create a SearchResult object from the parent element
            SearchResult resultelement = new SearchResult(webElement);

            // Verify that all results contain the searched text
            String elementText = resultelement.getTitleofResult();
            Assert.assertTrue("Test Case Failure. Test Results contain unexpected values: " + elementText, 
            elementText.toUpperCase().contains("YONEX"));
        }

        logStatus("Step Success", "Successfully validated the search results ", "PASS");

        // Search for product
        status = homePage.searchForProduct("Gesundheit");
        Assert.assertFalse("Test Case Failure. Invalid keyword returned results", status); 

        // Verify no search results are found
        searchResults = homePage.getSearchResults();
        if (searchResults.size() == 0) {
                //logStatus("Step Success", "Successfully validated that no products found message is displayed", "PASS");
            Assert.assertTrue("Successfully validated that no products found message is displayed", 
            homePage.isNoResultFound());
            
            }
             else {
            Assert.assertFalse("Test Case Fail. Expected: no results , actual: Results were available", 
            !(homePage.isNoResultFound()));
        }
    }

    /*
     * Verify the presence of size chart and check if the size chart content is as
     * expected
     */
    @Test(priority = 4, groups = {"Regression"})
    public void TestCase04() throws InterruptedException {
        logStatus("TestCase 4", "Start test case : Verify the presence of size Chart", "DONE");
        boolean status = true;

        // Visit home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Search for product and get card content element of search results
        status = homePage.searchForProduct("Running Shoes");
        List<WebElement> searchResults = homePage.getSearchResults();

        // Create expected values
        List<String> expectedTableHeaders = Arrays.asList("Size", "UK/INDIA", "EU", "HEEL TO TOE");
        List<List<String>> expectedTableBody = Arrays.asList(Arrays.asList("6", "6", "40", "9.8"),
                Arrays.asList("7", "7", "41", "10.2"), Arrays.asList("8", "8", "42", "10.6"),
                Arrays.asList("9", "9", "43", "11"), Arrays.asList("10", "10", "44", "11.5"),
                Arrays.asList("11", "11", "45", "12.2"), Arrays.asList("12", "12", "46", "12.6"));

        // Verify size chart presence and content matching for each search result
        for (WebElement webElement : searchResults) {
            SearchResult result = new SearchResult(webElement);

            // Verify if the size chart exists for the search result
        Assert.assertTrue("Successfully validated presence of Size Chart Link", result.verifySizeChartExists());
                // Verify if size dropdown exists
        logStatus("Step Success", "Validated presence of drop down", status ? "PASS" : "FAIL");
        Assert.assertTrue("Successfully not validated presence of drop down", result.verifyExistenceofSizeDropdown(driver));

                // Open the size chart
        Assert.assertTrue("TestCase 4. Test Case Fail. Failure to open Size Chart", result.openSizechart());
                    // Verify if the size chart contents matches the expected values
        Assert.assertTrue("Step Failure. Failure while validating contents of Size Chart Link", 
        result.validateSizeChartContents(expectedTableHeaders, expectedTableBody, driver));

        logStatus("Step Success", "Successfully validated contents of Size Chart Link", "PASS");

                    // Close the size chart modal
         Assert.assertTrue("Size Chart NOT closed", result.closeSizeChart(driver));

        }
        logStatus("TestCase 4", "End Test Case: Validated Size Chart Details", status ? "PASS" : "FAIL");
    }

    /*
     * Verify the complete flow of checking out and placing order for products is
     * working correctly
     */
    @Test(priority = 5, groups = {"Sanity"})
    @Parameters({"TC5_ProductNameToSearchFor", "TC5_ProductNameToSearchFor2", "TC5_AddressDetails", "TC1_Username", "TC1_Password"})
    public void TestCase05(String TC5_ProductNameToSearchFor, String TC5_ProductNameToSearchFor2, String TC5_AddressDetails, String TC1_Username, String TC1_Password) throws InterruptedException {
        Boolean status;
        logStatus("Start TestCase", "Test Case 5: Verify Happy Flow of buying products", "DONE");

        // Go to the Register page
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();

        // Register a new user
        Assert.assertTrue("Test Case Failure. Happy Flow Test Failed", registration.registerUser(TC1_Username, TC1_Password, true));

        // Save the username of the newly registered user
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Go to the login page
        Login login = new Login(driver);
        login.navigateToLoginPage();

        // Login with the newly registered user's credentials
        Assert.assertTrue("User Perform Login Failed", login.PerformLogin(lastGeneratedUserName, TC1_Password));

        // Go to the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Find required products by searching and add them to the user's cart
        status = homePage.searchForProduct("YONEX");
        homePage.addProductToCart(TC5_ProductNameToSearchFor);
        status = homePage.searchForProduct("Tan");
        homePage.addProductToCart(TC5_ProductNameToSearchFor2);

        // Click on the checkout button
        homePage.clickCheckout();

        // Add a new address on the Checkout page and select it
        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(TC5_AddressDetails);
        checkoutPage.selectAddress(TC5_AddressDetails);

        // Place the order
        checkoutPage.placeOrder();

        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));

        // Check if placing order redirected to the Thansk page
        Assert.assertTrue("The url ends with thanks", driver.getCurrentUrl().endsWith("/thanks"));

        // Go to the home page
        homePage.navigateToHome();

        // Log out the user
        homePage.PerformLogout();

        logStatus("End TestCase", "Test Case 5: Happy Flow Test Completed : ", status ? "PASS" : "FAIL");
    }

    /*
     * Verify the quantity of items in cart can be updated
     */
    @Test(priority = 6, groups = {"Regression"})
    @Parameters({"TC6_ProductNameToSearch1", "TC6_ProductNameToSearch2", "TC5_AddressDetails", "TC1_Username", "TC1_Password"})
    public void TestCase06(String TC6_ProductNameToSearch1, String TC6_ProductNameToSearch2, String TC5_AddressDetails, String TC1_Username, String TC1_Password) throws InterruptedException {
        Boolean status;
        logStatus("Start TestCase", "Test Case 6: Verify that cart can be edited", "DONE");
        Home homePage = new Home(driver);
        Register registration = new Register(driver);
        Login login = new Login(driver);

        registration.navigateToRegisterPage();
        // status = registration.registerUser("testUser", "abc@123", true);
        
        Assert.assertTrue("User Perform Register Failed", registration.registerUser(TC1_Username, TC1_Password, true));
        lastGeneratedUserName = registration.lastGeneratedUsername;

        login.navigateToLoginPage();
        // status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue("User Perform Login Failed", login.PerformLogin(lastGeneratedUserName, TC1_Password));

        homePage.navigateToHome();
        status = homePage.searchForProduct("Xtend");
        homePage.addProductToCart(TC6_ProductNameToSearch1);

        status = homePage.searchForProduct("Yarine");
        homePage.addProductToCart(TC6_ProductNameToSearch2);

        // update watch quantity to 2
        homePage.changeProductQuantityinCart(TC6_ProductNameToSearch1, 2);

        // update table lamp quantity to 0
        homePage.changeProductQuantityinCart(TC6_ProductNameToSearch2, 0);

        // update watch quantity again to 1
        homePage.changeProductQuantityinCart(TC6_ProductNameToSearch1, 1);

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(TC5_AddressDetails);
        checkoutPage.selectAddress(TC5_AddressDetails);

        checkoutPage.placeOrder();

        try {
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));
        } catch (TimeoutException e) {
            System.out.println("Error while placing order in: " + e.getMessage());
        }

        // status = driver.getCurrentUrl().endsWith("/thanks");
        Assert.assertTrue("The url ends with thanks", driver.getCurrentUrl().endsWith("/thanks"));

        homePage.navigateToHome();
        homePage.PerformLogout();

        logStatus("End TestCase", "Test Case 6: Verify that cart can be edited: ", status ? "PASS" : "FAIL");
    }

    @Test(priority = 7, groups = {"Sanity"})
    @Parameters({"TC7_ProductName", "TC7_Qty", "TC5_AddressDetails", "TC1_Username", "TC1_Password"})
    public void TestCase07(String TC7_ProductName, Integer TC7_Qty, String TC5_AddressDetails, String TC1_Username, String TC1_Password) throws InterruptedException {
        // Boolean status;
        logStatus("Start TestCase",
                "Test Case 7: Verify that insufficient balance error is thrown when the wallet balance is not enough",
                "DONE");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        Assert.assertTrue("User Perform Registration Failed", registration.registerUser(TC1_Username, TC1_Password, true));
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        Assert.assertTrue("User Perform Login Failed", login.PerformLogin(lastGeneratedUserName, TC1_Password));

        Home homePage = new Home(driver);
        homePage.navigateToHome();
        Assert.assertTrue("Search for product Stylecon", homePage.searchForProduct("Stylecon"));
        homePage.addProductToCart(TC7_ProductName);

        homePage.changeProductQuantityinCart(TC7_ProductName, TC7_Qty);

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(TC5_AddressDetails);
        checkoutPage.selectAddress(TC5_AddressDetails);

        checkoutPage.placeOrder();
        Thread.sleep(3000);

        Assert.assertTrue("The balance is sufficient", checkoutPage.verifyInsufficientBalanceMessage());

        logStatus("End TestCase",
                "Test Case 7: Verify that insufficient balance error is thrown when the wallet balance is not enough: ", "PASS");
    }
    @Test(priority = 8, groups = {"Regression"})
    @Parameters({"TC5_ProductNameToSearchFor", "TC1_Username", "TC1_Password"})
    public void TestCase08(String TC5_ProductNameToSearchFor, String TC1_Username, String TC1_Password) throws InterruptedException {
        // Boolean status = false;

        logStatus("Start TestCase",
                "Test Case 8: Verify that product added to cart is available when a new tab is opened",
                "DONE");
        takeScreenshot(driver, "StartTestCase", "TestCase09");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
    
        Assert.assertTrue("User Perform Registration Failed", registration.registerUser(TC1_Username, TC1_Password, true));
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        
       Assert.assertTrue("User Perform Login Failed", login.PerformLogin(lastGeneratedUserName, TC1_Password));

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        Assert.assertTrue("Search for Yonex product", homePage.searchForProduct("YONEX"));
        homePage.addProductToCart(TC5_ProductNameToSearchFor);

        String currentURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);

        driver.get(currentURL);
        Thread.sleep(2000);

        List<String> expectedResult = Arrays.asList("YONEX Smash Badminton Racquet");
        Assert.assertTrue("Verify cart contents", homePage.verifyCartContents(expectedResult));

        driver.close();

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

        logStatus("End TestCase",
        "Test Case 8: Verify that product added to cart is available when a new tab is opened", "PASS");
        //takeScreenshot(driver, "EndTestCase", "TestCase08");
    }
    @Test(priority = 9, groups = {"Regression"})
    @Parameters({"TC1_Username", "TC1_Password"})
    public void TestCase09(String TC1_Username, String TC1_Password) throws InterruptedException {

        logStatus("Start TestCase",
                "Test Case 09: Verify that the Privacy Policy, About Us are displayed correctly ",
                "DONE");
        takeScreenshot(driver, "StartTestCase", "TestCase09");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        
       Assert.assertTrue("Perform registration failed", registration.registerUser(TC1_Username, TC1_Password, true));
       lastGeneratedUserName = registration.lastGeneratedUsername;

       Login login = new Login(driver);
       login.navigateToLoginPage();
        
       Assert.assertTrue("Perform login failed", login.PerformLogin(lastGeneratedUserName, TC1_Password));

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        String basePageURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        
        Assert.assertTrue("Base page URL", driver.getCurrentUrl().equals(basePageURL));

        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);
        WebElement PrivacyPolicyHeading = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
        Assert.assertTrue("Privacy Policy Heading", PrivacyPolicyHeading.getText().equals("Privacy Policy"));

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);
        driver.findElement(By.linkText("Terms of Service")).click();

        handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[2]);
        WebElement TOSHeading = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
        Assert.assertTrue("Terma of service", TOSHeading.getText().equals("Terms of Service"));
        driver.close();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]).close();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

        logStatus("End TestCase",
        "Test Case 9: Verify that the Privacy Policy, About Us are displayed correctly ",
        "PASS");
        //takeScreenshot(driver, "EndTestCase", "TestCase9");
    }
    @Test(priority = 10, groups = {"Regression"})
    public void TestCase10() throws InterruptedException {
        logStatus("Start TestCase",
                "Test Case 10: Verify that contact us option is working correctly ",
                "DONE");
        takeScreenshot(driver, "StartTestCase", "TestCase10");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        driver.findElement(By.xpath("//*[text()='Contact us']")).click();

        WebElement name = driver.findElement(By.xpath("//input[@placeholder='Name']"));
        name.sendKeys("crio user");
        WebElement email = driver.findElement(By.xpath("//input[@placeholder='Email']"));
        email.sendKeys("criouser@gmail.com");
        WebElement message = driver.findElement(By.xpath("//input[@placeholder='Message']"));
        message.sendKeys("Testing the contact us page");

        WebElement contactUs = driver.findElement(
                By.xpath("/html/body/div[2]/div[3]/div/section/div/div/div/form/div/div/div[4]/div/button"));

        contactUs.click();

        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.invisibilityOf(contactUs));
        Assert.assertTrue("Contact Us form submission failed", wait.until(ExpectedConditions.invisibilityOf(contactUs)));
        logStatus("End TestCase",
                "Test Case 10: Verify that contact us option is working correctly ",
                "PASS");

        //takeScreenshot(driver, "EndTestCase", "TestCase10");

    }
    @Test(priority = 11, groups = {"Sanity", "Regression"})
    @Parameters({"TC5_AddressDetails", "TC5_ProductNameToSearchFor", "TC1_Username", "TC1_Password"})
    public void TestCase11(String TC5_AddressDetails, String TC5_ProductNameToSearchFor, String TC1_Username, String TC1_Password) throws InterruptedException {

        logStatus("Start TestCase",
                "Test Case 11: Ensure that the links on the QKART advertisement are clickable",
                "DONE");
        takeScreenshot(driver, "StartTestCase", "TestCase11");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        Assert.assertTrue("Perform Registration failed", registration.registerUser(TC1_Username, TC1_Password, true));
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        Assert.assertTrue("Perform Login failed", login.PerformLogin(lastGeneratedUserName, TC1_Password));

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        Assert.assertTrue("Search for ptoduct failed", homePage.searchForProduct(TC5_ProductNameToSearchFor));
        homePage.addProductToCart(TC5_ProductNameToSearchFor);
        homePage.changeProductQuantityinCart(TC5_ProductNameToSearchFor, 1);
        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(TC5_AddressDetails);
        checkoutPage.selectAddress(TC5_AddressDetails);
        checkoutPage.placeOrder();
        Thread.sleep(3000);

        String currentURL = driver.getCurrentUrl();

        List<WebElement> Advertisements = driver.findElements(By.xpath("//iframe"));

        Assert.assertTrue("Verify that 3 Advertisements are available", Advertisements.size() == 3);

        WebElement Advertisement1 = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[1]"));
        driver.switchTo().frame(Advertisement1);
        driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
        driver.switchTo().parentFrame();

        Assert.assertTrue("Verify that Advertisement 1 is clickable", !driver.getCurrentUrl().equals(currentURL));
        driver.get(currentURL);
        Thread.sleep(3000);

        WebElement Advertisement2 = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[2]"));
        driver.switchTo().frame(Advertisement2);
        driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
        driver.switchTo().parentFrame();

        Assert.assertTrue("Verify that Advertisement 2 is clickable", !driver.getCurrentUrl().equals(currentURL));
        logStatus("End TestCase",
                "Test Case 11:  Ensure that the links on the QKART advertisement are clickable", "PASS");
    }

    @AfterSuite
    public static void quitDriver() {
        System.out.println("quit()");
        driver.quit();
    }

    public static void logStatus(String type, String message, String status) {

        System.out.println(String.format("%s |  %s  |  %s | %s", String.valueOf(java.time.LocalDateTime.now()), type,
                message, status));
    }

    public static void takeScreenshot(WebDriver driver, String screenshotType, String description) {
        try {
            File theDir = new File("/screenshots");
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            String timestamp = String.valueOf(java.time.LocalDateTime.now());
            String fileName = String.format("screenshot_%s_%s_%s.png", timestamp, screenshotType, description);
            TakesScreenshot scrShot = ((TakesScreenshot) driver);
            File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
            File DestFile = new File("screenshots/" + fileName);
            FileUtils.copyFile(SrcFile, DestFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

