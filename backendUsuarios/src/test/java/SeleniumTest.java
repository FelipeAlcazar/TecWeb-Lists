import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class SeleniumTest {
    private WebDriver driverPepe, driverAna;
    private WebDriverWait waitPepe, waitAna;
    private JavascriptExecutor jsPepe, jsAna;

    private final String bdUsersUrl = "jdbc:mysql://localhost:3306/usuarioslistacompra?serverTimezone=UTC";
    private final String bdUsersUser = "listacompra";
    private final String bdUsersPassword = "listacompra";

    @BeforeAll
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:/Users/felip/Desktop/testingTECWEB/chromedriver-win64/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:/Users/felip/Desktop/testingTECWEB/chrome-win64/chrome.exe");
        options.addArguments("--remote-allow-origins=*");

        driverPepe = new ChromeDriver(options);
        waitPepe = new WebDriverWait(driverPepe, Duration.ofSeconds(10));
        jsPepe = (JavascriptExecutor) driverPepe;

        driverAna = new ChromeDriver(options);
        waitAna = new WebDriverWait(driverAna, Duration.ofSeconds(10));
        jsAna = (JavascriptExecutor) driverAna;

        // Position and size the drivers
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int halfWidth = screenWidth / 2;

        driverPepe.manage().window().setSize(new Dimension(halfWidth, 992));
        driverPepe.manage().window().setPosition(new Point(0, 0));

        driverAna.manage().window().setSize(new Dimension(halfWidth, 992));
        driverAna.manage().window().setPosition(new Point(halfWidth, 0));
    }

    @AfterAll
    public void tearDown() {
        driverPepe.quit();
        driverAna.quit();
    }

    @Test
    @Order(1)
    public void testScenario() {

        String email1="pepe12345@pepe.com";
        String pwd1= "Pepe12345";
        registerAndLogin(driverPepe, waitPepe, email1, pwd1 );

        // Confirmamos el email manualmente para simplificarlo (en un uso real, el usuario recibe un email con un enlace para confirmar su cuenta).
        confirmAccount(email1);

        // Step 3: Verify Pepe's account confirmation in the database
        assertTrue(isAccountConfirmed(email1));


        System.out.println("Test scenario completed successfully");

    }

    private void registerAndLogin(WebDriver driver, WebDriverWait wait, String email, String password) {
        driver.get("http://localhost:4200/Register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/app-register1/div/div[2]/div[1]/form/div[1]/input"))).sendKeys(email);
        driver.findElement(By.xpath("/html/body/app-root/div/app-register1/div/div[2]/div[1]/form/div[2]/input")).sendKeys(password);
        driver.findElement(By.xpath("/html/body/app-root/div/app-register1/div/div[2]/div[1]/form/div[3]/input")).sendKeys(password);
        driver.findElement(By.xpath("/html/body/app-root/div/app-register1/div/div[2]/div[1]/form/div[4]/button[1]")).click();
    
        // Wait for the registration to complete and navigate to the login page or directly to the dashboard
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("Login"),
            ExpectedConditions.urlContains("GestorListas")
        ));
    
        if (driver.getCurrentUrl().contains("Login")) {
            driver.get("http://localhost:4200/Login");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/app-root/div/app-login1/div/form/div[1]/input"))).sendKeys(email);
            driver.findElement(By.xpath("/html/body/app-root/div/app-login1/div/form/div[2]/input")).sendKeys(password);
            driver.findElement(By.xpath("/html/body/app-root/div/app-login1/div/form/div[3]/button[1]")).click();
            wait.until(ExpectedConditions.urlContains("GestorListas")); // Adjust the URL part to match your application's post-login URL
        }
    }

    private boolean isAccountConfirmed(String email) {
        boolean isConfirmed = false;
    
        try (Connection connection = DriverManager.getConnection(bdUsersUrl, bdUsersUser, bdUsersPassword)) {
            String query = "SELECT is_confirmed FROM usuario WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        isConfirmed = resultSet.getBoolean("is_confirmed");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return isConfirmed;
    }

    private void confirmAccount(String email) {
        try (Connection connection = DriverManager.getConnection(bdUsersUrl, bdUsersUser, bdUsersPassword)) {
            String query = "UPDATE usuario SET is_confirmed = true WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}