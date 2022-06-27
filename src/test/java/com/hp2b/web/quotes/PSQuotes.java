package com.hp2b.web.quotes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hp2b.common.FrameworkMethods;
import com.hp2b.common.HP2BDataProvider;
import com.hp2b.csv.CSVValidations;
import com.hp2b.interfaces.testcasetagging.IGroupsTagging;
import com.hp2b.pdf.PDFValidations;
import com.hp2b.web.pom.AccountSummary;
import com.hp2b.web.pom.Checkout;
import com.hp2b.web.pom.CreateAddress;
import com.hp2b.web.pom.CreateNewQuote;
import com.hp2b.web.pom.CustomerService;
import com.hp2b.web.pom.HomePage;
import com.hp2b.web.pom.Login;
import com.hp2b.web.pom.OrderConfirmation;
import com.hp2b.web.pom.PDP;
import com.hp2b.web.pom.PLP;
import com.hp2b.web.pom.PageGenerics;
import com.hp2b.web.pom.QuoteDetails;
import com.hp2b.web.pom.QuoteListing;
import com.hp2b.web.pom.ShoppingCart;
import com.hp2b.web.pom.gmail.GmailPage;
import com.hp2b.xls.XLSValidations;
import com.hpicorp.hpframework.reporting.Reporting;
import com.relevantcodes.extentreports.LogStatus;

public class PSQuotes extends FrameworkMethods {

	Logger logger = Logger.getLogger(PSQuotes.class);

	private String url = "";
	private static final List<String> shippingOptions = Arrays.asList("Standard Delivery","Two Day","Next Day, A.M.","Next Day, P.M.");

	public void setEnvironment(String store) {
		String selector = System.getProperty("env");
		selector = selector + "-PS" + store;
		url = _webcontrols.get().propFileHandler().readProperty("config", selector);
	}


