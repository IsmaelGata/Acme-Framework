/*
 * WaitConditions.java
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

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public abstract class WaitConditions {

	// Constructors -------------------------------------------------------

	protected WaitConditions() {
	}

	// Business methods -----------------------------------------------------

	public static ExpectedCondition<Boolean> stalenessOf(final WebElement element, final By locator) {
		assert element instanceof RemoteWebElement;
		assert locator != null;

		ExpectedCondition<Boolean> result;

		result = (final WebDriver driver) -> {
			boolean answer;
			WebElement target;
			String oldId, newId;

			target = driver.findElement(locator);
			assert target instanceof RemoteWebElement;
			oldId = ((RemoteWebElement) element).getId();
			newId = ((RemoteWebElement) target).getId();
			answer = !oldId.equals(newId);

			return answer;
		};

		return result;
	}

	public static ExpectedCondition<Boolean> documentComplete() {
		ExpectedCondition<Boolean> result;

		result = (final WebDriver driver) -> {
			boolean answer;
			String readyState;

			readyState = (String) ((JavascriptExecutor) driver).executeScript("return document.readyState;");
			answer = "complete".equals(readyState) || "interactive".equals(readyState);

			return answer;
		};

		return result;
	}

}
