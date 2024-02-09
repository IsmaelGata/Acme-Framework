/*
 * ApplicationAbstractTest.java
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

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import acme.Launcher;
import acme.client.data.models.Dataset;
import acme.client.helpers.StringHelper;
import acme.internals.helpers.ThrowableHelper;
import lombok.CustomLog;

@CustomLog
public abstract class ApplicationAbstractTest extends AbstractTest {

	// Internal state ---------------------------------------------------------

	private static FirefoxBrowser browser;

	// Properties -------------------------------------------------------------


	public FirefoxBrowser getBrowser() {
		return ApplicationAbstractTest.browser;
	}

	// Set-up methods ---------------------------------------------------------

	@BeforeAll
	@Override
	public void beforeAllTests(final TestInfo context) {
		assert context != null;

		try {
			if (super.isInitialised())
				Launcher.reset(false, true);
			else {
				Launcher.main("--platform", "testing", "--launcher", "tester");
				Launcher.reset(true, true);

				ApplicationAbstractTest.logger.debug("Launching Firefox browser.");
				ApplicationAbstractTest.browser = new FirefoxBrowser();
			}

			ApplicationAbstractTest.browser.deleteCookies();
			super.beforeAllTests(context);
		} catch (final Throwable oops) {
			ApplicationAbstractTest.logger.error("{}", ThrowableHelper.toString(oops));
			ApplicationAbstractTest.logger.error("Did you install the Gecko driver and Firefox properly?");
			Launcher.exit(null);
		}
	}

	@Override
	@BeforeEach
	public void beforeEachTest(final TestInfo context) {
		assert context != null && context.getTestMethod().isPresent();

		By locator;
		boolean existsSignOption;
		WebElement signElement;
		String href;

		// HINT: sanity check to ensure that every test starts with the anonymous user.
		locator = By.xpath("//*[@id=\"mainMenu\"]/ul[2]/li[2]/a");
		existsSignOption = this.getBrowser().exists(locator);
		if (existsSignOption) {
			signElement = this.getBrowser().locateOne(locator);
			href = signElement.getDomAttribute("href");
			assert href != null;
			if (href.contains("/authenticated/system/sign-out"))
				ApplicationAbstractTest.browser.clickAndWait(signElement);
		}

		super.beforeEachTest(context);
	}

	// Request methods --------------------------------------------------------

	public void requestHome() {
		ApplicationAbstractTest.browser.requestHome();
	}

	public void request(final String path) {
		assert !StringHelper.isBlank(path) && ApplicationAbstractTest.browser.isPath(path);

		ApplicationAbstractTest.browser.request(path, "");
	}

	public void request(final String path, final String query) {
		assert !StringHelper.isBlank(path) && ApplicationAbstractTest.browser.isPath(path);
		assert !StringHelper.isBlank(query) && ApplicationAbstractTest.browser.isQuery(query);

		ApplicationAbstractTest.browser.request(path, query);
	}

	public void request(final String path, final Dataset dataset) {
		assert !StringHelper.isBlank(path) && ApplicationAbstractTest.browser.isPath(path);
		assert dataset != null;

		ApplicationAbstractTest.browser.request(path, "", dataset);
	}

	public void request(final String path, final String query, final Dataset payload) {
		assert !StringHelper.isBlank(path) && ApplicationAbstractTest.browser.isPath(path);
		assert !StringHelper.isBlank(query) && ApplicationAbstractTest.browser.isQuery(query);
		assert payload != null;

		ApplicationAbstractTest.browser.request(path, query, payload);
	}

	// Path-related methods ---------------------------------------------------

	public String getCurrentUrl() {
		String result;

		result = ApplicationAbstractTest.browser.getCurrentUrl();

		return result;
	}

	public void checkCurrentUrl(final String expectedUrl) {
		assert ApplicationAbstractTest.browser.isUrl(expectedUrl);

		String currentUrl;

		currentUrl = ApplicationAbstractTest.browser.getCurrentUrl();
		assert currentUrl.equals(expectedUrl) : String.format("Current URL is '%s', but '%s' was expected.", currentUrl, expectedUrl);
	}

	public String getCurrentPath() {
		String result;

		result = ApplicationAbstractTest.browser.getCurrentPath();

		return result;
	}

	public void checkCurrentPath(final String expectedPath) {
		assert ApplicationAbstractTest.browser.isPath(expectedPath);

		String currentPath;

		if (!expectedPath.equals("#")) {
			currentPath = ApplicationAbstractTest.browser.getCurrentPath();
			assert currentPath.equals(expectedPath) : String.format("Current path is '%s', but '%s' was expected.", currentPath, expectedPath);
		}
	}

	public String getCurrentQuery() {
		String result;

		result = ApplicationAbstractTest.browser.getCurrentQuery();

		return result;
	}

	public void checkCurrentQuery(final String expectedQuery) {
		assert ApplicationAbstractTest.browser.isQuery(expectedQuery);

		String currentQuery;

		currentQuery = ApplicationAbstractTest.browser.getCurrentQuery();
		assert currentQuery.equals(expectedQuery) : String.format("Current query is '%s', but '%s' was expected.", currentQuery, expectedQuery);
	}

	// Existance methods ------------------------------------------------------

	public void checkFormExists() {
		By locator;

		locator = By.xpath("//form");
		assert ApplicationAbstractTest.browser.exists(locator) : "A form was expected, but does not exist.";
	}

	public void checkNotFormExists() {
		By locator;

		locator = By.xpath("//form");
		assert !ApplicationAbstractTest.browser.exists(locator) : "No form was expected, but exist.";
	}

	public void checkListingExists() {
		By locator;

		locator = By.xpath("//table[@id='list']");
		assert ApplicationAbstractTest.browser.exists(locator) : "A listing was expected, but does not exist.";
	}

	public void checkNotListingExists() {
		By locator;

		locator = By.xpath("//table[@id='list']");
		assert !ApplicationAbstractTest.browser.exists(locator) : "No listing was expected, but exist.";
	}

	public void checkLinkExists(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//a[normalize-space()='%s']", label));
		assert ApplicationAbstractTest.browser.exists(locator) : String.format("Cannot find link '%s'.", label);
	}

	public void checkNotLinkExists(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//a[normalize-space()='%s']", label));
		assert !ApplicationAbstractTest.browser.exists(locator) : String.format("Link '%s' was not expected.", label);
	}

	public void checkButtonExists(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//a[@class='btn btn-dark' and normalize-space()='%s']", label));
		assert ApplicationAbstractTest.browser.exists(locator) : String.format("Cannot find button '%s'.", label);
	}

	public void checkNotButtonExists(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//a[@class='btn btn-dark' and normalize-space()='%s']", label));
		assert !ApplicationAbstractTest.browser.exists(locator) : String.format("Button '%s' was not expected.", label);
	}

	public void checkSubmitExists(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//button[@type='submit' and normalize-space()='%s']", label));
		assert ApplicationAbstractTest.browser.exists(locator) : String.format("Cannot find button '%s'.", label);
	}

	public void checkNotSubmitExists(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//button[@type='submit' and normalize-space()='%s']", label));
		assert !ApplicationAbstractTest.browser.exists(locator) : String.format("Submit '%s' was not expected.", label);
	}

	public void checkAlertExists(final boolean success) {
		By locator;
		String className;

		className = success ? "alert-success" : "alert-danger";
		locator = By.xpath(String.format("//div[contains(@class, '%s')]", className));
		assert ApplicationAbstractTest.browser.exists(locator) : String.format("Cannot find alert '%s'.", className);
	}

	public void checkNotAlertExists(final boolean success) {
		By locator;
		String className;

		className = success ? "alert-success" : "alert-danger";
		locator = By.xpath(String.format("//div[contains(@class, '%s')]", className));
		assert !ApplicationAbstractTest.browser.exists(locator) : String.format("Alert '%s' was not expected.", className);
	}

	public void checkPanicExists() {
		By locator;

		locator = By.xpath("//h1[normalize-space() = 'Unexpected error']");
		assert ApplicationAbstractTest.browser.exists(locator) : "Action did not result in panic, but was expected.";
	}

	public void checkNotPanicExists() {
		By locator;

		locator = By.xpath("h1[normalize-space() = 'Unexpected error'");
		assert !ApplicationAbstractTest.browser.exists(locator) : "Action resulted in panic, but was not expected.";
	}

	public void checkErrorsExist() {
		By locator1, locator2;

		locator1 = By.className("text-danger");
		locator2 = By.xpath("//div[contains(@class, 'alert-danger')]");
		assert ApplicationAbstractTest.browser.exists(locator1) || ApplicationAbstractTest.browser.exists(locator2) : "Errors were expected, but not found.";
	}

	public void checkNotErrorsExist() {
		By locator1, locator2;

		locator1 = By.className("text-danger");
		locator2 = By.xpath("//div[contains(@class, 'alert-danger')]");
		assert !ApplicationAbstractTest.browser.exists(locator1) && !ApplicationAbstractTest.browser.exists(locator2) : "Errors were not expected, but found.";
	}

	public void checkErrorsExist(final String name) {
		assert !StringHelper.isBlank(name);

		By locator;

		if (!name.equals("*"))
			locator = By.xpath(String.format("//div[@class='form-group'][.//*[@id='%s'] and .//div[@class='text-danger']]", name));
		else
			locator = By.xpath("//div[contains(@class, 'alert-danger')]");
		assert ApplicationAbstractTest.browser.exists(locator) : String.format("Errors were expected for input box '%s', but were not found.", name);
	}

	public void checkNotErrorsExist(final String name) {
		assert !StringHelper.isBlank(name);

		By locator;

		if (!name.equals("*"))
			locator = By.xpath(String.format("//div[@class='form-group'][.//*[@id='%s'] and .//div[@class='text-danger']]", name));
		else
			locator = By.xpath("//div[contains(@class, 'alert-danger')]");
		assert !ApplicationAbstractTest.browser.exists(locator) : String.format("Unexpected errors were found for input box '%s'.", name);
	}

	// Read methods -----------------------------------------------------------

	public void checkInputBoxHasValue(final String name, final String expectedValue) {
		assert !StringHelper.isBlank(name);
		// HINT: expectedValue can be null

		By inputLocator, optionLocator;
		String inputTag, inputType;
		WebElement inputBox;
		WebElement option;
		String contents, value;

		inputLocator = By.name(name);
		inputBox = ApplicationAbstractTest.browser.locateOne(inputLocator);
		inputTag = inputBox.getTagName();
		switch (inputTag) {
		case "textarea":
			contents = inputBox.getAttribute("value");
			break;
		case "input":
			inputType = inputBox.getAttribute("type");
			switch (inputType) {
			case "text", "password", "hidden":
				contents = inputBox.getAttribute("data-label");
				if (contents == null)
					contents = inputBox.getAttribute("value");
				break;
			default:
				contents = null;
				assert false : String.format("Cannot check an input box of type '%s/%s'.", inputTag, inputType);
			}
			break;
		case "select":
			optionLocator = By.xpath(String.format("//select[@name='%s']/option[@selected]", name));
			assert ApplicationAbstractTest.browser.exists(optionLocator) : String.format("Cannot find selected option in input box '%s'.", name);
			option = ApplicationAbstractTest.browser.locateOne(optionLocator);
			contents = option.getText();
			break;
		default:
			contents = null;
			assert false : String.format("Cannot check an input box of type '%s'", inputTag);
		}
		contents = contents == null ? "" : contents.trim();
		value = expectedValue != null ? expectedValue.trim() : "";

		assert contents.equals(value) : String.format("Expected value '%s' in input box '%s', but '%s' was found.", value, name, contents);
	}

	public void checkListingEmpty() {
		WebElement paginator;
		By paginatorLocator, pageLinkLocator;
		List<WebElement> pageLinks;

		paginatorLocator = By.className("pagination");
		paginator = ApplicationAbstractTest.browser.locateOne(paginatorLocator);
		pageLinkLocator = By.className("page-link");
		pageLinks = paginator.findElements(pageLinkLocator);

		assert pageLinks.isEmpty() : "Listing was expected to be empty, but it is not.";
	}

	public void checkNotListingEmpty() {
		WebElement paginator;
		By paginatorLocator, pageLinkLocator;
		List<WebElement> pageLinks;

		paginatorLocator = By.className("pagination");
		paginator = ApplicationAbstractTest.browser.locateOne(paginatorLocator);
		pageLinkLocator = By.className("page-link");
		pageLinks = paginator.findElements(pageLinkLocator);

		assert !pageLinks.isEmpty() : "Listing was expected to have some records, but it is empty.";
	}

	public void checkColumnHasValue(final int recordIndex, final int columnIndex, final String expectedValue) {
		assert recordIndex >= 0;
		assert columnIndex >= 0;
		// HINT: expectedValue can be null

		List<WebElement> row;
		WebElement cell;
		// WebElement toggle;
		String contents, value;

		row = this.getListingRecord(recordIndex);
		assert columnIndex + 1 < row.size() : String.format("Column %d in record %d is out of range.", columnIndex, recordIndex);
		cell = row.get(columnIndex + 1);
		contents = (String) ApplicationAbstractTest.browser.executeScript("return arguments[0].innerText;", cell);
		// if (cell.isDisplayed())
		//   contents = cell.getText();
		// else {
		//    toggle = row.get(0);
		//    toggle.click();
		//    contents = (String) driver.executeScript("return arguments[0].innerText;", cell);
		//    toggle.click();
		// }

		contents = contents == null ? "" : contents.trim();
		value = expectedValue != null ? expectedValue.trim() : "";

		assert contents.equals(value) : String.format("Expected value '%s' in column %d of record %d, but found '%s'.", value, columnIndex, recordIndex, contents);
	}

	// Write methods ----------------------------------------------------------

	public void fillInputBoxIn(final String name, final String value) {
		assert !StringHelper.isBlank(name);
		// HINT: value can be null

		By inputLocator, proxyLocator, optionLocator;
		String inputTag, inputType, proxyXpath;
		WebElement inputBox, inputProxy, option;

		inputLocator = By.name(name);
		inputBox = ApplicationAbstractTest.browser.locateOne(inputLocator);
		inputTag = inputBox.getTagName();
		switch (inputTag) {
		case "textarea":
			ApplicationAbstractTest.browser.fill(inputLocator, value);
			break;
		case "input":
			inputType = inputBox.getAttribute("type");
			switch (inputType) {
			case "text", "password":
				ApplicationAbstractTest.browser.fill(inputLocator, value);
				break;
			case "hidden":
				proxyXpath = String.format("//*[@name='%s$proxy']", name);
				proxyLocator = By.xpath(proxyXpath);
				inputProxy = ApplicationAbstractTest.browser.locateOne(proxyLocator);
				if (inputProxy.getTagName().equals("input") && inputProxy.getAttribute("type").equals("checkbox")) {
					assert value == null || value.equals("true") || value.equals("false") : String.format("Input box '%s' cannot be set to '%s'.", name, value);
					if (inputProxy.getAttribute("checked") != null && (value == null || value.equals("false")))
						inputProxy.click();
					else if (inputProxy.getAttribute("checked") == null && (value == null || value.equals("true")))
						inputProxy.click();
				} else if (inputProxy.getTagName().equals("select")) {
					optionLocator = By.xpath(String.format("//select[@name='%s$proxy']/option[normalize-space(text())='%s']", name, value == null ? "" : value));
					assert ApplicationAbstractTest.browser.exists(optionLocator) : "Cannot find option with requested value in select.";
					option = ApplicationAbstractTest.browser.locateOne(optionLocator);
					option.click();
				} else
					assert false : String.format("Cannot fill input box '%s/%s' in.", name, inputType);
				break;
			default:
				assert false : String.format("Cannot fill input box '%s/%s' in.", name, inputType);
			}
			break;
		default:
			assert false : String.format("Cannot fill input box '%s' in.", name);
		}
	}

	// Click methods ----------------------------------------------------------

	public void clickOnMenu(final String header) {
		assert !StringHelper.isBlank(header);

		this.doClickOnMenu(header, null);
	}

	public void clickOnMenu(final String header, final String option) {
		assert !StringHelper.isBlank(header);
		assert !StringHelper.isBlank(option);

		this.doClickOnMenu(header, option);
	}

	public void sortListing(final int columnIndex, final String direction) {
		assert columnIndex >= 0;
		assert !StringHelper.isBlank(direction) && StringHelper.anyOf(direction, "asc|desc");

		ApplicationAbstractTest.browser.executeScript("$('#list').DataTable().page('first').draw();");
		ApplicationAbstractTest.browser.executeScript("$('#list').DataTable().order([[arguments[0], arguments[1]]]).draw();", columnIndex + 1, direction);
	}

	public void clickOnListingRecord(final int recordIndex) {
		assert recordIndex >= 0;

		List<WebElement> record;
		WebElement column;

		record = this.getListingRecord(recordIndex);
		column = record.get(1);
		ApplicationAbstractTest.browser.clickAndWait(column);
	}

	public void clickOnLink(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//a[normalize-space()='%s']", label));
		ApplicationAbstractTest.browser.clickAndWait(locator);
	}

	public void clickOnSubmit(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//button[@type='submit' and normalize-space()='%s']", label));
		ApplicationAbstractTest.browser.clickAndWait(locator);
	}

	public void clickOnButton(final String label) {
		assert !StringHelper.isBlank(label);

		By locator;

		locator = By.xpath(String.format("//a[@class='btn btn-dark' and normalize-space()='%s']", label));
		ApplicationAbstractTest.browser.clickAndWait(locator);
	}

	// Ancillary methods ------------------------------------------------------

	protected List<WebElement> getListingRecord(final int recordIndex) {
		assert recordIndex >= 0;

		List<WebElement> result;
		int pageIndex, rowIndex;
		By listLocator, lengthLocator, paginatorLocator, pageLinkLocator, rowLocator, columnLocator;
		WebElement list, length, paginator, pageLink, row;
		List<WebElement> pageLinks, rows;

		pageIndex = recordIndex / 100;
		rowIndex = 1 + recordIndex % 100;

		listLocator = By.id("list");
		list = ApplicationAbstractTest.browser.locateOne(listLocator);

		lengthLocator = By.xpath("//select[@name='list_length']/option[@value='100']");
		length = ApplicationAbstractTest.browser.locateOne(lengthLocator);
		length.click();

		paginatorLocator = By.className("pagination");
		paginator = ApplicationAbstractTest.browser.locateOne(paginatorLocator);
		pageLinkLocator = By.className("page-link");
		pageLinks = paginator.findElements(pageLinkLocator);
		assert pageIndex < pageLinks.size() : String.format("Record index %d is out of range.", recordIndex);
		pageLink = pageLinks.get(pageIndex);
		ApplicationAbstractTest.browser.clickAndGo(pageLink);

		rowLocator = By.tagName("tr");
		rows = list.findElements(rowLocator);
		assert rowIndex < rows.size() : String.format("Record index %d is out of range.", recordIndex);
		row = rows.get(rowIndex);
		columnLocator = By.tagName("td");
		result = row.findElements(columnLocator);

		return result;
	}

	protected void doClickOnMenu(final String header, final String option) {
		assert !StringHelper.isBlank(header);
		assert option == null || !StringHelper.isBlank(option);

		By headerLocator, optionLocator;

		headerLocator = By.xpath(String.format("//div[@id='mainMenu']/ul/li/a[normalize-space()='%s']", header));
		if (option == null)
			ApplicationAbstractTest.browser.clickAndWait(headerLocator);
		else {
			try {
				ApplicationAbstractTest.browser.clickAndGo(headerLocator);
			} catch (final Throwable oops) {
				; // HINT: Sometimes, the toggle gets stale unexpectedly.  Ignore the exception.
			}
			optionLocator = By.xpath(String.format("//div[@id='mainMenu']/ul/li[a[normalize-space()='%s']]/div[contains(@class, 'dropdown-menu')]/a[normalize-space()='%s']", header, option));
			ApplicationAbstractTest.browser.clickAndWait(optionLocator);
		}
	}

}
