import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FormFillingTests {

    private static final Logger logger = LogManager.getLogger(FormFillingTests.class);
    private static final String BASE_URL = System.getProperty("BASE_URL", "https://otus.home.kartushin.su/form.html");
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    public void setupWebDriverManager() {
        logger.info("Инициализация WebDriverManager");
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() {
        logger.info("Создание WebDriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            logger.info("Закрытие WebDriver");
            driver.quit();
        }
    }

    @Test
    @DisplayName("Заполнение формы и проверка отправки данных")
    public void testFormFillingAndSubmission() {
        logger.info("=== Начало теста заполнения формы ===");

        String username = System.getProperty("USERNAME");
        String password = System.getProperty("PASSWORD");

        if (username == null || username.isEmpty()) {
            String errorMsg = "USERNAME не задан. Передайте параметр при запуске: -DUSERNAME=your_username";
            logger.error(errorMsg);
            fail(errorMsg);
        }
        
        if (password == null || password.isEmpty()) {
            String errorMsg = "PASSWORD не задан. Передайте параметр при запуске: -DPASSWORD=your_password";
            logger.error(errorMsg);
            fail(errorMsg);
        }
        
        logger.info("Используемые параметры - Username: {}, Password: [скрыт]", username);

        logger.info("Открытие страницы: {}", BASE_URL);
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        logger.info("Форма загружена");

        logger.info("Заполнение поля имени пользователя");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@id='username']")));
        usernameField.clear();
        usernameField.sendKeys(username);
        logger.info("Имя пользователя введено: {}", username);

        logger.info("Заполнение поля email");
        String email = username + "@example.com";
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@id='email']")));
        emailField.clear();
        emailField.sendKeys(email);
        logger.info("Email введен: {}", email);

        logger.info("Заполнение поля пароля");
        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@id='password']")));
        passwordField.clear();
        passwordField.sendKeys(password);
        logger.info("Пароль введен");

        logger.info("Заполнение поля подтверждения пароля");
        WebElement confirmPasswordField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@id='confirm_password']")));
        confirmPasswordField.clear();
        confirmPasswordField.sendKeys(password);
        logger.info("Подтверждение пароля введено");

        logger.info("Проверка совпадения пароля и подтверждения");
        String passwordValue = passwordField.getAttribute("value");
        String confirmPasswordValue = confirmPasswordField.getAttribute("value");
        assertEquals(passwordValue, confirmPasswordValue, 
            "Пароль и подтверждение пароля не совпадают");
        logger.info("Проверка паролей пройдена: пароли совпадают");

        logger.info("Заполнение поля даты рождения");
        String birthDateInput = "15-05-1990";
        String birthDateExpected = "1990-05-15";
        WebElement birthDateField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@id='birthdate']")));
        birthDateField.clear();
        birthDateField.sendKeys(birthDateInput);
        logger.info("Дата рождения введена: {}", birthDateInput);

        logger.info("Выбор уровня знания языка");
        WebElement languageLevelSelectElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//select[@id='language_level']")));
        Select languageLevelSelect = new Select(languageLevelSelectElement);

        try {
            languageLevelSelect.selectByVisibleText("Intermediate");
            logger.info("Выбран уровень: Intermediate");
        } catch (Exception e) {
            try {
                languageLevelSelect.selectByVisibleText("Средний");
                logger.info("Выбран уровень: Средний");
            } catch (Exception e2) {
                languageLevelSelect.selectByIndex(2);
                logger.info("Выбран уровень языка по индексу 2");
            }
        }

        logger.info("Нажатие кнопки отправки формы");
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@type='submit']")));
        submitButton.click();
        logger.info("Кнопка отправки нажата");

        logger.info("Ожидание отображения результатов");
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[@id='output']")));

        logger.info("Проверка корректного вывода данных");
        String pageSource = driver.getPageSource();
        
        assertTrue(pageSource.contains(username) || pageSource.contains(username.toLowerCase()),
            "Имя пользователя не найдено в результатах");
        logger.info("Имя пользователя найдено в результатах");
        
        assertTrue(pageSource.contains(email) || pageSource.contains(email.toLowerCase()),
            "Email не найден в результатах");
        logger.info("Email найден в результатах");
        
        assertTrue(pageSource.contains(birthDateExpected) || 
                   pageSource.contains(birthDateExpected.replace("-", ".")) ||
                   pageSource.contains(birthDateExpected.replace("-", "/")),
            "Дата рождения не найдена в результатах в формате " + birthDateExpected);
        logger.info("Дата рождения найдена в результатах: {}", birthDateExpected);
        
        logger.info("=== Тест успешно завершен ===");
    }
}