package QKART_TESTNG;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ListenerClass extends QKART_Tests implements ITestListener {
    public void onStart(ITestContext context) {
        System.out.println("onStart method started");
        QKART_Tests.takeScreenshot(driver, context.getName(), "onStart method started screenshot");
    }
    public void onFinish(ITestContext context) {
        System.out.println("onFinish method started");
        QKART_Tests.takeScreenshot(driver, context.getName(), "onStart method started screenshot");
    }
    public void onTestFailure(ITestResult result) {
        System.out.println("Test Failed : "+ result.getName()+" Taking Screenshot ! ");
        QKART_Tests.takeScreenshot(driver, result.getName(), "test failed screenshot");
    }
    public void onTestSuccess(ITestResult result) {
        System.out.println("onTestSuccess Method" +result.getName());
        QKART_Tests.takeScreenshot(driver, result.getName(), "Ontest success screenshot");
    }
    }

