package com.hp2b.web.quotes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hp2b.common.FrameworkMethods;
import com.hp2b.common.HP2BDataProvider;
import com.hp2b.interfaces.testcasetagging.IGroupsTagging;
import com.hp2b.web.pom.AccountSummary;
import com.hp2b.web.pom.CreateNewQuote;
import com.hp2b.web.pom.CustomerService;
import com.hp2b.web.pom.Login;
import com.hp2b.web.pom.PDP;
import com.hp2b.web.pom.QuoteDetails;
import com.hp2b.web.pom.QuoteListing;
import com.hp2b.web.pom.ShoppingCart;
import com.hpicorp.hpframework.reporting.Reporting;
import com.relevantcodes.extentreports.LogStatus;

public class CEQuoteListingPage extends FrameworkMethods {
	
    Logger logger = Logger.getLogger(CEQuoteListingPage.class);
	
	private String url = "";	
	private static final String config = "config";
	private static final String module = "Quotes";
	private static final String storeCE = IGroupsTagging.IStoreType.CE;

	public void setEnvironment() {
		if (url.isEmpty()) {			
			url = _webcontrols.get().propFileHandler().readProperty(config, ENVIRONMENT);
		}
	}
	
	/**
	 * Verify search results for 'Quote Number' in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300046
	 * @param region APJ,EMEA,AMS-US,AMS-LA
	 * @since March 16, 2022
	 * @author RamanatM
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
		QuoteListing quoteListing = new QuoteListing(_webcontrols);

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

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Click on Organization and Catalog dropdown, Select Catalog under Catalog dropdown and Click on 'Apply' and 'Ok' in Overlay",
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

		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("PreCondition: Share quote with another user","Quote Should be shared successfully",purchaser2,true));

		data.replace("emailID", purchaser1,purchaser2);
		usersAvailability.replace(purchaser1, "Free");
		data.remove(purchaser1);
		updateUserStatus(purchaser1, "Free");

		Assert.assertTrue(quoteDetails.clickOnCustomerServiceLinkAndClickOnOkButtonInsideTerminateSessionPopup("PreCondition: Terminate the current session", "Customer Service Page Should be displayed for New Session ", purchaser2, true));
		
		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate purchaser user.", "Impersonated purchaser user successfully.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Select Organisation, contract and click on apply button <br> Step 2: Click on yes button" ,
				"The HP2B home page is refreshed with the selected org and contract", data,true));
		
		
		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 3: Mouse over on Orders & Quotes and click on quotes link", 
			"Quote listing page should display with list of existing quotes with gear icon", "Quotes"));
		
		Assert.assertTrue(quoteListing.verifyDefaultTextBoxSearchCriteraForQuotes("Step 4: Check Enter Quote number here text is available in Search text box", "Enter Quote Number here text should be displayed in Search text box by default"));
		
		Assert.assertTrue(quoteListing.verifyDefaultDropDownSearchCriteraForQuotes("Step 5: Verify select search criteria text is displayed in search criteria drop down", "Select Search Criteria text should be displayed in search criteria drop down to select options"));
		
		String sharedQuoteNumber=quoteListing.getQuoteNumberBasedOnQuoteName("Step 6.1: Get shared quote number based on quote name", "Shared quote number should display", quoteNumber, true);
		Assert.assertNotEquals(sharedQuoteNumber, null);
		
		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 6.2: Enter valid full string in Text box and click on Search Icon", "Search results must appear",
				dropdownValue, sharedQuoteNumber, false));
		
		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 6.3: Enter valid partial string in Text box and click on Search Icon",
				"Search results must appear across all the pages when the partial input given is found in the quote number.", dropdownValue, sharedQuoteNumber.substring(0, 2),
				false));

		Assert.assertTrue(quoteListing.verifySharedQuoteIsDisplayedByQuoteNumber("Step 7: Verify search results include both shared and created quotes",
				"Search results should display both the quotes that were created in their ID and also that were shared with them when search criteria is matched", sharedQuoteNumber));

		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 8: Verify search results include both valid and expired quotes", 
				"Search results must display both valid/expired quotes when the quote number contains with the search input"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 9: Enter a single number in Text box and click on Search", "Search results must appear across all the pages when the partial input given in the text box is found in the quote number",
				dropdownValue, singleDigitQuoteNumber, false));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 10: Verify search result appears with the most recently added Quote on Top-Descending Order", 
				"Most recently added quote that is matched with search criteria should display on Top"));

		Assert.assertTrue(quoteListing.enterInvalidQuoteNumberAndVerifyErrorMessage("Step 11: Enter an invalid number in Text box and click on Search Icon", 
				"User must be displayed with below error message<br>"
						+ "Quote(s) not available. If you encountered any issues, please contact your agent or HP representative.", invalidQuote));

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

		//Steps
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
		
		String quoteName = quoteDetails.getSingleQuoteData(quoteDetailsList, "Quote Name");  
		Assert.assertTrue(login.clickOnHomeTab("Precondition: Go to Home page", "Home page should be displayed", true));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 : Select Organisation, contract and click on apply button <br> Step 2: Click on yes button", "The HP2B home page is refreshed with the selected org and contract", data, true));
		
		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 3: Click on quotes under Orders and Quotes tab in Home Page", 
				"Quote listing page should be displayed with list of quotes created", "Quotes"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 4: Select Quote Name option from selection Drop down<br>"
				+ "Step 5: Enter the full valid quote name in Text box and click on Search Icon", "Quote Name option must have been selected<br>"
						+ "Search results must appear across all the pages", dropdownValue, quoteName, false));
		
		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 6.1: Select Quote Name option from selection Drop down, enter 'quote' in Text box and click on Search Icon", "All the quotes must be displayed based on the search value<br>"
						+ "Search results must appear across all the pages", dropdownValue, quoteName2, false));


		Assert.assertTrue(quoteListing.verifyQuotesAreDisplayed("Step 6.2: Verify search results include both shared and created quotes", 
				"Search results should display both the quotes that were created in their ID and also that were shared with them when, "
						+ "the search name is present in their quote name", quoteName2));
		
		Assert.assertTrue(quoteListing.verifyValidAndExpiredQuotes("Step 7: Verify search results include both valid and expired quotes", 
				"Search results must display both valid/expired quotes when the quote name contains with the search input"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 8: Enter a few characters (simple letters and combination of characters and numbers)in Text box and click on Search Icon Eg:quo,quo@\"$,uqote67@",
				"Search results must appear across all the pages when the partial input given in the text box is found in the quote name (Wild card search)", dropdownValue, quoteName2, false));

		Assert.assertTrue(quoteListing.verifyQuotesAppearInDescendingOrder("Step 9: Verify search result appears with the most recently added Quote on Top-Descending Order", 
				"Most recently added quote that is matched with search criteria should display on Top"));

		Assert.assertTrue(quoteListing.enterInvalidQuoteNumberAndVerifyErrorMessage("Step 10: Enter an invalid quote name in Text box and click on Search Icon", 
				"User must be displayed with below error message<br>"
						+ "Quote(s) not available. If you encountered any issues, please contact your agent or HP representative.", invalidQuote));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify search results with different sub options for 'Quote Created On' date range in quote listing page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300048
	 * @param region EMEA, APJ, AMS-US, AMS-LAf
	 * @since Mar 16, 2022
	 * @author Vijay R
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
}