	/**
	 * Verify user is able to "Checkout" from existing quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301103
	 * @since Apr 26, 2020
	 * @author Manpreet
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301103_Regression_PS_Quote_VerifyUserIsAbleToCheckoutFromExistingQuote_CSR() {

		// Reporting info
		initializeReporting("Verify user is able to Checkout from existing quote","C301103_Regression_PS_Quote_VerifyUserIsAbleToCheckoutFromExistingQuote_CSR", logger);

		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String poNumber = regData.get("Orders");
		String phoneNumber = "12345";
		String attentionText = "test";
		String PO = "Purchase Order";

		String bto=getProduct(USPS,BTO,LAPTOPS);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
        
		// Get user
		ArrayList<String> users = new ArrayList<String>();
		String partnerAgent = getUser("ID04", PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		users.add(partnerAgent);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser);
		users.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);					
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser);
		
		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("poNumber", poNumber);
		mandatoryData.put("phoneNumber", phoneNumber);
		mandatoryData.put("attentionText", attentionText);
		mandatoryData.put("paymentMethod", PO);
		mandatoryData.put("MDCPID", mdcpid);
		mandatoryData.put("catalogName", catalogName);
		mandatoryData.put("actionOnUsers", actionOnUsers);
		mandatoryData.put("OrgName", orgName);
		mandatoryData.put("emailID", purchaser);

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);		

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		/** Pre-Condition ends **/
		accountSummary = login.navigateToMyAccount("Step 1: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 2: Click on 'Quotes' under orders and quotes", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		checkout = quoteListing.selectCheckoutQuotesOptionFromActionLink("Step 3: Click on Action gear icon and select 'Checkout' option", "Checkout page should be displayed");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifyQuoteAndReferenceNumberIsDisplayed("Step 4: Verify Reference number and Quote number is displayed", "Reference number and quote number should be auto populated"));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 5.1:Enter all the Mandatory fields", "User should enter all the values successfully", mandatoryData));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 5.2: Click on Create purchase Order button to place an Order",
				"PO confirmation page should be displayed.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Quote_Verify search results for 'Quote Name' in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301107
	 * @since April 23, 2021
	 * @author ShishoSa
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301107_Regression_PS_Quotes_VerifySearchResultsForQuoteNameInQuoteListingPage_Direct() {

		//Reporting info
		initializeReporting("Quote_Verify search results for 'Quote Name' in quote listing page",
				"C301107_Regression_PS_Quotes_VerifySearchResultsForQuoteNameInQuoteListingPage_Direct", logger);

		//Test Data
		String scenarioID = ID04;
		Map<String, String> regData = getScenarioData(scenarioID);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");		
		String bto = getProduct(USPS, BTO, ACCESSORIES);
		Assert.assertNotNull(bto);
		String password = passwords.get(DIRECTUSERPWD);

		//Waiting for user availability		
		String purchaser = getUser(scenarioID, PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		String invalidQuote = "Quote768#8263589279";
		String dropdownValue = "Quote Name";
		String quoteName2 = "AutQuote";

		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > User Name > Enter Password > Click on Sign In", url, purchaser, password, true));
		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select Organization & Catalog", "Requested Organization & Catalog should be selected", data, true));
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "PS", false));
		Assert.assertNotNull(login.searchSKU("PreCondition: Search with BTO SKU", "Requested product PDP should load", bto));
		Assert.assertTrue(login.addProductToCart("PreCondition: Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));
		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page", "User should navigate to shopping cart page"));
		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page"));
		Assert.assertNotNull(createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save quote",
				"Quote should be created successfully", "AutQuote", purchaser));
		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotNull(quoteDetailsList);
		String quoteName = quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");  
		Assert.assertTrue(login.clickOnHomeTab("Precondition: Go to Home page", "Home page should be displayed", true));

		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 1: Click on quotes under Orders and Quotes tab in Home Page", 
				"Quote listing page should be displayed with list of quotes created", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 2: Select Quote Name option from selection Drop down<br>"
				+ "Step 3: Enter the full valid quote name in Text box and click on Search Icon", "Quote Name option must have been selected<br>"
						+ "Search results must appear across all the pages", dropdownValue, quoteName, false));

		Assert.assertTrue(quoteListing.verifyQuotesAreDisplayed("Step 4: Verify search results include both shared and created quotes", 
				"Search results should display both the quotes that were created in their ID and also that were shared with them when, "
						+ "the search name is present in their quote name", quoteName));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 5: Verify search results include both valid and expired quotes", 
				"Search results must display both valid/expired quotes when the quote name contains with the search input"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 6: Enter a few characters (simple letters and combination of characters and numbers)in Text box and click on Search Icon Eg:quo,quo@\"$,uqote67@",
				"Search results must appear across all the pages when the partial input given in the text box is found in the quote name (Wild card search)", dropdownValue, quoteName2, false));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 7: Verify search result appears with the most recently added Quote on Top-Descending Order", 
				"Most recently added quote that is matched with search criteria should display on Top"));

		Assert.assertTrue(quoteListing.enterInvalidQuoteNumberAndVerifyErrorMessage("Step 8: Enter an invalid quote name in Text box and click on Search Icon", 
				"User must be displayed with below error message<br>"
						+ "Quote(s) not available. If you encountered any issues, please contact your agent or HP representative.", invalidQuote));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_Verify expired quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301110
	 * @since Apr 27, 2020
	 * @author ShishoSa
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301110_Regression_PS_Quotes_VerifyExpiredQuote_CSR() {

		// Reporting info
		initializeReporting("Quote_Verify expired quote", "C301110_Regression_PS_Quotes_VerifyExpiredQuote_CSR", logger);

		// Test data
		String scenarioID = ID04;
		Map<String, String> regData = getScenarioData(scenarioID);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability	
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(FEDERALCSR);
		Assert.assertNotNull(csr);
		userSet.add(csr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(scenarioID, PURCHASER);
		Assert.assertNotNull(purchaser);				
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);
		String[] actionOptions = {"View quote details", "Copy quote", "Add items to current cart"};

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > Enter CSR User Name > Enter Password > Click on Sign In", url, csr, password, true));
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Click Customer Service link> Enter MDCPID and Email Id > Click on Search > Click on Action Gear against search result and Select Impersonate User option",
				"User should be impersonated", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1: Click on Organization and Catalog dropdown<br>"
				+ "Step 2: Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Requested Organization & Catalog should be selected", data, true));

		Assert.assertNotNull(login.navigateToMyAccount("Step 3: Click on 'My accounts' in home page", "My accounts page should be displayed"));

		Assert.assertNotNull(accountSummary.clickOnQuotesUnderMyAccountSection("Step 4: Click on 'Quotes' under Orders and Quotes", 
				"Quote list page should display with list of existing quotes"));

		Assert.assertTrue(quoteListing.clickActionBtnAndVerifyOptionsForExpiredQuote("Step 5: Select any expired quote from list of quotes displayed and click on action gear icon", 
				"Below list of options should be displayed<br>View quote details, Copy quote, Add items to current cart", actionOptions));

		Assert.assertTrue(quoteListing.clickOnExpiredQuote("PreCondition: Open expired quote", "Expired quote details page should be displayed"));

		Assert.assertTrue(quoteDetails.verifyQuoteExpiryDate("Step 6: Verify quote expiry date", "Expiry date should be exactly for 30 days from quote created date"));

		Assert.assertTrue(quoteDetails.verifyShareQuoteIsNotDisplayed("Step 7: Verify expired quote is not available to share", 
				"Share quote option should not be displayed for expired quote"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Hide and Unhide for quote on quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/367501
	 * @since Apr 19, 2021 9:09:47 AM
	 * @author  Vijay
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C367501_Regression_PS_Quotes_VerifyHideAndUnhideForQuoteOnQuoteListingPage_Direct(){

		// Reporting info
		initializeReporting("Verify Hide and Unhide for quote on quote listing page", 
				"C367501_Regression_PS_Quotes_VerifyHideAndUnhideForQuoteOnQuoteListingPage_Direct", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability		
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition : Delete product", "Product should be deleted", "PS", false));

		Assert.assertNotNull(login.searchSKU("PreCondition: Search with BTO SKU", "Requested product PDP should load", bto));

		Assert.assertTrue(login.addProductToCart("PreCondition: Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page", "User should navigate to shopping cart page"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page"));

		Assert.assertNotNull(createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save quote",
				"Quote should be created successfully", "AutQuote", purchaser));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotNull(quoteDetailsList);

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList, "Quote Number");  
		Assert.assertTrue(login.clickOnHomeTab("Precondition: Go to Home page", "Home page should be displayed", true));

		/** Pre-Condition ends **/

		Assert.assertTrue(accountSummary.clickOnOrderAndQuotesTabAndNavigate("Step 1: Click on Orders & Quotes Tab and click on Quotes", "Quotes listing page should display",
				"Quotes"));

		Assert.assertTrue(quoteListing.verifyDefaultToggleShowHiddenQuotesIsDisplayed("Step 2: Verify the default toggle for Hide,Unhide of quotes"
				, "The Default toggle 'Show Hidden Quotes' should be displayed", true));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1: Search Quote with quote number", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.selectHideQuotesOptionFromActionLink("Step 3.2: Select Hide quote option", "Hide quote should be selected", quoteNumber, true));

		Assert.assertTrue(quoteListing.clickOnShowHiddenQuoteToggleVerifyHiddenQuoteIsDisplayed("Step 4: Click on Show Hidden Quotes toggle",
				"Quoted which are hidden ( if any ) should be displayed with Eye icon strike off and toggle should be changed to 'Hide Hidden Quotes'" , true));

		Assert.assertTrue(quoteListing.clickOnHideHiddenQuoteToggle("Step 5: Click on Hide Hidden Quotes"
				, "The Hidden quotes should not be displayed", true));

		Assert.assertTrue(quoteListing.clickOnShowHiddenQuoteToggleVerifyHiddenQuoteIsDisplayed("Click on Show Hidden Quotes toggle",
				"Quoted which are hidden ( if any ) should be displayed with Eye icon strike off and toggle should be changed to 'Hide Hidden Quotes'", false));

		Assert.assertTrue(quoteListing.selectUnHideQuotesOptionFromActionLink("Step 6: For the same quote click on actions gear button and click on Unhide option", 
				"Hide quote should be selected", quoteNumber, true));

		Assert.assertTrue(quoteListing.clickOnMyAccount("Step 7: Click on My accounts", "My Accounts page should be displayed"));

		Assert.assertTrue(quoteListing.verifyElementIsDisplayedByText("Step 8: Verify the Quotes section", 
				"Only Unhidden Quotes should be displayed in My accounts page", quoteNumber, true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify search results for 'Created By' in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301108
	 * @since Apr 27, 2021 1:13:55 PM
	 * @author Rashi
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.PARTNERAGENT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301108_Regression_PS_Quotes_VerifySearchResultsForCreatedByInQuoteListingPage_PartnerAgent() {

		//Reporting info
		initializeReporting("Quote_Verify search results for 'CreatedBy' in quote listing page",
				"C301108_Regression_PS_Quotes_VerifySearchResultsForCreatedByInQuoteListingPage_PartnerAgent", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String password =passwords.get(DIRECTUSERPWD);
		String MDCPID = regData.get("MDCP ID");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String bto=getProduct(USPS,BTO);
		Assert.assertNotNull(bto);
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", MDCPID);
		data.put("catalogName", catalogName);
		data.put("actionOnUsers", "Buy On Behalf");
		data.put("OrgName", orgName);
		
		//Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String username = getUser("ID04", PARTNERAGENT);
		Assert.assertNotEquals(username,null);
		userSet.add(username);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser1 = getUser("ID04", PURCHASER);
		Assert.assertNotEquals(purchaser1,null);
		userSet.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);		
		String purchaser2 = getUser("ID04", PURCHASER);
		Assert.assertNotEquals(purchaser2,null);
		userSet.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		
		data.put("emailID", purchaser1);

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		HomePage homePage = new HomePage(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);

		/** Pre-Condition Starts **/
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>", "<b>Creating Quote & Sharing with Another User </b>");
		logger.info("<b>Pre-Condition Starts</b><br>Creating Quote & Sharing with Another User");

		Assert.assertTrue(login.loginToHP2B("Login with Partner agent user.", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition : Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully","UniqueAutomationQuoteToShare", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteReferenceNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButtonPS("Share quote with another user","Quote Should be shared successfully",purchaser2,true));

		data.replace("emailID", purchaser1,purchaser2);

		Assert.assertTrue(quoteDetails.clickOnCustomerServiceLinkAndClickOnOkButtonInsideTerminateSessionPopup("Terminate the current session", "Customer Service Page Should be displayed for New Session ", purchaser2, true));

		//usersAvailability.replace(purchaser1, "Free");
		updateUserStatus(purchaser1, "Free");
		userSet.remove(purchaser1);

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog", "Requested org & catalog should be selected", data,true));

		logger.info("<b>Pre-Condition Ends</b><br>Quote Created & Shared Successfully");
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Ends</b>", "<b>Quote Created & Shared Successfully</b>");

		/** Pre-Condition Ends **/

		Assert.assertNotNull(homePage.navigateToMyAccount("Step 1: Click on 'My accounts'", "User should navigate to My accounts page"));

		//Assert.assertTrue(accountSummary.navigateToQuotes("Step 2 : Click on Quotes Tab", "Quote listing page should be displayed with list of quotes available."));
        
		Assert.assertTrue(customerService.clickAndNavigateToLinksUnderMyAccount("Step 2: Mouseover on MyAccount and click on Quotes link"
				,"Quote listing page should be displayed with list of quotes available.", "Quotes"));
		
		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3 & 4 :Select Created By option from selection Drop down,<br>Enter a valid full/complete created by Text in Text box and click on Search Icon", "Created By option should be selected & Search results must appear across all the pages when the partial input given in the text box is found.",
				"Created By", "automation", false));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 5:Verify search results include both valid and expired quotes","Search results must display both valid/expired quotes when the quote number contains with the search input"));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 6:Verify search results include both shared and created quotes", "Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched", quoteReferenceNumber));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 7:Verify search result appears with the most recently added Quote on Top-Descending Order","Most recently added quote that is matched with search criteria should display on Top."));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 8:Enter a any single character in Text box and click on Search Icon", "Search results must appear across all the pages when the partial input given in the text box is found in the quote Created By regardless of Small or capital letter",
				"Created By", "a", false));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 9:Enter a few characters (simple letters and combination of characters and numbers)in Text box and click on Search Icon", "Search results must appear across all the pages when the partial input given in the text box is found in the quote Created By regardless of Small or capital letter",
				"Created By", "aut", false));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 10:Verify search results include both shared and created quotes", "Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched", quoteReferenceNumber));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 11:Verify search result appears with the most recently added Quote on Top-Descending Order","Most recently added quote that is matched with search criteria should display on Top."));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 12:Enter a invalid created by Text in text box and click on Search Icon", "User must be displayed with below error message Quote(s) not available. If you encountered any issues please contact your agent or HP representative.",
				"Created By", "Abc%%%$$##@", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify user is able to share existing quote from my account quotes page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301100
	 * @since Apr 27, 2021 7:53:08 PM
	 * @author Keshav
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301100_Regression_PS_Quotes_VerifyUserIsAbleToShareExistingQuoteFromMyAccountsQuotesPage_CSR(){

		// Reporting info
		initializeReporting("Verify user is able to share existing quote from my account quotes page",
				"C301100_Regression_PS_Quotes_VerifyUserIsAbleToShareExistingQuoteFromMyAccountsQuotesPage_CSR", logger);

		Map<String, String> regData = getScenarioData("ID04");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");

		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);		
        
		// Get user
		ArrayList<String> users = new ArrayList<String>();
		String partnerAgent = getUser("ID04", PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		users.add(partnerAgent);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser1 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser1);		
		users.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser2 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser2);
		users.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);								
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser1);
		
		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("MDCPID", mdcpid);
		mandatoryData.put("catalogName", catalogName);
		mandatoryData.put("actionOnUsers", actionOnUsers);
		mandatoryData.put("OrgName", orgName);

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);		

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition:  Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition:Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully","QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");

		/** Pre-Condition Ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1,2: Select requested catalog", "Requested catalog should be selected", data,true));

		AccountSummary accSummary = customerService.navigateToMyAccount("Step 3: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		quoteListing = accSummary.clickOnQuotesUnderMyAccountSection("Step 4: Click on 'My accounts' in home page", 
				"Quote list page should display with list of existing quotes with gear icon");
		Assert.assertNotNull(quoteListing);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 5.1 :Click on quote which is created by Partner agent", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 5.2: Click on Action gear icon","Clicked on Share quote",
				"Share quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 5.3: Select 'Share quote' option", "Share quote pop up with login id text field and with Cancel & Share quote buttons should be displayed",
				"Share quote"));

		Assert.assertTrue(quoteListing.enterEmailidAndVerifySharequoteMessage("Step 6: Enter valid login details in 'Login id' field and click on 'Share quote' button",
				"\"Quote is now available to : login Id\" message should be displayed and quote is shared successfully", purchaser2));

		Assert.assertTrue(login.clickOnCustomerServiceLink("Step 7.1: Click on customer service link in home page"
				, "Clicked on Customer Service link."));

		//		Assert.assertTrue(login.clickOnYesButtonOn("Step 7.2: Click on Yes popup after clicking on customer service link",
		//				"Clicked on Yes button.",true));

		//usersAvailability.replace(purchaser1, "Free");
		updateUserStatus(purchaser1, "Free");
		data.put("emailID", purchaser2);
		
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Step 7.2: Enter other user login mail id with whom quote is shared", "Impersonate the user", data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 7.3: Select requested catalog", "Requested catalog should be selected", data,true));

		accSummary = customerService.navigateToMyAccount("Step 7.4: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		quoteListing = accSummary.clickOnQuotesUnderMyAccountSection("Step 7.5: Click on 'My accounts' in home page", 
				"Quote list page should display with list of existing quotes with gear icon");
		Assert.assertNotNull(quoteListing);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 7.6: Search shared quote", "Shared Quote details should display",
				"Quote Number", quoteNumber, false));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify user is able to "Add items to current cart" from quote list page and checkout
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301104
	 * @since Apr 26, 2020
	 * @author Manpreet
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301104_Regression_PS_Quote_VerifyUserIsAbleToAddItemsToCurrentCartFromQuoteListPageAndCheckout_CSR() {

		// Reporting info
		initializeReporting("Verify user is able to Add items to current cart from quote list page and checkout","C301104_Regression_PS_Quote_VerifyUserIsAbleToAddItemsToCurrentCartFromQuoteListPageAndCheckout_CSR", logger);

		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String poNumber = regData.get("Orders");
		String phoneNumber = "12345";
		String attentionText = "test";
		String PO = "Purchase Order";
		String bto=getProduct(USPS,BTO,LAPTOPS);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Get user
		ArrayList<String> users = new ArrayList<String>();
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		users.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);				
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("emailID", purchaser);
		
		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("poNumber", poNumber);
		mandatoryData.put("phoneNumber", phoneNumber);
		mandatoryData.put("attentionText", attentionText);
		mandatoryData.put("paymentMethod", PO);
		mandatoryData.put("MDCPID", mdcpid);
		mandatoryData.put("catalogName", catalogName);
		mandatoryData.put("actionOnUsers", actionOnUsers);
		mandatoryData.put("OrgName", orgName);
		mandatoryData.put("emailID", purchaser);
		
		String dropdownValue = "Quote Number";		
		// String productSku = "B4U35AA";

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		
		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1: Click on Organization and Catalog dropdown<br>Step 2: Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected view dropdown is displayed<br>Selected catalog should be loaded", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition:  Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition:Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully","QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");

		/** Pre-Condition ends **/
		accountSummary = login.navigateToMyAccount("Step 3: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 4.1: Click on 'Quotes' under orders and quotes", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 4.2: Select Quote Name option from selection Drop down<br>"
				+ "Step 4.33: Enter the full valid quote name in Text box and click on Search Icon", "Quote Name option must have been selected<br>"
						+ "Search results must appear across all the pages", dropdownValue, quoteNumber, false));

		shoppingCart =  quoteListing.selectAddCurrentItemToCartQuotesOptionFromActionLink("Step 4.4: Click on action gear icon and select 'Add items to current cart' option", "Cart page should be displayed with list of items");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.verifyProductSkuInCart("Step 4.5: Verify Cart page should be displayed with list of items which are specified in the quote.", "Cart page should be displayed with list of items which are specified in the quote.", bto));

		checkout = shoppingCart.clickOnCheckOut("Step 5.1: Click on checkout button in cart page", "Checkout page should be displayed with list of products added in cart");
		Assert.assertNotEquals(checkout, null);

		//Assert.assertTrue(checkout.verifyElementIsDisplayedByText("Step 5.2: Verify Checkout page contains the products in cart", "Checkout page should contain the products in cart", bto, true));
		
		Assert.assertTrue(checkout.verifyProductInCheckout("Step 5.2: Verify Checkout page contains the products in cart", "Checkout page should contain the products in cart", bto, true));
		
		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 6.1:Enter all the Mandatory fields", "User should enter all the values successfully", mandatoryData));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 6.2: Click on Create purchase Order button to place an Order",
				"PO confirmation page should be displayed.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify pagination and sorting changes in quote list page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301105
	 * @since Apr 27, 2021 3:36:32 PM
	 * @author Rashi
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301105_Regression_PS_Quotes_VerifyPaginationAndSortingChangesInQuoteListPage_CSR() { 

		// Reporting info
		initializeReporting("Verify pagination and sorting changes in quote list page","C301105_Regression_PS_Quotes_VerifyPaginationAndSortingChangesInQuoteListPage_CSR", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);

		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String bto=getProduct(USPS,BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		ArrayList<String> userSet= new ArrayList<>();
		//Waiting for user availability
		String username = getUser(FEDERALCSR);
		Assert.assertNotEquals(username,null);
		userSet.add(username);
		String purchaser1 =getUser(ID04, PURCHASER);
		Assert.assertNotEquals(purchaser1,null);
		userSet.add(purchaser1);
		data.put("emailID", purchaser1);
		String purchaser2 =getUser(ID04, PURCHASER);
		Assert.assertNotEquals(purchaser2,null);
		userSet.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);

		/** Pre-Condition Starts **/
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>", "<b>Creating Quote & Sharing with Another User </b>");
		logger.info("<b>Pre-Condition Starts</b><br>Creating Quote & Sharing with Another User"); 

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with CSR user.", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Impersonate purchaser.", "User Should be Impersonated successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Navigate to shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully","AutoQuoteToShare", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteReferenceNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteReferenceNumber, null);

		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButtonPS("Share quote with another user","Quote Should be shared successfully",purchaser2,true));

		//usersAvailability.replace(purchaser1, "Free");
		userSet.remove(purchaser1);
		updateUserStatus(purchaser1, "Free");

		Assert.assertTrue(quoteDetails.clickOnCustomerServiceLinkAndClickOnOkButtonInsideTerminateSessionPopup("Terminate the current session", "Customer Service Page Should be displayed for New Session ", purchaser2, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Impersonate purchaser.", "User Should be Impersonated successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Ends</b>", "<b>Quote Created & Shared Successfully</b>");
		logger.info("<b>Pre-Condition Ends</b><br>Quote Created & Shared Successfully");
		/** Pre-Condition Ends **/

		accountSummary = login.navigateToMyAccount("Step 1: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		//Assert.assertTrue(accountSummary.navigateToQuotes("Step 2 : Click on Quotes link on Left hand side", "Quote listing page should be displayed with list of quotes available."));
		
		Assert.assertTrue(customerService.clickAndNavigateToLinksUnderMyAccount("Step 2: Mouseover on MyAccount and click on Quotes link"
				,"Quote listing page should be displayed with list of quotes available.", "Quotes"));

		Assert.assertTrue(quoteListing.verifyPaginationLinkIsDisplayed("Step 3: Verify pagination links are displayed in quote listing page","Pagination links should be displayed as per quotes available",true));

		Assert.assertTrue(quoteListing.clickOnPaginationLinkAndVerifyQuotesCountAsDisplayedInShowResultsDropDown("Step 4:Verify on click of pagination link", "Appropriate page should be displayed and pagination link should work as per the result count."));

		Assert.assertTrue(quoteListing.verifyPaginationForQuotesAccordingToShowResultsDropdown("Step 5: Verify pagination for quote search results", "Pagination should work as intended and should display search results as per the result count selected"));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 6:Verify search results include both shared and created quotes", "Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched", quoteReferenceNumber));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 7: Verify quotes default sorting in quote list page", "By default quotes are sorted by 'Created On' date"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 8: Click on 'Up ward' arrow for 'Quote number'",
				"Quotes should be sorted by 'Quote number' across all pages in ascending order.Quote numbers with low to high numbers should be displayed",
				"Quote number", "ascending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 9: Click on 'Down ward' arrow for 'Quote number'",
				"Quotes should be sorted by 'Quote number' across all pages in descending order.Quote numbers with high to low numbers should be displayed",
				"Quote number", "descending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 10: Click on 'Up ward' arrow for 'Quote name'",
				"Quotes should be sorted by 'Quote name' across all pages in ascending order Quote names with low to high precedence should be displayed",
				"Quote name", "ascending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 11: Click on 'Up ward' arrow for 'Quote name'",
				"Quotes should be sorted by 'Quote name' across all pages in ascending order Quote names with low to high precedence should be displayed",
				"Quote name", "descending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 12: Click on 'Up ward' arrow for 'Created by'",
				"Quotes should be sorted by 'Created by' names across all pages in ascending order.'Created by' name with low to high precedence should be displayed",
				"Created by", "ascending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 13: Click on 'Down ward' arrow for 'Created by'",
				"Quotes should be sorted by 'Created by' names across all pages in descending order 'Created by' name with high to low precedence should be displayed",
				"Created by", "descending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 14: Click on 'Up ward' arrow for 'Amount'",
				"Quotes should be sorted by 'Amount' names across all pages in ascending order.'Amount' name with low to high precedence should be displayed",
				"Amount", "ascending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 15: Click on 'Down ward' arrow for 'Amount'",
				"Quotes should be sorted by 'Amount' names across all pages in descending order 'Amount' name with high to low precedence should be displayed",
				"Amount", "descending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 16: Click on 'Up ward' arrow for 'Created on'",
				"Quotes should be sorted by 'Created on' names across all pages in ascending order.'Created on' name with low to high precedence should be displayed",
				"Created on", "ascending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 17: Click on 'Down ward' arrow for 'Created on'",
				"Quotes should be sorted by 'Created on' names across all pages in descending order 'Created on' name with high to low precedence should be displayed",
				"Created on", "descending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 18: Click on 'Up ward' arrow for 'Expires on'",
				"Quotes should be sorted by 'Expires on' names across all pages in ascending order.'Expires on' name with low to high precedence should be displayed",
				"Expires on", "ascending"));

		Assert.assertTrue(quoteListing.sortQuoteResults("Step 19: Click on 'Down ward' arrow for 'Expires on'",
				"Quotes should be sorted by 'Expires on' names across all pages in descending order 'Expires on' name with high to low precedence should be displayed",
				"Expires on", "descending"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");


	}

	/**
	 * Verify quote creation for change in billingAddress
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301085
	 * @since Apr 29, 2021 10:52:14 AM
	 * @author ThomasAn
	 */


	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.PARTNERAGENT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301085_Regression_PS_Quotes_VerifyQuoteCreationForChangeInBillingAddress_PartnerAgent() {

		//Reporting info
		initializeReporting("Verify quote creation for change in billingAddress",
				"C301085_Regression_PS_Quotes_VerifyQuoteCreationForChangeInBillingAddress_PartnerAgent", logger);
		

        //Test data
		Map<String, String> regData = getScenarioData("ID04");
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpId = regData.get("MDCP ID");
		String password = passwords.get(DIRECTUSERPWD);

		Map<String, String> data = new HashMap<String, String>();
		data.put("actionOnUsers", "Buy On Behalf");
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("MDCPID", mdcpId);

	//	String bto = getProduct(USPS, BTO);
		String bto = "7MC28UP";
		Assert.assertNotNull(bto);
		String qty = "900";

		
		//Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String username = getUser("ID04", PARTNERAGENT);
		Assert.assertNotNull(username);

		String purchaser =getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser);
		data.put("emailID", purchaser);
		userSet.add(username);
		userSet.add(purchaser);
		data.put("emailID", purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(
				login.loginToHP2B("PreCondition : Login to HP2B with Partner Agent", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser(
				"PreCondition : Enter MDCPID and mail id and Buy on behalf purchaser",
				"Purchaser should be buy on behalf successfully", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog",
				"Requested catalog should be selected", data, true));
		Assert.assertTrue(login.deleteProducts("PreCondition : Delete product", "Product should be deleted", "PS", false));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2.1: Enter the Qty as " + qty, "Quantity should be entered", qty));

		Assert.assertTrue(pdp.addProductToCart("Step 2.2: Add BTO product to cart at PDP",
				"Product should be added to cart", "pdp"));
		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 3: Click on 'Mini cart' icon and Click on 'Go to cart' button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 4: Click on save as quote button",
				"User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyPartnerAgentID("Step 5: Verify Partner Agent ID is pre-populated",
				"Partner Agent id should pre-populate"));

		Assert.assertTrue(createNewQuote.verifyBillingAddress("Step 6 : Verify billing address",
				"User should be able to see the billing address"));
		Assert.assertTrue(
				createNewQuote.verifyChangeBillingAddressButton("Step 7 : Verify change billing address button",
						"User should be able to see change billing address button"));

		Assert.assertTrue(createNewQuote.clickOnChangeBillingAddressAndVerifyPopupWindow(
				"Step 8: Click on change billing address button",
				"User should  click change billing address and pop up window should be verified"));

		String selectedNewBillingAddress = createNewQuote.clickOnChangeBillingAddressAndSelectNewBillingAddressAndClickOnOk("Step 9 :Select different billing address and click on ok button",
				"Selected billing address should be displayed", true);
		Assert.assertNotEquals(selectedNewBillingAddress, null);

		quoteDetails = createNewQuote.createQuote("Step 10: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		LinkedHashMap<String, String> LinkedHashMap = quoteDetails.getBillingInformationDetails(
				"Step 11: verify billing address in quotes details page", "Billing address should be verified", "CSV");
		Assert.assertNotEquals(LinkedHashMap, null);

		checkout = quoteDetails.navigateToCheckout("Step 12: Click on Checkout button in quote detail page",
				"User should navigate to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifyBillingAddress("Step 13.1: verify billing address in checkout page",
				"Billing address should verify"));
		
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}



	/**
	 * Verify quote creation with onfly invoice mailing address
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301087
	 * @since May 3, 2021 2:32:03 PM
	 * @author ThomasAn
	 */


	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.PARTNERAGENT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301087_Regression_PS_Quotes_VerifyQuoteCreationWithOnflyInvoiceMailingAddress_CSR() {

		//Reporting info
		initializeReporting("Verify quote creation with onfly invoice mailing address",
				"C301087_Regression_PS_Quotes_VerifyQuoteCreationWithOnflyInvoiceMailingAddress_CSR", logger);
		
		Map<String, String> regData = getScenarioData("ID04");
		Assert.assertNotEquals(regData.size(), 0);

		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpId = regData.get("MDCP ID");
		String password = commonData.get(CSRORFEDCSRUSERPWD);

		Map<String, String> data = new HashMap<String, String>();
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("MDCPID", mdcpId);
		String qty = "300";

		String bto = getProductsByProductTypeAndCategory(getRegion("US PS", "ID04"),"BTO","Laptops").get(0);

		//Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String username = getUser(FEDERALCSR);
		Assert.assertNotNull(username);

		String purchaser =getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser);
		data.put("emailID", purchaser);
		userSet.add(username);
		userSet.add(purchaser);
		data.put("emailID", purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects

		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(
				login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition : Delete product", "Product should be deleted", "PS", false));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter the Qty as " + qty,
				"Quantity should be entered", qty));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Add prroduct to cart",
				"Product should be added to cart", "pdp"));
		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click in minicart icon and click on go to cart button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button",
				"User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(
				createNewQuote.verifyBillingAddress("Step 6.1 : Verify billing address",
						"User should be able to see the billing address"));

		Assert.assertTrue(createNewQuote.verifyInvoiceMailingAddressOnCreateNewQuotePage(
				"Step 6.2 : Verify invoice mailing address",
				"User should be able to see the invoice mailing address"));

		Assert.assertTrue(createNewQuote.verifyChangeInvoiceMailingAddressButton(
				"Step 6.3 : Verify change Invoice mailing address button",
				"User should be able to see Invoice mailing address button"));

		Assert.assertTrue(createNewQuote.clickOnChangeInvoiceMailingAddressAndVerifyPopupWindow(
				"Step 7 :Click on change mailing address button under invoice mailing address",
				"Change invoice mailing address pop up window should be displayed"));

		Assert.assertTrue(createNewQuote.selectDifferentInvoiceMailingAddress(
				"Step 8: Select different invoice mailing address", "Different invoice mailing address should be selected"));

		quoteDetails = createNewQuote.createQuote("Step 9: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(quoteDetails.verifyInvoiceMailingAddress(
				"Step 10: verify invoice mailing address in quote confirmation page", "Invoice mailing address should verify in quote confirmation page"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify quote details in in quote create quote page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/295018
	 * @since May 10, 2021 11:34:25 AM
	 * @author ThomasAn
	 */
	

	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES } ,priority = 2)
	public void C295018_Regression_PS_Quotes_VerifyQuoteDetailsInCreateQuotePage_Direct() {

		//Reporting info
		initializeReporting("Verify quote details in in quote create quote page",
				"C295018_Regression_PS_Quotes_VerifyQuoteDetailsInCreateQuotePage_Direct", logger);

		
		//Test data
		Map<String, String> regData = getScenarioData("ID04");
		Assert.assertNotEquals(regData.size(), 0);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
	    String orgName = regData.get("Org Name");
	  
	    String password = passwords.get(DIRECTUSERPWD);
	    System.out.println(password);
	        
    	Map<String, String> data = new HashMap<String, String>();
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		String qty = "10";
			
		String username = getUser("ID04", PURCHASER);
		Assert.assertNotNull(username);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(username));
		String bundle = getProduct(USPS, BUNDLE);
		Assert.assertNotNull(bundle);
			
		// Get URL
		setEnvironment("-CSR");
		String url = this.url;
			
		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Login to HP2B with Direct User", url, username, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));
		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with bundle SKU", "Requested product PDP should load", bundle);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter the Qty as " + qty, "Quantity should be entered successfully",qty));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Click on add to cart button",
				"Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on mini cart icon and click on go to cart button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button",
				"User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		HashMap<String, String> data1 =	PageGenerics.getCurrentSystemDateAndEndDate(30);


		Assert.assertTrue(createNewQuote.verifyQuoteNameSectionAndFieldValidation(
				"Step 6 & 7: Verify quote name section and validate fields",
				"Quote name section should be verified and fileds should be validated",data1));

		Assert.assertTrue(createNewQuote.verifyOrderInformationSectionAndFieldValidation(
				"Step 8 & 9: Verify order information section and validate fields",
				"Order Information section should be verified and fileds should be validated"));

		Assert.assertTrue(createNewQuote.verifyPurchaserContactInformationSectionAndFieldValidation(
				"Step 10 & 11 : Verify purchaser contact information  section and validate fields on click of edit",
				"Purchaser contact information section should be verified and fileds should be validated on click of edit"));

		Assert.assertTrue(
				createNewQuote.verifyBillingAddress("Step 12 : Verify billing address session in create quote page",
						"User should be able to see the billing address session"));

		Assert.assertTrue(createNewQuote.clickOnChangeBillingAddressAndVerifyPopupWindow(
				"Step 13: Click on change billing address button under billing address session",
				"Change billing address popup should be displayed"));

		Assert.assertTrue(createNewQuote.verifyShippingAddressInCreateNewQuote("Step 14: Verify Shipping information in create quote page",
				"Default shipping address should be displayed"));

		Assert.assertTrue(createNewQuote.clickOnChangeShippingAddressButtonAndVerifyPopUpFileds(
				"Step 15: Click change shipping address button under shipping address session",
				"Change shipping address popup window should be displayed"));

		Assert.assertTrue(createNewQuote.verifyCartSummarySection("Step 16 : Verify cart summary section in create quote page",
				"Cart summary section should be verified"));

		Assert.assertTrue(createNewQuote.verifyBackToShoppingCartButton("Step 17 : Verify back to shopping cart button in create quote page",
				"Back to shopping cart button should be verified"));
		
		quoteDetails = createNewQuote.createQuote("Step 18: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", username);
		Assert.assertNotEquals(quoteDetails, null);

		List<String> quotesList = quoteDetails.getQuoteDetailsForVerification("Step 19: Verify quotes details in quote confirmation page",
				"Quote detail page should be displayed");
		Assert.assertNotEquals(quotesList, quotesList.isEmpty());

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
	
	/**
	 * Verify user is able to remove products in create quote page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301097
	 * @since Apr 19, 2021 9:09:47 AM
	 * @author  Vijay
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301097_Regression_PS_Quotes_VerifyUserIsAbleToRemoveProductsInCreateQuotePage_Direct(){

		// Reporting info
		initializeReporting("Verify user is able to remove products in create quote page", 
				"C301097_Regression_PS_Quotes_VerifyUserIsAbleToRemoveProductsInCreateQuotePage_Direct", logger);

		// Test data
		
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String kit = getProduct(USPS, KIT);
		Assert.assertNotNull(kit);
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability		
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		String quoteName = "Test_Automation_Quote_";
		String prodQty = "500";

		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("poNumber", "12345");
		mandatoryData.put("phoneNumber", "12345");
		mandatoryData.put("attentionText", "test");
		mandatoryData.put("emailID", purchaser);

		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition : Delete product", "Product should be deleted", "PS", false));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2:  Enter Qty as 500 and update",
				"Qty should be updated successfully", prodQty));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		pdp = customerService.searchSKU("Step 4: In search box , search for kit number", "PDP of searched product should be displayed", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 5:  Enter Qty as 500 and update",
				"Qty should be updated successfully", prodQty));

		Assert.assertTrue(pdp.addProductToCart("Step 6: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 7:Click on Mini cart and click Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 8.1: Click on save as quote button",
				"User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);
		Assert.assertTrue(createNewQuote.verifyProductIsDisplayed("Step 8.2: Verify First Added Products should be display", 
				"Verify First Added Products should be display", bto, true));
		Assert.assertTrue(createNewQuote.verifyProductIsDisplayed("Step 8.3: Verify Second Added Products should be display", 
				"Verify Second Added Products should be display", kit, true));

		Assert.assertTrue(createNewQuote.clickOnRemoveUnderCreatNewQuoteAndVerifyProductRemoved(
				"Step 9: Click on 'Remove' link under cart summary for first product", true));

		quoteDetails = createNewQuote.createQuote("Step 10 : Enter all mandatory details and Click on 'Save Quote' button",
				"Quote should be created successfully and navigated to Quote detail page.", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		checkout = quoteDetails.navigateToCheckoutPage("Step 11: Verify quote details and click on checkout button",
				"Checkout page should be displayed with details as specified in quote");
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 12.1:Enter all the Mandatory fields", "User should enter all the values successfully", mandatoryData));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 12.2: Click on Create purchase Order button to place an Order",
				"PO confirmation page should be displayed.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify user is able to share existing quote from home page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301101
	 * @since Apr 28, 2021 7:53:08 PM
	 * @author Keshav
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301101_Regression_PS_Quotes_VerifyUserIsAbleToShareExistingQuoteFromHomePage_CSR(){

		// Reporting info
		initializeReporting("Verify user is able to share existing quote from home page",
				"C301101_Regression_PS_Quotes_VerifyUserIsAbleToShareExistingQuoteFromHomePage_CSR", logger);

		Map<String, String> regData = getScenarioData("ID04");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");

		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);		
        
		// Get user
		ArrayList<String> users = new ArrayList<String>();
		String partnerAgent = getUser("ID04", PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		users.add(partnerAgent);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser1 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser1);
		users.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser2 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser2);
		users.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);								
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser1);	

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		HomePage homePage = new HomePage(_webcontrols);

		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("MDCPID", mdcpid);
		mandatoryData.put("catalogName", catalogName);
		mandatoryData.put("actionOnUsers", actionOnUsers);
		mandatoryData.put("OrgName", orgName);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition:  Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition:Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully","QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");

		/** Pre-Condition Ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1: Click on Organization and Catalog dropdown<br>Step 2:"
				+ "Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected view dropdown is displayed<br>Selected catalog should be loaded", data,true));

		//Assert.assertTrue(homePage.clickOnTabByName("Step 3: Click on Quotes in home page above the Quick order section",
		//		"List of existing quotes with gear icon should be displayed", "Quotes"));
		
		Assert.assertTrue(customerService.clickAndNavigateToLinksUnderMyAccount("Step 3: Mouseover on MyAccount and click on Quotes link"
				,"List of existing quotes with gear icon should be displayed", "Quotes"));

		//		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 4.1 :Search Quote", "Quote details should display",
		//				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 4.1: Click on Action gear icon","Clicked on Share quote",
				quoteNumber,"Share quote",true));

		Assert.assertTrue(quoteListing.clickOnActions("Step 4.2: Select or click 'Share quote' option", "Share quote pop up with login id text field and with Cancel & Share quote buttons should be displayed",
				"Share quote"));

		Assert.assertTrue(quoteListing.enterEmailidAndVerifySharequoteMessage("Step 5: Enter valid login details in 'Login id' field and click on 'Share quote' button",
				"\"Quote is now available to : login Id\" message should be displayed and quote is shared successfully", purchaser2));

		Assert.assertTrue(login.clickOnCustomerServiceLink("Step 6.1: Click on customer service link in home page", "Clicked on Customer Service link."));
		//		Assert.assertTrue(login.clickOnYesButtonOn("Step 7.2: Click on Yes popup after clicking on customer service link",
		//				"Clicked on Yes button.",true));

		//usersAvailability.replace(purchaser1, "Free");
		updateUserStatus(purchaser1, "Free");
		users.remove(purchaser1);
		data.put("emailID", purchaser2);
		
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Step 6.2: enter other user login mail id with whom quote is shared"
				,"Impersonate the user",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 7.1: Select requested catalog", "Requested catalog should be selected", data,true));

		//Assert.assertTrue(homePage.clickOnTabByName("Step 7.2: Navigate to quote list page",
		//		"List of existing quotes with gear icon should be displayed", "Quotes"));
		
		Assert.assertTrue(customerService.clickAndNavigateToLinksUnderMyAccount("Step 7.2: Mouseover on MyAccount and click on Quotes link"
				,"List of existing quotes with gear icon should be displayed", "Quotes"));

		Assert.assertTrue(homePage.verifyQuoteDisplayedonHomePage("Step 7.3: Verify Shared Quote", 
				"Shared quote should be displayed in quote list page", quoteNumber, true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify search results for 'Quote Number' in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301106
	 * @since Apr 26, 2020
	 * @author Manpreet
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301106_Regression_PS_Quote_VerifySearchResultsForQuoteNumberInQuoteListingPage_CSR() {

		// Reporting info
		initializeReporting("Verify search results for 'Quote Number' in quote listing page","C301106_Regression_PS_Quote_VerifySearchResultsForQuoteNumberInQuoteListingPage_CSR", logger);

		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String poNumber = regData.get("Orders");
		String phoneNumber = "12345";
		String attentionText = "test";
		String PO = "Purchase Order";

		String bto=getProduct(USPS,BTO,LAPTOPS);
		Assert.assertNotNull(bto);
		String password =passwords.get(CSRORFEDCSRUSERPWD);
		
        
		// Get user
		ArrayList<String> users = new ArrayList<String>();
		String purchaser1 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser1);		
		users.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser2 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser2);
		users.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);						
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("emailID", purchaser1);
		
		String singleDigitQuoteNumber = "5";
		String dropdownValue = "Quote Number";
		String invalidQuote = "123456789";

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Click on Organization and Catalog dropdown<br>Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected view dropdown is displayed<br>Selected catalog should be loaded", data, true));

		Assert.assertTrue(
				login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition:  Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition:Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully","QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButtonPS("Share quote with another user","Quote Should be shared successfully",purchaser2,true));

		data.replace("emailID", purchaser1,purchaser2);
		usersAvailability.replace(purchaser1, "Free");
		users.remove(purchaser1);
		updateUserStatus(purchaser1, "Free");

		Assert.assertTrue(quoteDetails.clickOnCustomerServiceLinkAndClickOnOkButtonInsideTerminateSessionPopup("Terminate the current session", "Customer Service Page Should be displayed for New Session ", purchaser2, true));

		//usersAvailability.replace(purchaser1, "Free");

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Ends</b>", "<b>Quote Created & Shared Successfully</b>");

		/** Pre-Condition ends **/
		AccountSummary accountSummary = login.navigateToMyAccount("Step 1: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		QuoteListing quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 2: Click on Quotes link on Left hand side", "Quote listing page should be displayed with list of quotes available");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.verifyDefaultSearchCriteraForQuotes("Step 3: Search Criteria Drop down must be defaulted to Quote Number", "Quote Number is the default option selected in the drop down"));

		String sharedQuoteNumber=quoteListing.getQuoteNumberBasedOnQuoteName("Step 4.1: Get shared quote number based on quote name", "Shared quote number should display", quoteNumber, true);
		Assert.assertNotEquals(sharedQuoteNumber, null);
		
		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 4.2: Enter valid full string in Text box and click on Search Icon", "Search results must appear",
				dropdownValue, sharedQuoteNumber, false));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 4.3: Enter valid partial string in Text box and click on Search Icon",
				"Search results must appear across all the pages when the partial input given is found in the quote number.", dropdownValue, sharedQuoteNumber.substring(0, 2),
				false));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 5: Verify search results include both shared and created quotes",
				"Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched", sharedQuoteNumber));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 6: Verify search results include both valid and expired quotes", 
				"Search results must display both valid/expired quotes when the quote number contains with the search input"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 7: Enter a single number in Text box and click on Search", "Search results must appear across all the pages when the partial input given in the text box is found in the quote number",
				dropdownValue, singleDigitQuoteNumber, false));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 8: Verify search result appears with the most recently added Quote on Top-Descending Order", 
				"Most recently added quote that is matched with search criteria should display on Top"));

		Assert.assertTrue(quoteListing.enterInvalidQuoteNumberAndVerifyErrorMessage("Step 9: Enter an invalid number in Text box and click on Search Icon", 
				"User must be displayed with below error message<br>"
						+ "Quote(s) not available. If you encountered any issues, please contact your agent or HP representative.", invalidQuote));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify user is able to share quote from quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301098
	 * @since Apr 29, 2021 1:13:55 PM
	 * @author Keshav
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.PARTNERAGENT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301098_Regression_PS_Quotes_VerifyUserIsAbleToShareQuoteFromQuoteConfirmationPage_PartnerAgent() {

		//Reporting info
		initializeReporting("Quote_Verify user is able to share quote from quote confirmation page",
				"C301098_Regression_PS_Quotes_VerifyUserIsAbleToShareQuoteFromQuoteConfirmationPage_PartnerAgent", logger);

		Map<String, String> regData = getScenarioData("ID04");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");		

		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String softBundle = getProduct(USPS, SOFTBUNDLE);
		Assert.assertNotNull(softBundle);
	    String password = passwords.get(DIRECTUSERPWD);   		
        
	    // Get user
	    ArrayList<String> users = new ArrayList<String>();
		String partnerAgent = getUser("ID04", PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		users.add(partnerAgent);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser1 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser1);
		users.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser2 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser2);
		users.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);				
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Buy On Behalf");
		data.put("emailID", purchaser1);
		
		String quatity = "400";
		
		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);

		/** Pre-Condition Starts **/
	
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login with Partner agent user.", url, partnerAgent,password, true));
	
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));
	
		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog", "Requested org & catalog should be selected", data,true));

		/** Pre-Condition Ends **/

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("Step 1 : In search box , search for soft bundle number", "PDP of searched product should be displayed", softBundle);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.clickOptionCodeDropdownAndSelectOption("Step 2.1: Select option code", "Option code selected.",true));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2.2: Enter the Qty as " + quatity + " and update", "Qty should be updated successfully", quatity));

		Assert.assertTrue(pdp.addProductToCart("Step 3.1:  Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		ShoppingCart shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5.1: Click on save as quote button", "'Create New Quote' page should be displayed with the list of products added in cart page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(shoppingCart.verifyFavoriteProductIsAvailableInCartPage("Step 5.2: Verify the product added is displayed in Create New Quote page", 
				"Product added is displayed in create new quote page",softBundle));

		Assert.assertTrue(createNewQuote.verifyPartnerAgentId("Step 6: Verify Partner Agent field", "Partner ID number should be auto populated",
				partnerAgent, true));

		quoteDetails = createNewQuote.createQuote("Step 7.1: Enter all mandatory details and Click on 'Save Quote' button",
				"Quote should be created successfully and navigated to Quote detail page.","QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 7.2: Getting Quote details",
				"Quote number should be fetched successfully.");
		Assert.assertNotEquals(createNewQuote, null);
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");

		String expectedMsg = "Quote is now available to : "+purchaser2;
		Assert.assertTrue(quoteDetails.verifyQuoteSharedWithMessage("Step 8: Click on 'Share quote' option in quote detail page"+
				"<br>Step 9: Enter valid login details in 'Login id' field and click on 'Share quote' button"+
				"<br>Step 10: Click on Cancel button in share quote popup",
				"Share quote pop up with login id text field with Cancel and Share quote buttons should be displayed"+
						"<br>\"Quote is now available to : login Id\" message should be displayed and quote is shared successfully"+
						"<br>Popup should be closed.", "Share quote popup verified with Cancel and Share quote buttons.<br>"
								+ "Quote Share message is also displayed: <b>"+expectedMsg+"</b><br>Closed the popup", purchaser2, expectedMsg, true));

		Assert.assertTrue(quoteDetails.logout("Step 11: Click on logout option in homepage", "User should be logged out successfully","HomePage", true));

		usersAvailability.replace(purchaser1, "Free");
		Assert.assertTrue(login.loginToHP2B("Step 12.1: Login with other user and check for shared quote", purchaser2, password, true));


		accountSummary = login.navigateToMyAccount("Step 12.2: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 12.3: Click on 'Quotes' under orders and quotes", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 12.4: Search Quote with quote number", "Quote details should display",
				"Quote Number", quoteNumber, false));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify Quote creation with onfly shipping address and checkout
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301088
	 * @since Apr 30, 2021 8:15:13 PM
	 * @author Vishwa A P
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301088_Regression_PS_Quotes_VerifyQuoteCreationWithOnFlyShippingAddressAndCheckout_CSR() { 

		// Reporting info
		initializeReporting("Verify Quote creation with onfly shipping address and checkout",
				"C301088_Regression_PS_Quotes_VerifyQuoteCreationWithOnFlyShippingAddressAndCheckout_CSR", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String mdcpid = regData.get("MDCP ID");
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		String bto = getProduct(USPS, BTO, ACCESSORIES);
		Assert.assertNotNull(bto);
		String cto = getProduct(USPS,CTO);
		Assert.assertNotNull(cto);
		
		// Waiting For Users Availability
		ArrayList<String> userSet= new ArrayList<>();
		String user = getUser(FEDERALCSR);
		Assert.assertNotNull(user);
		userSet.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);		
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		CreateAddress createAddress = new CreateAddress(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		
		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		HashMap<String, String> shippingAdress = new HashMap<String, String>();
		shippingAdress.put("Company", "MODESTO CITY SCHOOLS");
		shippingAdress.put("AttentionText", "test");
		shippingAdress.put("City", "MODESTO");
		shippingAdress.put("Phone", "9898989898");
		shippingAdress.put("Email", purchaser);
		shippingAdress.put("StateProvince", "CA");
		shippingAdress.put("ZipCode", "95351-1226");
		shippingAdress.put("GstId", "12345678");
		String addressLine = createAddress.systemDate();
		shippingAdress.put("Addressline1", addressLine+"RENO AVE STE B");

		/** Pre-Condition Starts **/
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>", "<b>Creating Quote & Sharing with Another User </b>");

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with CSR user.", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Impersonate purchaser.", "User Should be Impersonated successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog", "Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));
		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with CTO SKU", "Requested product PDP should load", cto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter Qty in quantity field","Quantity should be entered", "300"));

		Assert.assertTrue(pdp.addProductToCart("Step 3:  Add CTO product to cart at PDP", "Product should be added to cart", "pdp"));

		pdp = customerService.searchSKU("Step 4: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 5: Enter Qty in quantity field", "Quantity should be entered", "100", "PDP"));

		Assert.assertTrue(pdp.addProductToCart("Step 6: Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 7: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 8: Click on save quotes button", "User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyShippingAddress("Step 9: Verify Shipping address", "Default shipping address should be displayed"));

		Assert.assertTrue(createNewQuote.clickOnChangeShippingAddressButtonAndVerifyPopUpFileds(
				"Step 10 & 11: Verify and click on change shipping address button <br>And verify Ship to address popup fields",
				"User should verify and click on change shipping address button <br>And verify Ship to address popup fields"));

		Assert.assertTrue(createNewQuote.navigateToCreateAddressPage("Step 12.1: Click ship to new address", "Create address page should displayed"));

		Assert.assertTrue(createAddress.verifyAllFieldsInCreateAddress("Step 12.2: Verify all fields in create address page", 
				"All required fields should be displayed"));

		Assert.assertTrue(createAddress.fillMandatoryDetailsInCreateAddress("Step 13: Fill Mandatory details in create address page and click on submit ",
				"All Mandatory details fields should be Filled and clicked on submit button", shippingAdress,true));

		Assert.assertTrue(createNewQuote.vefifyNewShippingAddressAdded("Step 14.1: Verify newly created shipping address updated in Shipping address section",
				"Newly created shipping address should be displayed in shipping address section", shippingAdress, "contractType"));

		quoteDetails = createNewQuote.createQuote("Step 14.2: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(quoteDetails.verifyShippingAddressInQuoteDetailsPage("Step 16.2: verify Shipping address in quote details page", 
				"Updated Shipping address should be displayed", shippingAdress));

		checkout = quoteDetails.navigateToCheckout("Step 16: Navigate to checkout page", "User should navigate to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifyShippingAddressInCheckOutPage("Step 17: Verify Shipping address in checkout page",  "Updated shipping address should be displayed", shippingAdress));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
		
	}

	/**
	 * Verify Estimated Tax line item in exported quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301092
	 * @since May 5, 2021 9:09:47 AM
	 * @author ShishoSa
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301092_Regression_PS_Quotes_VerifyEstimatedTaxLineItemInExportedQuote_CSR() {

		//Reporting info
		initializeReporting("Verify Estimated Tax line item in exported quote",  "C301092_Regression_PS_Quotes_VerifyEstimatedTaxLineItemInExportedQuote_CSR", logger);

		//Test data
		String scenarioID = "ID04";
		Map<String, String> regData = getScenarioData(scenarioID);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String bto = getProduct(USPS, BTO, LAPTOPS);
		Assert.assertNotNull(bto);
		
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(FEDERALCSR);
		Assert.assertNotNull(csr);
		userSet.add(csr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(scenarioID, PURCHASER);
		Assert.assertNotNull(purchaser);				
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		XLSValidations xls = new XLSValidations(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > User Name > Enter Password > Click on Sign In", url, csr, password, true));
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));
		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog", "Requested org & catalog should be selected", data, true));
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "PS", false));

		Assert.assertNotNull(login.searchSKU("Step 1: In search box , search for BTO sku", "PDP of searched product should be displayed", bto));

		Assert.assertTrue(login.enterFirstProductQuantity("Step 2: Enter Qty as 999 and update", "Qty updated successfully", "999", "pdp"));

		Assert.assertTrue(login.addProductToCart("Step 3: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 5: Click on 'save as quote' button", "'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 6: Verify Estimated tax line item under cart summary section in Create quote page", 
				"Estimated tax line item should be displayed and it should be included in total", true));

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxPrice("Step 7: Verify estimated tax price if qty of the product is increased", 
				"Estimated tax price should be updated accordingly as per the qty of the product", "20", true));

		Assert.assertNotNull(createNewQuote.createQuote("Step 8: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", "AutQuote", purchaser));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotNull(quoteDetailsList);
		String quoteName = "AutQuote" + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 9: Verify Estimated tax line item under cart summary section in Quote detail page", 
				"Estimated tax line item should be displayed and it should be included in total", true));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 10.1: Click on Export", "Export popup should be displayed"));
		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 10.2: Select the export type as XLS", "File type should be selected", "xls"));
		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 10.3: Click on 'Export' in quote confirmation page", "XLS format should get exported successfully"));

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Precondition: Get Estimated Tax Value", "Estimated tax price is fetched", true);

		List<String> dataList = Arrays.asList(quoteName, "Estimated Tax:", estimatedTax);
		Assert.assertTrue(xls.verifyXlsFile("Step 11-12: Go to the file location and click on Quote to open<br>"
				+ "Verify estimated tax line item in exported quote", 
				"Estimated tax line item should be displayed with all other details of quote", quoteName, dataList));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify user is able to "Copy quote" from existing quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301102
	 * @since May 3, 2021 9:09:47 AM
	 * @author  Keshav
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301102_Regression_PS_Quotes_VerifyUserIsAbleToCopyQuoteFromExistingQuote_CSR(){


		// Reporting info
		initializeReporting("Verify user is able to Copy quote from existing quote", "C301102_Regression_PS_Quotes_VerifyUserIsAbleToCopyQuoteFromExistingQuote_CSR", logger);
        
		// Test data
		Map<String, String> regData = getScenarioData("ID04");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
        
		// Get user
		ArrayList<String> users = new ArrayList<String>();
		String partnerAgent = getUser("ID04", PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		users.add(partnerAgent);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser1 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser1);	
		users.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser2 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser2);
		users.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);		
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser1);
		
		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("MDCPID", mdcpid);
		mandatoryData.put("catalogName", catalogName);
		mandatoryData.put("actionOnUsers", actionOnUsers);
		mandatoryData.put("OrgName", orgName);

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);		

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user", "User is Impersonated.", data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition:  Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition:Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList, "Quote Number");

		/** Pre-Condition Ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1: Click on Organization and Catalog dropdown<br>"
				+ "Step 3: Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected view dropdown is displayed<br>Selected catalog should be loaded", data, true));

		AccountSummary accSummary = customerService.navigateToMyAccount("Step 3: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		quoteListing = accSummary.clickOnQuotesUnderMyAccountSection("Step 4: Click on 'Quotes' under Orders and Quotes",
				"Quote list page should display with list of existing quotes with gear icon");
		Assert.assertNotNull(quoteListing);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 5.1 :Search Quote", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 5.2: Click on Gear button", "Clicked on gear button", "Hide quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 5.3: Click on Action gear icon and select 'Hide' option", "Clicked on Hide Option", "Hide quote"));

		Assert.assertTrue(quoteListing.clickOnShowHiddenQuoteToggleVerifyHiddenQuoteIsDisplayed("Step 5.4: Click on show hidden", 
				"Clicked on Show hidden<br>he Quote Should be hidden successfully and Eye icon with stricken off should be displayed beside quote number.", true));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 6.1: Click on Gear button", "Clicked on gear button", "Unhide quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 6.2: Click on Action gear icon and select 'Unhide' option", "Clicked on Unhide Option", "Unhide quote"));

		Assert.assertTrue(quoteListing.verifyEyeImage("Step 6.3: Verify Quote is unhidden", "The Quote Should be Unhidden successfully.", "Quote is unhidden successfully"
				, false, true));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 7.1: Click on Gear button", "Clicked on gear button", "Hide quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 7.2: Select 'Copy Quote' option", "Clicked on Unhide Option", "Copy quote"));

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 8: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("Step 9.1: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully and navigated to Quote detail page.","QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 9.2: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String copiedQuoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");

		quoteListing = accSummary.clickOnQuotesUnderMyAccountSection("Step 10.1: Click on 'Quotes' under Orders and Quotes",
				"Quote list page should display with list of existing quotes with gear icon");
		Assert.assertNotNull(quoteListing);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 10.2 :Verify Copy quote is displayed in quote list page", 
				"Copy of quote created should be displayed in quote listing page", "Quote Number", copiedQuoteNumber, false));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify shipping options and shipping instructions from quote to checkout
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301089
	 * @since May 4, 2021 3:13:41 PM
	 * @author Vishwa A P
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301089_Regression_PS_Quotes_VerifyShippingOptionsAndShippingInstructionsFromQuoteToCheckout_Direct(){

		// Reporting info
		initializeReporting("Verify shipping options and shipping instructions from quote to checkout",
				"C301089_Regression_PS_Quotes_VerifyShippingOptionsAndShippingInstructionsFromQuoteToCheckout_Direct", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String kit = getProduct(USPS, KIT);
		Assert.assertNotNull(kit);
		
		String password = passwords.get(DIRECTUSERPWD);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		
		// Quote data
		Map<String, String> quoteData = new HashMap<String, String>();
		quoteData.put("PaymentOption", "Credit card");
		quoteData.put("ShippingOption", "Two Day");
		quoteData.put("ShippingInstructionText", "Shipping instructions Text is entered");
		quoteData.put("DefaultSelectedValue", "Standard Delivery");

		// Waiting for user availability
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);		

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition : Delete product", "Product should be deleted", "PS", false));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with KIT SKU", "Requested product PDP should load", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter Qty in quantity field", "Quantity should be entered", "42"));

		Assert.assertTrue(pdp.addProductToCart("Step 2.1: Add KIT product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save quotes button", "User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyShippingOptionsDropDownAndDefaultSelectedValue("Step 6: Verify shipping options displayed"
				+ " and default value should be selected", "All shipping options should be displayed and default value should be selected", 
				quoteData.get("DefaultSelectedValue")));

		Assert.assertTrue(createNewQuote.selectShippingOptionDropDown("Step 7: Select shipping option to Two Day",
				"Required shipping option should be selected", quoteData.get("ShippingOption")));

		Assert.assertTrue(createNewQuote.verifyShippingInstructionsOptionWithTextFieldAndEnterMaximumText("Step 8 & 9: Verify Shipping Instructions option with text "
				+ "field and enter maximum text characters",
				"Shipping instructions textbox should be displayed and maximum 40 Text characters should be entered", quoteData.get("ShippingInstructionText")));

		quoteDetails = createNewQuote.createQuote("Step 10: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(createNewQuote.VerifyShippingOptionAndShippingInstructions("Step 11: Select shipping option to Two Day",
				"Required shipping option should be selected", "Two Day", quoteData.get("ShippingInstructionText") ));

		checkout = quoteDetails.navigateToCheckout("Step 12: Navigate to checkout page",
				"User should navigate to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifyShippingOptionsAndInstructionsInCheckoutPage("Step 13: Verify Shipping options and Instructions in checkout page",
				"Updated shipping option and instructions should be displayed accordingly.", quoteData.get("ShippingOption"), quoteData.get("ShippingInstructionText") ));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify user defaults Billing address and Shipping address
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/359545
	 * @since Apr 19, 2021 9:09:47 AM
	 * @author  Vijay
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C359545_Regression_PS_Quotes_VerifyUserDefaulltsBillingAddressAndShippingAddress_Direct(){

		// Reporting info
		initializeReporting("Quote_CBN_Verify user defaults Billing address and Shipping address",
				"C359545_Regression_PS_Quotes_VerifyUserDefaulltsBillingAddressAndShippingAddress_Direct", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability	
		ArrayList<String> userSet= new ArrayList<>();
		String user = getUser(FEDERALCSR);
		Assert.assertNotNull(user);
		userSet.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(ID04, PURCHASER);
	    Assert.assertNotNull(purchaser);	
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		String quoteName = "Test_Automation_Quote_";
		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("poNumber", "12345");
		mandatoryData.put("phoneNumber", "12345");
		mandatoryData.put("attentionText", "test");

		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		CreateAddress createAddress = new CreateAddress(_webcontrols);

		HashMap<String, String> shippingAdress = new HashMap<String, String>();
		shippingAdress.put("Company", "hp2b");
		shippingAdress.put("AttentionText", "test");
		shippingAdress.put("City", "banglor");
		shippingAdress.put("Phone", "1234566");
		shippingAdress.put("Email", purchaser);
		String addressLine = createAddress.systemDate();
		shippingAdress.put("Addressline1", addressLine);

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		/** Pre-Condition ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("Step 2: Search with BTO SKU", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 3: Add BTO product in the Cart", "Product must be added to cart successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		String selectedNewBillingAddress = createNewQuote.clickOnChangeBillingAddressAndSelectNewBillingAddressAndClickOnOk("Step 6 & 7: Click on 'Change Billing Address And select the address that needs to be defaulted and click on ok",
				"Billing address popup should be displayed & Billing Address section is loaded with the selected address", false);
		Assert.assertNotEquals(selectedNewBillingAddress,null);

		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 8: Select 'Default Billing Address' checkbox",
				"Billing address is selected as default", "billing address"));

		String selectedNewShipAddress=createNewQuote.clickOnChangeShippingAddressAndSelectNewShippingAddressAndClickOnOk("Step 9 & 10 : Click on 'Change Shipping Address And select the address that needs to be defaulted and click on ok",
				"Shipping address popup should be displayed & Shipping Address section is loaded with the selected address", false);
		Assert.assertNotEquals(selectedNewShipAddress, null);

		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 11: Select 'Default Shipping Address' checkbox",
				"Shipping address is selected as default", "shipping address"));

		quoteDetails = createNewQuote.createQuote("Step 12: Provide all details and click on save quote",
				"Quote should created successfully", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		pdp = customerService.searchSKU("Step 13: Search with BTO SKU", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 14: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 15:Click on Mini cart and click Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		checkout = shoppingCart.clickOnCheckOut("Step 16: Click on 'Checkout' button", "User should  navigate to Checkout page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(checkout.verifyShppingAndBillingAddressDetails("Step 17: Verify the defaulted Billing Address is shown\r\n"
				+ "Note: Billing address as selected in Step 7", "Default Billing address verified successfully", selectedNewBillingAddress, true));

		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsSelected("Step 18: Verify 'Default Billing Address' checkbox is selected",
				"'Default Billing Address' checkbox is selected", true));

		Assert.assertTrue(checkout.verifyShppingAndBillingAddressDetails("Step 19: Verify the defaulted Shipping Address is shown\r\n"
				+ "Note: Shipping address as selected in Step 9", "Default Shipping address verified successfully", selectedNewShipAddress, false));

		Assert.assertTrue(createNewQuote.verifyDefaultShippingAddressCheckboxIsSelected("Step 20: Verify 'Default Shipping Address' checkbox is selected",
				"'Default Shipping Address' checkbox is selected", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify Ship Consolidated will be defaulted on Create quote page and user cannot change it to Ship Partial when Ship Complete set to Required
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301083
	 * @throws IOException 
	 * @since May 5, 2021 5:30:33 PM
	 * @author Rashi
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.PARTNERAGENT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301083_Regression_PS_Quotes_VerifyShipConsolidatedWillBeDefaultedOnCreateQuotePageAndUserCannotChangeItToShipPartialFwhenShipCompleteSetToRequired_PartnerAgent() throws IOException{

		// Reporting info
		initializeReporting("Verify Ship Consolidated will be defaulted on Create quote page and user cannot change it to Ship Partial when Ship Complete set to Required",
				"C301083_Regression_PS_Quotes_VerifyShipConsolidatedWillBeDefaultedOnCreateQuotePageAndUserCannotChangeItToShipPartialFwhenShipCompleteSetToRequired_PartnerAgent",
				logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String password = passwords.get(DIRECTUSERPWD);	
		
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String softBundle = getProduct(USPS, SOFTBUNDLE);
		Assert.assertNotNull(softBundle);
		
		// Get user
		ArrayList<String> userSet= new ArrayList<>();
		String partnerAgent = getUser("ID04", PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);		
		userSet.add(partnerAgent);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser1 = getUser("ID04", PURCHASER);
		Assert.assertNotNull(purchaser1);
		userSet.add(purchaser1);		
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("emailId", purchaser1);
		data.put("catalogName", catalogName);
		data.put("actionOnUsers", "Buy On Behalf");
		data.put("OrgName", orgName);
		data.put("poNumber", "9999");
		data.put("phoneNumber", "9090909090");
		data.put("attentionText", "test");
		data.put("paymentMethod", "Purchase Order");
		String quoteName = "Aut_Quote_";
		
		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		PLP plp = new PLP(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		/** Pre-Condition Starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login with Partner agent user.", url, partnerAgent,password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition :Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("Delete product", "Product should be deleted", "PS", false));		
		/** Pre-Condition Ends **/

		String category = (plp.selectCategoryInProductsAndServices("Step 1 & 2: Click On Product and Services tab and Click on any Category",
				"Clicked On Services Category ", "Services"));
		Assert.assertNotEquals(category, null);

		Assert.assertTrue(plp.addProductToCart("Step 3 :  Add BTO product in the Cart", "Product must be added to cart successfully","plp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4 : Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5 :In the shopping cart page, Click on save as quote link to navigate to create quote page",
				"User must be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 6.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogisticalPreferenceRadioButtonIsNotEnabled("Step 6.2: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Partial",true));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected("Step 7:Ensure that Ship Consolidated radio button selected by default","Ship Consolidated radio button should be selected as default", "Ship Consolidate"));

		Assert.assertTrue(createNewQuote.verifyLogisticalPreferenceRadioButtonIsNotEnabled("Step 8: Verify that user will not be able to select 'Ship Partial' radio button","User should not be able to select 'Ship Partial' radio button","Ship Partial",true));

		quoteDetails = createNewQuote.createQuote("Step 9: Fill in other details and click on save as quote link",
				"Quote should be created successfully & Quote detail page appears",quoteName,purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 10.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 10.2: Select requested file type as pdf. ", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 10.3: Click on export button to export file.", "PDF should get exported successfully"));

		String	pdfValue = pdfValidations.readPdfFileInDownloads("Getting PDF content", "PDF content should be fetched",quoteName+quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyLogisticalPreferenceInPDF("Step 11: Click to open pdf", "Ship Consolidated value is displayed in shipping options","Ship consolidate",pdfValue));

		checkout = quoteDetails.navigateToCheckoutPage("Step 12: Click on Check out button", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons(
				"Step 13.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "Checkout"));

		Assert.assertTrue(createNewQuote.verifyLogisticalPreferenceRadioButtonIsNotEnabled("Step 13.2: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Partial",true));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 14.1: Enter all the Mandatory fields",
				"Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder(
				"Step 14.2: Click on Create purchase Order button to place Place an Order","User successfully Placed Purchase Order and landed to Order confirmation Page",
				true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify Ship Partial will be defaulted on Create quote page and user can change it to Ship Consolidated for 'Ship Complete NO'
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301082
	 * @throws IOException 
	 * @since May 5, 2021 10:55:08 PM
	 * @author Rashi
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301082_Regression_PS_Quotes_VerifyShipPartialWillBeDefaultedOnCreateQuotePageAndUserCanChangeItToShipConsolidatedForShipCompleteNO_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify Ship Partial will be defaulted on Create quote page and user can change it to Ship Consolidated for 'Ship Complete NO'",
				"C301082_Regression_PS_Quotes_VerifyShipPartialWillBeDefaultedOnCreateQuotePageAndUserCanChangeItToShipConsolidatedForShipCompleteNO_Direct", logger);

		// Test data
		String scenarioID = ID04;
		Map<String, String> regData = getScenarioData(scenarioID);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String kit=getProduct(USPS,BTO,SERVICES);
		Assert.assertNotNull(kit);
		
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability		
		String purchaser = getUser(scenarioID, PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("poNumber", "9999");
		data.put("phoneNumber", "8786868685");
		data.put("attentionText", "test");
		data.put("paymentMethod", "Purchase Order");
		data.put("emailID", purchaser);
		String quoteName = "Aut_Quote_";

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;
		
		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with KIT SKU", "Requested product PDP should load", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 2: Add KIT product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 3: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 4: Click on save quotes button",  "User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 5.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 5.2: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Partial", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected("Step 6:Ensure that Ship Partial radio button selected by default","Ship Partial radio button should be selected as default", "Ship Partial"));

		Assert.assertTrue(createNewQuote.selectShipConsolidateAndShipPartialRadioButton("Step 7: Ensure that Ship Consolidated radio button can be selected", 
				"Ship Consolidated radio button can be selected", "Ship Consolidate"));

		Assert.assertTrue(createNewQuote.selectShipConsolidateAndShipPartialRadioButton("Step 8: Now select Ship partial again", 
				"Ship Partial radio button should be selected", "Ship Partial"));

		quoteDetails = createNewQuote.createQuote("Step 9: Fill in other details and click on Save quote button",
				"Quote should be created successfully & Quote detail page appears", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 10.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 10.2: Select requested file type as pdf.", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 10.3: Click on export button to export file.", "PDF should get exported successfully"));

		String pdfValue = pdfValidations.readPdfFileInDownloads("Getting PDF content", "PDF content should be fetched",quoteName + quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyLogisticalPreferenceInPDF(
				"Step 11: Click to open pdf", "Ship Partial value is displayed in shipping options","Ship Partial",pdfValue));

		checkout = quoteDetails.navigateToCheckoutPage("Step 12: Click on Check out button", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 13.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ship Consolidate option should display with Ship Consolidated – Combine items into a single shipment option", "Ship Consolidate", "Available", "Checkout"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 13.2: Ensure that Shipping options module display Logistic preference", 
				"Shipping options module should display Logistic preference", "Ship Partial", "Available", "Checkout"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected("Step 14:Ensure that Ship Partial radio button selected by default","Ship Partial radio button should be selected as default", "Ship Partial"));

		Assert.assertTrue(createNewQuote.verifyLogisticalPreferenceRadioButtonIsEnabled("Step 15: Ensure that Ship Consolidated radio button can be selected", 
				"Ship Consolidated radio button can be selected", "Ship Consolidated",true));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 16.1: Enter all the Mandatory fields","Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder(
				"Step 16.2: Click on Create purchase Order button to place Place an Order","User successfully Placed Purchase Order and landed to Order confirmation Page",true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_Verify Contract Surcharge in exported quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301095
	 * @since April 23, 2021
	 * @author ShishoSa
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301095_Regression_PS_Quotes_VerifyContractSurchargeInExportedQuote_Direct() {

		//Reporting info
		initializeReporting("Quote_Verify Contract Surcharge in exported quote", "C301095_Regression_PS_Quotes_VerifyContractSurchargeInExportedQuote_Direct", logger);

		//Test Data
		String scenarioID = ID04;
		Map<String, String> regData = getScenarioData(scenarioID);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String kit = getProduct(USPS, KIT, SERVICES);
		Assert.assertNotNull(kit);
		
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability		
		String purchaser = getUser(scenarioID, PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		CSVValidations csv = new CSVValidations(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > User Name > Enter Password > Click on Sign In", url, purchaser, password, true));
		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select Organization & Catalog", "Requested Organization & Catalog should be selected", data, true));
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "PS", false));

		Assert.assertNotNull(login.searchSKU("Step 1: In search box , search for KIT number", "PDP of searched product should be displayed", kit));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter Qty as 50 and update", "Qty should be updated successfully", "50"));

		Assert.assertTrue(login.addProductToCart("Step 3: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 5: Click on 'save as quote' button", "'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.verifyContractSurchargeIsDisplayed("Step 6: Verify the contract surcharge under cart summary section in Create quote page", 
				"The contract surcharge should be displayed in cart summary and should be included in price", true));

		Assert.assertNotNull(createNewQuote.createQuote("Step 7: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", "AutQuote", purchaser));

		Assert.assertTrue(createNewQuote.verifyContractSurchargeIsDisplayed("Step 8: Verify the contract surcharge under cart summary section in quote confirmation page", 
				"The contract surcharge should be displayed in cart summary and should be included in price", true));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotNull(quoteDetailsList);
		String quoteName = "AutQuote" + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");
		List<String> prodDetails = quoteDetails.getProductDetails("Precondition: Get Product details", "Product details should be fetched", true, true);
		Assert.assertNotNull(prodDetails);

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 9.1 : Click on 'Export' button", "Export popup should be displayed"));
		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 9.2: Select requested file type as csv", "Requested file type should be selected", "csv"));
		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 9.3: Click on export button to export file", "CSV should get exported successfully"));

		Assert.assertTrue(csv.verifyQuoteCSV("Step 10-11: Go to the file location and Click on Quote to open<br>"
				+ "Verify below details are displayed in exported quote<br>Product Name, Product Number, MFG#, Description, Qty, Unit Price, Total", 
				"All details should display accordingly", quoteName, prodDetails));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_Verify user is able to export quote for Part Verification File
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300062
	 * @since May 6, 2021 7:53:08 PM
	 * @author Vijay
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300062_Regression_PS_Quotes_VerifyUserIsAbleToExportQuoteForPartVerificationFile_CSR(){

		// Reporting info
		initializeReporting("Quote_Verify user is able to export quote for Part Verification File", "C300062_Regression_PS_Quotes_VerifyUserIsAbleToExportQuoteForPartVerificationFile_CSR", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability	
		ArrayList<String> userSet= new ArrayList<>();
		String user = getUser(FEDERALCSR);
		Assert.assertNotNull(user);
		userSet.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);				
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		String qty = "500";

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));
		/** Pre-Condition starts **/

		pdp = customerService.searchSKU("Step 1 : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 2: Enter Qty as 500 and update", "Qty should be updated successfully", qty, "PDP"));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Click on 'Add to cart' button", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("Step 6: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page","AytomationQuote", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 7: Getting Quote details", "Quote details page should have below details");
		Assert.assertNotEquals(quoteDetailsList, null);
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 8.1: Click on 'Export' option in quote detail page", "Succesfullly Clicked on Export Button"));
		Assert.assertTrue(quoteDetails.verifyExportPopUp("Step 8.2: Verify Overlay appears with details"
				, " Overlay should appears with below details:- 1. Select file type \n 2.CSV \n 3.XLS \n 4.PDF \n 5.Part Varification File"));
		String productPrice = quoteDetails.getProductUnitPrice("Getting the unit price", "Got unit price").split(" ")[1];

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 9.1: Select the export type as Part Verification File format ",
				"Part Verification File format should get Selected", "txt"));
		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 9.2: Click on Export button", "Part Verification File format should get exported successfully"));

		String text = quoteDetails.readTXTFile("Step 10: Go to the file location and click on Quote to open", 
				"Data in text file should fetch successfully", "Quote_Clin_"+quoteNumber);
		Assert.assertNotEquals(quoteDetailsList, null);

		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add(bto);
		arrayList.add(qty);
		arrayList.add(productPrice);

		Assert.assertTrue(quoteDetails.verifyDataInPartVarificationTextFile("Step 11: Verify below details are displayed in the file <br>"
				+ "Part Number <br>"
				+ "Price <br>"
				+ "Quantity ", "Details should get displayed successfully.", text, arrayList));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_Verify Regulatory fee in mailed & exported quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301093
	 * @since May 7, 2020
	 * @author ShishoSa
	 * @throws IOException 
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301093_Regression_PS_Quotes_VerifyRegulatoryFeeInMailedAndExportedQuote_CSR() throws IOException {

		//Reporting info
		initializeReporting("Quote_Verify Regulatory fee in mailed & exported quote", 
				"C301093_Regression_PS_Quotes_VerifyRegulatoryFeeInMailedAndExportedQuote_CSR", logger);

		//Test data
		String scenarioID = ID04;
		Map<String, String> regData = getScenarioData(scenarioID);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String bto = getProduct(USPS, BTO, LAPTOPS);
		Assert.assertNotNull(bto);
		
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		//Waiting for user availability		
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(FEDERALCSR);
		Assert.assertNotNull(csr);
		userSet.add(csr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(scenarioID, PURCHASER);
		Assert.assertNotNull(purchaser);				
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		GmailPage gmailPage = new GmailPage(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		XLSValidations xls = new XLSValidations(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR", url, csr, password, true));
		
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate direct user", "User should be impersonated", data));
		
		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select Organization and Catalog",
				"Requested Organization & Catalog should be selected", data, true));
		
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "PS", false));

		Assert.assertNotNull(login.searchSKU("Step 1: In search box , search for BTO number", "PDP of searched product should be displayed", bto));

		Assert.assertTrue(login.enterFirstProductQuantity("Step 2: Enter Qty as 700 and update", "Qty should be updated successfully", "700", "pdp"));

		Assert.assertTrue(login.addProductToCart("Step 3: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 5: Click on 'save as quote' button", "'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertNotNull(createNewQuote.verifyRegulatoryFeeIsDisplayed("Step 6: Verify Regulatory fee details under cart summary section in Create quote page", 
				"Regulatory should be displayed and it should be included in total", true));

		Assert.assertNotNull(createNewQuote.createQuote("Step 7: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", "AutQuote", csr));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotNull(quoteDetailsList);
		String quoteNum = quoteDetails.getQuoteData(quoteDetailsList, "Quote Number");
		String quoteName = "AutQuote" + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");
		List<String> regulatoryFeeDetails = new ArrayList<>();
		regulatoryFeeDetails.add(quoteName);

		List<String> regulatoryFee =  createNewQuote.verifyRegulatoryFeeIsDisplayed("Step 8: Verify Regulatory fee details under cart summary section in quote confirmation page", 
				"Regulatory should be displayed and it should be included in total", true);
		Assert.assertNotNull(regulatoryFee);
		regulatoryFeeDetails.addAll(regulatoryFee);

		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 9: Click on quotes under Orders and quotes tab in Home Page", 
				"Quotes listing page should be displayed with newly created quote", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 10.1: Click on any 'Quote number'", "Search results must appear", "Quote Number", quoteNum, false));
		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 10.2: Click on any 'Quote number'", 
				"Quote details page should be displayed with below details:<br> Print, Export, Email, Share quote, Order Information, Billing address, Shipping address", quoteNum));

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 11: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 12: Enter valid email in email address field and Select PDF radio button and click on Email button", 
				"Your quote was sent successfully message should be displayed", "PDF", purchaser));

		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 13.1: Login to the emailed account and download quote PDF", 
				"Quote PDF should be fetched from email", quoteName + ".pdf", 10));
		String pdfContent = "";

		pdfContent = pdfValidations.readPdfFileInDownloads("Step 13.2: Getting PDF content", "PDF content should be fetched", quoteName + ".pdf", true);
		Assert.assertNotEquals(pdfContent, "");

		Assert.assertTrue(pdfValidations.verifyRegulatoryFeeInPDF("Step 13.3: Verify regulatory fee is displayed in 'PDF mailed quote'", 
				"Regulatory fee should be displayed and it should be included in total", pdfContent, regulatoryFeeDetails));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 14.1 : Click on 'Export' button", "Export popup should be displayed"));
		
		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 14.2: Select requested file type as xls", "Requested file type should be selected", "xls"));
		
		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 14.3: Click on export button to export file", "XLS should get exported successfully"));

		Assert.assertTrue(xls.verifyXlsFile("Step 15-16: Go to the file location and Click on Quote to open<br>Verify regulatory fee is displayed in exported quote", 
				"Regulatory fee should be displayed and it should be included in total", quoteName, regulatoryFeeDetails));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Quote creation with SPC and export quote for XLS
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301090
	 * @since May 10, 2021 3:36:32 PM
	 * @author Vijay
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301090_Regression_PS_Quote_VerifyQuoteCreationWithSpcAndExportQuoteForXLS_CSR() { 

		// Reporting info
		initializeReporting("Verify Quote creation with SPC and export quote for XLS","C301090_Regression_PS_Quote_VerifyQuoteCreationWithSpcAndExportQuoteForXLS_CSR", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String spc = regData.get("SPC");
		
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability	
		ArrayList<String> userSet= new ArrayList<>();
		String user = getUser(FEDERALCSR);
		Assert.assertNotNull(user);
		userSet.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String requestor = getUser(ID04, REQUESTER);
		Assert.assertNotNull(requestor);				
		userSet.add(requestor);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", requestor);

		String quoteName = "Aut_Quote_";
		String quantity = "100";

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		XLSValidations xls = new XLSValidations(_webcontrols);

		/** Pre-Condition Starts **/
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>", "<b>Creating Quote & Sharing with Another User </b>");

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with CSR user.", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Impersonate purchaser.", "User Should be Impersonated successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog", "Requested org & catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		/** Pre-Condition Ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 2: Enter Qty as " + quantity + " and update",  "Qty should be updated successfully", quantity, "pdp"));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		String subTotalBeforeSPC = createNewQuote.getSubTotal("Step 6.1: Get Strike off price before applying spc", "Subtotal value should be fetched", true, "Quote");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(shoppingCart.enterTextInSpecialPricingCodeTextBox("Step 6.2: Enter valid 'Special pricing code' in cart summary section", 
				" valid 'Special pricing code' in cart summary section should entered", spc, true));

		Assert.assertTrue(shoppingCart.clickOnApplyLink("Step 6.3: Click on apply link", 
				"'Special pricing code' Should be applied successfully and product price should be reduced.", true, true));

		String subtotalAfterSPC = createNewQuote.getSubTotal("Step 6.4: Get Strike off price before applying spc",
				"Subtotal value should be fetched after apply spc", true, "Quote");
		Assert.assertNotEquals(createNewQuote, null);

		if (Double.valueOf(subtotalAfterSPC) < Double.valueOf(subTotalBeforeSPC)) {
			//Assert.assertNotEquals(subtotalBeforeSPC, subtotalAfterSPC);
			Reporting.getLogger().log(LogStatus.PASS, "Step 6.5: Strike off price should be reduced after applying spc", "Strike off price reduced after applying spc");
		}

		Assert.assertTrue(createNewQuote.verifyCartSummarySection("Step 7: Verify Cart summary section for SPC in create quote page",
				"Cart Summary section should display  with all details"));

		quoteDetails = createNewQuote.createQuote("Step 8: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully and navigated to quote confirmation page.",quoteName, requestor);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 9: Verify quote details in quote confirmation page",
				"Quote details page should have below details");
		Assert.assertNotEquals(quoteDetailsList, null);
		
		String quoteNameValue=quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 10.1: Click on 'Export' option in quote detail page", "Succesfullly Clicked on Export Button"));
		
		Assert.assertTrue(quoteDetails.verifyExportPopUp("Step 10.2: Verify Overlay appears with details"
				, " Overlay should appears with below details:- 1. Select file type <br> 2.CSV <br> 3.XLS <br> 4.PDF <br> 5.Part Varification File"));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 11.1: Select the export type as XLS", "File type should be selected", "xls"));
		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 11.2: Click on 'Export' in quote confirmation page", "XLS format should get exported successfully"));

		List<String> headers = Arrays.asList("Title/Name:","Product name", "Product number","Config/Bundle ID","Pricing source", "MFG#:", "Description", "QTY", "Unit price", "Total");

		Assert.assertTrue(xls.verifyXlsFile("Step 12: Go to the file location and click on Quote to open<br>"
				+ "Step 13: Verify quote Title /Name field is displayed <br>"
				+ "Step 14: Verify Product Number field is displayed <br>"
				+ "Step 15:Verify Product Name field is displayed <br>"
				+ "Step 16: Verify Config/Bundle ID field is displayed <br>"
				+ "Step 17: Verify Manufacturer# field is displayed <br>"
				+ "Step 18: Verify Pricing Source field is displayed <br>"
				+ "Step 19: Verify Quantity field is displayed <br>"
				+ "Step 20: Verify Unit Price field is displayed <br>"
				+ "Step 21: Verify Total Price field is displayed <br>"
				+ "Step 22: Verify Estimated tax line item(if enabled) is displaying in Quote export <br>"
				+ "Step 23: Verify regulatory fee line item(if enabled) is displaying in Quote export <br>"
				+ "Step 24: Verify Total line item(if enabled) is displaying in Quote export <br>",
				"All Quote Details should verify in xls ", quoteNameValue, headers));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify Ship Consolidated will be defaulted on Create quote page and user can change it to Ship Partial for 'Ship Complete YES'-PO
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301099
	 * @throws IOException -
	 * @since May 11, 2021 12:42:29 PM
	 * @author Rashi
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301099_Regression_PS_Quotes_VerifyShipConsolidatedWillBeDefaultedOnCreateQuotePageAndUserCanChangeItToShipPartialForShipCompleteYes_CSR() throws IOException{

		// Reporting info
		initializeReporting("Verify Ship Consolidated will be defaulted on Create quote page and user can change it to Ship Partial for 'Ship Complete YES'-PO",
				"C301099_Regression_PS_Quotes_VerifyShipConsolidatedWillBeDefaultedOnCreateQuotePageAndUserCanChangeItToShipPartialForShipCompleteYes_CSR", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String user = getUser(FEDERALCSR);
		Assert.assertNotNull(user);
		userSet.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("attentionText", "test");
		data.put("paymentMethod", "Purchase Order");
		data.put("poNumber", "9999");
		data.put("phoneNumber", "Purchase Order");
		data.put("emailID", purchaser);
		String quoteName = "Aut_Quote_";

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		PLP plp=new PLP(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > Enter CSR User Name > Enter Password > Click on Sign In", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Click Customer Service link> Enter MDCPID and Email Id > Click on Search > Click on Action Gear against search result and Impersonate user",
				"User should be impersonated", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select Organization and Catalog",
				"Requested Organization & Catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		String category = (plp.selectCategoryInProductsAndServices("Step 1 & 2: Click On Product and Services tab and Click on any Category",
				"Clicked On Services Category ", "Services"));
		Assert.assertNotEquals(category, null);

		Assert.assertTrue(plp.addProductToCart("Step 3 :  Add BTO product in the Cart","Product must be added to cart successfully", "plp"));

		shoppingCart = plp.navigateToShoppingCartThroughHeader("Step 4 : Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5 :In the shopping cart page, Click on save as quote link to navigate to create quote page",
				"User must be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 6.1: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ensure that Shipping options module displays below Logistic preference options 'Ship Consolidated'", "Ship Consolidate", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 6.2: Check for Ship consolidate Logistical preference under shipping Option module", 
				"Ensure that Shipping options module displays below Logistic preference options 'Ship Partial'", "Ship Partial", "Available", "CreateQuote"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected("Step 7:Ensure that Ship Consolidated radio button selected by default","Ship Consolidated radio button should be selected as default", "Ship Consolidate"));

		Assert.assertTrue(createNewQuote.verifyLogisticalPreferenceRadioButtonIsEnabled("Step 8: Ensure that Ship Partial radio button can be selected", 
				"Ship Partial radio button can be selected", "Ship Partial",true));

		quoteDetails = createNewQuote.createQuote("Step 9: Fill in other details and click on Save quote button",
				"Quote should be created successfully & Quote detail page appears", quoteName, user);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 10.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 10.2: Select requested file type as pdf.", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 10.3: Click on export button to export file.", "PDF should get exported successfully"));

		String pdfValue = pdfValidations.readPdfFileInDownloads("Getting PDF content", "PDF content should be fetched",quoteName + quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyLogisticalPreferenceInPDF("Step 11: Click to open pdf", "Ship Consolidated value should be displayed","Ship Consolidated ",pdfValue));

		checkout = quoteDetails.navigateToCheckoutPage("Step 12: Click on Check out button in quote detail page", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 13.1: Check for Ship consolidate Logistical preference under shipping Option module for Ship Consolidated ", 
				"Ensure that Shipping options module displays below Logistic preference options'Ship Consolidated'", "Ship Consolidate", "Available", "Checkout"));

		Assert.assertTrue(createNewQuote.verifyAvailabilityOfShipConsolidateAndShipPartialRadioButtons("Step 13.2:Check for Logistical preference under shipping Option module for 'Ship Partial'", 
				"Ensure that Shipping options module displays below Logistic preference options 'Ship Partial'", "Ship Partial", "Available", "Checkout"));

		Assert.assertTrue(createNewQuote.verifyLogiticalPreferenceRadioButtonIsSelected("Step 14:Ensure that Ship Consolidated radio button selected by default","Ship Consolidated radio button should be selected by default", "Ship Consolidate"));

		Assert.assertTrue(createNewQuote.verifyLogisticalPreferenceRadioButtonIsEnabled("Step 15: Ensure that Ship Partial radio button can be selected", 
				"Ship Partial radio button can be selected", "Ship Partial",true));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 16.1: Enter all the Mandatory fields","Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 16.2: Click on Create purchase Order button to place Place an Order","User successfully Placed Purchase Order and landed to Order confirmation Page",true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify search results with different sub options for 'Quote Created On' date range in quote listing page 
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/C301109
	 * @since May 3,2021
	 * @author RamaredU
	 */
	@Test(groups ={ IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301109_Regression_PS_Quotes_VerifySearchResultsWithDifferentSubOptionsForQuoteCreatedOnDateRangeInQuoteListingPage_CSR() {

		initializeReporting("Verify search results with different sub options for 'Quote Created On' date range in quote listing page","C301109_Regression_PS_Quotes_VerifySearchResultsWithDifferentSubOptionsForQuoteCreatedOnDateRangeInQuoteListingPage_CSR",logger);

		// Test data		
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String bto=getProduct(USPS,BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability		
		ArrayList<String> userSet= new ArrayList<>();
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);
		userSet.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser1 = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser1);
		userSet.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser2 = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser2);	
		userSet.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);								

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser1);
		
		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);

		/** Pre-Condition Starts **/

		logger.info("<b>Pre-Condition Starts : Creating Quote & Sharing with Another User </b>");

		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>","<b>Creating Quote & Sharing with Another User </b>");

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Impersonate purchaser.","Impersonated successfully.", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog","Requested org & catalog should be selected", data, true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Navigate to shopping cart page","User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Click on save as quote button","User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("Enter all the mandatory details and click on save a quote","Quote should be created successfully", "AutoQuoteToShare", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Getting Quote details","Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(quoteDetailsList, null);

		String quoteReferenceNumber = quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");
		Assert.assertNotEquals(quoteReferenceNumber, null);

		Assert.assertTrue(quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButtonPS("Share quote with another user", "Quote Should be shared successfully", purchaser2, true));

		data.replace("emailID", purchaser1, purchaser2);

		Assert.assertTrue(quoteDetails.clickOnCustomerServiceLinkAndClickOnOkButtonInsideTerminateSessionPopup("Terminate the current session", "Customer Service Page Should be displayed for New Session ",purchaser2, true));

		//usersAvailability.replace(purchaser1, "Free");
		userSet.remove(purchaser1);
		updateUserStatus(purchaser1, "Free");

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.","Buy on behalf is done successfully.", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog","Requested org & catalog should be selected", data, true));

		logger.info("<b>Pre-Condition Ends : Quote Created & Shared Successfully </b>");
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Ends</b>","<b>Quote Created & Shared Successfully</b>");

		/** Pre-Condition ends **/

		accountSummary = login.navigateToMyAccount("Step 1: Click on 'My Account'","My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		//Assert.assertTrue(accountSummary.navigateToQuotes("Step 2 : Click on Quotes link on Left hand side","Quote listing page should be displayed with list of quotes available."));
		
		Assert.assertTrue(customerService.clickAndNavigateToLinksUnderMyAccount("Step 2: Mouseover on MyAccount and click on Quotes link"
				,"Quote listing page should be displayed with list of quotes available.", "Quotes"));

		Assert.assertTrue(quoteListing.verifyQuoteSearchDropdownPlaceHolderText("Step 3: In Search Criteria Drop down select Created On Date Range option","Created On Date Range option must have been selected.", "Created On"));

		Assert.assertTrue(quoteListing.verifyQuoteCreatedOnDropdownOptions("step 4 : Now ensure that Text box is converted into drop down","Text box is converted into drop down with below data :<br> All Available Quotes, <br>Last 30 days,<br>Last 90 days,<br>Last 12 Months"));

		Assert.assertTrue(quoteListing.filterQuotesByCreatedOn("step 5: Verify All Available Quotes value is selected by default","All Available Quotes value should be displayed by default", "All Available Quotes"));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 6: Verify search result appears with the most recently added Quote on Top-Descending Order","Most recently added quote that is matched with search criteria should display on Top"));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 7: Verify search results include both valid and expired quotes","Search results must display both valid/expired quotes when the quote number contains with the search input"));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 8: Verify search results include both shared and created quotes","Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched",quoteReferenceNumber));

		Assert.assertTrue(quoteListing.filterQuotesByCreatedOn("step 9: Now select Last 30 Days value from drop down","List of quotes should be displayed as per the selected date range.", "30 Days"));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 10: Verify search results include both valid and expired quotes","Search results must display both valid/expired quotes when the quote number contains with the search input"));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 11: Verify search results include both shared and created quotes","Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched",quoteReferenceNumber));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 12: Verify search result appears with the most recently added Quote on Top-Descending Order","Most recently added quote that is matched with search criteria should display on Top"));

		Assert.assertTrue(quoteListing.filterQuotesByCreatedOn("step 13: Now select Last 90 Days value from drop down","List of quotes should be displayed as per the selected date range.", "90 Days"));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 14: Verify search results include both valid and expired quotes","Search results must display both valid/expired quotes when the quote number contains with the search input"));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 15: Verify search results include both shared and created quotes","Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched",quoteReferenceNumber));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 16: Verify search result appears with the most recently added Quote on Top-Descending Order","Most recently added quote that is matched with search criteria should display on Top"));

		Assert.assertTrue(quoteListing.filterQuotesByCreatedOn("step 17: Now select Last 12 months value from drop down","List of quotes should be displayed as per the selected date range.", "12 Months"));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 18: Verify search result appears with the most recently added Quote on Top-Descending Order","Most recently added quote that is matched with search criteria should display on Top"));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 19: Verify search results include both valid and expired quotes","Search results must display both valid/expired quotes when the quote number contains with the search input"));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 20: Verify search results include both shared and created quotes","Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched",quoteReferenceNumber));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify Quote creation with email
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301084
	 * @param region EMEA, APJ, AMS-US, AMS-LA
	 * @since May 13,2021
	 * @author RamaredU
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301084_Regression_PS_Quotes_VerifyQuoteCreationWithEmail_CSR() throws IOException {

		initializeReporting("Verify Quote creation with email","C301084_Regression_PS_Quotes_VerifyQuoteCreationWithEmail_CSR", logger);

		//Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		
		String bto=getProduct(USPS,BTO);
		Assert.assertNotNull(bto);
		String kit=getProduct(USPS,KIT);
		Assert.assertNotNull(kit);
		
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability	
		ArrayList<String> userSet= new ArrayList<>();
		String CSRUser = getUser(FEDERALCSR);
		Assert.assertNotNull(CSRUser);
		userSet.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser1 = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser1);
		userSet.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser2 = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser2);				
		userSet.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser1);
		
		String emailId ="hp2bfeautomation@gmail.com";

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		XLSValidations xlsPage = new XLSValidations(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);
		CSVValidations csvPage = new CSVValidations(_webcontrols);
		GmailPage gmailPage = new GmailPage(_webcontrols);		

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate a purchaser.","Impersonate is done successfully.", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Precondition: Select requested org & catalog","Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product","Product should be deleted","PS", false));

		Assert.assertNotNull(login.searchSKU("Step 1: In search box , search for BTO sku","PDP of searched product should be displayed", bto));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter Qty as 500 and update","Qty should be updated successfully", "500"));

		Assert.assertTrue(login.addProductToCart("Step 3: Click on 'Add to cart' button","Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.searchSKU("Step 4: In search box , search for KIT sku","PDP of searched product should be displayed", kit));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 5: Enter Qty as 20 and update","Qty should be updated successfully", "20"));

		Assert.assertTrue(login.addProductToCart("Step 6: Click on 'Add to cart' button","Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("Step 7: Click on 'Mini cart' icon and Click on 'Go to cart' button","Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 8: Click on 'save as quote' button","'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertNotNull(createNewQuote.createQuote("Step 9: Enter all mandatory details and click on Save quote button","Quote should be created successfully and navigated to quote confirmation page", "AutQuote",purchaser1));
		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Getting Quote details","Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(quoteDetailsList, null);
		String quoteName = "AutQuote" + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");
		List<String> quoteDetailsInEmail = new ArrayList<>();
		quoteDetailsInEmail.add(bto);
		quoteDetailsInEmail.add(kit);

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 10: Click on 'Email' option in quote detail page","Pop up with following options should be displayed:\r\n" + "Enter email address text field\r\n"+ "Select File type:\r\n" + "CSV Radio button\r\n" + "XLS Radio button\r\n"+ "PDF Radio button\r\n" + "Email and Cancel buttons"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 11 : Enter valid email address and select CSV radio button option and click on Email button","Your quote was sent successfully message should be displayed", "csv", emailId));

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 12.1: Click on 'Email' option in quote detail page","Pop up with following options should be displayed"));
		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 12.2 : Enter valid email address and select xls radio button option and click on Email button","Your quote was sent successfully message should be displayed", "xls", emailId));
		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 12.3: Login to the emailed account and download quote xls","Quote XLS should be fetched from email", quoteName + ".xls", 10));
		Assert.assertTrue(xlsPage.verifyXlsFile("Step 12.4: Check emails for XLS ","quote details should be displayed in xls file", quoteName, quoteDetailsInEmail));	

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 13.1: Click on 'Email' option in quote detail page","Pop up with following options should be displayed"));
		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 13.2 : Enter valid email address and select PDF radio button option and click on Email button","Your quote was sent successfully message should be displayed", "pdf", emailId));

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 14.1: Click on 'Email' option in quote detail page","Pop up with following options should be displayed"));
		Assert.assertTrue(quoteDetails.clickOnCancelButtonAndVerifyEmailPopup("Step 14.2: Click on Cancel button in Email popup", "Email popup should be closed."));

		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 15.1: Login to the emailed account and download quote CSV","Quote CSV should be fetched from email", quoteName + ".csv", 10));
		Assert.assertTrue(csvPage.verifyCSV("Step 15.2 : Check emails for CSV<br>" + "Verify CSV file contains quote details<br>","All details should display accordingly", quoteName, quoteDetailsInEmail, true));
		quoteDetailsInEmail.add(quoteName);
		quoteDetailsInEmail.add("Warranty");
		quoteDetailsInEmail.add("year limited warranty");

		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 15.3: Check email for PDF format","Quote PDF should be fetched from email", quoteName + ".pdf", 5));
		String pdfContent = "";
		pdfContent = pdfPage.readPdfFileInDownloads("Step 15.4: Getting PDF content", "PDF content should be fetched",quoteName + ".pdf", true);
		Assert.assertNotEquals(pdfContent, "");

		Assert.assertTrue(pdfPage.verifyBtoSpecsInPDF("Step 15.5: Verify BTO spec's on Quote email notification page.","BTO Spec's should be displayed on Quote email notifications page", pdfContent,quoteDetailsInEmail));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Quote_Verify Quote creation with SPC and export quote for PDF
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301091
	 * @since May 10, 2021 3:36:32 PM
	 * @author Vijay
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301091_Regression_PS_Quotes_VerifyQuoteCreationWithSpcAndExportQuoteForPDF_OnFly() throws IOException  {

		// Reporting info
		initializeReporting("Verify Quote creation with SPC and export quote for PDF", "C301091_Regression_PS_Quotes_VerifyQuoteCreationWithSpcAndExportQuoteForPDF_OnFly",logger);

		// Test data
		String scenarioId = ID04;
		Map<String, String> regData = getScenarioData(scenarioId);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String spc = regData.get("SPC");
		
		String cto = getProduct(USPS, CTO);
		Assert.assertNotNull(cto);
		
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability
		String purchaser = getUser(scenarioId, PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		String quoteName = "Aut_Quote_";
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add(cto);
		
		String qty = "200";

		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);

		/** Pre-Condition Starts **/
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>", "<b>Creating Quote & Sharing with Another User </b>");

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with CSR user.", url, purchaser, password, true));

		Assert.assertTrue(login.clickOnVDropDownAndVerifyingOrganizationAndCatalogDropdownAvailibility("PreCondition: Click on the 'v' option beside the organization name from the header in the home page", "'Select your view' overlay should be displayed with organization and catalog drop downs", false));

		Assert.assertTrue(login.selectContractName("PreCondition: Select requested org & catalog",
				"Requested org & catalog should be selected", data.get("catalogName"),true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

		/** Pre-Condition Ends **/

		pdp = customerService.searchSKU("Step 1: Search with CTO SKU", "Requested product PDP should load",cto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 2: Enter Qty as " + qty + " and update", "Qty should be updated successfully", qty, "pdp"));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Add CTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart and click Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		ArrayList<String> subTotalBeforeAndAfterApplyingSPC = shoppingCart.enterValidSPCAndClickOnApplyAndVerifySubtotalAndStrikedOutPrice("Step 5: Enter valid 'Special pricing code' and click on Apply", 
				"Special pricing code' Should be applied successfully and product price should be reduced.", spc, "Shopping cart");
		Assert.assertNotEquals(subTotalBeforeAndAfterApplyingSPC.get(0), subTotalBeforeAndAfterApplyingSPC.get(1));

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 6: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		LinkedHashMap<String, ArrayList<String>> productSkuAndpriceDetailsAfterSpc = createNewQuote
				.getReducedPriceStrikedPriceAndExpDateAfterSpc("Precondition: ",
						"Product reduced price, Striked price and Expiry"+ " date after applying SPC should displays", arrayList);
		Assert.assertNotEquals(productSkuAndpriceDetailsAfterSpc,null);

		Assert.assertTrue(createNewQuote.verifyCartSummarySection("Step 7: Verify Cart summary section for SPC in create quote page",
				"Cart Summary section should display with all details"));

		quoteDetails = createNewQuote.createQuote("Step 8: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully and navigated to quote confirmation page.",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Get Estimated Tax Value", "Estimated tax price is fetched", true);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 9: Verify quote details in quote confirmation page",
				"Quote details page should have all details");
		Assert.assertNotEquals(quoteDetailsList, null);

		String quoteNameValue=quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, "");
		String espot = quoteDetails.getESPOTInQuoteDetails("Getting Espot");

		Assert.assertTrue(login.clickOnHomeTab("Step 10.1: Go to Home page", "Home page should be displayed", true));

		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 10.2: Click on quotes under Orders and Quotes tab in Home Page", 
				"Quote listing page should be displayed with list of quotes created", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 10.3: Search nwly created quote with qote name","Shoukd be fetch quote", "Quote Name", quoteNameValue, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 11: Click on  newly created 'Serached Quote number'", "Quote details page should be displayed with below details : <br>"
				+ "Print <br>"
				+ "Export <br>"
				+ "Email <br>"
				+ "Share quote <br>"
				+ "Order Information <br>"
				+ "Billing address <br>"
				+ "Shipping address", quoteNumber));

		LinkedHashMap<String, String> quoteInfoDetailsForPdf = quoteDetails.getInformationDetails(
				"Pre Condition: Fetch Information & Details from Quote Details Page", "All details Should be fetched Successfully", "pdf", purchaser);
		Assert.assertNotNull(quoteInfoDetailsForPdf);

		LinkedHashMap<String, String> quoteBillingInfoDetailsForPdf = quoteDetails.getBillingInformationDetails(
				"Pre Condition: Fetch Billing Information from Quote Details Page.", "All details Should be fetched Successfully", "pdf");
		Assert.assertNotNull(quoteBillingInfoDetailsForPdf);

		LinkedHashMap<String, String> quoteShippingInfoDetailsForPdf = quoteDetails.getShippingInformationDetails(
				"Pre Condition: Fetch Shipping Information from Quote Details Page.", "All details Should be fetched Successfully", "pdf");
		Assert.assertNotNull(quoteShippingInfoDetailsForPdf);

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 12.1: Click on 'Export' option in quote detail page", "Succesfullly Clicked on Export Button"));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 12.2: Select the export type as PDF", "File type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 12.3: Click on 'Export' in quote confirmation page", "PDF format should get exported successfully"));

		String pdfValue  = pdfValidations.readPdfFileInDownloads("Step 13: Go to the file location and click on Quote to open",
				"Quote should get displayed successfully", quoteNameValue + ".pdf",true);
		Assert.assertNotEquals(pdfValue, null);

		ArrayList<String> pdfHeaderDetails = new ArrayList<String>();
		pdfHeaderDetails.add(quoteNameValue);
		pdfHeaderDetails.add(quoteNumber);
		
		Assert.assertTrue(pdfValidations.verifyHPPropreiteryDetailsInPDF("Step 14.1: Verify following are displayed <br>"
				+ "HP Logo <br>"
				+ "Quote Name <br>"
				+ "Quote number at top right side corner <br>"
				+ "HP Proprietary Information for customer use only <br>"
				+ "Do not share", "All the details should get displayed successfully.",pdfValue,pdfHeaderDetails));
		Assert.assertTrue(pdfValidations.verifyESpotDetailsInPDF("Step 14.2: Verify All displyed ESPOT Details", "ESPOT should be displayed in pdf", pdfValue, espot));

		Assert.assertTrue(pdfValidations.verifyInformationAndDetails(
				"Step 15: Verify the following details and headers are displaying under Information and Details.",
				"All the details should get displayed successfully", pdfValue, quoteInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyBillingAddressDetails(
				"Step 16: Verify the following details and headers are displaying under Billing Information.",
				"All the details should get displayed successfully", pdfValue, quoteBillingInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyShippingAddressDetails(
				"Step 17: Verify the following details and headers are displaying under Shipping Information.",
				"All the details should get displayed successfully", pdfValue, "", quoteShippingInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyComments("Step 18: Verify Comments.", "Comments should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyInvoiceInstructions("Step 19: Verify the Invoice Instructions.",
				"Invoice Instructions should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyShippingInstructions("Step 20: Verify the Shipping Instructions.",
				"Shipping Instructions should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyQuoteSummaryDetails(
				"Step 21: Verify Quote summary Header is displaying on Quote summary <br>"
						+ "Step 22: Verify Product Description Header is displaying on Quote summary <br>"
						+ "Step 23: Verify Manufacturer# Header is displaying on Quote summary <br>"
						+ "Step 24: Verify Pricing Source header is displaying on quote summary <br>"
						+ "Step 25: Verify Quantity Header is displaying on Quote summary <br>"
						+ "Step 26: Verify Unit Price Header is displaying on quote summary <br>"
						+ "Step 27: Verify Total Price Header is displaying on quote summary <br>"
						,"All the details should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyStrikeOutPriceAndExpiryDate(
				"Step 28: Verify Strike out price "
						+ "Step 29: Verify expiry date is displaying after applying spc.",
						"Strike out price and expiry date should get displayed successfully", pdfValue,
						productSkuAndpriceDetailsAfterSpc));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxInPDF("Step 34: Verify Estimated tax line item(if enabled) is displaying in Quote export",
				"Estimated tax line item should get displayed in Quote export successfully",estimatedTax,pdfValue));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
	/**
	 * Quote_Verify Standard delivery option in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/462889
	 * @since Mar 22, 2022
	 * @author Manjunath
	 * @throws IOException 
	 */ 
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS,IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C462889_Regression_PS_Quotes_VerifyStandardDeliveryOptionInQuoteCreationAndQuoteConfirmationPage_FederalCSR() throws IOException{
		
		// Reporting info
		initializeReporting("Quote_Verify Standard deilvery option in Quote creation and Quote confirmation page",
				"C462889_Regression_PS_Quotes_VerifyStandardDeliveryOptionInQuoteCreationAndQuoteConfirmationPage_FederalCSR", logger);
		
		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);	
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String bto =getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		
		// Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String fedCsr = getUser(FEDERALCSR);
		Assert.assertNotNull(fedCsr);
		userSet.add(fedCsr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);
		
		String emailId = purchaser;
		String shippingMethod =  "Standard Delivery";
		String quoteName = "autQuote";
		
		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		Checkout checkoutPage = new Checkout(_webcontrols);
		OrderConfirmation orderConfirmation = new OrderConfirmation(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Federal CSR user", url, fedCsr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "User should be impersonated", data));
		
		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
				+ "The HP2B home page is refreshed with the selected org and contract.", data, true));
		
		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));
		/** Pre-Condition ends **/
		
		pdp = customerService.searchSKU("Step 3: In search box search for BTO ", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);
		
		Assert.assertTrue(pdp.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 5: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);
		
		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 6: Click on 'save as quote' button", 
				"Quote creation page should be displayed"));
		
		Assert.assertTrue(checkoutPage.verifyAllValuesInShippingOptionsDropdown("Step 7: Verify below Shipping Option on Create new quote page,<br>1.Standard Delivery<br>" + 
				"2.Two Day<br>" + "3.Next Day,A.M<br>" + "4.Next Day,P.M.","Validate all the shipping options should be displayed.", shippingOptions));
		
		Assert.assertTrue(checkoutPage.verifySelectedValueInShippingOptionDropdown("Step 8: Verify the Standard Delivery option.", "Standard Delivery option should be selected as default.",
				"Standard Delivery"));
		
		Assert.assertTrue(checkoutPage.verifyRequestedDeliveryDateCalendar("Step 9: Verify Requested delivery date with MM/DD/YY", "Calendar pop up should be displayed with current month"));
		
		String reqDeliveryDate = createNewQuote.selectRequestedDeliveryDate("Step 10: Select any future date", "The selected date should be displayed on Requested delivery date", 5, true);
		
		quoteDetails = createNewQuote.createQuote("Step 11: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", quoteName, emailId);
		Assert.assertNotNull(quoteDetails);
	
		Assert.assertTrue(orderConfirmation.verifyShippingMethod("Step 12.1: Verify the Shipping delivery type on Quote Confirmation page.", 
				"Shipping delivery type should be displayed as Standard  as per the created quote.", shippingMethod));
		
		Assert.assertTrue(quoteDetails.verifyRequestedDeliveryDateOnConfirmationPage("Step 12.2:  Requested delivery date on Quote Confirmation page.", 
				"Requested delivery date as per the created quote.",reqDeliveryDate));
		
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
	
	/**
	 * Quote_Verify next day delivery option in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/462891
	 * @since Mar 23, 2022
	 * @author Manjunath
	 * @throws IOException 
	 */ 
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS,IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C462891_Regression_PS_Quotes_VerifyNextDayDeliveryOptionInQuoteCreationAndQuoteConfirmationPage_FederalCSR() throws IOException{
		
		// Reporting info
		initializeReporting("Quote_Verify next day delivery option in Quote creation and Quote confirmation page",
				"C462891_Regression_PS_Quotes_VerifyNextDayDeliveryOptionInQuoteCreationAndQuoteConfirmationPage_FederalCSR", logger);
		
		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);	
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String kit =getProduct(USPS, KIT);
		Assert.assertNotNull(kit);
		
		// Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String fedCsr = getUser(FEDERALCSR);
		Assert.assertNotNull(fedCsr);
		userSet.add(fedCsr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);
		
		String emailId = purchaser;
		String shippingMethod = "Next Day, A.M.";
		String quoteName = "autQuote";
		
		//Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		Checkout checkoutPage = new Checkout(_webcontrols);
		OrderConfirmation orderConfirmation = new OrderConfirmation(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Federal CSR user", url, fedCsr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "User should be impersonated", data));
		
		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
				+ "The HP2B home page is refreshed with the selected org and contract.", data, true));
		
		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));
		/** Pre-Condition ends **/
		
		pdp = customerService.searchSKU("Step 3: In search box search for KIT ", "PDP of searched product should be displayed", kit);
		Assert.assertNotEquals(pdp, null);
		
		Assert.assertTrue(pdp.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 5: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);
		
		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 6: Click on 'save as quote' button", "Quote creation page should be displayed"));
		
		Assert.assertTrue(checkoutPage.verifyAllValuesInShippingOptionsDropdown("Step 7: Verify below Shipping Option on Create new quote page,<br>1.Standard Delivery<br>" + 
				"2.Two Day<br>" + "3.Next Day,A.M<br>" + "4.Next Day,P.M.","Validate all the shipping options should be displayed.", shippingOptions));
		
		Assert.assertTrue(checkoutPage.selectValueInShippingOptionDropdown("Step 8: Select the Next Day,A.M Delivery option", "Selected Delivery option should be displayed.", shippingMethod));
		
		Assert.assertTrue(checkoutPage.verifyRequestedDeliveryDateCalendar("Step 9: Verify Requested delivery date with MM/DD/YY", "Calendar pop up should be displayed with current month"));
		
		String reqDeliveryDate = createNewQuote.selectRequestedDeliveryDate("Step 10: Select any future date", "The selected date should be displayed on Requested delivery date", 5, true);
		
		quoteDetails = createNewQuote.createQuote("Step 11: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", quoteName, emailId);
		Assert.assertNotNull(quoteDetails);
	
		Assert.assertTrue(orderConfirmation.verifyShippingMethod("Step 12.1: Verify the Shipping delivery type on Quote Confirmation page.", 
				"Shipping delivery type should be displayed as per the created quote.", shippingMethod));
		
		Assert.assertTrue(quoteDetails.verifyRequestedDeliveryDateOnConfirmationPage("Step 12.2:  Requested delivery date on Quote Confirmation page.", 
				"Requested delivery date as per the created quote.", reqDeliveryDate));
		
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
	
	/**
	 * Quote_Verify regulatory fee in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/458187
	 * @since Mar 23,2022
	 * @author Manjunath
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C458187_Regression_PS_Quotes_VerifyRegulatoryFeeInQuoteCreationAndQuoteConfirmationPage_FederalCSR() throws IOException {

		initializeReporting("Quote_Verify regulatory fee in Quote creation and Quote confirmation page", 
				"C458187_Regression_PS_Quotes_VerifyRegulatoryFeeInQuoteCreationAndQuoteConfirmationPage_FederalCSR", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);	
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String kit = getProduct(USPS, KIT);
		Assert.assertNotNull(kit);
		
		// Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String fedCsr = getUser(FEDERALCSR);
		Assert.assertNotNull(fedCsr);
		userSet.add(fedCsr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);
	
		String quoteName = "autQuote";
		String paymentMethod = "Purchase Order";
		String emailId = purchaser;

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Federal CSR user", url, fedCsr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate a purchaser.", "Impersonate is done successfully.", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.",
				"The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
						+ "The HP2B home page is refreshed with the selected org and contract.", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product","Product should be deleted","PS", false));

		/** Pre-Condition ends **/
		
		Assert.assertNotNull(login.searchSKU("Step 3: In search box , search for BTO sku", "PDP of searched product should be displayed", bto));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 4: Enter Qty as 500 and update", "Qty should be updated successfully", "500"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.searchSKU("Step 6: In search box , search for KIT sku", "PDP of searched product should be displayed", kit));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 7: Enter Qty as 20 and update", "Qty should be updated successfully", "20"));

		Assert.assertTrue(pdp.addProductToCart("Step 8: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Step 9: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 10: Click on 'save as quote' button", 
				"'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 11: Select Payment method as 'Purchase Order'", "Payment method is selected", paymentMethod));

		Assert.assertNotNull(createNewQuote.verifyRegulatoryFeeIsDisplayed("Step 12: Verify Regulatory fee details under cart summary section in Create quote page", 
				"Regulatory fee should be displayed and it should be included in total", true));

		quoteDetails = createNewQuote.createQuote("Step 13: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", quoteName, emailId);
		Assert.assertNotNull(quoteDetails);

		Assert.assertNotNull(quoteDetails.verifyRegulatoryFeeIsDisplayed("Step 14: Verify regulatory fee details in quote confirmation page", 
				"Regulatory fee should be displayed and should be include in the total as per the created quote.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Quote_Verify contract surcharge in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/458188
	 * @since Mar 23,2022
	 * @author Manjunath
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.DIRECT, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C458188_Regression_PS_Quotes_VerifyContractSurchargeInQuoteCreationAndQuoteConfirmationPage_Direct() throws IOException {

		initializeReporting("Quote_Verify contract surcharge in Quote creation and Quote confirmation page", 
				"C458188_Regression_PS_Quotes_VerifyContractSurchargeInQuoteCreationAndQuoteConfirmationPage_Direct", logger);
		
		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String password = passwords.get(DIRECTUSERPWD);	
		
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		String kit =getProduct(USPS, KIT);
		Assert.assertNotNull(kit);

		// Waiting for user availability
		String purchaser =  getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("emailID", purchaser);

		String quoteName = "autQuote";
		String emailId = purchaser;
		String qty = "200";

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		//Pre-condition Starts
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Purchaser user", url, purchaser, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
				+ "The HP2B home page is refreshed with the selected org and contract.", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product","Product should be deleted","PS", false));

		//Pre-condition ends
		
		Assert.assertNotNull(login.searchSKU("Step 3: In search box , search for BTO sku", "PDP of searched product should be displayed", bto));

		Assert.assertTrue(pdp.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.searchSKU("Step 5: In search box , search for KIT number", "PDP of searched product should be displayed", kit));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 6: Enter Qty as 200 and update", "Qty updated successfully", qty));

		Assert.assertTrue(pdp.addProductToCart("Step 7: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Step 8: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 9: Click on 'save as quote' button", 
				"'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.verifyContractSurchargeIsDisplayed("Step 10: Verify the contract surcharge under cart summary section in Create quote page",
				"The contract surcharge should be displayed in cart summary and should be included in total", true));
		
		Assert.assertNotNull(createNewQuote.createQuote("Step 11: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigate to quote confirmation page", quoteName, emailId));

		Assert.assertTrue(quoteDetails.verifyContractSurchargeIsDisplayed("Step 12: Verify the contract surcharge under cart summary section in quote confirmation page",
				"The contract surcharge should be displayed in cart summary and should be included in total as per the created Quote.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Quote_Verify user is able to create quote with attachments and check quote email for attachment
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/462886
	 * @since Mar 24, 2022
	 * @author Manjunath
	 */
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C462886_Regression_PS_Quotes_VerifyUserIsAbleToCreateQuoteWithAttachmentsAndCheckQuoteEmailForAttachment_FederalCSR() {

		// Reporting info
		initializeReporting("Quote_Verify user is able to create quote with attachments and check quote email for attachment", 
				"C462886_Regression_PS_Quotes_VerifyUserIsAbleToCreateQuoteWithAttachmentsAndCheckQuoteEmailForAttachment_FederalCSR", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
        String catalogName = regData.get("Contract");
        String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
        String password = passwords.get(CSRORFEDCSRUSERPWD);
        	
        // Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String fedCsr = getUser(FEDERALCSR);
		Assert.assertNotNull(fedCsr);
		userSet.add(fedCsr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
			
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);
		
		String attachementName = "note1.txt";
		String quoteName = "autQuote";
		//String paymentMethod = "Purchase Order";
		String emailId = purchaser;
		

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		OrderConfirmation orderConfirmation = new OrderConfirmation(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		GmailPage gmailPage = new GmailPage(_webcontrols);				
		
		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Federal CSR user", url, fedCsr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "User should be impersonated", data));
		
		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
				+ "The HP2B home page is refreshed with the selected org and contract.", data, true));
		
		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));
		/** Pre-Condition ends **/
		
		pdp = customerService.searchSKU("Step 3: In search box search for BTO ", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);
		
		Assert.assertTrue(pdp.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 5: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);
		
		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 6: Click on 'save as quote' button", 
				"'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.selectSendEmailNotificationCheckbox("Step 7: Check the checkbox Yes, Send this quote via email checkbox and enter any valid email", 
				"Check box should be checked and email is specified",true));
		
		Assert.assertTrue(checkout.clickOnAddAttachmentsLinkAndUploadFile(
				"Step 8: Check attachments links is available below Purchaser contact information <br>" + 
				"Step 9: Click on Attachments link <br>Step 10: Goto appropriate file and select the file <br>" + 
				"Step 11: Click on Open", "Attachments links should be available <br>"
						+ "Windows page should be opened to select the file <br>"
						+ "The file should be selected <br>The file should be attached", attachementName));
		
		quoteDetails = createNewQuote.createQuote("Step 12: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", quoteName, emailId);
		Assert.assertNotNull(quoteDetails);
		
		Assert.assertTrue(orderConfirmation.verifyAddedAttachmentFileIsDisplayedOnOrderConfirmationPage("Step 13: Verify attachment on quote confirmation page",
				"The attachment should be available on quote confirmation page", attachementName));
		
		Assert.assertTrue(gmailPage.verifyAttachmentInGmail("Step 14: Verify quote email for attachment", "The attachment should be available on quote email",
			"HP2B Purchase Quote", attachementName, 10));
				
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}


}