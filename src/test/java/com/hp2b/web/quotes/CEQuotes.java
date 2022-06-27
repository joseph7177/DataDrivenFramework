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
import com.hp2b.common.HP2BStaticData;
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
import com.hp2b.web.pom.PurchaseRequestsToApprove;
import com.hp2b.web.pom.QuoteDetails;
import com.hp2b.web.pom.QuoteListing;
import com.hp2b.web.pom.ShoppingCart;
import com.hp2b.web.pom.gmail.GmailPage;
import com.hp2b.xls.XLSValidations;
import com.hpicorp.hpframework.reporting.Reporting;
import com.relevantcodes.extentreports.LogStatus;

public class CEQuotes extends FrameworkMethods {

	Logger logger = Logger.getLogger(CEQuotes.class);
	private String url = "";
	private static final String module = "Quotes";
	private static final String storeCE = IGroupsTagging.IStoreType.CE;
	private static String serviceDescription = "Default;Inside / desk delivery;Consolidated delivery;HP std: mixed Euro and/or Industry pallet;No appointment required;Any working day at any time";

	private static final List<String> shippingOptions = Arrays.asList("Standard Delivery","Two Day","Next Day, A.M.","Next Day, P.M.");
	
	public void setEnvironment() {
		if (url.isEmpty()) {			
			url = _webcontrols.get().propFileHandler().readProperty("config", ENVIRONMENT);
		}
	}

	/**
	 * Verify user is able to Checkout from existing quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300033
	 * @param region APJ,AMS-LA,EMEA,AMS-US
	 * @since Apr 26, 2021 7:53:08 PM
	 * @author Keshav
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300033_Regression_CE_Quotes_VerifyUserIsAbleToShareExistingQuoteFromMyAccountsQuotesPage_CSR(String region){

		// Reporting info
		initializeReporting("Verify user is able to share existing quote from My Accounts->Quotes page",
				"C300033_Regression_CE_Quotes_VerifyUserIsAbleToShareExistingQuoteFromMyAccountsQuotesPage_CSR",region, logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01","ID03","ID02","ID05");
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
	    String password = passwords.get(CSRORFEDCSRUSERPWD);        
		String mdcpId = regData.get("MDCP ID");

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpId);
		data.put("actionOnUsers", actionOnUsers);
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);		
		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser1);
		String user = getUser(CSR);
		Assert.assertNotNull(user);	
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		users.add(purchaser1);
		users.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		data.put("emailID", purchaser);
		System.out.println(purchaser);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

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

		/** Pre-Condition Ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1,2: Select requested catalog", "Requested catalog should be selected", data,true));

		AccountSummary accSummary = customerService.navigateToMyAccount("Step 3: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);
		
		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount("Step 4: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 5.1 :Click on quote which is created by Partner agent", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 5.2: Click on Action gear icon","Clicked on Share quote", "Share quote"));


		Assert.assertTrue(quoteListing.clickOnActions("Step 5.3: Select 'Share quote' option", "Share quote pop up with login id text field and with Cancel & Share quote buttons should be displayed",
				"Share quote"));

		Assert.assertTrue(quoteListing.enterEmailidAndVerifySharequoteMessage("Step 6: Enter valid login details in 'Login id' field and click on 'Share quote' button",
				"\"Quote is now available to : login Id\" message should be displayed and quote is shared successfully", purchaser1));

		Assert.assertTrue(login.clickOnCustomerServiceLink("Step 7.1: Click on customer service link in home page"
				, "Clicked on Customer Service link."));

		//usersAvailability.replace(purchaser, "Free");
		updateUserStatus(purchaser, "Free");
		userSet.remove(purchaser);
		data.put("emailID", purchaser1);
		
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Step 7.2: Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 7.3: Select requested catalog", "Requested catalog should be selected", data,true));

		accSummary = customerService.navigateToMyAccount("Step 8.1: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount("Step 8.2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quote list page should display with list of existing quotes with gear icon", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 8.3: Search shared quote", "Shared Quote details should display",
				"Quote Number", quoteNumber, false));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_Verify search results for 'Quote Name' in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300047
	 * @param region APJ,EMEA,AMS-US,AMS-LA
	 * @since April 23, 2021
	 * @author ShishoSa
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300047_Regression_CE_Quotes_VerifySearchResultsForQuoteNameInQuoteListingPage_Direct(String region) {

		//Reporting info
		initializeReporting("Quote_Verify search results for 'Quote Name' in quote listing page",
				"C300047_Regression_CE_Quotes_VerifySearchResultsForQuoteNameInQuoteListingPage_Direct", region, logger);

		//Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, ACCESSORIES).get(0);
		String password = passwords.get(DIRECTUSERPWD);

		//Waiting for user availability
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		String invalidQuote = "Quote768#8263589279";
		String dropdownValue = "Quote Name";
		String quoteName2 = "Quote";

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Direct user", url, purchaser, password, true));
		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select Organization & Catalog", "Requested Organization & Catalog should be selected", data, true));
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", storeCE, false));
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
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300537
	 * @param region APJ,EMEA,AMS-US,AMS-LA
	 * @since Apr 27, 2020
	 * @author ShishoSa
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class,groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300537_Regression_CE_Quotes_VerifyExpiredQuote_CSR(String region) {

		//Reporting info
		initializeReporting("Quote_Verify expired quote", "C300537_Regression_CE_Quotes_VerifyExpiredQuote_CSR", region, logger);

		//Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = commonData.get(CSRORFEDCSRUSERPWD);

		//Waiting for user availability
		String csr = getUser(CSR);
		Assert.assertNotEquals(csr, "");
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		ArrayList<String> userSet = new ArrayList<>();
		userSet.add(csr);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);		

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);
		String[] actionOptions = {"View quote details", "Copy quote", "Add items to current cart"};

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR", url, csr, password, true));
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

		//		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyBtoSpecsAreDisplayed(
		//				"Step 6: Click on any quote which is expired", "BTO spec's should be displayed on quote details page"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify user is able to "Checkout" from existing quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300043
	 * @param region APJ,EMEA,AMS-US,AMS-LA
	 * @since Apr 26, 2020
	 * @author Manpreet
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class,groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300043_Regression_CE_Quotes_VerifyUserIsAbleToCheckoutFromExistingQuote_CSR(String region) {

		// Reporting info
		initializeReporting("Verify user is able to Checkout from existing quote",
				"C300043_Regression_CE_Quotes_VerifyUserIsAbleToCheckoutFromExistingQuote_CSR", region, logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String poNumber = regData.get("Orders");
		String phoneNumber = "12345";
		String attentionText = "test";
		String PO = "Purchase Order";

		//String bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)),"BTO","Accessories").get(0);
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);		
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		String CSRUser = getUser(CSR);
		Assert.assertNotNull(CSRUser);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

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

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		/** Pre-Condition ends **/

		accountSummary = login.navigateToMyAccount("Step 1: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnViewAllQuotesUnderMyAccountSection("Step 2: Click on View All Quotes link", "List of existing quotes with gear icon should be displayed");
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
	 * Verify Hide and unhide for quote on quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/367499
	 * @since Apr 19, 2021 9:09:47 AM
	 * @author  Vijay
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES },priority = 2)
	public void C367499_Regression_CE_Quotes_VerifyHideAndUnhideForQuoteOnQuoteListingPage_Direct(String region) {

		//Reporting info
		initializeReporting("Quote_Verify search results for 'Quote Name' in quote listing page",
				"C367499_Regression_CE_Quotes_VerifyHideAndUnhideForQuoteOnQuoteListingPage_Direct", region, logger);

		//Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
		String password = passwords.get(DIRECTUSERPWD);

		//Waiting for user availability
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > User Name > Enter Password > Click on Sign In", url, purchaser, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select Organization & Catalog", "Requested Organization & Catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

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

		/** Pre-Condition ends **/
		Assert.assertTrue(accountSummary.clickOnOrderAndQuotesTabAndNavigate(
				"Step 1: Click on Orders & Quotes Tab and click on Quotes", "Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.verifyDefaultToggleShowHiddenQuotesIsDisplayed("Step 2: Verify the default toggle for Hide,Unhide of quotes"
				, "The Default toggle 'Show Hidden Quotes' should be displayed", true));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1: Search Quote with quote number", "Quote details should display",
				"Quote Name", quoteName, false));

		Assert.assertTrue(quoteListing.selectHideQuotesOptionFromActionLink("Step 3.2: Select Hide quote option", "Hide quote should be selected", quoteName, true));

		Assert.assertTrue(quoteListing.clickOnShowHiddenQuoteToggleVerifyHiddenQuoteIsDisplayed("Step 4: Click on Show Hidden Quotes toggle",
				"Quoted which are hidden ( if any ) should be displayed with Eye icon strike off and toggle should be changed to 'Hide Hidden Quotes'" , true));

		Assert.assertTrue(quoteListing.clickOnHideHiddenQuoteToggle("Step 5: Click on Hide Hidden Quotes", 
				"The Hidden quotes should not be displayed", true));

		Assert.assertTrue(quoteListing.clickOnShowHiddenQuoteToggleVerifyHiddenQuoteIsDisplayed("Click on Show Hidden Quotes toggle",
				"Quoted which are hidden ( if any ) should be displayed with Eye icon strike off and toggle should be changed to 'Hide Hidden Quotes'" ,false));

		Assert.assertTrue(quoteListing.selectUnHideQuotesOptionFromActionLink("Step 6: For the same quote click on actions gear button and click on Unhide option", 
				"Hide quote should be selected", quoteName, true));

		Assert.assertTrue(quoteListing.clickOnMyAccount("Step 7: Click on My accounts", "My Accounts page should be displayed"));

		Assert.assertTrue(quoteListing.verifyElementIsDisplayedByText("Step 8: Verify the Quotes section", 
				"Only Unhidden Quotes should be displayed in My accounts page", quoteName, true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify search results for 'Created By' in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300049
	 * @param region EMEA, APJ, AMS-US, AMS-LA
	 * @since Apr 27, 2021 1:21:24 PM
	 * @author Rashi
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class,groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.PARTNERAGENT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300049_Regression_CE_Quotes_VerifySearchResultsForCreatedByInQuoteListingPage_PartnerAgent(String region) {

		// Reporting info
		initializeReporting("Verify search results for 'Created By' in quote listing page",
				"C300049_Regression_CE_Quotes_VerifySearchResultsForCreatedByInQuoteListingPage_PartnerAgent", region, logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01,ID03,ID02,ID05);
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);

		String MDCPID = regData.get("MDCP ID");
		String catalogName =regData.get("Contract");
		String poNumber ="1234";
		String directUserpassword =passwords.get(DIRECTUSERPWD);
		String orgName=regData.get("Org Name");
		//String bto=getProductsByProductType(getRegion(region, dataIDs.get(region)),BTO).get(4);
		String bto = getProductByDataSetID(region, dataIDs.get(region),BTO);
		Assert.assertNotNull(bto);
		Map<String, String> data = new HashMap<String, String>();
		data.put("poNumber", poNumber);
		data.put("MDCPID", MDCPID);
		data.put("catalogName", catalogName);
		data.put("actionOnUsers", "Buy On Behalf");
		data.put("OrgName", orgName);
		String valueOfCreatedBy="";
		if(region.equalsIgnoreCase("EMEA") || region.equalsIgnoreCase("APJ")) {
			valueOfCreatedBy="automation";
		}else{
			//valueOfCreatedBy="PARTNER";
			valueOfCreatedBy="automation";
		}

		ArrayList<String> userSet= new ArrayList<>();
		//Waiting for user availability
		String username = getUser(dataIDs.get(region), PARTNERAGENT);
		Assert.assertNotEquals(username,null);
		userSet.add(username);
		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser1,null);
		userSet.add(purchaser1);
		data.put("emailID", purchaser1);
		String purchaser2 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser2,null);
		userSet.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		// Get URL
		setEnvironment();
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

		logger.info("<b>Pre-Condition Starts</b><br>Creating Quote & Sharing with Another User </b>");
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>", "<b>Creating Quote & Sharing with Another User </b>");

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with Partner agent user.", url, username, directUserpassword, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

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

		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser2,true));

		data.replace("emailID", purchaser1,purchaser2);

		Assert.assertTrue(quoteDetails.clickOnCustomerServiceLinkAndClickOnOkButtonInsideTerminateSessionPopup("Terminate the current session", "Customer Service Page Should be displayed for New Session ", purchaser2, true));

		//usersAvailability.replace(purchaser1, "Free");		
		userSet.remove(purchaser1);
		updateUserStatus(purchaser1, "Free");

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Ends</b>", "<b>Quote Created & Shared Successfully</b>");
		logger.info("<b>Pre-Condition Ends</b><br>Quote Created & Shared Successfully");
		/** Pre-Condition Ends **/

		Assert.assertTrue(homePage.clickAndNavigateToLinksUnderMyAccount("Step 1 & 2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		/*Assert.assertNotNull(homePage.navigateToMyAccount("Step 1: Click on 'My accounts'", "User should navigate to My accounts page"));

		Assert.assertTrue(accountSummary.navigateToQuotes("Step 2 : Click on Quotes Tab", "Quote listing page should be displayed with list of quotes available."));
		 */

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3 & 4 :Select Created By option from selection Drop down,<br>Enter a valid full/complete created by Text in Text box and click on Search Icon", "Created By option should be selected & Search results must appear across all the pages when the partial input given in the text box is found.",
				"Created By", valueOfCreatedBy, false));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 5:Verify search results include both valid and expired quotes","Search results must display both valid/expired quotes when the quote number contains with the search input"));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 6:Verify search results include both shared and created quotes", "Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched", quoteReferenceNumber));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 7:Verify search result appears with the most recently added Quote on Top-Descending Order","Most recently added quote that is matched with search criteria should display on Top."));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 8:Enter a any single character in Text box and click on Search Icon", "Search results must appear across all the pages when the partial input given in the text box is found in the quote Created By regardless of Small or capital letter",
				"Created By",valueOfCreatedBy.substring(0,1), false));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 9:Enter a few characters (simple letters and combination of characters and numbers)in Text box and click on Search Icon", "Search results must appear across all the pages when the partial input given in the text box is found in the quote Created By regardless of Small or capital letter",
				"Created By", valueOfCreatedBy.substring(0,3), false));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 10:Verify search results include both shared and created quotes", "Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched", quoteReferenceNumber));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 11:Verify search result appears with the most recently added Quote on Top-Descending Order","Most recently added quote that is matched with search criteria should display on Top."));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 12:Enter a invalid created by Text in text box and click on Search Icon", "User must be displayed with below error message Quote(s) not available. If you encountered any issues please contact your agent or HP representative.",
				"Created By", "Abc%%%$$##@", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Quote creation with onfly shipping address and checkout
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300034
	 * @param region EMEA, APJ, AMS-US, AMS-LA
	 * @since Apr 27, 2021 3:52:39 PM
	 * @author Vishwa A P
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300034_Regression_CE_Quotes_VerifyQuoteCreationWithOnFlyShippingAddressAndCheckout_CSR(String region) {

		// Reporting info
		initializeReporting("Verify Quote creation with onfly shipping address and checkout",
				"C300034_Regression_CE_Quotes_VerifyQuoteCreationWithOnFlyShippingAddressAndCheckout_CSR", region, logger);

		//Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = commonData.get(CSRORFEDCSRUSERPWD);
		String cto = getProductsByProductType(getRegion(region, dataIDs.get(region)), CTO).get(0);
		String bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, MONITORS).get(0);

		// Waiting for user availability
		String username = getUser(CSR);
		Assert.assertNotEquals(username, "");
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		ArrayList<String> userSet= new ArrayList<>();
		userSet.add(username);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		PDP pdp=new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		CreateAddress createAddress=new CreateAddress(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		HashMap<String, String> shippingAdress = new HashMap<String, String>();
		shippingAdress.put("AttentionText", "test");
		shippingAdress.put("Email", purchaser);
		shippingAdress.put("Phone", "6787787878");
		shippingAdress.put("ZipCode", "583104");
		shippingAdress.put("GstId", "12345678");
		String contractType = region.equalsIgnoreCase("EMEA")? "CBN" :"S4";
		if(region.equalsIgnoreCase("AMS-NA"))
		{
			shippingAdress.put("Company", "Capito ltd");
			shippingAdress.put("City", "LIVINGSTON");
			shippingAdress.put("StateProvince", "GB");
			shippingAdress.put("Addressline1", "CAPUTHALL ROAD");
		} else {
			shippingAdress.put("Company", "hp2b");
			shippingAdress.put("City", "banglore");
			shippingAdress.put("StateProvince", "KA");
			String addressLine = createAddress.systemDate();
			shippingAdress.put("Addressline1", addressLine);
		}

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with CTO SKU", "Requested product PDP should load", cto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter Qty in quantity field", "Quantity should be entered", "300"));

		Assert.assertTrue(pdp.addProductToCart("Step 3:  Add CTO product to cart at PDP", "Product should be added to cart", "pdp"));

		pdp = customerService.searchSKU("Step 4: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 5: Enter Qty in quantity field", "Quantity should be entered", "100"));

		Assert.assertTrue(pdp.addProductToCart("Step 6: Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 7: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 8: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 9: Select payment method from dropdown", 
				"Payment should be selected", "Purchase Order"));

		Assert.assertTrue(createNewQuote.verifyShippingAddress("Step 10: Verify Shipping address", "Default shipping address should be displayed"));

		Assert.assertTrue(createNewQuote.clickOnChangeShippingAddressButtonAndVerifyPopUpFileds(
				"Step 11 & 12: Verify and click on change shipping address button <br>And verify Ship to address popup fields", 
				"User should verify and click on change shipping address button <br>And verify Ship to address popup fields"));

		Assert.assertTrue(createNewQuote.navigateToCreateAddressPage("Step 13.1: Click ship to new address", "Create address page should displayed"));

		Assert.assertTrue(createAddress.verifyAllFieldsInCreateAddress("Step 13.2: Verify all fields in create address page", 
				" All required fields should be displayed"));

		Assert.assertTrue(createAddress.fillMandatoryDetailsInCreateAddress("Step 14.1: Fill Mandatory details in create address page and click on submit ", 
				" All Mandatory details fields should be Filled and clicked on submit button", shippingAdress,true));

