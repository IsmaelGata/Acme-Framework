/*
 * FirefoxBrowser.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.testing;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import acme.client.data.models.Dataset;
import acme.client.helpers.FileHelper;
import acme.client.helpers.StringHelper;
import acme.internals.components.exceptions.PassThroughException;
import acme.internals.helpers.EnvironmentHelper;
import lombok.Getter;

public class FirefoxBrowser {

	// Properties -------------------------------------------------------------

	@Getter
	public String						protocol;

	@Getter
	public String						host;

	@Getter
	public int							port;

	@Getter
	public String						contextPath;

	@Getter
	public String						contextHome;

	@Getter
	public String						contextQuery;

	@Getter
	public boolean						userDelay;

	@Getter
	public boolean						headless;

	@Getter
	public int							shortTimeout;

	@Getter
	public int							longTimeout;

	@Getter
	public int							shortPause;

	@Getter
	public int							longPause;

	@Getter
	public String						baseUrl;

	@Getter
	public String						homeUrl;

	// Internal state ---------------------------------------------------------

	private static int					MAX_URL_FETCH_ATTEMPTS	= 10;
	private final FirefoxOptions		options;
	private WebDriver					driver;
	private final JavascriptExecutor	executor;
	private final Options				manager;

	// Constructor ------------------------------------------------------------


	public FirefoxBrowser() {
		Point position;
		Dimension size;
		Thread closeDriverHook;
		Runtime runtime;

		this.protocol = "http";
		this.host = "localhost";
		this.port = EnvironmentHelper.getRequiredProperty("server.port", int.class);
		this.contextPath = EnvironmentHelper.getRequiredProperty("server.servlet.contextPath", String.class);
		this.contextHome = "/any/system/welcome";
		this.contextQuery = "?language=en&debug=true";
		this.userDelay = EnvironmentHelper.getRequiredProperty("acme.testing.user-delay", boolean.class);
		this.headless = EnvironmentHelper.getRequiredProperty("acme.testing.headless", boolean.class);
		this.shortTimeout = EnvironmentHelper.getRequiredProperty("acme.testing.short-timeout", int.class);
		assert this.shortTimeout >= 1;
		this.longTimeout = EnvironmentHelper.getRequiredProperty("acme.testing.long-timeout", int.class);
		assert this.longTimeout >= 1;
		this.shortPause = EnvironmentHelper.getRequiredProperty("acme.testing.short-pause", int.class);
		assert this.shortPause >= 1;
		this.longPause = EnvironmentHelper.getRequiredProperty("acme.testing.long-pause", int.class);
		assert this.longPause >= 1;

		this.baseUrl = String.format("%s://%s:%s%s", this.protocol, this.host, this.port, this.contextPath);
		this.homeUrl = String.format("%s%s%s", this.baseUrl, this.contextHome, this.contextQuery);

		FileHelper.roll("./logs/firefox.log");
		System.setProperty("webdriver.firefox.logfile", "./logs/firefox.log");

		this.options = new FirefoxOptions();
		this.options.setHeadless(this.headless);
		this.options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		this.options.setLogLevel(FirefoxDriverLogLevel.ERROR);
		this.options.setAcceptInsecureCerts(true);

		this.driver = new FirefoxDriver(this.options);
		this.executor = (JavascriptExecutor) this.driver;
		this.manager = this.driver.manage();

		this.manager.timeouts().implicitlyWait(Duration.ofSeconds(this.shortTimeout));
		this.manager.timeouts().pageLoadTimeout(Duration.ofSeconds(this.longTimeout));
		this.manager.timeouts().scriptTimeout(Duration.ofSeconds(this.longTimeout));

		this.manager.window().maximize();
		position = new Point(0, 0);
		size = this.manager.window().getSize();
		size = new Dimension(size.getWidth() / 3, size.getHeight());
		this.manager.window().setPosition(position);
		this.manager.window().setSize(size);

		closeDriverHook = new Thread(() -> {
			if (this.driver != null) {
				this.driver.quit();
				this.driver = null;
			}
		});
		runtime = Runtime.getRuntime();
		runtime.addShutdownHook(closeDriverHook);

		this.driver.get("https://www.example.org");
	}

	// Sleep methods ----------------------------------------------------------

	public void sleep(final int seconds, final boolean exact) {
		assert seconds >= 0 && seconds <= 3600;

		long delayTime;

		try {
			if (exact)
				Thread.sleep(1000L * seconds);
			else if (this.userDelay) {
				// HINT: we cannot use RandomHelper because it has a single backbone, yet.
				// HINT+ The random delays must not have any impacts on the global random generator. 
				delayTime = (long) (1 + Math.random() * seconds) * 1000L;
				Thread.sleep(delayTime);
			}
		} catch (final Throwable oops) {
			throw new PassThroughException(oops);
		}
	}

	public void shortSleep() {
		this.sleep(this.shortPause, false);
	}

	public void longSleep() {
		this.sleep(this.longPause, false);
	}

	// Path-related methods ---------------------------------------------------

	public String getCurrentUrl() {
		String result;
		int counter;

		this.waitUntilComplete();
		result = this.driver.getCurrentUrl();
		for (counter = 0; counter < FirefoxBrowser.MAX_URL_FETCH_ATTEMPTS && result.contains("/any/system/referrer"); counter++) {
			this.sleep(counter + 1, true);
			result = this.driver.getCurrentUrl();
		}
		if (result.endsWith("/"))
			result = result.substring(0, result.length() - 1);

		return result;
	}

	public String getCurrentPath() {
		String result;
		String currentUrl;

		currentUrl = this.getCurrentUrl();
		result = this.extractPath(currentUrl);

		return result;
	}

	public String getCurrentQuery() {
		String result;
		String currentUrl;

		currentUrl = this.getCurrentUrl();
		result = this.extractQuery(currentUrl);

		return result;
	}

	public String extractPath(final String url) {
		assert !StringHelper.isBlank(url);

		String result;
		int queryPosition;

		result = url.replace(this.getBaseUrl(), "");
		queryPosition = result.indexOf("?");
		if (queryPosition != -1)
			result = result.substring(0, queryPosition);

		return result;
	}

	public String extractQuery(final String url) {
		assert !StringHelper.isBlank(url);

		String result;
		int queryPosition;

		result = url.replace(this.getBaseUrl(), "");
		queryPosition = result.indexOf("?");
		result = queryPosition == -1 ? "" : result.substring(queryPosition);

		return result;
	}

	public boolean isUrl(final String text) {
		// HINT: text can be null

		boolean result;

		result = !StringHelper.isBlank(text) && (text.startsWith("http://") || text.startsWith("https://"));

		return result;
	}

	public boolean isPath(final String text) {
		// HINT: text can be null

		boolean result;

		result = !StringHelper.isBlank(text) && //
			(text.equals("#") || text.equals("/") || text.startsWith("/") && !text.endsWith("/") && !text.contains("?"));

		return result;
	}

	public boolean isQuery(final String query) {
		// HINT: query can be null

		boolean result;

		result = StringHelper.isBlank(query) || !query.startsWith("?") && !query.startsWith("&");

		return result;
	}

	// Javascript methods -----------------------------------------------------

	public Object executeScript(final String script, final Object... arguments) {
		assert !StringHelper.isBlank(script);
		assert arguments != null;  // HINT: some arguments may be null

		Object result;

		result = this.executor.executeScript(script, arguments);

		return result;
	}

	// Location methods -------------------------------------------------------

	public WebElement locateOne(final By locator) {
		assert locator != null;
		assert this.exists(locator) : String.format("Cannot find '%s'.", locator.toString());

		WebElement result;

		result = this.driver.findElement(locator);

		return result;
	}

	public List<WebElement> locateMany(final By locator) {
		assert locator != null;
		assert this.exists(locator) : String.format("Cannot find '%s'.", locator.toString());

		List<WebElement> result;

		result = this.driver.findElements(locator);

		return result;
	}

	public boolean exists(final By locator) {
		assert locator != null;

		boolean result;

		try {
			this.driver.findElement(locator);
			result = true;
		} catch (final Throwable oops) {
			result = false;
		}

		return result;
	}

	public void checkExists(final By locator) {
		assert locator != null;

		assert this.exists(locator) : String.format("Element '%s' is expected.", locator);
	}

	public void checkNotExists(final By locator) {
		assert locator != null;

		assert !this.exists(locator) : String.format("Element '%s' is not expected.", locator);
	}

	// Form-filling methods ---------------------------------------------------

	public void clear(final By locator) {
		assert locator != null;

		WebElement element;

		element = this.locateOne(locator);
		element.clear();
		this.waitUntilComplete();
		this.shortSleep();
	}

	public void fill(final By locator, final String value) {
		assert locator != null;
		// HINT: value can be null

		WebElement element;

		element = this.locateOne(locator);
		element.clear();
		if (!StringHelper.isBlank(value))
			element.sendKeys(value);
		this.waitUntilComplete();
		this.shortSleep();
	}

	// Request methods --------------------------------------------------------

	public void requestHome() {
		this.doRequest("GET", this.homeUrl, null, true);
	}

	public void request(final String path, final String query) {
		assert this.isPath(path);
		assert this.isQuery(query);

		String separator, url;

		separator = StringHelper.isBlank(this.contextQuery) ? "?" : "&";
		url = String.format("%s%s%s%s%s", this.baseUrl, path, this.contextQuery, separator, query);
		this.doRequest("GET", url, null, true);
	}

	public void request(final String path, final String query, final Dataset payload) {
		assert this.isPath(path);
		assert this.isQuery(query);
		assert payload != null;

		String separator, url;

		separator = StringHelper.isBlank(this.contextQuery) ? "?" : "&";
		url = String.format("%s%s%s%s%s", this.baseUrl, path, this.contextQuery, separator, query);
		this.doRequest("POST", url, payload, true);
	}

	// Click methods ----------------------------------------------------------

	public void clickAndGo(final By locator) {
		assert locator != null;

		WebElement element;

		element = this.locateOne(locator);
		this.clickAndGo(element);
	}

	public void clickAndGo(final WebElement element) {
		assert element != null;

		// HINT: WebElement::click is a nightmare. Do not use it!
		this.doRequest("CLICK", null, element, false);
	}

	public void clickAndWait(final By locator) {
		assert locator != null;

		WebElement element;

		element = this.locateOne(locator);
		this.clickAndWait(element);
	}

	public void clickAndWait(final WebElement element) {
		assert element != null;

		this.doRequest("CLICK", null, element, true);
	}

	// Status methods ---------------------------------------------------------

	public void waitStalenessOf(final WebElement html) {
		assert html != null;

		By locator;
		WebDriverWait wait;

		try {
			locator = By.tagName("html");
			wait = new WebDriverWait(this.driver, Duration.ofSeconds(this.longTimeout));
			wait.until(WaitConditions.stalenessOf(html, locator));
		} catch (final Throwable oops) {
			assert false : "Timeout waiting for completion!";
		}
	}

	public void waitUntilComplete() {
		WebDriverWait wait;

		try {
			wait = new WebDriverWait(this.driver, Duration.ofSeconds(this.longTimeout));
			wait.until(WaitConditions.documentComplete());
		} catch (final Throwable oops) {
			assert false : "Timeout waiting for completion!";
		}
	}

	// Cookie-related methods -------------------------------------------------

	public Set<Cookie> getCookies() {
		Set<Cookie> result;

		result = this.manager.getCookies();

		return result;
	}

	public Cookie getCookie(final String name) {
		assert !StringHelper.isBlank(name);

		Cookie result;

		result = this.manager.getCookieNamed(name);

		return result;
	}

	public void setCookie(final Cookie cookie) {
		assert cookie != null;

		this.manager.addCookie(cookie);
	}

	public void deleteCookie(final String name) {
		assert !StringHelper.isBlank(name);

		this.manager.deleteCookieNamed(name);
	}

	public void deleteCookies() {
		this.manager.deleteAllCookies();
	}

	// Ancillary methods ------------------------------------------------------

	protected void doRequest(final String method, final String url, final Object data, final boolean wait) {
		assert !StringHelper.isBlank(method) || StringHelper.anyOf(method, "GET|POST|CLICK");
		assert !method.equals("GET") || this.isUrl(url) && data == null;
		assert !method.equals("POST") || this.isUrl(url) && data instanceof Dataset;
		assert !method.equals("CLICK") || url == null && data instanceof WebElement;

		By locator;
		WebElement html;

		locator = By.tagName("html");
		html = this.driver.findElement(locator);
		assert html != null;

		switch (method) {
		case "GET":
			this.driver.get(url);
			break;
		case "POST":
			assert url != null && url.contains(this.contextPath) : String.format("Cannot post to %s.", url);
			this.executeScript("performRequest(arguments[0], arguments[1], arguments[2]);", "POST", url, data);
			break;
		case "CLICK":
			this.executeScript("arguments[0].click();", data);
			break;
		default:
			assert false;
		}
		if (!wait)
			this.shortSleep();
		else {
			this.waitStalenessOf(html);
			this.waitUntilComplete();
			this.longSleep();
		}
	}

}
