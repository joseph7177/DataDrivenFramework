package com.hp2b.web.quotes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hp2b.common.FrameworkMethods;
import com.hp2b.interfaces.testcasetagging.IGroupsTagging;
import com.hp2b.pdf.PDFValidations;
import com.hp2b.web.pom.Checkout;
import com.hp2b.web.pom.CreateNewQuote;
import com.hp2b.web.pom.CustomerService;
import com.hp2b.web.pom.Login;
import com.hp2b.web.pom.PDP;
import com.hp2b.web.pom.PageGenerics;
import com.hp2b.web.pom.QuoteDetails;
import com.hp2b.web.pom.ShoppingCart;
import com.hp2b.xls.XLSValidations;
import com.hpicorp.hpframework.reporting.Reporting;
import com.relevantcodes.extentreports.LogStatus;

public class CELogisticalPreference extends FrameworkMethods {

	Logger logger = Logger.getLogger(CELogisticalPreference.class);

	private String url = "";
	private static final String config = "config";

	public void setEnvironment() {
		if (url.isEmpty()) {			
			url = _webcontrols.get().propFileHandler().readProperty(config, ENVIRONMENT);
		}
	}

	/**
	 * Verify that Logistical preference options are Not displayed in Create quote page ,when Ship complete is set to Yes-Lease
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301045
	 * @param region APJ
	 * @since Apr 26, 2021 3:08:18 PM
	 * @author rajoriap
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
	//		IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301045_Regression_CE_LogisticalPreferenceCBN_VerifyLogisticalPreferenceOptionsAreNotDisplayedInCreateQuotePageWhenShipCompleteIsSetToYesLease_Direct(){

		// Reporting info
		initializeReporting("Verify that Logistical preference options are Not displayed in Create quote page ,when Ship complete is set to Yes-Lease",
				"C301045_Regression_CE_LogisticalPreferenceCBN_VerifyLogisticalPreferenceOptionsAreNotDisplayedInCreateQuotePageWhenShipCompleteIsSetToYesLease_Direct",
				logger);

		//Need to remove once data is available
		Reporting.getLogger().log(LogStatus.INFO, "<b>Added invalid scenario ID80</b>", "<b>Since CBN Contract with Logistical preference options Not displayed when Ship complete is set to Yes-Lease is not available</b>");

		// Test data
		Map<String, String> regData = getScenarioData("ID80");
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String quoteName = "MyQuote";

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser("ID80", PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		String categoryName = customerService.selectCategoryInProductsAndServices("Step 1 & 2: Click on Products & Services & Select Category",
				"User must be landed in PLP page","Services");
		Assert.assertEquals("Services", categoryName);

		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart", 
				"Product should be added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown(
				"Step 6: Select payment method as Lease from dropdown", "Lease payment should be selected", "Lease"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should not display", "Ship Consolidate", "UnAvailable", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should not display", "Ship Partial", "UnAvailable", "CreateQuote"));

		quoteDetails = createNewQuote.createQuote("Step 8: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Logistical preference options in create quote page ,when user select ship Partial
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301079
	 * @since Apr 27, 2021 11:59:23 AM
	 * @author rajoriap
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301079_Regression_CE_LogisticalPreferenceS4Flow_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectShipPartial_Direct(){

		// Reporting info
		initializeReporting("Verify Logistical preference options in create quote page ,when user select ship Partial",
				"C301079_Regression_CE_LogisticalPreferenceS4Flow_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectShipPartial_Direct",
				logger);

		//Need to remove once data is available
		Reporting.getLogger().log(LogStatus.INFO, "<b>Added invalid scenario ID80</b>", "<b>S4 Flow Contract for which ship partial by default selected is not available</b>");

		// Test data
		Map<String, String> regData = getScenarioData("ID80");
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String quoteName = "MyQuote";

		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(0);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser("ID80", PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		String categoryName = customerService.selectCategoryInProductsAndServices("Step 1 & 2: Click on Products & Services & Select Category",
				"User must be landed in PLP page","Services");
		Assert.assertEquals("Services", categoryName);

		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart", 
				"Product should be added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyContractIdInBillingInformation("Step 6: Ensure that Billing information "
				+ "module display the S4 Contract ID for which Logistical preference is set to 'Null/Blank'", "S4 Contract ID must be displayed", contractID));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should display with Ship Items as they become available' option", "Ship Partial", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected(
				"Step 7.3: Verify Ship Partial radio button is by default selected", 
				"Ship Partial radio button should be by default selected", "Ship Partial"));

		quoteDetails = createNewQuote.createQuote("Step 8: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Logistical preference options in create quote page ,when user selects 'Ship Consolidated'
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301080&group_by=cases:section_id&group_id=42095&group_order=asc&display_deleted_cases=0
	 * @since Apr 27, 2021 3:12:09 PM
	 * @author rajoriap
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301080_Regression_CE_LogisticalPreferenceS4Flow_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectsShipConsolidated_Direct(){

		// Reporting info
		initializeReporting("Verify Logistical preference options in create quote page ,when user selects 'Ship Consolidated'",
				"C301080_Regression_CE_LogisticalPreferenceS4Flow_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectsShipConsolidated_Direct",
				logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID06);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String quoteName = "MyQuote";

		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(1);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser(ID06, PURCHASERWITHORGPARTROLE);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		String categoryName = customerService.selectCategoryInProductsAndServices("Step 1 & 2: Click on Products & Services & Select Category",
				"User must be landed in PLP page","Services");
		Assert.assertEquals("Services", categoryName);

		/**Adding BTO as CTO is not working**/
		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart", 
				"Product should be added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyContractIdInBillingInformation("Step 6: Ensure that Billing information "
				+ "module display the S4 Contract ID for which Logistical preference is set to 'Null/Blank'", "S4 Contract ID must be displayed", contractID));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should display with Ship Items as they become available' option", "Ship Partial", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected(
				"Step 7.3: Verify Ship Consolidate radio button is by default selected", 
				"Ship Consolidate radio button should be by default selected", "Ship Consolidate"));

		Assert.assertTrue(createNewQuote.selectShipConsolidateAndShipPartialRadioButton(
				"Step 8: Ensure that user can select Ship Partial", 
				"User will be able to select Ship Partial", "Ship Partial"));

		Assert.assertTrue(createNewQuote.selectShipConsolidateAndShipPartialRadioButton(
				"Step 9: Select Ship consolidated radio button", 
				"User will be able to select Ship Consolidated", "Ship Consolidate"));

		quoteDetails = createNewQuote.createQuote("Step 10: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify that Logistical preference options are Not displayed in create quote page ,when Ship complete is set to No
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301046&group_by=cases:section_id&group_id=42096&group_order=asc&display_deleted_cases=0
	 * @since Apr 27, 2021 7:33:04 PM
	 * @author rajoriap
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
	//		IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301046_Regression_CE_LogisticalPreferenceCBN_VerifyLogisticalPreferenceOptionsAreNotDisplayedInCreateQuotePageWhenShipCompleteIsSetToNo_CSR(){

		// Reporting info
		initializeReporting("Verify that Logistical preference options are Not displayed in create quote page ,when Ship complete is set to No",
				"C301046_Regression_CE_LogisticalPreferenceCBN_VerifyLogisticalPreferenceOptionsAreNotDisplayedInCreateQuotePageWhenShipCompleteIsSetToNo_CSR",
				logger);

		//Need to remove once data is available
		Reporting.getLogger().log(LogStatus.INFO, "<b>Added invalid scenario ID80</b>", "<b>Since CBN Contract with Logistical preference options Not displayed when Ship complete is set to No is not available</b>");

		// Test data
		Map<String, String> regData = getScenarioData("ID80");
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String kit = getProductsByProductType(APJ, KIT).get(0);
		String quoteName = "MyQuote";

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("MDCPID", mdcpid);
		data.put("poNumber", "9999");
		data.put("phoneNumber", "8787878787");
		data.put("attentionText", "test");
		data.put("firstName", "automation");
		data.put("lastName", "User");
		data.put("paymentMethod", "Purchase Order");

		// Waiting For Users Availability
		String username = getUser(CSR);
		Assert.assertNotEquals(username, "");
		String purchaser = getUser("ID80", PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		ArrayList<String> userSet= new ArrayList<>();
		userSet.add(username);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with CSR user", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Enter MDCPID and mail id and impersonate purcahser", "Purchaser should be impersonated successfully", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		pdp = pdp.searchSKU("Step 1: Search with KIT SKU", "Requested product PDP should load", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 2: Add KIT product to cart at PDP", "KIT should be added to cart","pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 3: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 4: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 5.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should not display", "Ship Consolidate", "UnAvailable", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 5.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should not display", "Ship Partial", "UnAvailable", "CreateQuote"));

		quoteDetails = createNewQuote.createQuote("Step 6: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, username);
		Assert.assertNotEquals(quoteDetails, null);

		checkout = quoteDetails.navigateToCheckoutPage("Step 7: Click on Check out button", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 8.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should not display", "Ship Consolidate", "UnAvailable", "Checkout"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 8.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should not display", "Ship Partial", "UnAvailable", "Checkout"));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 9.1: Enter all the Mandatory fields",
				"Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder(
				"Step 9.2: Click on Create purchase Order button to place Place an Order","User successfully Placed Purchase Order and landed to Order confirmation Page",
				true));

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}


	/**
	 * Verify Logistical preference options in create quote page ,when user select ship Partial
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301047
	 * @since May 3, 2021 11:40:50 AM
	 * @author rajoriap
	 * @throws IOException 
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
	//		IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301047_Regression_CE_LogisticalPreferenceCBN_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectShipPartial_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify Logistical preference options in create quote page ,when user select ship Partial",
				"C301047_Regression_CE_LogisticalPreferenceCBN_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectShipPartial_Direct",
				logger);

		//Need to remove once data is available
		Reporting.getLogger().log(LogStatus.INFO, "<b>Added invalid scenario ID80</b>", "<b>Since CBN Contract when user select ship Partial is not available</b>");

		// Test data
		Map<String, String> regData = getScenarioData("ID80");
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String quoteName = "MyQuote";

		Map<String, String> data = new HashMap<String, String>();
		data.put("poNumber", "9999");
		data.put("phoneNumber", "6778767878");
		data.put("attentionText", "test");
		data.put("paymentMethod", "Purchase Order");
		data.put("MDCPID", mdcpid);
		data.put("catalogName", catalogName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser("ID80", PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		String categoryName = customerService.selectCategoryInProductsAndServices("Step 1 & 2: Click on Products & Services & Select Category",
				"User must be landed in PLP page","Accessories");
		Assert.assertNotEquals(categoryName, null);

		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart", 
				"Product should be added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 6.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 6.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should display with Ship Items as they become available' option", "Ship Partial", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected(
				"Step 6.3: Verify Ship Partial radio button is by default selected", 
				"Ship Partial radio button should be by default selected", "Ship Partial"));

		quoteDetails = createNewQuote.createQuote("Step 7: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteName + quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 8.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 8.2: Select requested file type as pdf. ",
				"Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopUpAndVerifyExportedFileAvailability(
				"Step 8.3: Click on export button to export file.", "PDF should get exported successfully",quoteNameValue + ".pdf"));

		String pdfValue = pdfValidations.readPdfFileInDownloads("Precondition: Getting PDF content", "PDF content should be fetched",quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyLogisticalPreferenceInPDF(
				"Step 9: Click to open pdf", "Ensure that Shipping options module display Logistic preference as 'Ship Partial'","Ship Partial",pdfValue));

		checkout = quoteDetails.navigateToCheckoutPage("Step 10: Click on Check out button", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 11.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "Checkout"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 11.2: Ensure that Shipping options module display Logistic preference", 
				"Shipping options module should display Logistic preference", "Ship Partial", "Available", "Checkout"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected(
				"Step 11.3: Verify Ship Partial radio button is selected", 
				"Ship Partial radio button should be selected", "Ship Partial"));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 12.1: Enter all the Mandatory fields",
				"Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder(
				"Step 12.2: Click on Create purchase Order button to place Place an Order","User successfully Placed Purchase Order and landed to Order confirmation Page",
				true));

		PageGenerics.deleteAFile(quoteNameValue + ".pdf ");

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Logistical preference options in create quote page, when user selects 'Ship Consolidated'
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301048&group_by=cases:section_id&group_id=42097&group_order=asc&display_deleted_cases=0
	 * @throws IOException 
	 * @since May 4, 2021 8:05:42 PM
	 * @author rajoriap
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
	//		IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301048_Regression_CE_LogisticalPreferenceCBN_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectShipConsolidated_CSR() throws IOException{

		// Reporting info
		initializeReporting("Verify Logistical preference options in create quote page, when user selects 'Ship Consolidated'",
				"C301048_Regression_CE_LogisticalPreferenceCBN_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectShipConsolidated_CSR",
				logger);

		//Need to remove once data is available
		Reporting.getLogger().log(LogStatus.INFO, "<b>Added invalid scenario ID80</b>", "<b>Since CBN Contract when user select ship Consolidated is not available</b>");

		// Test data
		Map<String, String> regData = getScenarioData("ID80");
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String quoteName = "MyQuote";

		Map<String, String> data = new HashMap<String, String>();
		data.put("poNumber", "9999");
		data.put("phoneNumber", "9889979786");
		data.put("attentionText", "test");
		data.put("paymentMethod", "Purchase Order");
		data.put("MDCPID", mdcpid);
		data.put("catalogName", catalogName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);

		// Waiting For Users Availability
		String username = getUser(CSR);
		Assert.assertNotEquals(username, "");
		String purchaser = getUser("ID80", PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		ArrayList<String> userSet= new ArrayList<>();
		userSet.add(username);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		XLSValidations xlsValidations = new XLSValidations(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with CSR user", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Enter MDCPID and mail id and impersonate purcahser", "Purchaser should be impersonated successfully", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		String categoryName = customerService.selectCategoryInProductsAndServices("Step 1: Click on Products & Services & Select Category",
				"User must be landed in PLP page","Accessories");
		Assert.assertNotEquals(categoryName, null);

		Assert.assertTrue(customerService.addProductToCart("Step 2: Add BTO product to the cart", 
				"Product should be added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 3: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 4: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 5.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 5.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should display with Ship Items as they become available' option", "Ship Partial", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected(
				"Step 5.3: Verify Ship Partial radio button is by default selected", 
				"Ship Partial radio button should be by default selected", "Ship Partial"));

		Assert.assertTrue(createNewQuote.selectShipConsolidateAndShipPartialRadioButton(
				"Step 6: Select Ship consolidated radio button", 
				"User will be able to select Ship Consolidated", "Ship Consolidate"));

		quoteDetails = createNewQuote.createQuote("Step 7: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, username);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		quoteNameValue = quoteName + quoteNameValue;

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 8.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 8.2: Select requested file type as pdf. ",
				"Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopUpAndVerifyExportedFileAvailability(
				"Step 8.3: Click on export button to export file.", "PDF should get exported successfully",quoteNameValue + ".pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 8.4 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 8.5: Select requested file type as xls. ",
				"Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopUpAndVerifyExportedFileAvailability(
				"Step 8.6: Click on export button to export file.", "XLS should get exported successfully", quoteNameValue + ".xls"));

		String pdfValue = pdfValidations.readPdfFileInDownloads("Precondition: Getting PDF content", "PDF content should be fetched",quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyLogisticalPreferenceInPDF(
				"Step 9: Click to open pdf", "Ship consolidated must be displayed","Ship Consolidated ",pdfValue));

		Assert.assertTrue(xlsValidations.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching(
				"Step 10.1: Open xls file", "HTML Embedded xls file should be opened", quoteNameValue + ".xls"));

		Assert.assertTrue(xlsValidations.verifyLogisticalPreferenceInHTMLEmbeddedXLSFile(
				"Step 10.2: Verify ship consolidate shipping method in xls file","Ship consolidated must be displayed",
				"Ship Consolidated"));

		Assert.assertTrue(xlsValidations.navigateToPreviousPage("Checkout page"));

		checkout = quoteDetails.navigateToCheckoutPage("Step 11: Click on Check out button", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected(
				"Step 12: Ensure that 'Ship consolidated' is selected", 
				"Ship consolidated must be displayed as selected", "Ship consolidate"));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 13.1: Enter all the Mandatory fields",
				"Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder(
				"Step 13.2: Click on Create purchase Order button to place Place an Order","User successfully Placed Purchase Order and landed to Order confirmation Page",
				true));

		PageGenerics.deleteAFile(quoteNameValue + ".pdf ");
		PageGenerics.deleteAFile(quoteNameValue + ".xls");

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	// New TestCases with different IDs after changes
	
	/**
	 * Verify Logistical preference options in create quote page ,when user select ship Partial
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301079
	 * @since Apr 27, 2021 11:59:23 AM
	 * @author rajoriap
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C457874_Regression_CE_LogisticalPreferenceS4Flow_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectShipPartial_Direct(){

		// Reporting info
		initializeReporting("Verify Logistical preference options in create quote page ,when user select ship Partial",
				"C457874_Regression_CE_LogisticalPreferenceS4Flow_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectShipPartial_Direct",
				logger);

		//Need to remove once data is available
		Reporting.getLogger().log(LogStatus.INFO, "<b>Added invalid scenario ID80</b>", "<b>S4 Flow Contract for which ship partial by default selected is not available</b>");

		// Test data
		Map<String, String> regData = getScenarioData(ID06);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String quoteName = "MyQuote";

		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(0);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		//String purchaser = getUser("ID01", PURCHASER);
		String purchaser = getUser(ID06, PURCHASERWITHORGPARTROLE);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

	//	String categoryName = customerService.selectCategoryInProductsAndServices("Step 1 & 2: Click on Products & Services & Select Category",
		//		"User must be landed in PLP page","Services");
		//Assert.assertEquals("Services", categoryName);

		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart", 
				"Product should be added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyContractIdInBillingInformation("Step 6: Ensure that Billing information "
				+ "module display the S4 Contract ID for which Logistical preference is set to 'Null/Blank'", "S4 Contract ID must be displayed", contractID));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should display with Ship Items as they become available' option", "Ship Partial", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected(
				"Step 7.3: Verify Ship Partial radio button is by default selected", 
				"Ship Partial radio button should be by default selected", "Ship Partial"));

		quoteDetails = createNewQuote.createQuote("Step 8: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Logistical preference options in create quote page ,when user selects 'Ship Consolidated'
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301080&group_by=cases:section_id&group_id=42095&group_order=asc&display_deleted_cases=0
	 * @since Apr 27, 2021 3:12:09 PM
	 * @author rajoriap
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C457875_Regression_CE_LogisticalPreferenceS4Flow_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectsShipConsolidated_Direct(){

		// Reporting info
		initializeReporting("Verify Logistical preference options in create quote page ,when user selects 'Ship Consolidated'",
				"C457875_Regression_CE_LogisticalPreferenceS4Flow_VerifyLogisticalPreferenceOptionsInCreateQuotePageWhenUserSelectsShipConsolidated_Direct",
				logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID06);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String quoteName = "MyQuote";

		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(1);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser(ID06, PURCHASERWITHORGPARTROLE);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

	//	String categoryName = customerService.selectCategoryInProductsAndServices("Step 1 & 2: Click on Products & Services & Select Category",
		//		"User must be landed in PLP page","Services");
	//	Assert.assertEquals("Services", categoryName);

		/**Adding BTO as CTO is not working**/
		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart", 
				"Product should be added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyContractIdInBillingInformation("Step 6: Ensure that Billing information "
				+ "module display the S4 Contract ID for which Logistical preference is set to 'Null/Blank'", "S4 Contract ID must be displayed", contractID));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 7.2: Check for Ship partial Logistical preference under shipping Option module", 
				"Ship Partial option should display with Ship Items as they become available' option", "Ship Partial", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected(
				"Step 7.3: Verify Ship Consolidate radio button is by default selected", 
				"Ship Consolidate radio button should be by default selected", "Ship Consolidate"));

		Assert.assertTrue(createNewQuote.selectShipConsolidateAndShipPartialRadioButton(
				"Step 8: Ensure that user can select Ship Partial", 
				"User will be able to select Ship Partial", "Ship Partial"));

		Assert.assertTrue(createNewQuote.selectShipConsolidateAndShipPartialRadioButton(
				"Step 9: Select Ship consolidated radio button", 
				"User will be able to select Ship Consolidated", "Ship Consolidate"));

		quoteDetails = createNewQuote.createQuote("Step 10: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	
}