//		/Assert.assertTrue(createNewQuote.vefifyNewShippingAddressAdded("Step 14.2: Verify newly created shipping address updated in Shipping address section", 
		//		"Newly created shipping address should be displayed in shipping address section", shippingAdress, contractType));

		quoteDetails = createNewQuote.createQuote("Step 15: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(quoteDetails.verifyPaymentMethodInQuoteDetailsPage(
				"Step 16.1: verify payment method in quote details page", "Payment method should verify", "Purchase Order"));

		Assert.assertTrue(quoteDetails.verifyShippingAddressInQuoteDetailsPage("Step 16.2: verify Shipping address in quote details page", 
				"Updated Shipping address should be displayed", shippingAdress));

		checkout = quoteDetails.navigateToCheckout("Step 17: Navigate to checkout page", "User should navigate to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifyPaymentMethodInCheckoutPage("Step 18.1: verify payment method in check out page",
				"Payment method should verify", "Purchase Order"));

		Assert.assertTrue(checkout.verifyShippingAddressInCheckOutPage("Step 18.2: Verify Shipping address in checkout page", 
				"Updated shipping address should be displayed", shippingAdress));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify pagination and sorting changes in quote list page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/295812
	 * @param region EMEA, APJ, AMS-US, AMS-LA
	 * @since Apr 28, 2021 12:21:39 AM
	 * @author Rashi
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C295812_Regression_CE_Quotes_VerifyPaginationAndSortingChangesInQuoteListPage_CSR(String region) {

		initializeReporting("Verify pagination and sorting changes in quote list page",
				"C295812_Regression_CE_Quotes_VerifyPaginationAndSortingChangesInQuoteListPage_CSR", region, logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01,ID03,ID02,ID05);
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);

		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String bto = getProductByDataSetID(region, dataIDs.get(region),BTO);
		Assert.assertNotNull(bto);
		String password =passwords.get(CSRORFEDCSRUSERPWD);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		
		ArrayList<String> userSet= new ArrayList<>();
		//Waiting for user availability
		String username = getUser(CSR);
		Assert.assertNotEquals(username,null);
		userSet.add(username);
		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser1,null);
		userSet.add(purchaser1);
		data.put("emailID", purchaser1);
		String purchaser2 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser2,null);
		userSet.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
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

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

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

		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser2,true));

		data.replace("emailID", purchaser1,purchaser2);

		Assert.assertTrue(quoteDetails.clickOnCustomerServiceLinkAndClickOnOkButtonInsideTerminateSessionPopup("Terminate the current session", "Customer Service Page Should be displayed for New Session ", purchaser2, true));

		//usersAvailability.replace(purchaser1, "Free");
		
		userSet.remove(purchaser1);
		updateUserStatus(purchaser1, "Free");
		

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Impersonate purchaser.", "User Should be Impersonated successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Ends</b>", "<b>Quote Created & Shared Successfully</b>");
		logger.info("<b>Pre-Condition Ends</b><br>Quote Created & Shared Successfully");
		/** Pre-Condition Ends **/

		Assert.assertTrue(login.clickAndNavigateToLinksUnderMyAccount("Step 1 & 2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

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
	 * Verify quote creation with onfly invoice mailing address
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300044
	 * @param region APJ,AMS-LA,EMEA,AMS-US
	 * @since May 3, 2021 10:46:07 AM
	 * @author ThomasAn
	 */			
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300044_Regression_CE_Quotes_VerifyQuoteCreationWithOnflyInvoiceMailingAddress_CSR(String region) {

		// Reporting info
		initializeReporting("Verify quote creation with onfly invoice mailing address",
				"C300044_Regression_CE_Quotes_VerifyQuoteCreationWithOnflyInvoiceMailingAddress_CSR",region,logger);
				
		// Test data		
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01","ID03","ID02","ID05");
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);
		
	    String catalogName = regData.get("Contract");
        String orgName = regData.get("Org Name");
        String mdcpId = regData.get("MDCP ID");
        String password = passwords.get(CSRORFEDCSRUSERPWD);
		   
		Map<String, String> data = new HashMap<String, String>();
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("MDCPID", mdcpId);
		String qty = "300";

		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);
		Assert.assertNotNull(bto);
				
		//Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String username = getUser(CSR);
		Assert.assertNotNull(username);
		
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		data.put("emailID", purchaser);
		userSet.add(username);
		userSet.add(purchaser);
		data.put("emailID", purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		
		// Get URL
		setEnvironment();
		String url = this.url;
		
		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		
		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter the Qty as " + qty, "Quantity should be entered", qty));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Click add to cart button",
				"Product should be added to cart", "pdp"));
		//Assert.assertTrue(pdp.addProductToCart("Step 1.2 : Add BTO product to cart at PDP", "Product should be added to cart","pdp"));
		
		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on minicart icon and click on go to cart button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button",
				"User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyBillingAddress("Step 6.1 : Verify default billing address",
				"User should be able to see the billing address"));

		Assert.assertTrue(createNewQuote.verifyInvoiceMailingAddressOnCreateNewQuotePage("Step 6.2 : Verify invoice mailing address",
				"User should be able to see the invoice mailing address"));

		Assert.assertTrue(createNewQuote.verifyChangeInvoiceMailingAddressButton("Step 6.3 : Verify change billing address button",
						"User should be able to see change billing address button"));

		Assert.assertTrue(createNewQuote.clickOnChangeInvoiceMailingAddressAndVerifyPopupWindow(
				"Step 7 : Click on Change mailing address button under Invoice mailing address section",
				"Change invoice mailing address popup window should be displayed."));

		Assert.assertTrue(createNewQuote.selectDifferentInvoiceMailingAddress(
				"Step 8: Select different invoice mailing address and click on ok", "Different invoice mailing address should be selected"));

		quoteDetails = createNewQuote.createQuote("Step 9: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(quoteDetails.verifyInvoiceMailingAddress(
				"Step 10: verify invoice mailing address in quote confirmation page", "Invoice mailing address should be verified"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	
	/**
	 * Verify quote details in in quote create quote page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301086
	 * @param region APJ,AMS-LA,EMEA,AMS-US
	 * @since May 7, 2021 3:46:57 PM
	 * @author ThomasAn
	 */	
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301086_Regression_CE_Quotes_VerifyQuoteDetailsInCreateQuotePage_Direct(String region) {

		// Reporting info
		initializeReporting("Verify quote details in in quote create quote page",
				"C301086_Regression_CE_Quotes_VerifyQuoteDetailsInCreateQuotePage_Direct",region,logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01","ID03","ID02","ID05");
		Map<String, String> regData = getScenarioData(dataIDs,region);
		
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
	    String orgName = regData.get("Org Name");
	    String password = passwords.get(DIRECTUSERPWD);		
	     
	    Map<String, String> data = new HashMap<String, String>();
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		String qty = "10";
	        
	    String username = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(username);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(username));
		
		String bundle = getProductByDataSetID(region, dataIDs.get(region),BUNDLE); 
		Assert.assertNotNull(bundle);
		
		// Get URL
		setEnvironment();
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

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/
		
		pdp = customerService.searchSKU("Step 1: Search with bundle SKU", "Requested product PDP should load", bundle);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter the Qty as " + qty, "Quantity should be entered", qty));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Add Bundle product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on mini cart icon and go to cart button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button", "Create New Quote' page should be displayed");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyBTOSpecsInCreateNewQuotePage("Step 6 : Verify BTO specs on create new quote page",
				"BTO Spec's should be displayed on create quote page."));

		HashMap<String, String> data1 = PageGenerics.getCurrentSystemDateAndEndDate(30);

		Assert.assertTrue(createNewQuote.verifyQuoteNameSectionAndFieldValidation("Step 7 & 8: Verify quote name section and validate fields",
				"Quote name section should be verified and fileds should be validated", data1));

		Assert.assertTrue(createNewQuote.verifyOrderInformationSectionAndFieldValidation(
				"Step 9 & 10: Verify order information section and validate fields",
				"Order Information section should be verified and fileds should be validated"));

		Assert.assertTrue(createNewQuote.verifyPurchaserContactInformationSectionAndFieldValidation(
				"Step 11 &12 : Verify purchaser contact information  section and validate fields on click on edit",
				"Purchaser contact information section should be verified and fileds should be validated on click on edit"));

		Assert.assertTrue(createNewQuote.verifyDefaultSelectedPaymentMethod("Step 13:Verify default payment method","Default payment method should be displayed","PurchaseOrder"));

		Assert.assertTrue(createNewQuote.verifyBillingAddress("Step 14 : Verify billing address session in create quote page",
						"User should be able to see the billing address session"));

		Assert.assertTrue(createNewQuote.verifyChangeBillingAddressButton("Step 15.1 : Verify change billing address button",
						"User should be able to see change billing address button"));

		Assert.assertTrue(createNewQuote.clickOnChangeBillingAddressAndVerifyPopupWindow(
						"Step 15.2: Click on change billing address button under billing address section",
						"User should click change billing address and verify pop up window"));

		Assert.assertTrue(createNewQuote.verifyShippingAddressInCreateNewQuote("Step 16: Verify Shipping information section in create quote page",
				"Default shipping address should be displayed"));

		Assert.assertTrue(createNewQuote.clickOnChangeShippingAddressButtonAndVerifyPopUpFileds(
				"Step 17: Click on Change shipping address button under shipping address section",
				"User should click change shipping address and verify pop up window"));

		Assert.assertTrue(createNewQuote.verifyCartSummarySection("Step 18 : Verify Cart summary section in create quote page",
				"Cart summary section should be verified"));
		Assert.assertTrue(createNewQuote.verifyBackToShoppingCartButton("Step 19 : Verify Back to shopping cart button in create quote page",
				"Back to shopping cart button should be verified"));
		
		quoteDetails = createNewQuote.createQuote("Step 20: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue",username);
		Assert.assertNotEquals(quoteDetails, null);
		
		List<String> quotesList = quoteDetails.getQuoteDetailsForVerification("Step 21 :Verify quote details in quote confirmation page",
				"Quote details should be displayed");
		Assert.assertNotEquals(quotesList, quotesList.isEmpty());

		Assert.assertTrue(quoteDetails.verifyBTOSpecsInQuoteDetailsPage("Step 22 : Verify BTO spec's on Quote details page",
				"BTO Specs should be verified"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
		
	/**
	 * Verify user is able to "Add items to current cart" from quote list page and checkout
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300132
	 * @param region APJ,EMEA,AMS-US,AMS-LA
	 * @since Apr 26, 2020
	 * @author Manpreet
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class,groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300132_Regression_CE_Quotes_VerifyUserIsAbleToAddItemsToCurrentCartFromQuoteListPageAndCheckout_CSR(String region) {

		// Reporting info
		initializeReporting("Verify user is able to Add items to current cart from quote list page and checkout",
				"C300132_Regression_CE_Quotes_VerifyUserIsAbleToAddItemsToCurrentCartFromQuoteListPageAndCheckout_CSR", region, logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String poNumber = regData.get("Orders");
		String phoneNumber = "12345";
		String attentionText = "test";
		String PO = "Purchase Order";

		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);		
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		String CSRUser = getUser(CSR);
		Assert.assertNotNull(CSRUser);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		users.add(CSR);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser);

		// Get URL
		setEnvironment();
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

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1: Click on Organization and Catalog dropdown<br>Step 2: Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected view dropdown is displayed<br>Selected catalog should be loaded", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

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

		quoteListing = accountSummary.clickOnViewAllQuotesUnderMyAccountSection("Step 4.1: Click on View All Quotes link", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 4.2: Select Quote Name option from selection Drop down<br>"
				+ "Step 4.3: Enter the full valid quote name in Text box and click on Search Icon", "Quote Name option must have been selected<br>"
						+ "Search results must appear across all the pages", dropdownValue, quoteNumber, false));

		shoppingCart =  quoteListing.selectAddCurrentItemToCartQuotesOptionFromActionLink("Step 4.4: Click on action gear icon and select 'Add items to current cart' option", "Cart page should be displayed with list of items");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.verifyProductSkuInCart("Step 4.5: Verify Cart page should be displayed with list of items which are specified in the quote.", "Cart page should be displayed with list of items which are specified in the quote.", bto));

		checkout = shoppingCart.clickOnCheckOut("Step 5.1: Click on checkout button in cart page", "Checkout page should be displayed with list of products added in cart");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifySKUisDisplayedOnCheckoutPage("Step 5.2: Verify Checkout page contains the products in cart", "Checkout page should contain the products in cart", bto, true));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 6.1:Enter all the Mandatory fields", "User should enter all the values successfully", mandatoryData));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 6.2: Click on Create purchase Order button to place an Order",
				"PO confirmation page should be displayed.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify quote creation for change in billingAddress
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300030
	 * @param region APJ,EMEA,AMS-US,AMS-LA
	 * @since Apr 28, 2021 12:55:22 PM
	 * @author Anjumol
	 */		
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300030_Regression_CE_Quotes_VerifyQuoteCreationForChangeInBillingAddress_PartnerAgent(String region) {

		// Reporting info
		initializeReporting("Verify quote creation for change in billingAddress",
				"C300030_Regression_CE_Quotes_VerifyQuoteCreationForChangeInBillingAddress_PartnerAgent",region,logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01","ID03","ID02","ID05");
		Map<String, String> regData = getScenarioData(dataIDs,region);
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
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);

		String qty = "900";
		
		//Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String username = getUser(dataIDs.get(region), PARTNERAGENT);
		Assert.assertNotNull(username);
		
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		data.put("emailID", purchaser);
		userSet.add(username);
		userSet.add(purchaser);
		data.put("emailID", purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		
		// Get URL
		setEnvironment();
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

		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with Partner Agent", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Enter MDCPID and mail id and Buy on behalf purchaser",
				"Purchaser should be buy on behalf successfully", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog",
				"Requested catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1.1: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2.1: Enter the Qty as " + qty, "Quantity should be entered", qty));

		Assert.assertTrue(pdp.addProductToCart("Step 2.2: Add product to cart at PDP", "Product should be added to cart", "pdp"));
		
		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 3 :Click on 'Mini cart' icon and Click on 'Go to cart' button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 4: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyPartnerAgentID("Step 5: Verify Partner Agent ID is pre-populated",
				"Partner Agent id should pre-populate"));

		Assert.assertTrue(checkout.selectPaymentMethod("Step 6: Select Payment method as 'lease'", "Lease payment Option should be selected", "Lease"));
		
		Assert.assertTrue(createNewQuote.verifyBillingAddress("Step 7 : Verify billing address", "User should be able to see the billing address"));

		Assert.assertTrue(createNewQuote.verifyChangeBillingAddressButton("Step 8 : Verify change billing address button",
						"User should be able to see change billing address button"));

		Assert.assertTrue(createNewQuote.clickOnChangeBillingAddressAndVerifyPopupWindow(
				"Step 9 : Click on change billing address button",
				"Change billing address button should be clikced and popup window should be verified"));

		String selectedNewBillingAddress = createNewQuote.clickOnChangeBillingAddressAndSelectNewBillingAddressAndClickOnOk("Step 10 :Select different billing address and click on ok button",
				"Selected billing address should be displayed", false);
		Assert.assertNotEquals(selectedNewBillingAddress, null);

		quoteDetails = createNewQuote.createQuote("Step 11: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(quoteDetails.verifyPaymentMethodInQuoteDetailsPage(
				"Step 12.1: verify payment method in quote details page", "Payment method should verify", "Lease"));

		LinkedHashMap<String, String> LinkedHashMap = quoteDetails.getBillingInformationDetails(
				"Step 12.2:verify billing address in quotes details page", "Billing address should be verified", "CSV");
		Assert.assertNotEquals(LinkedHashMap, null);

		checkout = quoteDetails.navigateToCheckout("Step 13: Click on Checkout button in quote detail page",
				"User should navigate to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifyBillingAddress("Step 14.1: Verify billing address in checkout page",
				"Billing address should verify"));

		Assert.assertTrue(checkout.verifyPaymentMethodInCheckoutPage("Step 14.2: Verify payment method in check out page",
				"Payment method should verify", "Lease"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}



	/**
	 * Verify user is able to share existing quote from home page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300032
	 * @param region APJ,AMS-LA,EMEA,AMS-US
	 * @since Apr 26, 2021 7:53:08 PM
	 * @author Keshav
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300032_Regression_CE_Quotes_VerifyUserIsAbleToShareExistingQuoteFromHomePage_CSR(String region){

		// Reporting info
		initializeReporting("Verify user is able to share existing quote from home page",
				"C300032_Regression_CE_Quotes_VerifyUserIsAbleToShareExistingQuoteFromHomePage_CSR",region,
				logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01","ID03","ID02","ID05");
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		//String bto=getProductsByProductType(getRegion(region, dataIDs.get(region)),"BTO").get(3);
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);                
		String mdcpId = regData.get("MDCP ID");

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpId);
		data.put("actionOnUsers", actionOnUsers);
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser1);
		String purchaser2 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser2);
		String user = getUser("CSR");
		Assert.assertNotNull(user);
		System.out.println(user);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser1);
		users.add(purchaser2);
		users.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		HomePage homePage = new HomePage(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);

		data.put("emailID", purchaser1);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user","User is Impersonated.",data));


		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Click on Organization and Catalog dropdown<br>Step 2:"
				+ "Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected view dropdown is displayed<br>Selected catalog should be loaded", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition:  Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		//		Assert.assertTrue(createNewQuote.verifyAndSelectB2BiQuoteCheckBox(
		//				"PreCondition : Verify & Select B2Bi checkbox.", "B2Bi quote checkbox should verified and selected"));

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

		//		Assert.assertTrue(homePage.clickOnTabByName("Step 3: Click on Quotes in home page above the Quick order section",
		//				"List of existing quotes with gear icon should be displayed", "Quotes"));

		Assert.assertTrue(customerService.clickAndNavigateToLinksUnderMyAccount(
				"Step 3: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		//		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 4.1 :Search Quote", "Quote details should display",
		//				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 4.1: Click on Action gear icon","Clicked on Share quote",
				quoteNumber,"Share quote",true));

		Assert.assertTrue(quoteListing.clickOnActions("Step 4.2: Select or click 'Share quote' option", "Share quote pop up with login id text field and with Cancel & Share quote buttons should be displayed",
				"Share quote"));

		Assert.assertTrue(quoteListing.enterEmailidAndVerifySharequoteMessage("Step 5: Enter valid login details in 'Login id' field and click on 'Share quote' button",
				"Quote is now available to : login Id message should be displayed and quote is shared successfully", purchaser2));

		Assert.assertTrue(login.clickOnCustomerServiceLink("Step 6.1: Click on customer service link in home page"
				, "Clicked on Customer Service link."));

		//		Assert.assertTrue(login.clickOnYesButtonOn("Step 7.2: Click on Yes popup after clicking on customer service link",
		//				"Clicked on Yes button.",true));

		usersAvailability.replace(purchaser1, "Free");
		users.remove(purchaser1);
		data.put("emailID", purchaser2);
		System.out.println("Impersonate user 2: "+purchaser2);
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Step 6.2: enter other user login mail id with whom quote is shared"
				,"Impersonate the user",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 7.1: Select requested catalog", "Requested catalog should be selected", data,true));

		//		Assert.assertTrue(homePage.clickOnTabByName("Step 7.2: Navigate to quote list page",
		//				"List of existing quotes with gear icon should be displayed", "Quotes"));
		Assert.assertTrue(customerService.clickAndNavigateToLinksUnderMyAccount(
				"Step 7: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(homePage.verifyQuoteDisplayedonHomePage("Step 7.3: Verify Shared Quote", 
				"Shared quote should be displayed in quote list page", quoteNumber, true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify user is able to remove products in create quote page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300035
	 * @since Apr 19, 2021 9:09:47 AM
	 * @author  Vijay
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES },priority = 2)
	public void C300035_Regression_CE_Quotes_VerifyUserIsAbleToRemoveProductsInCreateQuotePage_Direct(String region) {

		//Reporting info
		initializeReporting("Verify user is able to remove products in create quote page",
				"C300035_Regression_CE_Quotes_VerifyUserIsAbleToRemoveProductsInCreateQuotePage_Direct", region, logger);

		//Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		//String bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, ACCESSORIES).get(0);
		 String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	        Assert.assertNotNull(bto);
//		String kit = "";
//		if(region.equals(APJ))
//			kit = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), KIT, ACCESSORIES).get(0);
//		else kit = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), KIT, DESKTOPS).get(0);
	    String kit = getProductByDataSetID(region, dataIDs.get(region),KIT);
	    Assert.assertNotNull(kit);
		String password = passwords.get(DIRECTUSERPWD);

		//Waiting for user availability
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("phoneNumber", "12345");
		mandatoryData.put("attentionText", "test");
		mandatoryData.put("emailID", purchaser);

		//Get URL
		setEnvironment();
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

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", 
				"Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition : Delete product", "Product should be deleted", "CE", false));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter Qty as 500 and update",
				"Qty should be updated successfully", "500"));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Click on 'Add to cart' button", 
				"Product should be added to cart Successfully", "pdp"));

		pdp = customerService.searchSKU("Step 4: In search box, search for kit number", 
				"PDP of searched product should be displayed", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 5:  Enter Qty as 500 and update",
				"Qty should be updated successfully", "500"));

		Assert.assertTrue(pdp.addProductToCart("Step 6: Click on 'Add to cart' button", 
				"Product should be added to cart Successfully", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 7: Click on Mini cart and click Go to cart button",
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

		quoteDetails = createNewQuote.createQuote("Step 10: Enter all mandatory details and Click on 'Save Quote' button",
				"Quote should be created successfully and navigated to Quote detail page.", "Aut_Quote_", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		checkout = quoteDetails.navigateToCheckoutPage("Step 11: Verify quote details and click on checkout button",
				"Checkout page should be displayed with details as specified in quote");
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 12.1: Enter all the Mandatory fields", 
				"User should enter all the values successfully", mandatoryData));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 12.2: Click on Create purchase Order button to place an Order",
				"PO confirmation page should be displayed.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify search results for 'Quote Number' in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300046
	 * @param region APJ,EMEA,AMS-US,AMS-LA
	 * @since Apr 26, 2020
	 * @author Manpreet
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class,groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300046_Regression_CE_Quotes_VerifySearchResultsForQuoteNumberInQuoteListingPage_CSR(String region) {

		// Reporting info
		initializeReporting("Verify search results for 'Quote Number' in quote listing page",
				"C300046_Regression_CE_Quotes_VerifySearchResultsForQuoteNumberInQuoteListingPage_CSR", region, logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String poNumber = regData.get("Orders");
		String phoneNumber = "12345";
		String attentionText = "test";
		String PO = "Purchase Order";

		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);		
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser1);
		String purchaser2 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser2);
		String CSRUser = getUser(CSR);
		System.out.println(CSRUser);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser1);
		users.add(purchaser2);
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser1);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("poNumber", poNumber);
		mandatoryData.put("phoneNumber", phoneNumber);
		mandatoryData.put("attentionText", attentionText);
		mandatoryData.put("paymentMethod", PO);
		mandatoryData.put("MDCPID", mdcpid);
		mandatoryData.put("catalogName", catalogName);
		mandatoryData.put("actionOnUsers", actionOnUsers);
		mandatoryData.put("OrgName", orgName);
		String dropdownValue = "Quote Number";
		String invalidQuote = "123456789";
		String singleDigitQuoteNumber = "5";

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1: Click on Organization and Catalog dropdown<br>Step 2: Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected view dropdown is displayed<br>Selected catalog should be loaded", data, true));


		Assert.assertTrue(
				login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

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

		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser2,true));

		data.replace("emailID", purchaser1,purchaser2);
		usersAvailability.replace(purchaser1, "Free");
		data.remove(purchaser1);
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

		QuoteListing quoteListing = accountSummary.clickOnViewAllQuotesUnderMyAccountSection("Step 2: Click on View All Quotes link", "Quote listing page should be displayed with list of quotes available");
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
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300031
	 * @since May 3, 2021 1:13:55 PM
	 * @author Keshav
	 */
	@Test(dataProvider = "region_data-provider",dataProviderClass = HP2BDataProvider.class,
			groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.PARTNERAGENT,
					IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300031_Regression_CE_Quotes_VerifyUserIsAbleToShareQuoteFromQuoteConfirmationPage_PartnerAgent(String region) {

		//Reporting info
		initializeReporting("Quote_Verify user is able to share quote from quote confirmation page",
				"C300031_Regression_CE_Quotes_VerifyUserIsAbleToShareQuoteFromQuoteConfirmationPage_PartnerAgent",region,
				logger);

		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");

		//String bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)),"BTO","Laptops").get(0);
		//String softBundle = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)),"SoftBundle","Desktops").get(0);
		
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
		String softBundle = getProductByDataSetID(region, dataIDs.get(region),SOFTBUNDLE);
		Assert.assertNotNull(softBundle);
		String password = passwords.get(DIRECTUSERPWD);  
		System.out.println(password);

		String partnerAgent = getUser(dataIDs.get(region), PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser1);
		System.out.println(purchaser1);
		String purchaser2 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser2);
		System.out.println(purchaser2);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser1);
		users.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Buy On Behalf");
		data.put("emailID", purchaser1);


		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);

		//		String partnerAgentId = login.getPartnerAgentID(username, partnerAgentIds, partnerAgents);
		//		Assert.assertNotEquals(partnerAgentId, null);

		/** Pre-Condition Starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with Partner agent user.", url, partnerAgent,password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		/** Pre-Condition Ends **/

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 1 : In search box , search for soft bundle number",
				"PDP of searched product should be displayed", softBundle);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.clickOptionCodeDropdownAndSelectOption("Step 2.1: Select option code", "Option code selected.",true));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2.2: Enter the Qty as " + 400 + " and update", "Qty should be updated successfully", "400"));

		Assert.assertTrue(pdp.addProductToCart("Step 3.1:  Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		ShoppingCart shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5.1: Click on save as quote button", "'Create New Quote' page should be displayed with the list of products added in cart page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(shoppingCart.verifyFavoriteProductIsAvailableInCartPage("Step 5.2: Verify the product added is displayed in Create New Quote page", 
				"Product added is displayed in create new quote page",softBundle));

		//		Assert.assertTrue(createNewQuote.verifyPartnerAgentId("Step 6: Verify Partner Agent field", "Partner ID number should be auto populated",
		//				partnerAgentId, true));

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
		Assert.assertTrue(login.loginToHP2B("Step 12.1: Login to HP2B with other user and check for shared quote", purchaser2, password, true));


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
	 * Verify that user is able to see the defaulted billing and shipping address in Create Quote Page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/365041
	 * @param region EMEA, APJ, AMS-US, AMS-LA
	 * @since Apr 28, 2021 6:44:20 PM
	 * @author Rashi
	 */
	@Test(dataProvider = "region_data-provider",dataProviderClass = HP2BDataProvider.class,groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C365041_Regression_CE_Quotes_S4VerifyUserIsAbleToSeeDefaultedBillingAndShippingAddressInCreateQuotePage_CSR(String region) {
		// Reporting info
		initializeReporting("Verify that user is able to see the defaulted billing and shipping address in Create Quote Page",
				"C365041_Regression_CE_Quotes_S4VerifyUserIsAbleToSeeDefaultedBillingAndShippingAddressInCreateQuotePage_CSR",region, logger);

		//Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01,ID03,ID02,ID05); //ID02 & ID05 to be mapped with valid data sets
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String password =passwords.get(CSRORFEDCSRUSERPWD);
		
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);		
		Assert.assertNotNull(bto);
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		ArrayList<String> userSet= new ArrayList<>();
		// Waiting For Users Availability
		String user = getUser(CSR);
		Assert.assertNotEquals(user, null);
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser, null);
		data.put("emailID", purchaser);
		userSet.add(purchaser);
		userSet.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PDP pdp=new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition : Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("PreCondition : Click on Go to cart button inside Mini cart Popup",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition : Click on save quote link", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("PreCondition : Select default Billing and Shipping address checkbox", "Billing and Shipping address checkbox Should be selected","Billing and Shipping Address"));

		Assert.assertTrue(createNewQuote.clickOnOkButtonInsideDefaultAddressConfirmationPopup("PreCondition : Click on ok button inside default address confirmation popup", "Ok button should be selected",false));

		LinkedHashMap<String, String> billingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"createNewQuote","billing");
		Assert.assertNotEquals(billingInfoInCreateNewQuotePage, null);

		LinkedHashMap<String, String>shippingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"createNewQuote","shipping");
		Assert.assertNotEquals(shippingInfoInCreateNewQuotePage, null);


		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 2.1: Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 2.2: Click on Mini cart and Navigate to shopping cart page",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 3: Click on save quote link", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyDefaultSelectedPaymentMethod("Step 4:Ensure that defaulted Payment method PO is displayed","Defaulted PO method must be displayed","PurchaseOrder"));

		LinkedHashMap<String, String> billingInfoInCreateNewQuotePage2= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"createNewQuote","billing");
		Assert.assertTrue(billingInfoInCreateNewQuotePage2.size()>0);

		LinkedHashMap<String, String>shippingInfoInCreateNewQuotePage2= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"createNewQuote","shipping");
		Assert.assertTrue(shippingInfoInCreateNewQuotePage2.size()>0);

		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsSelected("Step 5.1: Ensure that previously selected 'Default Billing Address' checkbox should be selected by default.","'Default Billing Address' checkbox should be selected by default",true));

		Assert.assertTrue( createNewQuote.compareTwoHashMap("Step 5.2 :Ensure that previously selected 'Default Billing Address' must be displayed","User must be able to see the address with which it was created", billingInfoInCreateNewQuotePage, billingInfoInCreateNewQuotePage2));

		Assert.assertTrue(createNewQuote.verifyDefaultShippingAddressCheckboxIsSelected("Step 6.1: Ensure that previously selected 'Default Shipping Address' checkbox should be selected by default.","'Default Shipping Address' checkbox should be selected by default",true));

		Assert.assertTrue( createNewQuote.compareTwoHashMap("Step 6.2:Ensure that previously selected 'Default Shipping Address' must be displayed","User must be able to see the address with which it was created", shippingInfoInCreateNewQuotePage, shippingInfoInCreateNewQuotePage2));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify user is able to "Copy quote" from existing quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300036
	 * @since May 4, 2021 9:09:47 AM
	 * @author  Keshav
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class,
			groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
					IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300036_Regression_CE_Quotes_VerifyUserIsAbleToCopyQuoteFromExistingQuote_CSR(String region){

		// Reporting info
		initializeReporting("Verify user is able to Copy quote from existing quote",
				"C300036_Regression_CE_Quotes_VerifyUserIsAbleToCopyQuoteFromExistingQuote_CSR",region,logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01","ID03","ID02","ID05");
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
	    String password = passwords.get(CSRORFEDCSRUSERPWD); 
		String mdcpId = regData.get("MDCP ID");

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpId);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		// Waiting For Users Availability
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		String user = getUser(CSR);
		Assert.assertNotNull(user);
		System.out.println(CSR);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		users.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		data.put("emailID", purchaser);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user","User is Impersonated.", data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog",
				"Requested catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition:  Add BTO product to cart at PDP",
				"Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Go to the shopping cart page",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button",
				"User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote(
				"PreCondition:Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification(
				"PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList, "Quote Number");

		/** Pre-Condition Ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1: Click on Organization and Catalog dropdown<br>"
				+ "Step 3: Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected view dropdown is displayed<br>Selected catalog should be loaded", data, true));

		AccountSummary accSummary = customerService.navigateToMyAccount("Step 3: Click on 'My accounts' in home page",
				"My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount(
				"Step 4: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 5.1 :Search Quote", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 5.2: Click on Gear button", "Clicked on gear button", "Hide quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 5.3: Click on Action gear icon and select 'Hide' option"
				, "Clicked on Hide Option", "Hide quote"));

		Assert.assertTrue(quoteListing.clickOnShowHiddenQuoteToggleVerifyHiddenQuoteIsDisplayed("Step 5.4: Click on show hidden", 
				"Clicked on Show hidden<br>he Quote Should be hidden successfully and Eye icon with stricken off should be displayed beside quote number.", true));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 6.1: Click on Gear button", "Clicked on gear button", "Unhide quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 6.2: Click on Action gear icon and select 'Unhide' option"
				, "Clicked on Unhide Option", "Unhide quote"));

		Assert.assertTrue(quoteListing.verifyEyeImage("Step 6.3: Verify Quote is unhidden", "The Quote Should be Unhidden successfully.", "Quote is unhidden successfully"
				, false, true));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 7.1: Click on Gear button", "Clicked on gear button", "Hide quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 7.2: Select 'Copy Quote' option"
				, "Clicked on Unhide Option", "Copy quote"));


		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 8: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("Step 9.1: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully and navigated to Quote detail page.","QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 9.2: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String copiedQuoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");

		//		quoteListing = accSummary.clickOnQuotesUnderMyAccountSection("Step 10.1: Click on 'Quotes' under Orders and Quotes",
		//				"Quote list page should display with list of existing quotes with gear icon");
		//		Assert.assertNotNull(quoteListing);
		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount(
				"Step 10.1: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 10.2 :Verify Copy quote is displayed in quote list page", 
				"Copy of quote created should be displayed in quote listing page",
				"Quote Number", copiedQuoteNumber, false));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify shipping options and shipping instructions from quote to checkout
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300039
	 * @param region AMS-US
	 * @since May 3, 2021 11:44:02 AM
	 * @author Vishwa A P
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES },priority = 2)
	public void C300039_Regression_CE_Quotes_VerifyShippingOptionsAndShippingInstructionsFromQuoteToCheckout_Direct() {

		//Reporting info
		initializeReporting("Verify shipping options and shipping instructions from quote to checkout",
				"C300039_Regression_CE_Quotes_VerifyShippingOptionsAndShippingInstructionsFromQuoteToCheckout_Direct", logger);

		// Test Data
		Map<String, String> regData = getScenarioData(ID02);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);

		String kit = getProductsByProductType(AMS_NA, KIT).get(0);

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		//Waiting for user availability
		String purchaser = getUser(ID02, PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		//String shippingInstructionsText="Shipping instructions Text is entered";

		//Quote data
		Map<String, String> quoteData = new HashMap<String, String>();
		quoteData.put("PaymentOption", "Credit Card");
		quoteData.put("ShippingOption", "Two Day");
		quoteData.put("ShippingInstructionText", "Shipping instructions Text is entered");
		quoteData.put("DefaultSelectedValue", "Standard Delivery");


		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition : Delete product", "Product should be deleted", "CE", false));

		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with KIT SKU", "Requested product PDP should load", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter Qty in quantity field", "Quantity should be entered", "42"));

		Assert.assertTrue(pdp.addProductToCart("Step 2.1: Add KIT product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save quotes button", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 6: Select payment method from dropdown", 
				"Payment should be selected", quoteData.get("PaymentOption")));

		Assert.assertTrue(createNewQuote.verifyShippingOptionsDropDownAndDefaultSelectedValue("Step 7: Verify shipping options displayed"
				+ " and default value should be selected", "All shipping options should be displayed and default value should be selected", 
				quoteData.get("DefaultSelectedValue")));

		Assert.assertTrue(createNewQuote.selectShippingOptionDropDown("Step 8: Select shipping option to Two Day",
				"Required shipping option should be selected", quoteData.get("ShippingOption")));

		Assert.assertTrue(createNewQuote.verifyShippingInstructionsOptionWithTextFieldAndEnterMaximumText("Step 9 & 10: Verify Shipping Instructions option with text "
				+ "field and enter maximum text characters",
				"Shipping instructions textbox should be displayed and maximum 40 Text characters should be entered", quoteData.get("ShippingInstructionText")));

		quoteDetails = createNewQuote.createQuote("Step 11: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(createNewQuote.VerifyPaymentMethodShippingOptionAndShippingInstructions("Step 12: Select shipping option to Two Day",
				"Required shipping option should be selected", quoteData.get("PaymentOption"), "Two Day", quoteData.get("ShippingInstructionText") ));

		checkout = quoteDetails.navigateToCheckout("Step 13: Navigate to checkout page",
				"User should navigate to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifyShippingOptionsAndInstructionsInCheckoutPage("Step 14: Select shipping option to Two Day",
				"Required shipping option should be selected", quoteData.get("ShippingOption"), quoteData.get("ShippingInstructionText") ));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify that user is able to change 'Default billing address'-Lease payment method"
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/365041
	 * @param region EMEA, APJ
	 * @since Apr 28, 2021 6:44:20 PM
	 * @author Rashi
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C365040_Regression_CE_Quotes_S4VerifyUserIsAbleToChangeDefaultBillingAddressLeasePaymentMethod_CSR(String region) {

		// Reporting info
		initializeReporting("Verify that user is able to change 'Default billing address'-Lease payment method",
				"C365040_Regression_CE_Quotes_S4VerifyUserIsAbleToChangeDefaultBillingAddressLeasePaymentMethod_CSR", region, logger);

//		ArrayList<String> regionsForWhichDataIsUnavailable= new ArrayList<String>(Arrays.asList("EMEA"));
//		Assert.assertFalse(PageGenerics.reportDataUnavailability(regionsForWhichDataIsUnavailable,region));

		// Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01, ID03,ID02,ID05);
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String password =passwords.get(CSRORFEDCSRUSERPWD);
		//String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, LAPTOPS);
		String bto = getProductByDataSetID(region, dataIDs.get(region),BTO);
		Assert.assertNotNull(bto);
		
		
		// Waiting for user availability
		String CSRUser = getUser(CSR);
		Assert.assertNotNull(CSRUser);
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(CSRUser, purchaser));
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PDP pdp=new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));
		
		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("PreCondition : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition : Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("PreCondition : Click on Go to cart button inside Mini cart Popup",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition : Click on save quote link", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("PreCondition : Select default Billing address checkbox", "Billing address checkbox Should be selected","billing address"));

		Assert.assertTrue(createNewQuote.clickOnOkButtonInsideDefaultAddressConfirmationPopup("PreCondition : Click on ok button inside default address confirmation popup", "Ok button should be selected",false));

		Assert.assertTrue(checkout.navigateBackToShoppingCartPage("PreCondition : Click on Back to cart", "Navigated to cart page"));
		
		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 2.1: Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 2.2: Click on Mini cart and Navigate to shopping cart page",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 3: Click on save quote link", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsSelected("Step 4: Ensure that previously selected 'Default Billing Address' checkbox should be selected by default.","'Default Billing Address' checkbox should be selected by default",true));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 5: Select payment method from dropdown", 
				"Payment should be selected", "Lease"));

		Assert.assertTrue(createNewQuote.verifyBillingAddressesPopupAfterClickingOnChangeBillingAddressButton("Step 6 : Click change billing address button","Billing addresses popup must appear"));

		String updatedAddress = createNewQuote.clickOnChangeBillingAddressAndSelectNewBillingAddressAndClickOnOk("Step 7 : select any S4 ID","Selected S4 ID and associated billing address must be displayed", false);
		Assert.assertNotEquals(updatedAddress,null);

		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsNotSelected("Step 8: unselected checkbox has to appear for the newly selected billing address.","unselected checkbox must appear for the newly selected billing address",true));

		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 9: select 'Default Billing Address' checkbox", "Billing address checkbox Should be selected","billing address"));

		Assert.assertTrue(createNewQuote.verifyDefaultAddressConfirmationPopupAndClickOnOK("Step 9.2: Verify Default Address Confirmatio popup and Click on ok button ", " Below message must be displayed to the user: </br> Default Address Confirmation </br>"
				+ "When selecting a default billing address, the associated payment method is also set as default. </br>\r\n"
				+ "Current payment method: Lease </br>","Lease",true));

		quoteDetails = createNewQuote.createQuote("Step 10:Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully","Aut_Quote_", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Estimated Tax line item in exported quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300040
	 * @since May 5, 2021 9:09:47 AM
	 * @author ShishoSa
	 */
	//	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
	//			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300040_Regression_CE_Quotes_VerifyEstimatedTaxLineItemInExportedQuote_CSR() {

		//Reporting info
		initializeReporting("Quote_Verify Estimated Tax line item in exported quote",
				"C300040_Regression_CE_Quotes_VerifyEstimatedTaxLineItemInExportedQuote_CSR", logger);

		//Test data
		String scenarioId = "ID03";
		Map<String, String> regData = getScenarioData(scenarioId);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProductsByProductTypeAndCategory(EMEA, BTO, LAPTOPS).get(1);;
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		//Waiting for user availability		
		String csr = getUser(CSR);
		Assert.assertNotEquals(csr, "");
		String purchaser = getUser(scenarioId, PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		ArrayList<String> userSet= new ArrayList<>();
		userSet.add(csr);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		//		HashMap<String, String> regData = getTestData("CE", "DT001_EMEA");

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		//Get URL
		setEnvironment();
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
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", storeCE, false));

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
				"Quote should be created successfully and navigated to quote confirmation page", "AutQuote", csr));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotNull(createNewQuote);
		String quoteName = "AutQuote" + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 9: Verify Estimated tax line item under cart summary section in Quote detail page", 
				"Estimated tax line item should be displayed and it should be included in total", true));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 10.1: Click on Export", "Export popup should be displayed"));
		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 10.2: Select the export type as XLS", "File type should be selected", "xls"));
		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 10.3: Click on 'Export' in quote confirmation page", "XLS format should get exported successfully"));

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Precondition: Get Estimated Tax Value", "Estimated tax price is fetched", true);

		Assert.assertTrue(xls.verifyEstimatedTaxInHtmlEmbeddedXls("Step 11-12: Go to the file location and click on Quote to open<br>"
				+ "Verify estimated tax line item in exported quote",
				"Estimated tax line item should be displayed with all other details of quote", quoteName, estimatedTax));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_Verify Shipping & Handling charges in exported & printed quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300045
	 * @since May 5, 2021 9:09:47 AM
	 * @author ShishoSa
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300045_Regression_CE_Quotes_VerifyShippingAndHandlingChargesInExportedAndPrintedQuote_CSR() {

		//Reporting info
		initializeReporting("Quote_Verify Shipping & Handling charges in exported & printed quote", 
				"C300045_Regression_CE_Quotes_VerifyShippingAndHandlingChargesInExportedAndPrintedQuote_CSR", logger);

		//Test data
		Map<String, String> regData = getScenarioData(ID03);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProduct(EMEA, BTO, MONITORS);

		String password = passwords.get(CSRORFEDCSRUSERPWD);

		//Waiting for user availability		
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotEquals(csr, "");
		String purchaser = getUser(ID03, PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		userSet.add(csr);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		XLSValidations xls = new XLSValidations(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > Enter CSR User Name > Enter Password > Click on Sign In", url, csr, password, true));
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Click Customer Service link> Enter MDCPID and Email Id > Click on Search > Click on Action Gear against search result and Select Impersonate User option", "User should be impersonated", data));
		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog", "Requested org & catalog should be selected", data, true));
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", storeCE, false));

		Assert.assertNotNull(login.searchSKU("Step 1: In search box, search for BTO sku", "PDP of searched product should be displayed", bto));

		Assert.assertTrue(login.enterFirstProductQuantity("Step 2: Enter Qty as 100 and update", "Qty should be updated successfully", "100", "pdp"));

		Assert.assertTrue(login.addProductToCart("Step 3: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 5: Click on 'save as quote' button", "'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.verifyShippingAndHandlingChargesAreDisplayed("Step 6: Verify Shipping & Handling charges under cart summary section in Create quote page", 
				"Shipping & Handling charges should be displayed and should be included to total", true));

		Assert.assertNotNull(createNewQuote.createQuote("Step 7: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", "AutQuote", csr));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotNull(quoteDetailsList);
		String quoteName = "AutQuote" + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 8.1: Click on Export", "Export popup should be displayed"));
		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 8.2: Select the export type as XLS", "File type should be selected", "xls"));
		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 8.3: Click on 'Export' in quote confirmation page", "XLS format should get exported successfully"));

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Precondition: Get Shipping & Hnadling Charges Value", "Shipping & Hnadling Charges should be fetched", true);

		/*
		 * Assert.assertTrue(xls.
		 * verifyShippingAndHandlingChargesInHtmlEmbeddedXls("Step 9-10: Go to the file location and click on Quote to open<br>"
		 * +
		 * "Verify Shipping & Handling charges under cart summary section in exported quote"
		 * ,
		 * "Shipping & Handling charges should be displayed and should be included to total"
		 * , quoteName, shippingCharges));
		 */

		
		Assert.assertTrue(xls.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching("Step 9: Go to the file location and click on Quote to open", "Quote should get displayed successfully", quoteName));
		
		Assert.assertTrue(xls.verifyShipAndHandlingChargesInHtmlEmbededXls("Step 10:Verify Shipping & Handling charges under cart summary section in exported quote", "Shipping & Handling charges should be displayed and should be included to total", shippingCharges));
		/**FT needs to remove Print validation steps**/
		/*Assert.assertTrue(xls.navigateToPreviousPage("Quote Details page"));

    	Assert.assertTrue(quoteDetails.clickOnPrintButton("Step 11.1: Click on 'Print' option in quote confirmation page", "Print button should be clicked"));

		quoteDetails.elseWarningBlockweb("Step 11: Click on 'Print' option in quote confirmation page<br>"
				+ "Expected Result: Print preview page with quote details should be displayed", 
				"We cannot verify with Selenium, not able to inspect print window with AutoIT as well", "Quote Details");

		quoteDetails.elseWarningBlockweb("Step 12: Verify Shipping & Handling charges under cart summary section in printed quote", 
				"We cannot verify with Selenium, not able to inspect print window with AutoIT as well", "Quote Details");*/

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify user defaults Billing address and Shipping address
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/359230
	 * @since Apr 19, 2021 9:09:47 AM
	 * @author  Vijay
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C359230_Regression_CE_Quotes_VerifyUserDefaulltsBillingAddressAndShippingAddress_CSR(String region){

		// Reporting info
		initializeReporting("Quote_CBN_Verify user defaults Billing address and Shipping address",
				"C359230_Regression_CE_Quotes_VerifyUserDefaulltsBillingAddressAndShippingAddress_CSR", region, logger);

		// Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID06", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, ACCESSORIES).get(0);;
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability		
		String user = getUser(CSR);
		Assert.assertNotNull(user);
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		ArrayList<String> userSet= new ArrayList<>();
		userSet.add(user);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		String quoteName = "Aut_Quote_";
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PDP pdp=new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		/** Pre-Condition ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 2: Search with BTO SKU", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(login.addProductToCart("Step 3: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button",
				"User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

//		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 6: Select payment method from dropdown", 
//				"Payment should be selected", "Credit Card"));

		String selectedNewBillingAddress = createNewQuote.clickOnChangeBillingAddressAndSelectNewBillingAddressAndClickOnOk("Step 7 & 8 : Click on 'Change Billing Address And select the address that needs to be defaulted and click on ok",
				"Billing address popup should be displayed & Billing Address section is loaded with the selected address", false);
		Assert.assertNotEquals(selectedNewBillingAddress, null);

		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 9.1: Select 'Default Billing Address' checkbox",
				"Billing address is selected as default", "billing address"));

		Assert.assertTrue(createNewQuote.clickOnOkButtonInsideDefaultAddressConfirmationPopup("Step 9.2 : Click on ok button inside default address confirmation popup", "Ok button should be selected",false));

//		String selectedNewShipAddress=createNewQuote.clickOnChangeShippingAddressAndSelectNewShippingAddressAndClickOnOk("Step 10 & 11 : Click on 'Change Shipping Address And select the address that needs to be defaulted and click on ok",
//				"Shipping address popup should be displayed & Shipping Address section is loaded with the selected address", false);
//		Assert.assertNotEquals(selectedNewShipAddress, null);

		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 12: Select 'Default Shipping Address' checkbox",
				"Shipping address is selected as default", "shipping adddress"));

		quoteDetails = createNewQuote.createQuote("Step 13: Provide all details and click on save quote",
				"Quote should created successfully", quoteName, user);
		Assert.assertNotEquals(quoteDetails, null);

		pdp = customerService.searchSKU("Step 14: Search with BTO SKU", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 15 : Add BTO product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 16 : Click on Go to cart button inside Mini cart Popup",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		checkout = shoppingCart.clickOnCheckOut("Step 17: Click on 'Checkout' button",
				"User should  navigate to Checkout page");
		Assert.assertNotEquals(createNewQuote, null);

//		Assert.assertTrue(checkout.verifyPaymentMethodInCheckoutPage("Step 18: Verify the defaulted payment method is shown\r\n"
//				+ "Note : Payment method selected in step 6", "Payment method verified successfully", "Credit Card"));

		Assert.assertTrue(checkout.verifyShppingAndBillingAddressDetails("Step 19: Verify the defaulted Billing Address is shown\r\n"
				+ "Note: Billing address as selected in Step 7", "Default Billing address verified successfully", selectedNewBillingAddress, true));

		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsSelected("Step 20: Verify 'Default Billing Address' checkbox is selected",
				"'Default Billing Address' checkbox is selected", true));

//		Assert.assertTrue(checkout.verifyShppingAndBillingAddressDetails("Step 21: Verify the defaulted Shipping Address is shown\r\n"
//				+ "Note: Shipping address as selected in Step 9", "Default Shipping address verified successfully", selectedNewShipAddress, true));

		Assert.assertTrue(createNewQuote.verifyDefaultShippingAddressCheckboxIsSelected("Step 22: Verify 'Default Shipping Address' checkbox is selected",
				"'Default Shipping Address' checkbox is selected", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Quote_Verify Contract Surcharge in exported quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300042
	 * @since April 23, 2021
	 * @author ShishoSa
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300042_Regression_CE_Quotes_VerifyContractSurchargeInExportedQuote_Direct(String region) {

		//Reporting info
		initializeReporting("Quote_Verify Contract Surcharge in exported quote",
				"C300042_Regression_CE_Quotes_VerifyContractSurchargeInExportedQuote_Direct", region, logger);

		//Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID06, ID12, ID07, "");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String kit = getProductByDataSetID(region, dataIDs.get(region), KIT,DESKTOPS);
		String password = commonData.get(DIRECTUSERPWD);

		//Waiting for user availability
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		//Get URL
		setEnvironment();
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
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", storeCE, false));

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
	 * Quote_Verify Regulatory fee in mailed & exported quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300041
	 * @since May 7, 2020
	 * @author ShishoSa
	 * @throws IOException 
	 */
	@Test(dataProvider = "emeaAndAmsRegion_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300041_Regression_CE_Quotes_VerifyRegulatoryFeeInMailedAndExportedQuote_CSR(String region) throws IOException {

		// Reporting info
		initializeReporting("Quote_Verify Regulatory fee in mailed & exported quote", 
				"C300041_Regression_CE_Quotes_VerifyRegulatoryFeeInMailedAndExportedQuote_CSR", region, logger);

		// Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("", ID03, ID02, ID05);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = "";
		if(region.equals(EMEA))
			bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, MONITORS).get(1);
		else bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, DESKTOPS).get(2);
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability		
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotEquals(csr, "");
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		userSet.add(csr);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		GmailPage gmailPage = new GmailPage(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		XLSValidations xls = new XLSValidations(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > Enter CSR User Name > Enter Password > Click on Sign In", url, csr, password, true));
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Click Customer Service link> Enter MDCPID and Email Id > Click on Search > Click on Action Gear against search result and Select Impersonate User option",
				"User should be impersonated", data));
		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select Organization and Catalog",
				"Requested Organization & Catalog should be selected", data, true));
		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", storeCE, false));

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

		Assert.assertTrue(xls.verifyRegulatoryFeesInHtmlEmbeddedXls("Step 15-16: Go to the file location and Click on Quote to open<br>Verify regulatory fee is displayed in exported quote", 
				"Regulatory fee should be displayed and it should be included in total", quoteName, regulatoryFee.get(1)));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Logistical preference options in create quote page ,when user select ship Partial
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/294888
	 * @since May 10, 2021 11:59:23 AM
	 * @author Keshav
	 * @throws IOException 
	 */
	
	
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,IGroupsTagging.IUserType.DIRECT,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C294888_Regression_CE_Quotes_S4_VerifyTheDetailedLogisticalServicesInQuoteExportPDF_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify the Detailed Logistical Services in Quote Export - PDF",
				"C294888_Regression_CE_Quotes_S4_VerifyTheDetailedLogisticalServicesInQuoteExportPDF_Direct",
				logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = new LinkedHashMap<>();
		dataIDs.put("EMEA", "ID03");
		Map<String, String> regData = getScenarioData(dataIDs,"EMEA");
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		//String bto=getProductsByProductType(getRegion("EMEA", "ID03"),"BTO").get(1);
		String bto = getProduct(EMEA, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(DIRECTUSERPWD);        
		String quoteName = "Aut_Quote_";

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser(dataIDs.get("EMEA"), PURCHASER);
		Assert.assertNotNull(purchaser);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

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
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		pdp = customerService.searchSKU("PreCondition: Search with BTO SKU", "Requested product PDP should load",bto, false, false);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Navigate to shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		/** Pre-Condition ends **/

		AccountSummary accSummary = customerService.navigateToMyAccount("Step 1: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount(
				"Step 2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1 :Search quote which is created by direct user", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 3.2: Click on any 'Quote number'", 
				"Quote details is displayed", quoteNumber));

		String contractId = quoteDetails.getContractId("Step 3.3: Verify the S4 Contractid in 'Quote detail page'", "S4 Contractid should be displayed");
		Assert.assertNotNull(contractId);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargesValue("Step 3.4: Verify Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched and not equal to 0.00", false));

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Step 3.5: Get Shipping & Hnadling Charges Value", "Shipping & Hnadling Charges should be fetched", false);
		Assert.assertNotEquals(shippingCharges.replaceAll("[a-zA-Z]", "").trim(),"0.00");

		List<String> skuIds = Arrays.asList("W9G34AA","V1H71AA");
		Map<String,List<String>> lstAddedItems = quoteDetails.getDetailedLogisticalData("Step 3.5: Getting Logistical Items", "Fetched the logistical items"
				,"PDF",skuIds);
		Assert.assertNotNull(lstAddedItems);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 3.6: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "QuoteDetails"));


		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 4.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 4.2: Select requested file type as pdf. ",
				"Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 4.3: Click on export button to export file.", "Quote should be exported successfully in PDF format"));


		String pdfContent = pdfPage.readPdfFileInDownloads("Step 5.1: Read PDF file", "Pdf file content is read", newQuote+".pdf ", true);

		lstAddedItems.put("headers", Arrays.asList(HP2BStaticData.logisticalHeadersPDF));
		lstAddedItems.put("Service Description", Arrays.asList(commonData.get("ServiceDescription")));
		lstAddedItems.put("Shipping Charges", Arrays.asList(shippingCharges));
		lstAddedItems.put("Contract id", Arrays.asList(contractId));
		Assert.assertTrue(pdfPage.verifyContentInPDF("Step 5.2: Verify the detailed Logistical view in exported Quote PDF<br>"
				+ "Step 6: Verify the Shipping & handling charges in Quote PD<br>"
				+ "Step 7: Verify the S4 Contractid in 'Quote PDF Export'"
				,"Detailed Logistical view should be displayed in Quote PDF as below:<br>" + 
						"Detailed Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description"
						+ "<br>Items<Logistical sku><br>Product description<br>Qty<br>Unit price<br>Total<br>"
						+ "Shipping & handling charges should be sum of Logistical sku charges<r>"
						+ "S4 Contractid should be displayed", pdfContent, lstAddedItems));


		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	
	/**
	 * Verify existing quote and validate the billing and shipping addresses for which the default was not set by the user
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/359277
	 * @param region EMEA, APJ, AMS-US, AMS-LA
	 * @throws IOException -
	 * @since May 10, 2021 9:45:15 PM
	 * @author Rashi
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class,groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C359277_Regression_CE_Quotes_S4VerifyExistingQuoteAndValidateBillingAndShippingAddressesForWhichDefaultWasNotSetByUser_Direct(String region) throws IOException {

		//Reporting info
		initializeReporting("Verify existing quote and validate the billing and shipping addresses for which the default was not set by the user",
				"C359277_Regression_CE_Quotes_S4VerifyExistingQuoteAndValidateBillingAndShippingAddressesForWhichDefaultWasNotSetByUser_Direct",region, logger);

		//Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01,ID03,ID02,ID05);
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);

		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);		
		Assert.assertNotNull(bto);

		String quoteName = "Automation_Quote_";
		String poNumber ="1234";
		String phoneNumber="1232535";
		String attentionText="TEST";

		/*
		 * HashMap<String, String> csrAndPassword = getTestData("CE", module);
		 * Assert.assertNotEquals(csrAndPassword.size(), 0);
		 */
		String password =passwords.get(DIRECTUSERPWD);

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("poNumber", poNumber);
		data.put("phoneNumber", phoneNumber);
		data.put("attentionText", attentionText);
		data.put("paymentMethod", "Purchase Order");

		//Waiting for user availability
		String user = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(user, null);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(user));

		//Get URL
		setEnvironment();
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

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Direct user", url, user, password, true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition : Select requested catalog", "Requested catalog should be selected", data,true));

		pdp = customerService.searchSKU("Precondition: Search with BTO SKU", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Precondition: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("PreCondition : Click on Go to cart button inside Mini cart Popup",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition : Click on save quote link", 
				"User should be navigated to create quote page");
		Assert.assertNotEquals(createNewQuote, null);

		//Assert.assertTrue(createNewQuote.deselectDefaultBillingAndShippingAddressCheckbox("PreCondition : De-Select default Billing address checkbox", "Billing and shipping address checkbox Should not be selected","billing and shipping address",false));

		LinkedHashMap<String, String> billingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"createNewQuote","billing");
		Assert.assertNotEquals(billingInfoInCreateNewQuotePage, null);

		LinkedHashMap<String, String>shippingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"createNewQuote","shipping");
		Assert.assertNotEquals(shippingInfoInCreateNewQuotePage, null);

		quoteDetails = createNewQuote.createQuote("PreCondition : Fill in other details and click on Save quote button","Quote should be created successfully & Quote detail page appears", quoteName, user);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details","Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		LinkedHashMap<String, String> billingInfoInQuoteDetailsPage= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"quoteDetails","billing");
		Assert.assertNotEquals(billingInfoInQuoteDetailsPage, null);

		LinkedHashMap<String, String>shippingInfoInQuoteDetailsPage= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"quoteDetails","shipping");
		Assert.assertNotEquals(shippingInfoInQuoteDetailsPage, null);

		logger.info("<b>Pre-condition completed. Continuing From Step 4.<br>Skipping Step 1,2 and 3 as we are already on Quote Details Page");
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-condition completed. Continuing From Step 4.", "<b>Skipping Step 1,2 and 3 as we are already on Quote Details Page</b>");

		/** Pre-Condition ends **/

		Assert.assertTrue(createNewQuote.compareTwoHashMap(
				"Step 4.1 :Ensure that previously selected 'Default Billing Address' & 'Default Shipping Address' must be displayed",
				"User must be able to see the address with which it was created", billingInfoInCreateNewQuotePage,
				billingInfoInQuoteDetailsPage));
		Assert.assertTrue(createNewQuote.compareTwoHashMap(
				"Step 4.2:Ensure that previously selected 'Default Billing Address' & 'Default Shipping Address' must be displayed",
				"User must be able to see the address with which it was created", shippingInfoInCreateNewQuotePage,
				shippingInfoInQuoteDetailsPage));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 5.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 5.2: Select requested file type as pdf. ",
				"Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 5.3: Click on export button to export file.",
				"PDF should get exported successfully"));

		String pdfValue = pdfValidations.readPdfFileInDownloads("Getting PDF content", "PDF content should be fetched",
				quoteName + quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyBillingAddressDetails("Step 6.1: Open PDF",
				"User must be able to see the billing with which it was created", pdfValue,
				billingInfoInQuoteDetailsPage));

		checkout = quoteDetails.navigateToCheckout("Step 7: Navigate to checkout page",
				"User should navigate to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(checkout.verifyPaymentMethodInCheckoutPage("Step 8: verify payment method in checkout page",
				"PO method must be displayed as default payment", "Purchase Order"));

		LinkedHashMap<String, String> billingInfoInCheckoutPage = createNewQuote.fetchAddress(
				"PreCondition : Fetch Billing address in checkout page", "Billing  address Should be fetched", true,
				"checkout", "billing");
		Assert.assertNotEquals(billingInfoInCheckoutPage, null);

		LinkedHashMap<String, String> shippingInfoInCheckoutPage = createNewQuote.fetchAddress(
				"PreCondition : Fetch Shipping address in checkout page", "Shipping address Should be fetched", true,
				"checkout", "shipping");
		Assert.assertNotEquals(shippingInfoInCheckoutPage, null);

		Assert.assertTrue(
				checkout.compareTwoHashMap("Step 9.1 :Ensure that user selected billing addresses are displayed",
						"User must be able to see the billing address with which it was created",
						billingInfoInQuoteDetailsPage, billingInfoInCheckoutPage));
		Assert.assertTrue(
				checkout.compareTwoHashMap("Step 9.2:Ensure that user selected shipping addresses are displayed",
						"User must be able to see the shipping address with which it was created",
						shippingInfoInQuoteDetailsPage, shippingInfoInCheckoutPage));

		Assert.assertTrue(createNewQuote.verifyDefaultBillingAndShippingAddressCheckboxAreNotChecked(
				"Step 10 : Ensure that default address checkbox is not enabled for the non default addresses with which quote was created",
				"Default address checkbox MUST NOT BE enabled for the non default addresses with which quote was created",
				"billing and shipping address", false));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 11.1:Enter all the Mandatory fields",
				"User should enter all the values successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder(
				"Step 11.2: Click on Create purchase Order button to place an Order",
				"PO confirmation page should be displayed.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify Logistical preference options in create quote page ,when user select ship Partial
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/295227
	 * @since May 10, 2021 11:59:23 AM
	 * @author Keshav
	 * @throws IOException 
	 */
	
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
			IGroupsTagging.IUserType.DIRECT,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C295227_Regression_CE_Quotes_S4_VerifyTheDetailedLogisticalServicesInQuoteExportXLS_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify the Detailed Logistical Services in Quote Export - XLS",
				"C295227_Regression_CE_Quotes_S4_VerifyTheDetailedLogisticalServicesInQuoteExportXLS_Direct",
				logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = new LinkedHashMap<>();
		dataIDs.put("EMEA", "ID03");
		Map<String, String> regData = getScenarioData(dataIDs,"EMEA");
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		//String bto=getProductsByProductType(getRegion("EMEA", "ID03"),"BTO").get(1);
		String bto = getProduct(EMEA, BTO);
		Assert.assertNotNull(bto);
		String wsl=getProductsByProductTypeAndCategory(getRegion("EMEA", "ID03"),"BTO","WsL").get(3);
		String password = passwords.get(DIRECTUSERPWD);       
		System.out.println(password);
		String quoteName = "Aut_Quote_";

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser("ID03", PURCHASER);
		Assert.assertNotNull(purchaser);
		System.out.println(purchaser);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

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
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		XLSValidations xlsPage = new XLSValidations(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		pdp = customerService.searchSKU("PreCondition: Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Navigate to shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		/** Pre-Condition ends **/


		AccountSummary accSummary = customerService.navigateToMyAccount("Step 1: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount(
				"Step 2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1 :Search quote which is created by direct user", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 3.2: Click on any 'Quote number'", 
				"Quote details is displayed", quoteNumber));

		String contractId = quoteDetails.getContractId("Step 3.3: Verify the S4 Contractid in 'Quote detail page'", "S4 Contractid should be displayed");
		Assert.assertNotNull(contractId);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargesValue("Step 3.4: Verify Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched and not equal to 0.00", false));

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Step 3.5: Get Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched", false);
		Assert.assertNotEquals(shippingCharges.replaceAll("[a-zA-Z]", "").trim(),"0.00");
		System.out.println(shippingCharges);

		List<String> skuIds = Arrays.asList(wsl);
		Map<String,List<String>> lstLogisticalData = quoteDetails.getDetailedLogisticalData("Step 3.5: Getting Logistical Items", "Fetched the logistical items"
				,"XLS",skuIds);
		Assert.assertNotNull(lstLogisticalData);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 3.6: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "QuoteDetails"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 4.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 4.2: Select requested file type as xls. ",
				"Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 4.3: Click on export button to export file.", "Quote should be exported successfully in xls format"));


		lstLogisticalData.put("headers", Arrays.asList(HP2BStaticData.logisticalHeadersXLS));
		lstLogisticalData.put("Service Description", Arrays.asList(commonData.get("ServiceDescription")));
		lstLogisticalData.put("Shipping Charges", Arrays.asList(shippingCharges));
		List<String> values = xlsPage.getDataInList(lstLogisticalData);
		Assert.assertTrue(xlsPage.verifyXlsFile("Step 5.2: Verify the detailed Logistical view in exported Quote XLS<br>"
				+ "Step 6: Verify the Shipping & handling charges in Quote XLS<br>"
				+ "Step 7: Verify the S4 Contractid in 'Quote XLS Export'"
				,"Detailed Logistical view should be displayed in Quote XLS as below:<br>" + 
						"Detailed Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description"
						+ "<br>Items<Logistical sku><br>Product description<br>Qty<br>Unit price<br>Total<br>"
						+ "Shipping & handling charges should be sum of Logistical sku charges<r>"
						+ "S4 Contractid should be displayed", newQuote, values));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}


	/**
	 * Verify the Detailed Logistical Services in Quote Export - CSV
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/299901
	 * @since May 10, 2021 11:59:23 AM
	 * @author Keshav
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
			IGroupsTagging.IUserType.DIRECT,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C299901_Regression_CE_Quotes_S4_VerifyTheDetailedLogisticalServicesInQuoteExportCSV_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify the Detailed Logistical Services in Quote Export - CSV",
				"C299901_Regression_CE_Quotes_S4_VerifyTheDetailedLogisticalServicesInQuoteExportCSV_Direct",
				logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = new LinkedHashMap<>();
		dataIDs.put("EMEA", "ID03");
		Map<String, String> regData = getScenarioData(dataIDs,"EMEA");
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		//String bto=getProductsByProductType(getRegion("EMEA", "ID03"),"BTO").get(1);
		String bto = getProduct(EMEA, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		System.out.println(password);
		String quoteName = "Aut_Quote_";

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser(dataIDs.get("EMEA"), PURCHASER);
		Assert.assertNotNull(purchaser);
		System.out.println(purchaser);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

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
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		CSVValidations csvPage = new CSVValidations(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		//		Assert.assertTrue(customerService.impersonate(data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("PreCondition: Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Navigate to shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		/** Pre-Condition ends **/


		AccountSummary accSummary = customerService.navigateToMyAccount("Step 1: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		//		quoteListing = accSummary.clickOnQuotesUnderMyAccountSection("Step 2: Click on 'Quotes' tab on LHS", 
		//				"Quote list page should display with list of existing quotes with gear icon");
		//		Assert.assertNotNull(quoteListing);

		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount(
				"Step 2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1 :Search quote which is created by direct user", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 3.2: Click on any 'Quote number'", 
				"Quote details is displayed", quoteNumber));

		String contractId = quoteDetails.getContractId("Step 3.3: Verify the S4 Contractid in 'Quote detail page'", "S4 Contractid should be displayed");
		Assert.assertNotNull(contractId);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargesValue("Step 3.4: Verify Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched and not equal to 0.00", false));

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Step 3.5: Get Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched", false);
		Assert.assertNotEquals(shippingCharges.replaceAll("[a-zA-Z]", "").trim(),"0.00");
		System.out.println(shippingCharges);


		List<String> skuIds = Arrays.asList("W9G34AA","V1H71AA");
		Map<String,List<String>> lstLogisticalData = quoteDetails.getDetailedLogisticalData("Step 3.5: Getting Logistical Items", "Fetched the logistical items"
				,"CSV",skuIds);
		Assert.assertNotNull(lstLogisticalData);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 3.6: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "QuoteDetails"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 4.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 4.2: Select requested file type as csv. ",
				"Requested file type should be selected", "csv"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 4.3: Click on export button to export file.", "Quote should be exported successfully in csv format"));


		lstLogisticalData.put("headers", Arrays.asList(HP2BStaticData.logisticalHeadersXLS));
		lstLogisticalData.put("Service Description", Arrays.asList(commonData.get("ServiceDescription")));
		List<String> values = csvPage.getDataInList(lstLogisticalData);

		Assert.assertTrue(csvPage.verifyCSV("Step 5.2: Verify the detailed Logistical view in exported Quote CSV<br>"
				+ "Step 6: Verify the Shipping & handling charges in Quote CSV<br>"
				+ "Step 7: Verify the S4 Contractid in 'Quote CSV Export'"
				,"Detailed Logistical view should be displayed in Quote CSV as below:<br>" + 
						"Detailed Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description"
						+ "<br>Items<Logistical sku><br>Product description<br>Qty<br>Unit price<br>Total<br>"
						+ "Shipping & handling charges should be sum of Logistical sku charges<r>"
						+ "S4 Contractid should be displayed", newQuote, values, true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_CBN_Verify the Logistical Services in Shared Quote
	 * @TestCaseLink https:https://hpitdce.testrail.net/index.php?/cases/view/295277
	 * @since May 10, 2021 11:59:23 AM
	 * @author Vijay
	 * @throws IOException 
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
	//		IGroupsTagging.IUserType.CSR,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C295277_Regression_CE_Quotes_CBNVerifyTheLogsticalServicesInSharedQuote_CSR() throws IOException{

		// Reporting info
		initializeReporting("Verify the Logistical Services in Shared Quote",
				"C295277_Regression_CE_Quotes_CBNVerifyTheLogsticalServicesInSharedQuote_CSR", logger);

		// Test data
		String scenarioId = "ID800";
		Map<String, String> regData = scenarioData.get(scenarioId);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpId = regData.get("MDCP ID");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String quoteName = "Aut_Quote_";
		//String bto = getProductsByProductTypeAndCategory(EMEA, BTO, MONITORS).get(1);
		String bto = getProduct(EMEA, BTO);
		Assert.assertNotNull(bto);

		// Waiting for user availability
		String user = getUser(CSR);
		Assert.assertNotNull(user);;
		String purchaser1 = getUser(scenarioId, PURCHASER);
		Assert.assertNotNull(purchaser1);
		String purchaser2 = getUser(scenarioId, PURCHASER);
		Assert.assertNotNull(purchaser2);
		ArrayList<String> users = new ArrayList<String>();
		users.add(user);
		users.add(purchaser1);
		users.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpId);
		data.put("actionOnUsers", actionOnUsers);
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("emailID", purchaser1);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accSummary = new AccountSummary(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition: Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		Assert.assertNotEquals(customerService.searchSKU("PreCondition: Search with BTO SKU", 
				"Requested product PDP should load", bto), null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add bto product to cart at PDP", "Product should be added to cart","pdp"));

		Assert.assertNotEquals(pdp.navigateToShoppingCartThroughHeader("PreCondition: Click on mini cart icon and click on Go to cart button", 
				"User should navigate to shopping cart page"), null);

		Assert.assertNotEquals(shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", 
				"User should  navigate to quote creation page"), null);

		Assert.assertNotEquals(createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully",quoteName, purchaser1), null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");

		/** Pre-Condition Ends **/

		Assert.assertNotNull(customerService.navigateToMyAccount("Step 1: Click on 'My accounts' in home page", 
				"My accounts page should be displayed"));

		Assert.assertNotNull(accSummary.clickOnQuotesUnderMyAccountSection("Step 2: Click on 'Quotes' under Orders and Quotes", 
				"Quote listing page should be displayed"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1: Search quote which is created", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 3.2: Click on Action gear icon", "Clicked on Action gear",
				"Share quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 3.3: Click on 'Share Quote'", "Share quote pop up with login id text field and with Cancel & Share quote buttons should be displayed",
				"Share quote"));

		Assert.assertTrue(quoteListing.enterEmailidAndVerifySharequoteMessage("Step 4: Enter valid login details in 'Login id' field and click on 'Share quote' button",
				"Quote should be successfully shared to provided login in", purchaser2));

		Assert.assertTrue(login.clickOnCustomerServiceLink("Step 5.1: Click on customer service link in home page"
				, "Clicked on Customer Service link."));

		usersAvailability.replace(purchaser1, "Free");
		users.remove(purchaser1);

		data.put("emailID", purchaser2);
		System.out.println("Impersonate user 2: "+purchaser2);
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Step 5.2: Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 5.3: Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertNotNull(customerService.navigateToMyAccount("Step 5.4: Click on 'My accounts' in home page", 
				"My accounts page should be displayed"));

		Assert.assertNotNull(accSummary.clickOnQuotesUnderMyAccountSection("Step 5.5: Click on 'Quotes' under Orders and Quotes", 
				"Quote list page should display with list of existing quotes with gear icon"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 6.1: Search shared quote", "Shared Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuote("Step 6.2 : Click on searched quote", "Should be clicked on searched quote", quoteNumber));

		LinkedHashMap<String,List<String>> logisticSkuDetails = quoteDetails.verifyLogisticalServicesDetails("Step 7: Verify Items,Logistical sku,Product description,Qty,Unit price and Total", 
				"Items,Logistical sku,Product description,Qty,Unit price and Total should display", "QUOTEDETAILS");
		Assert.assertNotEquals(logisticSkuDetails, null);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 8: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "QUOTEDETAILS"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify the Summarized Logistical Services in Quote Export - XLS
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/294812
	 * @since May 12, 2021 11:59:23 AM
	 * @author Keshav
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
			IGroupsTagging.IUserType.CSR,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C294812_Regression_CE_Quotes_S4_VerifyTheSummarizedLogisticalServicesInQuoteExportXLS_CSR() throws IOException{

		// Reporting info
		initializeReporting("Verify the Summarized Logistical Services in Quote Export - XLS",
				"C294812_Regression_CE_Quotes_S4_VerifyTheSummarizedLogisticalServicesInQuoteExportXLS_CSR",
				logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = new LinkedHashMap<>();
		dataIDs.put("EMEA", "ID03");
		Map<String, String> regData = getScenarioData(dataIDs,"EMEA");
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		//String bto=getProductsByProductType(getRegion("EMEA", "ID03"),"BTO").get(1);
		String bto = getProduct(EMEA, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		System.out.println(password);
		String quoteName = "Aut_Quote_";
		String mdcpId = regData.get("MDCP ID");

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("MDCPID", mdcpId);


		//Waiting for user availability
		String purchaser = getUser(dataIDs.get("EMEA"), PURCHASER);
		Assert.assertNotNull(purchaser);
		System.out.println(purchaser);
		String CSR = getUser("CSR");
		System.out.println(CSR);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		data.put("emailID", purchaser);

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
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		XLSValidations xlsPage = new XLSValidations(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with CSR user", url, CSR, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("PreCondition: Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Navigate to shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		/** Pre-Condition ends **/

		AccountSummary accSummary = customerService.navigateToMyAccount("Step 1: Click on My accounts", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		//		quoteListing = accSummary.clickOnQuotesUnderMyAccountSection("Step 2: Click on 'Quotes' tab on LHS", 
		//				"Quote list page should displayed");
		//		Assert.assertNotNull(quoteListing);
		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount(
				"Step 2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1 :Search quote which is created by direct user", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 3.2: Click on any 'Quote number'", 
				"Quote details is displayed", quoteNumber));

		String contractId = quoteDetails.getContractId("Step 3.3: Verify the S4 Contractid in 'Quote detail page'", "S4 Contractid should be displayed");
		Assert.assertNotNull(contractId);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargesValue("Step 3.4: Verify Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched and not equal to 0.00", false));

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Step 3.5: Get Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched", false);
		Assert.assertNotEquals(shippingCharges.replaceAll("[a-zA-Z]", "").trim(),"0.00");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 4.1 : Click on 'Export catalog' button","Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 4.2: Select requested file type as xls. ",
				"Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 4.3: Click on export button to export file.", "Quote should be exported successfully in xls format"));

		List<String> values = new ArrayList<String>();
		values.add("Logistical Services Profile");
		values.add(serviceDescription);

		Assert.assertTrue(xlsPage.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching(
				"Step 5: Open xls file", "HTML Embedded xls file should be opened", newQuote + ".xls"));

		Assert.assertTrue(xlsPage.verifyLogisticalServicesSummarizedViewInEmbeddedHTML("Step 5.2: Verify the Summarized Logistical view in Quote XLS",
				"Summarized Logistical view should be displayed as below in Quote XLS:<br>Logistical Services Profile:<br>Service description"));

		Assert.assertTrue(xlsPage.verifyShippingAndHandlingChargesInHtmlEmbeddedXls("Step 6: Verify the Shipping & handling charges",
				"Shipping & Handling charges should be displayed and should be included to total", newQuote, shippingCharges));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 7: Verify the S4 Contractid in 'Quote XLS Export'"
				, "S4 Contractid should be displayed", newQuote, "OM ID", "", contractId));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify the Summarized Logistical Services in Quote Export - CSV
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/299875
	 * @since May 12, 2021 11:59:23 AM
	 * @author Keshav
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
			IGroupsTagging.IUserType.CSR,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C299875_Regression_CE_Quotes_S4_VerifyTheSummarizedLogisticalServicesInQuoteExportCSV_CSR() throws IOException{

		// Reporting info
		initializeReporting("Verify the Summarized Logistical Services in Quote ExportCSV",
				"C299875_Regression_CE_Quotes_S4_VerifyTheSummarizedLogisticalServicesInQuoteExportCSV_CSR",
				logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = new LinkedHashMap<>();
		dataIDs.put("EMEA", "ID03");
		Map<String, String> regData = getScenarioData(dataIDs,"EMEA");
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		//String bto=getProductsByProductType(getRegion("EMEA", "ID03"),"BTO").get(1);
		String bto = getProduct(EMEA, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		System.out.println(password);
		String quoteName = "Aut_Quote_";
		String mdcpId = regData.get("MDCP ID");
		

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("MDCPID", mdcpId);


		//Waiting for user availability
		String purchaser = getUser("ID03", PURCHASER);
		Assert.assertNotNull(purchaser);
		System.out.println(purchaser);
		String CSR = getUser("CSR");
		Assert.assertNotNull(CSR);
		System.out.println(CSR);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		data.put("emailID", purchaser);

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
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		CSVValidations csvPage = new CSVValidations(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with CSR user", url, CSR, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));


		pdp = customerService.searchSKU("PreCondition: Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Navigate to shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		/** Pre-Condition ends **/


		AccountSummary accSummary = customerService.navigateToMyAccount("Step 1: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);

		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount(
				"Step 2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1 :Search quote which is created by direct user", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 3.2: Click on any 'Quote number'", 
				"Quote details is displayed", quoteNumber));

		String contractId = quoteDetails.getContractId("Step 3.3: Verify the S4 Contractid in 'Quote detail page'", "S4 Contractid should be displayed");
		Assert.assertNotNull(contractId);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargesValue("Step 3.4: Verify Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched and not equal to 0.00", false));

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Step 3.5: Get Shipping & Handling Charges Value", "Shipping & Handling Charges should be fetched", false);
		Assert.assertNotEquals(shippingCharges.replaceAll("[a-zA-Z]", "").trim(),"0.00");
		System.out.println(shippingCharges);

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 4.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 4.2: Select requested file type as csv. ",
				"Requested file type should be selected", "csv"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 4.3: Click on export button to export file.", "Quote should be exported successfully in csv format"));

		List<String> values = new ArrayList<String>();
		values.add("Logistical Services Profile");
		values.add(serviceDescription);

		Assert.assertTrue(csvPage.verifyCSV("Step 5: Verify the Summarized Logistical view in Quote CSV<br>"
				,"Detailed Logistical view should be displayed in Quote CSV as below:<br>" + 
						"Summarized Logistical view should be displayed as below in Quote CSV:<br>"
						+ "Logistical Services Profile:<br>Service description<br>", newQuote, values, true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify the Summarized Logistical Services in Quote Export - CSV
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/299875
	 * @since May 12, 2021 11:59:23 AM
	 * @author Keshav
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
			IGroupsTagging.IUserType.CSR,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C295230_Regression_CE_Quotes_S4_VerifyTheSummarizedLogisticalServicesInQuoteExportPDF_CSR() throws IOException{

		// Reporting info
		initializeReporting("Verify the Summarized Logistical Services in Quote ExportCSV",
				"C295230_Regression_CE_Quotes_S4_VerifyTheSummarizedLogisticalServicesInQuoteExportPDF_CSR",
				logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = new LinkedHashMap<>();
		dataIDs.put("EMEA", "ID03");
		Map<String, String> regData = getScenarioData(dataIDs,"EMEA");
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String bto = getProduct(EMEA, BTO);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		System.out.println(password);
		String quoteName = "Aut_Quote_";
		String mdcpId = regData.get("MDCP ID");
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("MDCPID", mdcpId);


		//Waiting for user availability
		String purchaser = getUser("ID03", PURCHASER);
		Assert.assertNotNull(purchaser);
		System.out.println(purchaser);
		String CSR = getUser("CSR");
		Assert.assertNotNull(CSR);
		System.out.println(CSR);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser);
		users.add(CSR);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		data.put("emailID", purchaser);

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
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with CSR user", url, CSR, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("PreCondition: Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Navigate to shopping cart page", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		/** Pre-Condition ends **/


		AccountSummary accSummary = customerService.navigateToMyAccount("Step 1: Click on 'My accounts' in home page", "My accounts page should be displayed");
		Assert.assertNotNull(accSummary);
		
		Assert.assertTrue(accSummary.clickAndNavigateToLinksUnderMyAccount("Step 2: Mouseover on 'My account' icon and  Click on 'Quotes' link", 
				"Quotes listing page should display", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1 :Search quote which is created by direct user", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 3.2: Click on any 'Quote number'", 
				"Quote details is displayed", quoteNumber));

		String contractId = quoteDetails.getContractId("Step 3.3: Verify the S4 Contractid in 'Quote detail page'", "S4 Contractid should be displayed");
		Assert.assertNotNull(contractId);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargesValue("Step 3.4: Verify Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched and not equal to 0.00", false));

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Step 3.5: Get Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched", false);
		Assert.assertNotEquals(shippingCharges.replaceAll("[a-zA-Z]", "").trim(),"0.00");
		System.out.println(shippingCharges.replaceAll("[a-zA-Z]", "").trim());

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 4.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 4.2: Select requested file type as pdf. ",
				"Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 4.3: Click on export button to export file.", "Quote should be exported successfully in pdf format"));

		Map<String,List<String>> lstAddedItems = new HashMap<>();
		lstAddedItems.put("Service Description", Arrays.asList(commonData.get("ServiceDescription").substring(0,21)));
		lstAddedItems.put("Shipping charge", Arrays.asList(shippingCharges));
		lstAddedItems.put("Contract id", Arrays.asList(contractId));

		String pdfContent = pdfPage.readPdfFileInDownloads("Step 5.1: Read PDF file", "Pdf file content is read", newQuote+".pdf ", true);

		Assert.assertTrue(pdfPage.verifyContentInPDF("Step 5.2: Verify the Summarized Logistical view in Quote PDF<br>"
				+ "Step 6: Verify the Shipping & handling charges<br>"
				+ "Step 7: Verify the S4 Contractid in 'Quote PDF Export'"
				,"Detailed Logistical view should be displayed in Quote PDF as below:<br>" + 
						"Summarized Logistical view should be displayed as below in Quote PDF:"
						+ "Logistical Services Profile:<br>Service description" 
						+ "Shipping & handling charges should be sum of Logistical sku charges<r>"
						+ "S4 Contractid should be displayed", pdfContent, lstAddedItems));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_CBN_Verify that Summarized Logistical view display Service description, SKU and Price in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/295229
	 * @since May 12, 2021 11:59:23 AM
	 * @author ShishoSa
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT, 
	//		IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	@Test
	public void C295229_Regression_CE_Quotes_VerifySummarizedLogisticalViewDisplayServiceDescriptionSKUAndPriceInQuoteCreationAndQuoteConfirmationPage_Direct(){

		//Reporting info
		initializeReporting("Quote_CBN_Verify that Summarized Logistical view display Service description, SKU and Price in Quote creation and Quote confirmation page",
				"C295229_Regression_CE_Quotes_VerifySummarizedLogisticalViewDisplayServiceDescriptionSKUAndPriceInQuoteCreationAndQuoteConfirmationPage_Direct", logger);

		//Test data
		String scenarioId = "ID800";
		Map<String, String> regData = scenarioData.get(scenarioId);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bundle = getProductsByProductTypeAndCategory(EMEA, BUNDLE, MONITORS).get(0);
		String softBundle = getProductsByProductTypeAndCategory(EMEA, SOFTBUNDLE, MONITORS).get(0);
		String password = passwords.get(DIRECTUSERPWD);

		//Waiting for user availability
		String purchaser = getUser(scenarioId, PURCHASER);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//		HashMap<String, String> regData = getTestData("CE", "DT006_EMEA");

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		String quoteName = "AutQuote";
		String paymentMethod = "Purchase Order";
		String shippingOption = "Default";
		String serviceDesc = "Default;Inside / desk delivery;Consolidated delivery;Unpacking and waste removal;HP std: mixed Euro and/or Industry pallet;At 1.00 pm;Delivery advice prior to delivery;Access constraints, special truck size is required;Fixed Delivery Date";

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		PDP pdp = new PDP(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with direct user", url, purchaser, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract(
				"Step 1-2: Click on Organization and Catalog dropdown<br>Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay", 
				"Requested catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete products", "Products should be deleted", storeCE, false));

		Assert.assertNotNull(login.searchSKU("Step 3: In search box, search for Bundle sku", "PDP of searched product should be displayed", bundle));

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 4: Enter Qty as 3 and update", "Qty should be updated successfully", "3", "pdp"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Click on 'Add to cart' button", "Product should be added to cart", "pdp"));

		Assert.assertNotNull(pdp.searchSKU("Step 6: In search box, search for Soft Bundle sku", "PDP of searched product should be displayed", softBundle));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 7: Enter Qty as 2 and update", "Qty should be updated successfully", "2"));

		Assert.assertTrue(pdp.addProductToCart("Step 8: Click on 'Add to cart' button", "Product should be added to cart", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Step 9: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 10: Click on 'save as quote' button", "'Quote creation page' should be displayed"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 11: Select Payment method as 'Purchase Order'", 
				"Payment method should be selected", paymentMethod));

		Assert.assertTrue(createNewQuote.selectPreconfiguredProfileFromDropDown("Step 12: Select below Shipping Option(s)<br>"
				+ "1. preconfigured profile as 'Custom'", "Option should be selected accordingly", shippingOption));

		Assert.assertTrue(createNewQuote.verifyElementIsDisplayedByText(
				"Step 13: Verify that Logistical Services section is displayed between cart summary and Total price",
				"Logistical Services section should be displayed between cart summary and Total price", "Logistical Services", true));

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Step 14: Verify the Summarized Logistical view in Quote creation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(createNewQuote.verifyShippingChargesInSummarizedLogisticalView("Step 15: Verify the Shipping & handling charges", 
				"Shipping & handling charges should not be 0", true));

		Assert.assertNotNull(createNewQuote.createQuote("Step 16: Enter all mandatory details and Click on 'Save quote' button",
				"Quote should be created successfully", quoteName, purchaser));

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Step 17: Verify the Summarized Logistical view in Quote confirmation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(createNewQuote.verifyShippingChargesInSummarizedLogisticalView("Step 18: Verify the Shipping & handling charges", 
				"Shipping & handling charges should not be 0", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_CBN_Verify Logistical view and Charges are displayed when Shipping flag set to 'Custom enable flag custom' and Hide 'No'
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/294889
	 * @since May 12, 2021 11:59:23 AM
	 * @author ShishoSa
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, 
	//		IGroupsTagging.IUserType.DIRECT, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	@Test
	public void C294889_Regression_CE_Quotes_VerifyLogisticalViewAndChargesAreDisplayedWhenShippingFlagSetToCustomEnableFlagCustomAndHideNo_Direct(){

		//Reporting info
		initializeReporting("Quote_CBN_Verify Logistical view and Charges are displayed when Shipping flag set to 'Custom enable flag custom' and Hide 'No'",
				"C294889_Regression_CE_Quotes_VerifyLogisticalViewAndChargesAreDisplayedWhenShippingFlagSetToCustomEnableFlagCustomAndHideNo_Direct", logger);

		//Test data
		String scenarioId = "ID800";
		Map<String, String> regData = scenarioData.get(scenarioId);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String kit = getProductsByProductTypeAndCategory(EMEA, KIT, MONITORS).get(0);
		String password = passwords.get(DIRECTUSERPWD);

		//Waiting for user availability
		String purchaser = getUser(scenarioId, PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//		HashMap<String, String> regData = getTestData("CE", "DT006_EMEA");

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		String quoteName = "AutQuote";
		String paymentMethod = "Purchase Order";
		String preConfiguredProfile = "Custom";
		String basicServices = "Door / dock delivery";
		String serviceDesc = "Custom;Door / dock delivery;Consolidated delivery;Saturday Delivery;HP std: mixed Euro and/or Industry pallet;Any working day at any time;No appointment required";

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		PDP pdp = new PDP(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with direct user", url, purchaser, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract(
				"Step 1-2: Click on Organization and Catalog dropdown<br>Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay", 
				"Requested catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete products", "Products should be deleted", storeCE, false));

		Assert.assertNotNull(login.searchSKU("Step 3: In search box, search for KIT sku", "PDP of searched product should be displayed", kit));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 4: Enter Qty as 5 and update", "Qty should be updated successfully", "5"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Click on 'Add to cart' button", "Product should be added to cart", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Step 6: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 7: Click on 'save as quote' button", "'Quote creation page' should be displayed"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 8: Select Payment method as 'Purchase Order'", 
				"Payment method should be selected", paymentMethod));

		Assert.assertTrue(createNewQuote.selectPreconfiguredProfileFromDropDown("Step 9.1: Select below Shipping Option(s)<br>" + 
				"preconfigured profile as 'Custom'", "Option should be selected accordingly", preConfiguredProfile));

		Assert.assertTrue(createNewQuote.selectBasicServiceInShippingOptions("Step 9.2: Select Door/dock delivery", 
				"Option should be selected accordingly", basicServices, true));

		Assert.assertTrue(createNewQuote.selectConsolidatedDeliveryCheckbox("Step 9.3: Select Consolidated delivery checkbox", 
				"Option should be selected accordingly", true));

		Assert.assertTrue(createNewQuote.selectSaturdayDeliveryCheckbox("Step 9.4: Select Saturday Delivery checkbox", 
				"Option should be selected accordingly", true));

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Step 10: Verify the Summarized Logistical view in Quote creation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(createNewQuote.verifyShippingChargesInSummarizedLogisticalView("Step 11: Verify the Shipping & handling charges", 
				"Shipping & handling charges should not be 0", true));

		Assert.assertNotNull(createNewQuote.createQuote("Step 12: Enter all mandatory details and Click on 'Save quote' button",
				"Quote should be created successfully", quoteName, purchaser));

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Step 13: Verify the Summarized Logistical view in Quote confirmation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(createNewQuote.verifyShippingChargesInSummarizedLogisticalView("Step 14: Verify the Shipping & handling charges", 
				"Shipping & handling charges should not be 0", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify the Logistical view and Charges are not displayed when Shipping flag set to 'Custom Enable Flag No'
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/295275
	 * @param region Emea
	 * @since May 12, 2021 6:25:06 PM
	 * @author Rashi
	 */
	@Test(groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C295275_Regression_CE_Quotes_CBNVerifyLogisticalViewAndChargesAreNotDisplayedWhenShippingFlagSetToCustomEnableFlagNo_Direct() {

		//Reporting info
		initializeReporting("Verify the Logistical view and Charges are not displayed when Shipping flag set to 'Custom Enable Flag No'",
				"C295275_Regression_CE_Quotes_CBNVerifyLogisticalViewAndChargesAreNotDisplayedWhenShippingFlagSetToCustomEnableFlagNo_Direct", logger);

		//Test Data
		HashMap<String, String> regData = getScenarioData(ID01); //CBN dataset need to be added
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name"); 
		String quoteName = "Auto_Quote_";
		String bto = getProductByDataSetID(ID01, BTO, ACCESSORIES);		
		Assert.assertNotNull(bto);
		String password =passwords.get(DIRECTUSERPWD);

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		//Waiting for user availability
		String user = getUser(ID01, PURCHASER);
		Assert.assertNotEquals(user, null);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(user));

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Launch Storefront URL > User Name > Enter Password > Click on Sign In", url, user, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 & 2: Select Organization & Catalog", "Requested Organization & Catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		Assert.assertNotNull(login.searchSKU("Step 3: In search box , search for Bto number", "PDP of searched product should be displayed",bto));

		Assert.assertTrue(pdp.addProductToCart("Step 4: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 5:Click on 'Mini cart' icon and Click on 'Go to cart' button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 6: Click on 'save as quote' button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 7: Select payment method from dropdown","Payment should be selected", "Purchase Order"));

		Assert.assertTrue(createNewQuote.verifyShippingProfileIsNotDisplayed("Step 8: Verify Shipping profile is not available", "Shipping profile should not be availble","Create New Quote"));

		Assert.assertTrue(createNewQuote.verifyLogisticalSectionIsNotDisplayedOnCreateQuote("Step 9: Verify Logistical section is not displayed", "Logistical section should not be displayed"));

		Assert.assertTrue(createNewQuote.verifyShippingAndHandlingChargesIsNotDisplayed("Step 10: Verify the Shipping & handling charges is not available", "Shipping & handling charges should not be available","Create New Quote"));

		quoteDetails = createNewQuote.createQuote("Step 11: Enter all the mandatory details and click on save a quote","Quote should be created successfully",quoteName, user);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(quoteDetails.verifyShippingProfileIsNotDisplayed("Step 12: Verify Shipping profile is not available in 'Quote confirmation page'", "Shipping profile should not be availble","Quote Details"));

		Assert.assertTrue(quoteDetails.verifyLogisticalSectionIsNotDisplayedOnQuoteDetails("Step 13: Verify Logistical section is not displayed in 'Quote confirmation page'", "Logistical section should not be displayed"));

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargesIsNotDisplayed("Step 14: Verify the Shipping & handling charges is not available", "Shipping & handling charges should not be available","Quote Details"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_CBN_Verify the Summarized Logistical view displays only service description for FOC service
	 * @TestCaseLink https:https://hpitdce.testrail.net/index.php?/cases/view/295020
	 * @since May 10, 2021 11:59:23 AM
	 * @author Vijay
	 * @throws IOException 
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
	//		IGroupsTagging.IUserType.CSR,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C295020_Regression_CE_Quotes_CBNVerifyTheSummarizedLogsticalViewDisplayOnlyServicesDecsriptionsForFOCService_CSR() throws IOException{

		// Reporting info
		initializeReporting("Verify the Summarized Logistical view displays only service description for FOC service",
				"C295020_Regression_CE_Quotes_CBNVerifyTheSummarizedLogsticalViewDisplayOnlyServicesDecsriptionsForFOCService_CSR", logger);

		// Test data
		String scenariodId = "ID800";
		Map<String, String> regData = getScenarioData(scenariodId);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProduct(EMEA, BTO, MONITORS);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);  

		//Waiting for user availability		
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotEquals(csr, "");
		String purchaser = getUser(scenariodId, PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		userSet.add(csr);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		
		String shippingOption = "Default";
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("emailID", purchaser);
		String serviceDesc = commonData.get("DefaultSelectedLogisticalServicesProfileForDetailedCBNContract");

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition: Impersonate user","User is Impersonated.",data));
		/** Pre-Condition Ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 : Click on Organization and Catalog dropdown <br> Step 2: Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 3 : Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 4: Enter Qty as 10 and update", 
				"Qty should be updated successfully", "10", "pdp"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart =pdp.navigateToShoppingCartThroughHeader("Step 6: Click on 'Mini cart' icon and Click on 'Go to cart' button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 7: Click on 'save as quote' button", "'Quote creation page' should be displayed"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 8: Select Payment method as 'Purchase Order'", 
				"Payment method should be selected", "Purchase Order"));

		Assert.assertTrue(createNewQuote.selectShippingOptionDropDown("Step 9: Select below Shipping Option(s)<br>"
				+ "1. preconfigured profile as 'Default'", "Option should be selected accordingly", shippingOption));

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Step 10: Verify the Summarized Logistical view in Quote creation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(createNewQuote.verifyShippingAndHandlingChargeShouldBeZero("Step 11: Verify that Shipping & handling charges is '0'",
				"Shipping & handling charges should be '0'"));

		quoteDetails = createNewQuote.createQuote("Step 12: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully","QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Step 13: Verify the Summarized Logistical view in Quote creation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeShouldBeZero("Step 14: Verify that Shipping & handling charges is '0'",
				"Shipping & handling charges should be '0'"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify Quote creation with SPC and export quote for XLS
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300037
	 * @since May 10, 2021 3:36:32 PM
	 * @author Vijay
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR, 
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300037_Regression_CE_Quotes_VerifyQuoteCreationWithSpcAndExportQuoteForXLS_CSR()  {

		// Reporting info
		initializeReporting("Verify user is able to share existing quote from home page",
				"C300037_Regression_CE_Quotes_VerifyQuoteCreationWithSpcAndExportQuoteForXLS_CSR", logger);

		//Test Data
		Map<String, String> regData = getScenarioData(ID03);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto = getProduct(EMEA, BTO, MONITORS);
		Assert.assertNotNull(bto);
		String password = passwords.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability		
		ArrayList<String> userSet= new ArrayList<>();
		String user = getUser(CSR);
		Assert.assertNotNull(user);
		String purchaser = getUser(ID03, PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(user);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		String quoteName = "Aut_Quote_";
		String spc = regData.get("SPC");
		String emailComments ="Email Comments";
		String invoiceInstr ="Invoice";
		String shpInstructionText ="Shipping";

		//Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		XLSValidations xls = new XLSValidations(_webcontrols);

		/** Pre-Condition Starts **/
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>", "<b>Creating Quote & Sharing with Another User </b>");

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Impersonate purchaser.", "User Should be Impersonated successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		/** Pre-Condition Ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 2: Enter Qty as 100 and update", 
				"Qty should be updated successfully", "100", "pdp"));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 6: Select payment method as 'Purchase Order'", 
				"Payment method is selected", "Purchase Order"));

		String subTotalBeforeSPC = createNewQuote.getSubTotal("Step 7.1: Get Strike off price before applying spc",
				"Subtotal value should be fetched", true, "Quote");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(shoppingCart.enterTextInSpecialPricingCodeTextBox("Step 7.2: Enter valid 'Special pricing code' in cart summary section", 
				" valid 'Special pricing code' in cart summary section should entered", spc, true));

		Assert.assertTrue(shoppingCart.clickOnApplyLink("Step 7.3: Click on apply link", 
				"'Special pricing code' Should be applied successfully and product price should be reduced.", true, true));

		String subtotalAfterSPC = createNewQuote.getSubTotal("Step 7.4: Get Strike off price before applying spc",
				"Subtotal value should be fetched after apply spc", true, "Quote");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyUpdatedSubtotalIsReducedAfterApplyingSPC("Step 7.5: Strike off price should be reduced after applying spc", "Strike off price reduced after applying spc", subTotalBeforeSPC, subtotalAfterSPC));

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Get Shipping & Hnadling Charges Value", "Shipping & Hnadling Charges should be fetched", false);
		Assert.assertNotEquals(shippingCharges, null);

		String emailNotificationComments = createNewQuote.enterEmailNotificationComments("Enter email notifications comments", 
				"Email notifications comments should entered", emailComments);
		Assert.assertNotEquals(emailNotificationComments, null);

		String invoiceInstrComments = createNewQuote.enterInvoiceInstructions("Enter invoice instruction comment", "Invoice instruction should be entered", invoiceInstr);
		Assert.assertNotEquals(invoiceInstrComments, null);

		String shippingInstructionComments = createNewQuote.enterShipingInstructions("Enter shipping instructions", "Shipping Instruction should be entered", shpInstructionText);
		Assert.assertNotEquals(shippingInstructionComments, null);

		Assert.assertTrue(createNewQuote.verifyCartSummarySection("Step 8: Verify Cart summary section for SPC in create quote page",
				"Cart Summary section should display  with all details"));

		quoteDetails = createNewQuote.createQuote("Step 9: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully and navigated to quote confirmation page.",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 10: Verify quote details in quote confirmation page",
				"Quote details page should have below details");
		Assert.assertNotEquals(quoteDetailsList, null);

		System.out.println(quoteDetailsList);
		String quoteNameValue=quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, "");

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 11.1: Click on 'Export' option in quote detail page", "Succesfullly Clicked on Export Button"));

		Assert.assertTrue(quoteDetails.verifyExportPopUp("Step 11.2: Verify Overlay appears with details"
				, " Overlay should appears with below details:- 1. Select file type <br> 2.CSV <br> 3.XLS <br> 3.PDF"));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 12.1: Select the export type as XLS", "File type should be selected", "xls"));
		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 12.2: Click on 'Export' in quote confirmation page", "XLS format should get exported successfully"));

		LinkedHashMap<String, String> quoteInfoDetails = quoteDetails.getInformationDetails(
				"Pre Condition: Fetch Information & Details from Quote Details Page",
				"All details Should be fetched Successfully", "xls", user);
		Assert.assertFalse(quoteInfoDetails.isEmpty());

		LinkedHashMap<String, String> quoteBillingInfoDetails = quoteDetails.getBillingInformationDetails(
				"Pre Condition: Fetch Billing Information from Quote Details Page.",
				"All details Should be fetched Successfully", "xls");
		Assert.assertFalse(quoteBillingInfoDetails.isEmpty());

		LinkedHashMap<String, String> quoteShippingInfoDetails = quoteDetails.getShippingInformationDetails(
				"Pre Condition: Fetch Shipping Information from Quote Details Page.",
				"All details Should be fetched Successfully", "xls");
		Assert.assertFalse(quoteShippingInfoDetails.isEmpty());

		Assert.assertTrue(xls.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching("Step 13: Go to the file location and click on Quote to open",
				"Quote should get displayed successfully", quoteNameValue + ".xls"));

		Assert.assertTrue(xls.verifyHpLogoAndPropreiteryDetails("Step 14: Verify following are displayed <br>"
				+ "HP Logo <br>"
				+ "Quote Name <br>"
				+ "Quote number at top right side corner <br>"
				+ "HP Proprietary Information for customer use only <br>"
				+ "Do not share", "All the details should get displayed successfully.",quoteNameValue,quoteNumber));

		Assert.assertTrue(xls.verifyInformationAndDetails(
				"Step 15: Verify the following details and headers are displaying under Information and Details.",
				"All the details should get displayed successfully", quoteInfoDetails));

		Assert.assertTrue(xls.verifyBillingInformation(
				"Step 16: Verify the following details and headers are displaying under Billing Information.",
				"All the details should get displayed successfully", quoteBillingInfoDetails));

		Assert.assertTrue(xls.verifyShippingInformation(
				"Step 17: Verify the following details and headers are displaying under Shipping Information.",
				"All the details should get displayed successfully", quoteShippingInfoDetails));

		Assert.assertTrue(xls.verifySalesRepoHeader(
				"Step 18: Verify following details are displaying under Sales Rep Information <br>"
						+ "Agent Name <br>"
						+ "Agent Phone <br>"
						+ "Agent Email.",
				"All the details should get displayed successfully"));

		Assert.assertTrue(xls.verifyComments("Step 19: Verify Comments(If Entered) is displaying",
				"Comments should get displayed successfully",emailNotificationComments));

		Assert.assertTrue(xls.verifyInvoiceInstructions("Step 20: Verify Invoice Instructions(If Entered) is displaying",
				"Invoice Instructions should get displayed successfully",invoiceInstrComments));

		Assert.assertTrue(xls.verifyShippingInstructions("Step 21: Verify Shipping Instructions(If Entered) is displaying",
				"Shipping Instructions should get displayed successfully",shippingInstructionComments));

		Assert.assertTrue(xls.verifyQuoteSummaryDetails(
				"Step 22: Verify the following details and headers are displaying under Quote summary <br>"
						+ "Step 23: Verify Product Desc Header is displayed <br>"
						+ "Step 24: Verify Manufacturer# Header is displayed <br>"
						+ "Step 25: Verify Pricing Source Header is displayed <br>"
						+ "Step 26: Verify Quantity Header is displayed <br>"
						+ "Step 27: Verify Unit Price Header is displayed <br>"
						+ "Step 28: Verify Total Price Header is displayed <br>",
						"All the details should get displayed successfully", EMEA));

		Assert.assertTrue(xls.verifyShipAndHandlingChargesInHtmlEmbededXls("Step 29: Verify Shipping & Handling charges under cart summary section in exported quote",
				"Shipping & Handling charges should be displayed and should be included to total", shippingCharges));

		Assert.assertTrue(xls.verifyBTOSpecificationFormat(
				"Step 30: BTO Specification should be displayed with defined format",
				"BTO Specification should be displayed successfully with defined format", bto));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");


	}

	/**
	 * Quote_S4_Verify that Detailed Logistical view display Service description, SKU and Price in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/294805
	 * @since May 13, 2021 2:30:34 PM
	 * @author rajoriap
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C294805_Regression_CE_Quotes_S4VerifyDetailedLogisticalViewDisplayServiceDescriptionSKUAndPriceInQuoteCreationAndQuoteConfirmationPage_Direct(){

		// Reporting info
		initializeReporting("Quote_S4_Verify that Detailed Logistical view display Service description, SKU and Price in Quote creation and Quote confirmation page",
				"C294805_Regression_CE_Quotes_S4VerifyDetailedLogisticalViewDisplayServiceDescriptionSKUAndPriceInQuoteCreationAndQuoteConfirmationPage_Direct",
				logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID03);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String logisticalServicesProfile = commonData.get("DefaultSelectedLogisticalServicesProfileForDetailedS4Contract");
		String quoteName = "MyQuote";

		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(EMEA,mdcpid,catalogName,"Purchase Order","Yes");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(0);

		String kit =  getProductsByProductType(getRegion(EMEA, ID03),KIT).get(0);
		String bto =  getProductsByProductTypeAndCategory(EMEA, BTO, MONITORS).get(0);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser(ID03, PURCHASER);
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
		PDP pdp = new PDP(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));
		/** Pre-Condition ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Click on Organization and Catalog "
				+ "dropdown and Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected catalog should be loaded", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 3: Search for a BTO", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 4: Enter Qty as 3 and update ", "Qty updated successfully", "3"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		pdp = customerService.searchSKU("Step 6: Search for a KIT", "Requested product PDP should load", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 7: Enter Qty as 2 and update ", "Qty updated successfully", "2"));

		Assert.assertTrue(pdp.addProductToCart("Step 8: Add KIT product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 9: Click on mini cart icon and click on Go to cart button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 10: Click on 'Save as quote' button", "'Quote creation page' should be displayed"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown(
				"Step 11: Select Payment method as 'Purchase Order'", "Payment method should be selected", "Purchase Order"));

		LinkedHashMap<String, String> selectedS4ContractAddressDetails =createNewQuote.clickOnChangeBillingAddressEnterContractIDandVerifySelectedContractID(
				"Step 12: Click on 'Change billing address'<br>"
						+ "Step 13: Select Search Criteria as 'S4 Contract Id' and Enter S4 Contract Id<br>"
						+ "Step 14: click on Search icon and Click OK", "Selected Billing address must be displayed",contractID);
		Assert.assertNotEquals(selectedS4ContractAddressDetails, null);

		Assert.assertTrue(createNewQuote.verifyContractIdInBillingInformation("Step 15: verify the S4 Contract Id in Billing address", 
				"S4 Contract ID must be displayed", contractID));

		Assert.assertTrue(createNewQuote.selectPreconfiguredProfileFromDropDown("Step 16: Select below Shipping Option(s) preconfigured "
				+ "profile as 'Default'","Option(s) should be selected accordingly","Default"));

		Assert.assertTrue(createNewQuote.verifyElementIsDisplayedByText(
				"Step 17: Verify that Logistical Services section is displayed between cart summary and Total price",
				"Logistical Services section should be displayed between cart summary and Total price", "Logistical Services", true));

		Assert.assertTrue(createNewQuote.verifyLogisticalViewForDefaultPreConfiguredProfile(
				"Step 18.1: Verify Logistical Services profile and Services description","Logistical Services profile and Services description should display", logisticalServicesProfile));

		LinkedHashMap<String,List<String>> logisticSkuDetails = quoteDetails.verifyLogisticalServicesDetails("Step 18.2: Verify Items,Logistical sku,Product description,Qty,Unit price and Total", 
				"Items,Logistical sku,Product description,Qty,Unit price and Total should display", "CreateQuote");
		Assert.assertNotEquals(logisticSkuDetails, null);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 19: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "CreateQuote"));

		quoteDetails = createNewQuote.createQuote("Step 20: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertNotNull(quoteDetails.getContractId("Step 21: Verify the S4 Contractid in 'Quote detail page'", "S4 Contractid should be displayed"));

		Assert.assertTrue(createNewQuote.verifyLogisticalViewForDefaultPreConfiguredProfile(
				"Step 22.1: Verify Logistical Services profile and Services description","Logistical Services profile and Services description should display", logisticalServicesProfile));

		LinkedHashMap<String,List<String>> logisticSkuDetailsInQuoteDetails = quoteDetails.verifyLogisticalServicesDetails("Step 22.2: Verify Items,Logistical sku,Product description,Qty,Unit price and Total", 
				"Items,Logistical sku,Product description,Qty,Unit price and Total should display", "QuoteDetails");
		Assert.assertNotEquals(logisticSkuDetailsInQuoteDetails, null);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 23: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "QuoteDetails"));

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_CBN_Verify that Detailed Logistical view display Service description, SKU and Price in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/295226
	 * @since May 13, 2021 5:36:13 PM
	 * @author rajoriap
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C295226_Regression_CE_Quotes_CBNVerifyDetailedLogisticalViewDisplayServiceDescriptionSKUAndPriceInQuoteCreationAndQuoteConfirmationPage_CSR(){

		// Reporting info
		initializeReporting("Quote_CBN_Verify that Detailed Logistical view display Service description, SKU and Price in Quote creation and Quote confirmation page",
				"C295226_Regression_CE_Quotes_CBNVerifyDetailedLogisticalViewDisplayServiceDescriptionSKUAndPriceInQuoteCreationAndQuoteConfirmationPage_CSR",
				logger);

		//Need to remove once data is available
		Reporting.getLogger().log(LogStatus.INFO, "<b>Added invalid scenario ID80</b>", "<b>Since CBN WSL contract with detailed logitical view is not available</b>");

		// Test data
		Map<String, String> regData = getScenarioData("ID80");
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String quoteName = "MyQuote";

		String bto = getProductsByProductTypeAndCategory(EMEA, "BTO", MONITORS).get(0);
		String kit = getProductsByProductType(EMEA, KIT).get(0);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("MDCPID", mdcpid);

		// Waiting for user availability
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

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with CSR user", url, username, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Enter MDCPID and mail id and impersonate purcahser", "Purchaser should be impersonated successfully", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));
		/** Pre-Condition ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Click on Organization and Catalog "
				+ "dropdown and Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
				"Selected catalog should be loaded", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 3: Search for a BTO", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 4: Enter Qty as 3 and update ", "Qty updated successfully", "3"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		pdp = customerService.searchSKU("Step 6: Search for a KIT", "Requested product PDP should load", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 7: Enter Qty as 2 and update ", "Qty updated successfully", "2"));

		Assert.assertTrue(pdp.addProductToCart("Step 8: Add KIT product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 9: Click on mini cart icon and click on Go to cart button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 10: Click on 'Save as quote' button", "'Quote creation page' should be displayed"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown(
				"Step 11: Select Payment method as 'Purchase Order'", "Payment method should be selected", "Purchase Order"));

		Assert.assertTrue(createNewQuote.selectPreconfiguredProfileFromDropDown("Step 12: Select below Shipping Option(s) preconfigured "
				+ "profile as 'Default'","Option(s) should be selected accordingly","Default"));

		Assert.assertTrue(createNewQuote.verifyElementIsDisplayedByText(
				"Step 13: Verify that Logistical Services section is displayed between cart summary and Total price",
				"Logistical Services section should be displayed between cart summary and Total price", "Logistical Services", true));

		Assert.assertTrue(createNewQuote.verifyLogisticalViewForDefaultPreConfiguredProfile(
				"Step 14.1: Verify Logistical Services profile and Services description","Logistical Services profile and Services description should display", HP2BStaticData.defaultSelectedLogisticalServicesProfileForDetailedCBNContract));

		LinkedHashMap<String,List<String>> logisticSkuDetails = quoteDetails.verifyLogisticalServicesDetails("Step 14.2: Verify Items,Logistical sku,Product description,Qty,Unit price and Total", 
				"Items,Logistical sku,Product description,Qty,Unit price and Total should display", "CreateQuote");
		Assert.assertNotEquals(logisticSkuDetails, null);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 15: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "CreateQuote"));

		quoteDetails = createNewQuote.createQuote("Step 16: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		Assert.assertTrue(createNewQuote.verifyLogisticalViewForDefaultPreConfiguredProfile(
				"Step 17.1: Verify Logistical Services profile and Services description","Logistical Services profile and Services description should display", HP2BStaticData.defaultSelectedLogisticalServicesProfileForDetailedCBNContract));

		LinkedHashMap<String,List<String>> logisticSkuDetailsInCreatedQuote = quoteDetails.verifyLogisticalServicesDetails("Step 17.2: Verify Items,Logistical sku,Product description,Qty,Unit price and Total", 
				"Items,Logistical sku,Product description,Qty,Unit price and Total should display", "QuoteDetails");
		Assert.assertNotEquals(logisticSkuDetailsInCreatedQuote, null);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 18: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "QuoteDetails"));

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}


	/**
	 * Quote_CBN_Verify the Logistical Services in Mailed Quote
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/294950
	 * @param region EMEA
	 * @since May 17, 2021 6:34:49 PM
	 * @author Rashi
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
			IGroupsTagging.IUserType.DIRECT, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C294950_Regression_CE_Quotes_CBNVerifyLogisticalServicesInMailedQuote_Direct() throws IOException{

		//Reporting info
		initializeReporting("Quote_CBN_Verify the Logistical Services in Mailed Quote","C294950_Regression_CE_Quotes_CBNVerifyLogisticalServicesInMailedQuote_Direct", logger);

		//Test data
		HashMap<String, String> regData = getScenarioData(ID03); //CBN data set need to be added For EMEA
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String bto = getProductByDataSetID(ID03, BTO, ACCESSORIES);		
		Assert.assertNotNull(bto);
		String bundle = getProductByDataSetID(ID03, BUNDLE, ACCESSORIES);		
		Assert.assertNotNull(bundle);
		String softBundle = getProductByDataSetID(ID03, SOFTBUNDLE, ACCESSORIES);		
		Assert.assertNotNull(bundle);
		String password =passwords.get(DIRECTUSERPWD);

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		String paymentMethod = "Purchase Order";
		String shippingOption = "Default";
		String emailID = "hp2bfeautomation+directCE01@gmail.com";
		String quoteName = "Automation_Quote_";
		String serviceDesc = "Default;Inside / desk delivery;Consolidated delivery;Unpacking and waste removal;HP std: mixed Euro and/or Industry pallet;At 1.00 pm;Delivery advice prior to delivery;Access constraints, special truck size is required;Fixed Delivery Date";

		//Waiting for user availability
		String user = getUser(ID03, PURCHASER);
		Assert.assertNotEquals(user, null);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(user));

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		QuoteListing quoteListing=new QuoteListing(_webcontrols);
		QuoteDetails quoteDetails= new QuoteDetails(_webcontrols);
		GmailPage gmailPage = new GmailPage(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		XLSValidations xls = new XLSValidations(_webcontrols);
		AccountSummary accountSummary=new AccountSummary(_webcontrols);
		CSVValidations csvPage= new CSVValidations(_webcontrols);

		logger.info("<b>Pre-Condition Started </b><br><b>Login to HP2B & Creating Quote with reference to C295229</b>");
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Started </b>", "<b>Login to HP2B & Creating Quote with reference to C295229</b>");

		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with direct user", url, user, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("Click on Organization and Catalog dropdown<br>Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay", "Requested catalog should be selected", data, true));

		Assert.assertNotNull(login.searchSKU("In search box, search for Bundle sku", "PDP of searched product should be displayed", bundle));

		Assert.assertTrue(pdp.enterFirstProductQuantity("Enter Qty as 3 and update", "Qty should be updated successfully", "3", "pdp"));

		Assert.assertTrue(pdp.addProductToCart("Click on 'Add to cart' button", "Product should be added to cart", "pdp"));

		Assert.assertNotNull(pdp.searchSKU("In search box, search for Soft Bundle sku", "PDP of searched product should be displayed", softBundle));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Enter Qty as 2 and update", "Qty should be updated successfully", "2"));

		Assert.assertTrue(pdp.addProductToCart("Click on 'Add to cart' button", "Product should be added to cart", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Click on 'Mini cart' icon and Click on 'Go to cart' button","Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Click on 'save as quote' button", "'Quote creation page' should be displayed"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Select Payment method as 'Purchase Order'","Payment method should be selected", paymentMethod));

		Assert.assertTrue(createNewQuote.selectPreconfiguredProfileFromDropDown("Select below Shipping Option(s)<br>"
				+ "1. preconfigured profile as 'Default'", "Option should be selected accordingly", shippingOption));

		Assert.assertTrue(createNewQuote.verifyElementIsDisplayedByText(
				"Verify that Logistical Services section is displayed between cart summary and Total price",
				"Logistical Services section should be displayed between cart summary and Total price", "Logistical Services", true));

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Verify the Summarized Logistical view in Quote creation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(createNewQuote.verifyShippingChargesInSummarizedLogisticalView("Verify the Shipping & handling charges", 
				"Shipping & handling charges should not be 0", true));

		Assert.assertNotNull(createNewQuote.createQuote("Enter all mandatory details and Click on 'Save quote' button",
				"Quote should be created successfully", quoteName, user));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null); 
		String quoteNameValue = quoteName + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList, "Quote Number");
		Assert.assertNotEquals(quoteNumber, "");

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Verify the Summarized Logistical view in Quote confirmation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(createNewQuote.verifyShippingChargesInSummarizedLogisticalView("Verify the Shipping & handling charges", 
				"Shipping & handling charges should not be 0", true));

		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Ended </b>", "<b>Quote Created Successfully</b>");
		logger.info("<b>Pre-Condition Ended </b><br>Quote Created Successfully");

		accountSummary = quoteDetails.navigateToMyAccount("Step 1: Click on 'My accounts'.", "My accounts page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 2 : Click on Quotes tab in MyAccount Page.","User should redirects to Quote Listing Page.");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.clickOnQuote("Click on newly created Quote number", "Quote Details Page Should be displayed", quoteNumber));

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 3: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 4: Enter valid email in email address field and Select PDF radio button and click on Email button", 
				"Your quote was sent successfully message should be displayed", "PDF", emailID));

		String shippingAndHandlingCharges=quoteDetails.fetchShippingAndHandlingChargesValue("Fetching Shipping and Handling charges","Value of Shipping and Handling charges must be fetched","Quote details");
		Assert.assertNotNull(shippingAndHandlingCharges);

		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 5: Login to the emailed account and download quote PDF", 
				"Quote PDF should be fetched from email",quoteNameValue+ ".pdf", 5));

		String pdfContent = pdfValidations.readPdfFileInDownloads("Step 6.1: Getting PDF content", "PDF content should be fetched", quoteNameValue+ ".pdf", true);
		Assert.assertNotEquals(pdfContent, "");

		Assert.assertTrue(pdfValidations.verifyLogisticalViewForDefaultOptionInPDF("Step 6.2: verify the Logistical services in 'pdf mailed quote'","Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", pdfContent));

		Assert.assertTrue(pdfValidations.verifyShippingAndHandlingChargeInPDF("Step 7: Verify the shipping and handling charges","Shipping and handling charges should be the sum of Logistical sku charges", pdfContent,shippingAndHandlingCharges));

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 8.1: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 8.2: Enter valid email in email address field and Select CSV radio button and click on Email button", 
				"Your quote was sent successfully message should be displayed", "CSV", emailID));

		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 9: Login to the emailed account and download quote CSV","Quote CSV should be fetched from email", quoteNameValue + ".csv", 10));

		List<String> expectedHeaders = Arrays.asList("Logistical Services","Logistical Services Profile","Default;Inside / desk delivery;Consolidated delivery;Unpacking and waste removal;HP std: mixed Euro and/or Industry pallet;At 1.00 pm;Delivery advice prior to delivery;Access constraints","special truck size is required;Fixed Delivery Date");
		Assert.assertTrue(csvPage.verifyCSV("Step 10: Verify the Summarized Logistical view in Quote CSV<br>","Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description",quoteNameValue,expectedHeaders, true));

		logger.info("Skipped Step 11:Verify the shipping and handling charges,As shipping and handling charges will not be displayed in CSV");
		Reporting.getLogger().log(LogStatus.INFO, "<b>Skipped Step 11:Verify the shipping and handling charges,</b>", "<b>As shipping and handling charges will not be displayed in CSV </b>");

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 12.1: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 12.2: Enter valid email in email address field and Select XLS radio button and click on Email button","Your quote was sent successfully message should be displayed", "XLS", emailID));

		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 13: Login to the emailed account and download quote XLS","Quote XLS should be fetched from email", quoteNameValue + ".xls", 10));

		List<String> headers = Arrays.asList("Logistical Services","Logistical Services Profile",HP2BStaticData.defaultSelectedLogisticalServicesProfile,"Shipping & Handling:",shippingAndHandlingCharges.split(" ")[1]);
		Assert.assertTrue(xls.verifyXlsFile("Step 14:Verify the Logistical view in exported quote XLS<br>Step 15:Verify the shipping and handling charges", "Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description<br>Shipping and handling charges should be the sum of Logistical sku charges", quoteNameValue, headers));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify Quote creation with email
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/C294949
	 * @param region EMEA, APJ, AMS-US, AMS-LA
	 * @since May 6,2021
	 * @author RamaredU
	 * @throws IOException 
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C294949_Regression_CE_Quotes_VerifyQuoteCreationWithEmail_CSR(String region) throws IOException {

		initializeReporting(" Verify Quote creation with email","C294949_Regression_CE_Quotes_VerifyQuoteCreationWithEmail_CSR", region, logger);
		// Test data		
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		System.out.println("tsting");
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		//String quoteName = "MyQuote";

		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, LAPTOPS);		
		Assert.assertNotNull(bto); 
		String kit = getProductByDataSetID(region, dataIDs.get(region), KIT, DESKTOPS);		
		Assert.assertNotNull(bto);

		// Waiting for user availability				
		String CSRUser = getUser(CSR);
		Assert.assertNotNull(CSRUser);	
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		ArrayList<String> userSet= new ArrayList<>();
		userSet.add(CSRUser);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("MDCPID", mdcpid);
		data.put("emailID", purchaser);
			
		String paymentMethod = "Purchase Order";
		String shippingOption = "Default";
		
		// Get URL
		setEnvironment();
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

		String emailId =HP2BStaticData.emailID;

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate a purchaser.",
				"Impersonate is done successfully.", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Precondition: Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product","Product should be deleted","CE", false));

		Assert.assertNotNull(login.searchSKU("Step 1: In search box , search for BTO sku",
				"PDP of searched product should be displayed", bto));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter Qty as 500 and update",
				"Qty should be updated successfully", "500"));

		Assert.assertTrue(login.addProductToCart("Step 3: Click on 'Add to cart' button",
				"Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.searchSKU("Step 4: In search box , search for KIT sku",
				"PDP of searched product should be displayed", kit));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 5: Enter Qty as 20 and update",
				"Qty should be updated successfully", "20"));

		Assert.assertTrue(login.addProductToCart("Step 6: Click on 'Add to cart' button",
				"Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader(
				"Step 7: Click on 'Mini cart' icon and Click on 'Go to cart' button",
				"Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 8: Click on 'save as quote' button",
				"'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown(
				"Step 9: Select Payment method as 'Purchase Order'", "Payment method is selected", "Purchase Order"));

		Assert.assertNotNull(createNewQuote.createQuote("Step 10: Enter all mandatory details and click on Save quote button","Quote should be created successfully and navigated to quote confirmation page", "AutQuote",purchaser));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(quoteDetailsList, null);
		String quoteName = "AutQuote" + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");
		List<String> quoteDetailsInEmail = new ArrayList<>();
		quoteDetailsInEmail.add(bto);
		quoteDetailsInEmail.add(kit);

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 11: Click on 'Email' option in quote detail page",
				"Pop up with following options should be displayed:\r\n" + "Enter email address text field\r\n"
						+ "Select File type:\r\n" + "CSV Radio button\r\n" + "XLS Radio button\r\n"
						+ "PDF Radio button\r\n" + "Email and Cancel buttons"));
		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp(
				"Step 12 : Enter valid email address and select CSV radio button option and click on Email button",
				"Your quote was sent successfully message should be displayed", "csv", emailId));

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 13.1: Click on 'Email' option in quote detail page",
				"Pop up with following options should be displayed"));
		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp(
				"Step 13.2 : Enter valid email address and select xls radio button option and click on Email button",
				"Your quote was sent successfully message should be displayed", "xls", emailId));
		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 13.3: Login to the emailed account and download quote xls",
				"Quote XLS should be fetched from email", quoteName + ".xls", 10));

		Assert.assertTrue(xlsPage.verifyQuoteNameBTOAndKITSkuIdFromEmbeddedXlsFile("Step 13.4: Check emails for XLS ",
				"quote details should be displayed in xls file", quoteName, bto, kit));
		_webcontrols.get().navigateToPreviousPage("QuoteDetails");

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 14.1: Click on 'Email' option in quote detail page",
				"Pop up with following options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp(
				"Step 14.2 : Enter valid email address and select PDF radio button option and click on Email button",
				"Your quote was sent successfully message should be displayed", "pdf", emailId));
		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 15.1: Click on 'Email' option in quote detail page",
				"Pop up with following options should be displayed"));
		Assert.assertTrue(quoteDetails.clickOnCancelButtonAndVerifyEmailPopup(
				"Step 15.2: Click on Cancel button in Email popup", "Email popup should be closed."));

		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 16.1: Login to the emailed account and download quote CSV",
				"Quote CSV should be fetched from email", quoteName + ".csv", 15));

		Assert.assertTrue(csvPage.verifyCSV("Step 16.2 : Check emails for CSV<br>" + "Verify CSV file contains quote details<br>","All details should display accordingly", quoteName, quoteDetailsInEmail, true));
		quoteDetailsInEmail.add(quoteName);
		quoteDetailsInEmail.add("Warranty");
		quoteDetailsInEmail.add("year limited warranty");

		Assert.assertNotNull(gmailPage.getAttachmentInDownloads("Step 17: Check email for PDF format","Quote PDF should be fetched from email", quoteName + ".pdf", 5));
		String pdfContent = "";
		pdfContent = pdfPage.readPdfFileInDownloads("Step 18.1: Getting PDF content", "PDF content should be fetched",quoteName + ".pdf", true);
		Assert.assertNotEquals(pdfContent, "");
		Assert.assertTrue(pdfPage.verifyBtoSpecsInPDF("Step 18.2: Verify BTO spec's on Quote email notification page.","BTO Spec's should be displayed on Quote email notifications page", pdfContent,quoteDetailsInEmail));
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Verify search results with different sub options for 'Quote Created On' date range in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300048
	 * @param region EMEA, APJ, AMS-US, AMS-LAf
	 * @since Apr 29,2021
	 * @author RamaredU
	 */

	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300048_Regression_CE_Quotes_VerifySearchResultsWithDifferentSubOptionsForQuoteCreatedOnDateRangeInQuoteListingPage_CSR(
			String region) {

		initializeReporting("Verify search results with different sub options for 'Quote Created On' date range in quote listing page",
				"C300048_Regression_CE_Quotes_VerifySearchResultsWithDifferentSubOptionsForQuoteCreatedOnDateRangeInQuoteListingPage_CSR",
				region, logger);

		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO);		
		Assert.assertNotNull(bto);
		String CSRUser = getUser(CSR);
		Assert.assertNotEquals(CSRUser , "");
		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser1, "");
		String purchaser2 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotEquals(purchaser2, "");
		ArrayList<String> userSet= new ArrayList<>();
		userSet.add(CSRUser);
		userSet.add(purchaser2);
		userSet.add(purchaser1);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("MDCPID", mdcpid);
		data.put("emailID", purchaser1);
	
		// Get URL
		setEnvironment();
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
		logger.info("<b>Pre-Condition Starts: Creating Quote & Sharing with Another User </b>");
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>","<b>Creating Quote & Sharing with Another User </b>");

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with CSR user", url,CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Impersonate purchaser.","Impersonated successfully.", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog","Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("Delete product","Product should be deleted","CE", false));

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

		String quoteReferenceNumber = quoteDetails.getQuoteData(quoteDetailsList, "Quote Number");
		Assert.assertNotEquals(quoteReferenceNumber, null);

		Assert.assertTrue(quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user", "Quote Should be shared successfully", purchaser2, true));

		usersAvailability.replace(purchaser1, "Free");
		userSet.remove(purchaser1);
		updateUserStatus(purchaser1, "Free");

		Assert.assertTrue(quoteDetails.clickOnCustomerServiceLinkAndClickOnOkButtonInsideTerminateSessionPopup("Terminate the current session", "Customer Service Page Should be displayed for New Session ",purchaser2, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.","Buy on behalf is done successfully.", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog","Requested org & catalog should be selected", data, true));

		logger.info("<b>Pre-Condition Ends : Quote Created & Shared Successfully</b>");
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Ends</b>","<b>Quote Created & Shared Successfully</b>");

		/** Pre-Condition ends **/

		accountSummary = login.navigateToMyAccount("Step 1: Click on 'My Account'","My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		//Assert.assertTrue(accountSummary.navigateToQuotes("Step 2 : Click on Quotes link on Left hand side","Quote listing page should be displayed with list of quotes available."));
		Assert.assertTrue(accountSummary.clickAndNavigateToLinksUnderMyAccount("Step 2 : Click on Quotes link on Left hand side","Quote listing page should be displayed with list of quotes available","Quotes"));
		
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
	 * Quote_S4_Verify that Summarized Logistical view display Service description, SKU and Price in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/294882
	 * @since May 18, 2021 11:59:23 AM
	 * @author drharsh
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
			IGroupsTagging.IUserType.DIRECT, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C294882_Regression_CE_Quotes_VerifySummarizedLogisticalviewdisplayServicedescriptionSKU_Direct(){

		//Reporting info
		initializeReporting("Quote_S4_Verify that Summarized Logistical view display Service description, SKU and Price in Quote creation and Quote confirmation page",
				"C294882_Regression_CE_Quotes_VerifySummarizedLogisticalviewdisplayServicedescriptionSKU_Direct", logger);

		//Test data
		String scenarioId = "ID03";
		Map<String, String> regData = getScenarioData(scenarioId);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String mdcpId = regData.get("MDCP ID");
		String catalogName = regData.get("Contract");
		String bundle = getProductsByProductTypeAndCategory(EMEA, BUNDLE, MONITORS).get(0);
		String softBundle = getProductsByProductTypeAndCategory(EMEA, SOFTBUNDLE, MONITORS).get(0);
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability
		String purchaser = getUser(scenarioId, PURCHASER);
		Assert.assertNotEquals(purchaser, "");
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		//		HashMap<String, String> regData = getTestData("CE", "DT008_EMEA");

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		String contractID = getS4ContractIDFromS4AndWslSheet(EMEA, mdcpId, catalogName, "Purchase Order", "Yes").get(0);
		String quoteName = "Aut_Quote_";
		String paymentMethod = "Purchase Order";
		String shippingOption = "Default";

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		String serviceDesc = "Default;Door / dock delivery;Consolidated delivery;Special delivery equipment required;"
				+ "Forklift is required at delivery site;Driver must unload the truck alone (non std practice);"
				+ "Two people required at delivery desk;"
				+ "Pre-alert with booking slot timed (For service description and list of countries not supporting the service, please click here);"
				+ "Euro pallet 6 ft max pallet height;Delivery Tuesday at 1pm;No appointment required;Any working day at any time";

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Direct user", url, purchaser, password, true));

		/** Pre-Condition ends **/

		Assert.assertTrue(login.selectOrganizationAndContract(
				"Step 1 & 2 : Click on Organization and Catalog dropdown and Select Catalog under Catalog dropdown and Click on Apply and Ok in Overlay",
				"Selected catalog should be loaded", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete products", "Products should be deleted", "CE", false));

		Assert.assertNotNull(login.searchSKU("Step 3: In search box, search for Bundle sku", "PDP of searched product should be displayed", bundle));

		Assert.assertTrue(pdp.enterProductQuantityforBundle("Step 4: Enter Qty as 3 and update", "Qty should be updated successfully", "3"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Click on 'Add to cart' button", "Product should be added to cart", "pdp"));

		Assert.assertNotNull(pdp.searchSKU("Step 6: In search box, search for Soft Bundle sku", "PDP of searched product should be displayed", softBundle));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 7: Enter Qty as 2 and update", "Qty should be updated successfully", "2"));

		Assert.assertTrue(pdp.addProductToCart("Step 8: Click on 'Add to cart' button", "Product should be added to cart", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Step 9: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed"));

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 10: Click on 'Save as quote' button", "'Quote creation page' should be displayed"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 11: Select Payment method as 'Purchase Order'", 
				"Payment method should be selected", paymentMethod));

		LinkedHashMap<String, String> selectedS4ContractAddressDetails =createNewQuote.clickOnChangeBillingAddressEnterContractIDandVerifySelectedContractID(
				"Step 12: Click on 'Change billing address'<br>"
						+ "Step 13: Select Search Criteria as 'S4 Contract Id' and Enter S4 Contract Id<br>"
						+ "Step 14: click on Search icon and Click OK", "Selected Billing address must be displayed", contractID);
		Assert.assertNotEquals(selectedS4ContractAddressDetails, null);

		Assert.assertTrue(createNewQuote.verifyContractIdInBillingInformation(
				"Step 15: verify the S4 Contract Id in Billing address", "S4 Contract Id should be displayed", contractID));

		Assert.assertTrue(createNewQuote.selectPreconfiguredProfileFromDropDown("Step 16: Select below Shipping Option(s)<br>"
				+ "1. preconfigured profile as 'Default'", "Option should be selected accordingly", shippingOption));

		Assert.assertTrue(createNewQuote.verifyLogisticalServices(
				"Step 17: Verify that Logistical Services section is displayed between cart summary and Total price",
				"Logistical Services section should be displayed between cart summary and Total price", true));

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Step 18: Verify the Summarized Logistical view in Quote creation page",
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description", serviceDesc, true));

		Assert.assertTrue(createNewQuote.verifyShippingChargesInSummarizedLogisticalView("Step 19: Verify the Shipping & handling charges", 
				"Shipping & handling charges should not be 0", true));

		Assert.assertNotNull(createNewQuote.createQuote("Step 20: Enter all mandatory details and Click on 'Save quote' button",
				"Quote should be created successfully", quoteName, purchaser));

		String contractId = quoteDetails.getContractId("Step 21: Verify the S4 Contractid in 'Quote detail page'", "S4 Contractid should be displayed");
		Assert.assertNotNull(contractId);

		Assert.assertTrue(createNewQuote.verifySummarizedLogisticalView("Step 22: Verify the Summarized Logistical view in Quote confirmation page", 
				"Summarized Logistical view should be displayed as below:<br>Logistical Services Profile:<br>Service description",serviceDesc, true));

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargesValue("Step 23: Verify Shipping & Handling Charges Value", "Shipping & Hnadling Charges should be fetched and not equal to 0.00", false));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_Verify Quote creation with SPC and export quote for PDF
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300038
	 * @since May 10, 2021 3:36:32 PM
	 * @author Vijay
	 * @throws IOException 
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, 
			groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
					IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300038_Regression_CE_Quotes_VerifyQuoteCreationWithSpcAndExportQuoteForPDF_Direct(String region) throws IOException  {

		// Reporting info
		initializeReporting("Verify user is able to share existing quote from home page", 
				"C300038_Regression_CE_Quotes_VerifyQuoteCreationWithSpcAndExportQuoteForPDF_Direct", region, logger);

		// Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String spc = regData.get("SPC");
//		String bto = "";
//		if(region.equals(EMEA))
//			bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, MONITORS).get(1);
//		else bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, LAPTOPS).get(0);
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
	    String cto = getProductByDataSetID(region, dataIDs.get(region),CTO);
		Assert.assertNotNull(cto);
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		String quoteName = "Aut_Quote_";

		// Get URL
		setEnvironment();
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

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with Purchaser user.", url, purchaser, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
		/** Precondition Ends **/

		pdp = customerService.searchSKU("Step 1: Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 2: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		pdp = customerService.searchSKU("Step 3: Search with CTO SKU ", "Requested product PDP should load",cto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 4: Enter Qty as 200 and update", 
				"Qty should be updated successfully", "200", "pdp"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Add CTO product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 6: Click on 'Mini cart' icon and Click on 'Go to cart' button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		ArrayList<String> subtotalBeforeAndAfterSPC = shoppingCart.enterValidSPCAndClickOnApplyAndVerifySubtotalAndStrikedOutPrice("Step 7: Enter valid 'Special pricing code'in text box and click on 'Apply' button", 
				"SPC should be applied successfully and strike through price is updated",spc,"Shopping cart");
		Assert.assertTrue(subtotalBeforeAndAfterSPC.size()>2);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 8: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 9: Select payment method as 'Purchase Order'", 
				"Payment method 'Purchase Order' should be selected ", "Purchase Order"));

		LinkedHashMap<String, ArrayList<String>> productSkuAndpriceDetailsAfterSpc = 
				createNewQuote.getReducedPriceStrikedPriceAndExpDateAfterSpc("Precondition: ",
						"Product reduced price, Striked price and Expiry date after applying SPC should displays", Arrays.asList(bto));
		Assert.assertNotEquals(productSkuAndpriceDetailsAfterSpc, null);

		Assert.assertTrue(createNewQuote.verifyBTOSpecsInCreateNewQuotePage(
				"Step 10: Verify BTO spec's on Create quote page", "BTO spec's should be displayed on quote details page"));

		Assert.assertTrue(createNewQuote.verifyCartSummarySection("Step 11: Verify Cart summary section for SPC in create quote page",
				"Cart Summary section should display  with all details"));

		quoteDetails = createNewQuote.createQuote("Step 12: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully and navigated to quote confirmation page.",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Get Estimated Tax Value", "Estimated tax price is fetched", true);

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Get Shipping & Hnadling Charges Value", "Shipping & Hnadling Charges should be fetched", false);

		List<String> ctoSpecs = quoteDetails.getCTODetailsInQuoteDetails("Getiing list od cto specs", "Got ot", false);
		String espot = quoteDetails.getESPOTInQuoteDetails("Getting Espot");
		System.out.println(espot);
		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 13: Verify quote details in quote confirmation page",
				"Quote details page should have below details");
		Assert.assertNotEquals(quoteDetailsList, null);

		System.out.println(quoteDetailsList);
		String quoteNameValue=quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, "");

		Assert.assertTrue(login.clickOnHomeTab("Precondition: Go to Home page", "Home page should be displayed", true));

		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 14.1: Click on quotes under Orders and Quotes tab in Home Page", 
				"Quote listing page should be displayed with list of quotes created", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 14.2: Search quote number", "Quote should be searched successfully","Quote Name", quoteNameValue, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 15: Click on newly 'Serached Quote number'", "Quote details page should be displayed with below details : <br>"
				+ "Print <br>"
				+ "Export <br>"
				+ "Email <br>"
				+ "Share quote <br>"
				+ "Order Information <br>"
				+ "Billing address <br>"
				+ "Shipping address", quoteNumber));

		LinkedHashMap<String, String> quoteInfoDetailsForPdf = quoteDetails.getInformationDetails(
				"Pre Condition: Fetch Information & Details from Quote Details Page",
				"All details Should be fetched Successfully", "pdf", purchaser);
		Assert.assertNotNull(quoteInfoDetailsForPdf);

		LinkedHashMap<String, String> quoteBillingInfoDetailsForPdf = quoteDetails.getBillingInformationDetails(
				"Pre Condition: Fetch Billing Information from Quote Details Page.",
				"All details Should be fetched Successfully", "pdf");
		Assert.assertNotNull(quoteBillingInfoDetailsForPdf);

		LinkedHashMap<String, String> quoteShippingInfoDetailsForPdf = quoteDetails.getShippingInformationDetails(
				"Pre Condition: Fetch Shipping Information from Quote Details Page.",
				"All details Should be fetched Successfully", "pdf");
		Assert.assertNotNull(quoteShippingInfoDetailsForPdf);

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 16.1: Click on 'Export' option in quote detail page", "Succesfullly Clicked on Export Button"));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 16.2: Select the export type as PDF", "File type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 16.3: Click on 'Export' in quote confirmation page", "PDF format should get exported successfully"));

		String pdfValue  = pdfValidations.readPdfFileInDownloads("Step 17: Go to the file location and click on Quote to open",
				"Quote should get displayed successfully", quoteNameValue + ".pdf",true);
		Assert.assertNotEquals(pdfValue, null);

		ArrayList<String> pdfHeaderDetails = new ArrayList<String>();
		pdfHeaderDetails.add(quoteNameValue);
		pdfHeaderDetails.add(quoteNumber);
		Assert.assertTrue(pdfValidations.verifyHPPropreiteryDetailsInPDF("Step 18.1: Verify following are displayed <br>"
				+ "HP Logo <br>"
				+ "Quote Name <br>"
				+ "Quote number at top right side corner <br>"
				+ "HP Proprietary Information for customer use only <br>"
				+ "Do not share", "All the details should get displayed successfully.",pdfValue,pdfHeaderDetails));
		Assert.assertTrue(pdfValidations.verifyESpotDetailsInPDF("Step 18.2: Verify All displyed ESPOT Details", "ESPOT should be displayed in pdf", pdfValue, espot));

		Assert.assertTrue(pdfValidations.verifyInformationAndDetails(
				"Step 19: Verify the following details and headers are displaying under Information and Details.",
				"All the details should get displayed successfully", pdfValue, quoteInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyBillingAddressDetails(
				"Step 20: Verify the following details and headers are displaying under Billing Information.",
				"All the details should get displayed successfully", pdfValue, quoteBillingInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyShippingAddressDetails(
				"Step 21: Verify the following details and headers are displaying under Shipping Information.",
				"All the details should get displayed successfully", pdfValue,region,
				quoteShippingInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyComments("Step 22: Verify Comments.",
				"Comments should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyInvoiceInstructions("Step 23: Verify the Invoice Instructions.",
				"Invoice Instructions should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyShippingInstructions("Step 24: Verify the Shipping Instructions.",
				"Shipping Instructions should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyQuoteSummaryDetails(
				"Step 25: Verify Quote summary Header is displaying on Quote summary <br>"
						+ "Step 26: Verify Product Description Header is displaying on Quote summary <br>"
						+ "Step 27: Verify Manufacturer# Header is displaying on Quote summary <br>"
						+ "Step 28: Verify Pricing Source header is displaying on quote summary <br>"
						+ "Step 29: Verify Quantity Header is displaying on Quote summary <br>"
						+ "Step 30: Verify Unit Price Header is displaying on quote summary <br>"
						+ "Step 31: Verify Total Price Header is displaying on quote summary <br>"
						,"All the details should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyStrikeOutPriceAndExpiryDate(
				"Step 32: Verify Strike out price "
						+ "Step 33: Verify expiry date is displaying after applying spc.",
						"Strike out price and expiry date should get displayed successfully", pdfValue,
						productSkuAndpriceDetailsAfterSpc));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxInPDF("Step 34: Verify Estimated tax line item(if enabled) is displaying in Quote export",
				"Estimated tax line item should get displayed in Quote export successfully",estimatedTax,pdfValue));

		Assert.assertTrue(pdfValidations.verifyShippingAndHandlingChargeInPDF("Step 35: Verify the shipping and handling charges","Shipping and handling charges should be the sum of Logistical sku charges", pdfValue,shippingCharges));

		Assert.assertTrue(pdfValidations.verifyCTOSpecsInPDF("Step 36: Verify CTO spec's.","CTO Spec's should be displayed",
				pdfValue,ctoSpecs));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");


	}


	/**
	 * Verify all the details in Quote details page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/301086
	 * @param region APJ,AMS-LA,EMEA,AMS-US
	 * @since May 7, 2021 3:46:57 PM
	 * @author ThomasAn
	 */	
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C301086_Regression_CE_Quote_VerifyAllTheDetailsInQuoteDetailsPage_Direct(String region) {

		// Reporting info
		initializeReporting("Verify all the details in Quote details page",
				"C301086_Regression_CE_Quote_VerifyAllTheDetailsInQuoteDetailsPage_Direct",region,logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01","ID03","ID02","ID05");
		Map<String, String> regData = getScenarioData(dataIDs,region);
		
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
	    String orgName = regData.get("Org Name");
	    String password = commonData.get(CSRORFEDCSRUSERPWD);
	     
	    Map<String, String> data = new HashMap<String, String>();
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		String qty = "10";
	        
	    String username = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(username);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(username));
		
		String bundle = getProductByDataSetID(region, dataIDs.get(region),BUNDLE); 
		Assert.assertNotNull(bundle);
		
		// Get URL
		setEnvironment();
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

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/
		
		pdp = customerService.searchSKU("Step 1: Search with bundle SKU", "Requested product PDP should load", bundle);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2: Enter the Qty as " + qty, "Quantity should be entered", qty));

		Assert.assertTrue(pdp.addProductToCart("Step 3: Add Bundle product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on mini cart icon and go to cart button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: Click on save as quote button", "Create New Quote' page should be displayed");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyBTOSpecsInCreateNewQuotePage("Step 6 : Verify BTO specs on create new quote page",
				"BTO Spec's should be displayed on create quote page."));

		HashMap<String, String> data1 = PageGenerics.getCurrentSystemDateAndEndDate(30);

		Assert.assertTrue(createNewQuote.verifyQuoteNameSectionAndFieldValidation("Step 7 & 8: Verify quote name section and validate fields",
				"Quote name section should be verified and fileds should be validated", data1));

		Assert.assertTrue(createNewQuote.verifyOrderInformationSectionAndFieldValidation(
				"Step 9 & 10: Verify order information section and validate fields",
				"Order Information section should be verified and fileds should be validated"));

		Assert.assertTrue(createNewQuote.verifyPurchaserContactInformationSectionAndFieldValidation(
				"Step 11 &12 : Verify purchaser contact information  section and validate fields on click on edit",
				"Purchaser contact information section should be verified and fileds should be validated on click on edit"));

		Assert.assertTrue(createNewQuote.verifyDefaultSelectedPaymentMethod("Step 13:Verify default payment method","Default payment method should be displayed","PurchaseOrder"));

		Assert.assertTrue(createNewQuote.verifyBillingAddress("Step 14 : Verify billing address session in create quote page",
						"User should be able to see the billing address session"));

		Assert.assertTrue(createNewQuote.verifyChangeBillingAddressButton("Step 15.1 : Verify change billing address button",
						"User should be able to see change billing address button"));

		Assert.assertTrue(createNewQuote.clickOnChangeBillingAddressAndVerifyPopupWindow(
						"Step 15.2: Click on change billing address button under billing address section",
						"User should click change billing address and verify pop up window"));

		Assert.assertTrue(createNewQuote.verifyShippingAddressInCreateNewQuote("Step 16: Verify Shipping information section in create quote page",
				"Default shipping address should be displayed"));

		Assert.assertTrue(createNewQuote.clickOnChangeShippingAddressButtonAndVerifyPopUpFileds(
				"Step 17: Click on Change shipping address button under shipping address section",
				"User should click change shipping address and verify pop up window"));

		Assert.assertTrue(createNewQuote.verifyCartSummarySection("Step 18 : Verify Cart summary section in create quote page",
				"Cart summary section should be verified"));
		Assert.assertTrue(createNewQuote.verifyBackToShoppingCartButton("Step 19 : Verify Back to shopping cart button in create quote page",
				"Back to shopping cart button should be verified"));
		
		quoteDetails = createNewQuote.createQuote("Step 20: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully", "QuoteValue",username);
		Assert.assertNotEquals(quoteDetails, null);
		
		List<String> quotesList = quoteDetails.getQuoteDetailsForVerification("Step 21 :Verify quote details in quote confirmation page",
				"Quote details should be displayed");
		Assert.assertNotEquals(quotesList, quotesList.isEmpty());

		Assert.assertTrue(quoteDetails.verifyBTOSpecsInQuoteDetailsPage("Step 22 : Verify BTO spec's on Quote details page",
				"BTO Specs should be verified"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	/**
	 * Verify user is able to share quote from quote details page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300031
	 * @since May 3, 2021 1:13:55 PM
	 * @author Keshav
	 */
	@Test(dataProvider = "region_data-provider",dataProviderClass = HP2BDataProvider.class,
			groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.PARTNERAGENT,
					IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300031_Regression_CE_Quotes_Quote_VerifyUserIsAbleTtoShareQuoteFromQuoteDetailsPage_PartnerAgent(String region) {

		//Reporting info
		initializeReporting("Verify user is able to share quote from quote details page",
				"C300031_Regression_CE_Quotes_Quote_VerifyUserIsAbleTtoShareQuoteFromQuoteDetailsPage_PartnerAgent",region,
				logger);

		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");

		//String bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)),"BTO","Laptops").get(0);
		//String softBundle = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)),"SoftBundle","Desktops").get(0);
		
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
		String softBundle = getProductByDataSetID(region, dataIDs.get(region),SOFTBUNDLE);
		Assert.assertNotNull(softBundle);
		String password = passwords.get(DIRECTUSERPWD);  
		System.out.println(password);

		String partnerAgent = getUser(dataIDs.get(region), PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser1);
		System.out.println(purchaser1);
		String purchaser2 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser2);
		System.out.println(purchaser2);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser1);
		users.add(purchaser2);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Buy On Behalf");
		data.put("emailID", purchaser1);


		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);

		//		String partnerAgentId = login.getPartnerAgentID(username, partnerAgentIds, partnerAgents);
		//		Assert.assertNotEquals(partnerAgentId, null);

		/** Pre-Condition Starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with Partner agent user.", url, partnerAgent,password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Buy on behalf purchaser.", "Buy on behalf is done successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		/** Pre-Condition Ends **/

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 1 : In search box , search for soft bundle number",
				"PDP of searched product should be displayed", softBundle);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.clickOptionCodeDropdownAndSelectOption("Step 2.1: Select option code", "Option code selected.",true));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 2.2: Enter the Qty as " + 400 + " and update", "Qty should be updated successfully", "400"));

		Assert.assertTrue(pdp.addProductToCart("Step 3.1:  Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		ShoppingCart shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5.1: Click on save as quote button", "'Create New Quote' page should be displayed with the list of products added in cart page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(shoppingCart.verifyFavoriteProductIsAvailableInCartPage("Step 5.2: Verify the product added is displayed in Create New Quote page", 
				"Product added is displayed in create new quote page",softBundle));

		//		Assert.assertTrue(createNewQuote.verifyPartnerAgentId("Step 6: Verify Partner Agent field", "Partner ID number should be auto populated",
		//				partnerAgentId, true));

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
		Assert.assertTrue(login.loginToHP2B("Step 12.1: Login to HP2B with other user and check for shared quote", purchaser2, password, true));


		accountSummary = login.navigateToMyAccount("Step 12.2: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 12.3: Click on 'Quotes' under orders and quotes", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 12.4: Search Quote with quote number", "Quote details should display",
				"Quote Number", quoteNumber, false));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
	
	// New TestCases with different IDs after changes
	
	/**
	 * Verify contract surcharge in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300038
	 * @since May 10, 2021 3:36:32 PM
	 * @author Vijay
	 * @throws IOException 
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, 
			groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
					IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300038_Regression_CE_Quote_VerifyContractSurchargeInQuoteCreationAndQuoteConfirmationPage_Direct(String region) throws IOException  {

		// Reporting info
		initializeReporting("Verify contract surcharge in Quote creation and Quote confirmation page", 
				"C300038_Regression_CE_Quote_VerifyContractSurchargeInQuoteCreationAndQuoteConfirmationPage_Direct", region, logger);

		// Test Data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01", "ID03", "ID02", "ID05");
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String spc = regData.get("SPC");
//		String bto = "";
//		if(region.equals(EMEA))
//			bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, MONITORS).get(1);
//		else bto = getProductsByProductTypeAndCategory(getRegion(region, dataIDs.get(region)), BTO, LAPTOPS).get(0);
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
	    String cto = getProductByDataSetID(region, dataIDs.get(region),CTO);
		Assert.assertNotNull(cto);
		String kit = getProductByDataSetID(region, dataIDs.get(region),KIT);
	    Assert.assertNotNull(kit);
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		String quoteName = "Aut_Quote_";

		// Get URL
		setEnvironment();
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
		
		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition Starts</b>", "<b>Creating Quote & Sharing with Another User </b>");

		Assert.assertTrue(login.loginToHP2B("Login to HP2B with Purchaser user.", url, purchaser, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 & 2: Select requested org & catalog",
				"Requested org & catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
	
		pdp = customerService.searchSKU("Step 3: Search with BTO SKU", "Requested product PDP should load",bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 4: Add BTO product to cart at PDP", "Product should be added to cart","pdp"));

		pdp = customerService.searchSKU("Step 5: Search with CTO SKU ", "Requested product PDP should load",cto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 6: Enter Qty as 200 and update", 
				"Qty should be updated successfully", "200", "pdp"));

	//	Assert.assertTrue(pdp.addProductToCart("Step 7: Add CTO product to cart at PDP", "Product should be added to cart","pdp"));
		
		pdp = customerService.searchSKU("Step 7: In search box, search for kit number", 
				"PDP of searched product should be displayed", kit);
		Assert.assertNotEquals(pdp, null);


		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 8: Click on 'Mini cart' icon and Click on 'Go to cart' button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

	/*	ArrayList<String> subtotalBeforeAndAfterSPC = shoppingCart.enterValidSPCAndClickOnApplyAndVerifySubtotalAndStrikedOutPrice("Step 7: Enter valid 'Special pricing code'in text box and click on 'Apply' button", 
				"SPC should be applied successfully and strike through price is updated",spc,"Shopping cart");
		Assert.assertTrue(subtotalBeforeAndAfterSPC.size()>2); */

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 9: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 10: Select payment method as 'Purchase Order'", 
				"Payment method 'Purchase Order' should be selected ", "Purchase Order"));

	/*	LinkedHashMap<String, ArrayList<String>> productSkuAndpriceDetailsAfterSpc = 
				createNewQuote.getReducedPriceStrikedPriceAndExpDateAfterSpc("Precondition: ",
						"Product reduced price, Striked price and Expiry date after applying SPC should displays", Arrays.asList(bto));
		Assert.assertNotEquals(productSkuAndpriceDetailsAfterSpc, null);

		Assert.assertTrue(createNewQuote.verifyBTOSpecsInCreateNewQuotePage(
				"Step 10: Verify BTO spec's on Create quote page", "BTO spec's should be displayed on quote details page"));
             */
		Assert.assertTrue(createNewQuote.verifyCartSummarySection("Step 11: Verify Cart summary section for SPC in create quote page",
				"Cart Summary section should display  with all details"));

		quoteDetails = createNewQuote.createQuote("Step 12: Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully and navigated to quote confirmation page.",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

	/*	String estimatedTax = quoteDetails.getEstimatedTaxValue("Get Estimated Tax Value", "Estimated tax price is fetched", true);

		String shippingCharges = quoteDetails.getShippingAndHandlingChargesValue("Get Shipping & Hnadling Charges Value", "Shipping & Hnadling Charges should be fetched", false);

		List<String> ctoSpecs = quoteDetails.getCTODetailsInQuoteDetails("Getiing list od cto specs", "Got ot", false);
		String espot = quoteDetails.getESPOTInQuoteDetails("Getting Espot");
		System.out.println(espot);
		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 13: Verify quote details in quote confirmation page",
				"Quote details page should have below details");
		Assert.assertNotEquals(quoteDetailsList, null);

		System.out.println(quoteDetailsList);
		String quoteNameValue=quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		Assert.assertNotEquals(quoteNumber, "");

		Assert.assertTrue(login.clickOnHomeTab("Precondition: Go to Home page", "Home page should be displayed", true));

		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 14.1: Click on quotes under Orders and Quotes tab in Home Page", 
				"Quote listing page should be displayed with list of quotes created", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 14.2: Search quote number", "Quote should be searched successfully","Quote Name", quoteNameValue, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 15: Click on newly 'Serached Quote number'", "Quote details page should be displayed with below details : <br>"
				+ "Print <br>"
				+ "Export <br>"
				+ "Email <br>"
				+ "Share quote <br>"
				+ "Order Information <br>"
				+ "Billing address <br>"
				+ "Shipping address", quoteNumber));

		LinkedHashMap<String, String> quoteInfoDetailsForPdf = quoteDetails.getInformationDetails(
				"Pre Condition: Fetch Information & Details from Quote Details Page",
				"All details Should be fetched Successfully", "pdf", purchaser);
		Assert.assertNotNull(quoteInfoDetailsForPdf);

		LinkedHashMap<String, String> quoteBillingInfoDetailsForPdf = quoteDetails.getBillingInformationDetails(
				"Pre Condition: Fetch Billing Information from Quote Details Page.",
				"All details Should be fetched Successfully", "pdf");
		Assert.assertNotNull(quoteBillingInfoDetailsForPdf);

		LinkedHashMap<String, String> quoteShippingInfoDetailsForPdf = quoteDetails.getShippingInformationDetails(
				"Pre Condition: Fetch Shipping Information from Quote Details Page.",
				"All details Should be fetched Successfully", "pdf");
		Assert.assertNotNull(quoteShippingInfoDetailsForPdf);

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 16.1: Click on 'Export' option in quote detail page", "Succesfullly Clicked on Export Button"));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 16.2: Select the export type as PDF", "File type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 16.3: Click on 'Export' in quote confirmation page", "PDF format should get exported successfully"));

		String pdfValue  = pdfValidations.readPdfFileInDownloads("Step 17: Go to the file location and click on Quote to open",
				"Quote should get displayed successfully", quoteNameValue + ".pdf",true);
		Assert.assertNotEquals(pdfValue, null);

		ArrayList<String> pdfHeaderDetails = new ArrayList<String>();
		pdfHeaderDetails.add(quoteNameValue);
		pdfHeaderDetails.add(quoteNumber);
		Assert.assertTrue(pdfValidations.verifyHPPropreiteryDetailsInPDF("Step 18.1: Verify following are displayed <br>"
				+ "HP Logo <br>"
				+ "Quote Name <br>"
				+ "Quote number at top right side corner <br>"
				+ "HP Proprietary Information for customer use only <br>"
				+ "Do not share", "All the details should get displayed successfully.",pdfValue,pdfHeaderDetails));
		Assert.assertTrue(pdfValidations.verifyESpotDetailsInPDF("Step 18.2: Verify All displyed ESPOT Details", "ESPOT should be displayed in pdf", pdfValue, espot));

		Assert.assertTrue(pdfValidations.verifyInformationAndDetails(
				"Step 19: Verify the following details and headers are displaying under Information and Details.",
				"All the details should get displayed successfully", pdfValue, quoteInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyBillingAddressDetails(
				"Step 20: Verify the following details and headers are displaying under Billing Information.",
				"All the details should get displayed successfully", pdfValue, quoteBillingInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyShippingAddressDetails(
				"Step 21: Verify the following details and headers are displaying under Shipping Information.",
				"All the details should get displayed successfully", pdfValue,region,
				quoteShippingInfoDetailsForPdf));

		Assert.assertTrue(pdfValidations.verifyComments("Step 22: Verify Comments.",
				"Comments should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyInvoiceInstructions("Step 23: Verify the Invoice Instructions.",
				"Invoice Instructions should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyShippingInstructions("Step 24: Verify the Shipping Instructions.",
				"Shipping Instructions should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyQuoteSummaryDetails(
				"Step 25: Verify Quote summary Header is displaying on Quote summary <br>"
						+ "Step 26: Verify Product Description Header is displaying on Quote summary <br>"
						+ "Step 27: Verify Manufacturer# Header is displaying on Quote summary <br>"
						+ "Step 28: Verify Pricing Source header is displaying on quote summary <br>"
						+ "Step 29: Verify Quantity Header is displaying on Quote summary <br>"
						+ "Step 30: Verify Unit Price Header is displaying on quote summary <br>"
						+ "Step 31: Verify Total Price Header is displaying on quote summary <br>"
						,"All the details should get displayed successfully", pdfValue));

		Assert.assertTrue(pdfValidations.verifyStrikeOutPriceAndExpiryDate(
				"Step 32: Verify Strike out price "
						+ "Step 33: Verify expiry date is displaying after applying spc.",
						"Strike out price and expiry date should get displayed successfully", pdfValue,
						productSkuAndpriceDetailsAfterSpc));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxInPDF("Step 34: Verify Estimated tax line item(if enabled) is displaying in Quote export",
				"Estimated tax line item should get displayed in Quote export successfully",estimatedTax,pdfValue));

		Assert.assertTrue(pdfValidations.verifyShippingAndHandlingChargeInPDF("Step 35: Verify the shipping and handling charges","Shipping and handling charges should be the sum of Logistical sku charges", pdfValue,shippingCharges));

		Assert.assertTrue(pdfValidations.verifyCTOSpecsInPDF("Step 36: Verify CTO spec's.","CTO Spec's should be displayed",
				pdfValue,ctoSpecs)); */

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	

	/*New Test Cases */
	
	/**
	 * Quote_Verify Estimated Tax in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300040
	 * @since Mar 15, 2022
	 * @author Manjunath
	 */
	@Test(dataProvider = "apjAndAmsRegion_data-provider", dataProviderClass = HP2BDataProvider.class, 
			groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300040_Regression_CE_Quotes_VerifyEstimatedTaxInQuoteCreationAndQuoteConfirmationPage_Direct(String region) {

		//Reporting info
		initializeReporting("Quote_Verify Estimated Tax in Quote creation and Quote confirmation page",
				"C300040_Regression_CE_Quotes_VerifyEstimatedTaxInQuoteCreationAndQuoteConfirmationPage_Direct", region, logger);

		//Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMapForApjAndAmsNa(ID01, ID02);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String bto =  getProductByDataSetID(region, dataIDs.get(region), BTO);
		String password = passwords.get(DIRECTUSERPWD);

		// Waiting for user availability
		//String purchaser = "hp2bfeautomation+PurAMSNA001@gmail.com";
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		String quoteName = "AutQuote";

		//Get URL
		setEnvironment();
		String url = this.url;

		//Page Objects
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Purchaser user", url, purchaser, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The HP2B home page is refreshed with the selected org and contract.", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		Assert.assertNotNull(login.searchSKU("Step 3: In search box , search for BTO sku", "PDP of searched product should be displayed", bto));

		Assert.assertTrue(pdp.enterFirstProductQuantity("Step 4: Enter Qty as 99 and update", "Qty updated successfully", "99", "pdp"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Step 6: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 7: Click on 'save as quote' link", "'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 8: Verify Estimated tax under cart summary section in Create quote page", 
				"Estimated tax should be displayed and it should be included in totall", true));

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxPrice("Step 9: Verify estimated tax price if qty of the product is increased", 
				"Estimated tax price should be updated accordingly as per the qty of the product", "200", true));

		quoteDetails = createNewQuote.createQuote("Step 10: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page.", quoteName, purchaser);
		Assert.assertNotNull(quoteDetails);

		Assert.assertTrue(quoteDetails.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 11: Verify estimated tax in Quote confirmation page", 
				"Estimated tax should be displayed and it should be included in total.", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Quote_Verify regulatory fee in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/294949
	 * @param region EMEA, AMS-US
	 * @since Mar 16,2022
	 * @author Manjunath
	 * @throws IOException 
	 */
	@Test(dataProvider = "emeaAndAmsRegion_data-provider", dataProviderClass = HP2BDataProvider.class, 
			groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C294949_Regression_CE_Quotes_VerifyRegulatoryFeeInQuoteCreationAndQuoteConfirmationPage_CSR(String region) throws IOException {

		initializeReporting("Quote_Verify regulatory fee in Quote creation and Quote confirmation page", 
				"C294949_Regression_CE_Quotes_VerifyRegulatoryFeeInQuoteCreationAndQuoteConfirmationPage_CSR", region, logger);

		// Test data		
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID02,ID03);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);


		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, LAPTOPS);		
		Assert.assertNotNull(bto); 
		String kit = getProductByDataSetID(region, dataIDs.get(region), KIT, DESKTOPS);		
		Assert.assertNotNull(bto);

		// Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotNull(csr);
		userSet.add(csr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("actionOnUsers", actionOnUsers);
		data.put("MDCPID", mdcpid);
		data.put("emailID", purchaser);

		String quoteName = "autQuote";
		String paymentMethod = "Purchase Order";
		String emailId = purchaser;

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);

		/** Pre-Condition starts **/

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate a purchaser.", "Impersonate is done successfully.", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.",
				"The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
						+ "The HP2B home page is refreshed with the selected org and contract.", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product","Product should be deleted","CE", false));
		
		/** Pre-Condition ends **/
		
		Assert.assertNotNull(login.searchSKU("Step 3: In search box , search for BTO sku", "PDP of searched product should be displayed", bto));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 4: Enter Qty as 500 and update", "Qty should be updated successfully", "500"));

		Assert.assertTrue(pdp.addProductToCart("Step 5: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.searchSKU("Step 6: In search box , search for KIT sku", "PDP of searched product should be displayed", kit));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 7: Enter Qty as 20 and update", "Qty should be updated successfully", "20"));

		Assert.assertTrue(pdp.addProductToCart("Step 8: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Step 9: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 10: Click on 'save as quote' button", 
				"'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 11: Select Payment method as 'Purchase Order'", "Payment method is selected", paymentMethod));

		Assert.assertNotNull(createNewQuote.verifyRegulatoryFeeIsDisplayed("Step 12: Verify Regulatory fee details under cart summary section in Create quote page", 
				"Regulatory fee should be displayed as per the products qty and it should be included in total", true));

		quoteDetails = createNewQuote.createQuote("Step 13: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", quoteName, emailId);
		Assert.assertNotNull(quoteDetails);

		Assert.assertNotNull(quoteDetails.verifyRegulatoryFeeIsDisplayed("Step 14: Verify regulatory fee details in quote confirmation page", 
				"Regulatory fee should be displayed as per the products qty and should be include in the total.", true));


		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}

	/**
	 * Quote_Verify contract surcharge in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300038
	 * @param region APJ, EMEA, AMS-NA
	 * @since Mar 16,2022
	 * @author Manjunath
	 * @throws IOException 
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, 
			groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300038_Regression_CE_Quotes_VerifyContractSurchargeInQuoteCreationAndQuoteConfirmationPage_Direct(String region) throws IOException {

		initializeReporting("Quote_Verify contract surcharge in Quote creation and Quote confirmation page", 
				"C300038_Regression_CE_Quotes_VerifyContractSurchargeInQuoteCreationAndQuoteConfirmationPage_Direct", region, logger);
		
		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID06, ID12, ID07);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String password = passwords.get(DIRECTUSERPWD);	
		//String bto = "D9Y32AA";//getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);
		//String bto = commonData.get("ProductsForContractSurcharge_" + region).split(";")[0];
		//String kit = commonData.get("ProductsForContractSurcharge_" + region).split(";")[1];
		String bto =getProductByDataSetID(region, dataIDs.get(region), BTO); // "5FD36UC";
		Assert.assertNotNull(bto);
		String kit =  getProductByDataSetID(region, dataIDs.get(region), KIT); //"423003";
		Assert.assertNotNull(kit);
		
		
//		String purchaser = "";
//		if(region.equals(AMS_NA)) {
//			bto = "CC487A";
//			kit = "423022";
//			purchaser = "hp2bfeautomation+PurAMSNA001@gmail.com";
//		} else
//			purchaser =  getUser(dataIDs.get(region), PURCHASER);

		// Waiting for user availability
		String purchaser =  getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("emailID", purchaser);

		String quoteName = "autQuote";
		String paymentMethod = "Purchase Order";
		String emailId = purchaser;
		String qty = "200";

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);


		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Purchaser user", url, purchaser, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
				+ "The HP2B home page is refreshed with the selected org and contract.", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product","Product should be deleted","CE", false));

		Assert.assertNotNull(login.searchSKU("Step 3: In search box , search for BTO sku", "PDP of searched product should be displayed", bto));

		Assert.assertTrue(pdp.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.searchSKU("Step 5: In search box , search for KIT number", "PDP of searched product should be displayed", kit));

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 6: Enter Qty as 200 and update", "Qty should be updated successfully", qty));

		Assert.assertTrue(pdp.addProductToCart("Step 7: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(pdp.navigateToShoppingCartThroughHeader("Step 8: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed"));

		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 9: Click on 'save as quote' button", 
				"'Create New Quote' page should be displayed with the list of products added in cart page"));

		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 10: Select Payment method as 'Purchase Order'", "Payment method is selected", paymentMethod));

		Assert.assertTrue(createNewQuote.verifyContractSurchargeIsDisplayed("Step 11: Verify the contract surcharge under cart summary section in Create quote page",
				"The contract surcharge should be displayed in cart summary and should be included in total", true));
		
		quoteDetails = createNewQuote.createQuote("Step 12: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigate to quote confirmation page", quoteName, emailId);
		Assert.assertNotNull(quoteDetails);

		Assert.assertTrue(quoteDetails.verifyContractSurchargeIsDisplayed("Step 13: Verify the contract surcharge under cart summary section in quote confirmation page",
				"The contract surcharge should be displayed in cart summary and should be included in total", true));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
	
	/**
	 * Quote_Verify user is able to create quote with attachments and check quote email for attachment
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/459134
	 * @param region EMEA, APJ, AMS-US
	 * @since Mar 17, 2022
	 * @author Manjunath
	 */
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, 
			groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C459134_Regression_CE_Quotes_VerifyUserIsAbleToCreateQuoteWithAttachmentsAndCheckQuoteEmailForAttachment_CSR(String region) {

		// Reporting info
		initializeReporting("Quote_Verify user is able to create quote with attachments and check quote email for attachment", 
				"C459134_Regression_CE_Quotes_VerifyUserIsAbleToCreateQuoteWithAttachmentsAndCheckQuoteEmailForAttachment_CSR", region, logger);

		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01, ID03, ID02);
		Map<String, String> regData = getScenarioData(dataIDs,region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
        String catalogName = regData.get("Contract");
       	String bto = getProductByDataSetID(region, dataIDs.get(region), BTO);
        Assert.assertNotNull(bto);
        String password = passwords.get(CSRORFEDCSRUSERPWD);
        	
        // Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotNull(csr);
		userSet.add(csr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
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
		setEnvironment();
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
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "User should be impersonated", data));
		
		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
				+ "The HP2B home page is refreshed with the selected org and contract.", data, true));
		
		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
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
	/**
	 * Quote_Verify Standard delivery option in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/462892
	 * region APJ, AMS-NA
	 * @since Mar 18, 2022
	 * @author Manjunath
	 * @throws IOException 
	 */ 
	@Test(dataProvider = "apjAndAmsRegion_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {IGroupsTagging.ITestType.REGRESSION,
			IGroupsTagging.IStoreType.CE,IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C462892_Regression_CE_Quotes_VerifyStandardDeliveryOptionInQuoteCreationAndQuoteConfirmationPage_CSR(String region) throws IOException{
		
		// Reporting info
		initializeReporting("Quote_Verify Standard deilvery option in Quote creation and Quote confirmation page",
				"C462892_Regression_CE_Quotes_VerifyStandardDeliveryOptionInQuoteCreationAndQuoteConfirmationPage_CSR", region, logger);
		
		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMapForApjAndAmsNa(ID14, ID02);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);	
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		//String bto = "U7897E";
		String bto =  getProductByDataSetID(region, dataIDs.get(region), BTO);
		//String catalogName = region.equals(APJ)? regData.get("Contract") : commonData.get("InvalidContractName_" + ID02);
		
		// Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotNull(csr);
		userSet.add(csr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
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
		setEnvironment();
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
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "User should be impersonated", data));
		
		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
				+ "The HP2B home page is refreshed with the selected org and contract.", data, true));
		
		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/
		
		pdp = customerService.searchSKU("Step 3: In search box search for BTO ", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);
		
		Assert.assertTrue(pdp.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 5: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);
		
		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 6: Click on 'save as quote' button", 
				"'Create New Quote' page should be displayed with the list of products added in cart page"));
		
		Assert.assertTrue(checkoutPage.verifyAllValuesInShippingOptionsDropdown("Step 7: Verify below Shipping Option on Create new quote page,<br>1.Standard Delivery<br>" + 
				"2.Two Day<br>" + "3.Next Day,A.M<br>" + "4.Next Day,P.M.","Validate all the shipping options should be displayed.", shippingOptions));
		
		Assert.assertTrue(checkoutPage.verifySelectedValueInShippingOptionDropdown("Step 8: Verify the Standard Delivery option.", "Standard Delivery option should be selected as default.",
				"Standard Delivery"));
		
		Assert.assertTrue(checkoutPage.verifyRequestedDeliveryDateCalendar("Step 9: Verify Requested delivery date with MM/DD/YY", "Calendar pop up should be displayed with current month"));
		
		String reqDeliveryDate = createNewQuote.selectRequestedDeliveryDate("Step 10: Select any future date", "The selected date should be displayed on Requested delivery date", 5, true);
		
		quoteDetails = createNewQuote.createQuote("Step 11: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigate to quote confirmation page", quoteName, emailId);
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
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/462893
	 * region APJ, AMS-NA
	 * @since Mar 22, 2022
	 * @author Manjunath
	 * @throws IOException 
	 */ 
	@Test(dataProvider = "apjAndAmsRegion_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {IGroupsTagging.ITestType.REGRESSION,
			IGroupsTagging.IStoreType.CE,IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C462893_Regression_CE_Quotes_VerifyNextDayDeliveryOptionInQuoteCreationAndQuoteConfirmationPage_CSR(String region) throws IOException{
		
		// Reporting info
		initializeReporting("Quote_Verify next day deilvery option in Quote creation and Quote confirmation page",
				"C462893_Regression_CE_Quotes_VerifyNextDayDeliveryOptionInQuoteCreationAndQuoteConfirmationPage_CSR", region, logger);
		
		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMapForApjAndAmsNa(ID14, ID02);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);	
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String kit = getProductByDataSetID(region, dataIDs.get(region), KIT); //"U7897E"
		//String catalogName = region.equals(APJ)? regData.get("Contract") : commonData.get("InvalidContractName_" + ID02);
		
		// Waiting for user availability
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotNull(csr);
		userSet.add(csr);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		String purchaser = getUser(dataIDs.get(region), PURCHASER);
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
		String shippingMethod =  "Next Day, A.M.";
		String quoteName = "autQuote";
		
		//Get URL
		setEnvironment();
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
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "User should be impersonated", data));
		
		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Click the down arrow at the left top to select the target organization [organization], target contract [contract] and click the 'Apply' button at the bottom.<br>"
				+ "Step 2: Click the 'YES' button.", "The message displayed as 'Some products might not be available for other contracts, are you sure you would like to change contracts?' pops up.<br>"
				+ "The HP2B home page is refreshed with the selected org and contract.", data, true));
		
		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/
		
		pdp = customerService.searchSKU("Step 3: In search box search for KIT ", "PDP of searched product should be displayed", kit);
		Assert.assertNotEquals(pdp, null);
		
		Assert.assertTrue(pdp.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 5: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);
		
		Assert.assertNotNull(shoppingCart.navigateToQuoteCreationPage("Step 6: Click on 'save as quote' button", 
				"Quote creation page should be displayed"));
		
		Assert.assertTrue(checkoutPage.verifyAllValuesInShippingOptionsDropdown("Step 7: Verify below Shipping Option on Create new quote page,<br>1.Standard Delivery<br>" + 
				"2.Two Day<br>" + "3.Next Day,A.M<br>" + "4.Next Day,P.M.","Validate all the shipping options should be displayed.", shippingOptions));
		
		Assert.assertTrue(checkoutPage.selectValueInShippingOptionDropdown("Step 8: Select the Next Day,A.M Delivery option", "Selected Delivery option should be displayed.", shippingMethod));
		
		Assert.assertTrue(checkoutPage.verifyRequestedDeliveryDateCalendar("Step 9: Verify Requested delivery date with MM/DD/YY", "Calendar pop up should be displayed with current month"));
		
		String reqDeliveryDate = createNewQuote.selectRequestedDeliveryDate("Step 10: Select any future date", "The selected date should be displayed on Requested delivery date", 5, true);
		
		quoteDetails = createNewQuote.createQuote("Step 11: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigate to quote confirmation page", quoteName, emailId);
		Assert.assertNotNull(quoteDetails);
	
		Assert.assertTrue(orderConfirmation.verifyShippingMethod("Step 12.1: Verify the Shipping delivery type on Quote Confirmation page.", 
				"Shipping delivery type should be displayed as per the created quote.", shippingMethod));
		
		Assert.assertTrue(quoteDetails.verifyRequestedDeliveryDateOnConfirmationPage("Step 12.2:  Requested delivery date on Quote Confirmation page.", 
				"Requested delivery date as per the created quote.", reqDeliveryDate));
		
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
}
