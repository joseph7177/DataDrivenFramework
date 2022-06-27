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
import com.hp2b.interfaces.testcasetagging.IGroupsTagging;
import com.hp2b.pdf.PDFValidations;
import com.hp2b.web.pom.AccountSummary;
import com.hp2b.web.pom.Checkout;
import com.hp2b.web.pom.CreateNewQuote;
import com.hp2b.web.pom.CustomerService;
import com.hp2b.web.pom.Login;
import com.hp2b.web.pom.OrderConfirmation;
import com.hp2b.web.pom.PDP;
import com.hp2b.web.pom.PageGenerics;
import com.hp2b.web.pom.QuoteDetails;
import com.hp2b.web.pom.QuoteListing;
import com.hp2b.web.pom.ShoppingCart;
import com.hpicorp.hpframework.reporting.Reporting;
import com.relevantcodes.extentreports.LogStatus;

public class CEQuoteDetails extends FrameworkMethods {
	
    Logger logger = Logger.getLogger(CEQuoteDetails.class);
	
	private String url = "";	
	private static final String config = "config";
	private static final String module = "Quotes";

	public void setEnvironment() {
		if (url.isEmpty()) {			
			url = _webcontrols.get().propFileHandler().readProperty(config, ENVIRONMENT);
		}
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
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap("ID01","ID22","ID02");
		Map<String, String> regData = getScenarioData(dataIDs,region);
		
		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
	    String orgName = regData.get("Org Name");
	    String password = passwords.get(DIRECTUSERPWD);	
		String purchaser1 = getUser(dataIDs.get(region), PURCHASER);
	     
	    Map<String, String> data = new HashMap<String, String>();
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser1);
	//	String quoteName = "Aut_Quote_";
		// String qty = "10";
	        
	    String username = getUser(dataIDs.get(region), PURCHASER);
	    HashMap<String, String> shippingAdress = new HashMap<String, String>();
		Assert.assertNotNull(username);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(username));
		
		String bto = getProductByDataSetID(region, dataIDs.get(region),BTO); 
		Assert.assertNotNull(bto);
		
//		String purchaser = getUser(dataIDs.get(region), PURCHASER);
//		Assert.assertNotNull(purchaser);
//		System.out.println(purchaser);
		
		
		
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
	//	AccountSummary accountSummary = new AccountSummary(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct User", url, username, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
	
		pdp = customerService.searchSKU("PreCondition: Search with bto SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add Bto product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Click on mini cart icon and go to cart button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "Create New Quote' page should be displayed");
		Assert.assertNotEquals(createNewQuote, null);
	
		
		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all mandatory details and Click on 'Save Quote' button",
				"Quote should be created successfully and navigated to Quote detail page.","QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote number should be fetched successfully.");
		Assert.assertNotEquals(createNewQuote, null);
		
		/** Pre-Condition ends **/
	
		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 1: Click on quotes under Orders and Quotes tab in Home Page", 
				"Quote listing page should be displayed with list of quotes created", "Quotes"));
		
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		
		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 2: Search quote number", "Quote should be searched successfully","Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 2.1: Click on newly 'Serached Quote number'", "Quote details page should be displayed with below details : <br>"
				+ "Print <br>"
				+ "Export <br>"
				+ "Email <br>"
				+ "Share quote <br>"
				+ "Order Information <br>"
				+ "Billing address <br>"
				+ "Shipping address", quoteNumber));

		//HashMap<String, String> data1 = PageGenerics.getCurrentSystemDateAndEndDate(30);
		
		Assert.assertTrue(quoteDetails.getQuoteDetails("Step 3 & 5: Get quote details",
				"Quotes should be available for requested quote"));

		
		String espot = quoteDetails.getESPOTInQuoteDetails("Getting Espot");
		Reporting.getLogger().log(LogStatus.INFO, "Step 4 : espot details", espot);
		System.out.println(espot);
		
Assert.assertTrue(quoteDetails.verifyElementIsDisplayedByText("Step 6: Verify Order information", "Order information should be displayed", "Order Information", true));
		
		Assert.assertTrue(quoteDetails.verifyEmailNotification("Step 6.1: Verify Email notification", "Email notification should be displayed", purchaser1, true));
	

//		Assert.assertTrue(quoteDetails.verifyOrderInformationSectionAndFieldValidation(
//				"Step 6: Verify order information section and validate fields",
//				"Order Information section should be verified and fileds should be validated"));

		Assert.assertTrue(quoteDetails.verifyPurchaserContactInformationSectionAndFieldValidation(
				"Step 7 : Verify purchaser contact information  section and validate fields on click on edit",
				"Purchaser contact information section should be verified and fileds should be validated on click on edit"));

		
		Assert.assertTrue(quoteDetails.verifyPaymentMethodInQuoteDetailsPage("Step 8: Verify Payment method", "Payment method should be displayed", "Purchase Order"));
		
		Assert.assertNotNull(quoteDetails.getBillingInformationDetails("Step 10: Verify Billing information", "Billing information should be displayed", ""));
	
//		Assert.assertTrue(quoteDetails.verifyShippingAddressInQuoteDetailsPage("Step 11 & 12: verify Shipping address in quote details page", 
//				"Updated Shipping address should be displayed", shippingAdress));
		
		Assert.assertNotNull(quoteDetails.getShippingInformationDetails("Step 11 & 12: Verify Shipping information", "Shipping information should be displayed", ""));

		Assert.assertTrue(quoteDetails.verifyCartSummarySection("Step 13 : Verify Cart summary section in create quote page",
				"Cart summary section should be verified"));
		
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	
	
	/**
	 * Verify user is able to share quote from quote details page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300031
	 * @since May 3, 2021 1:13:55 PM
	 * @author AmithReddy
	 */
	@Test(dataProvider = "region_data-provider",dataProviderClass = HP2BDataProvider.class,
			groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
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
		String purchaser3 = getUser(dataIDs.get(region), PURCHASER);
		Assert.assertNotNull(purchaser3);
		System.out.println(purchaser3);
		ArrayList<String> users = new ArrayList<String>();
		users.add(purchaser1);
		users.add(purchaser2);
		users.add(purchaser3);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Buy On Behalf");
		data.put("emailID", purchaser1);
		
		  String username = getUser(dataIDs.get(region), PURCHASER);
		    HashMap<String, String> shippingAdress = new HashMap<String, String>();
			Assert.assertNotNull(username);
			usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(username));


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
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);

		//		String partnerAgentId = login.getPartnerAgentID(username, partnerAgentIds, partnerAgents);
		//		Assert.assertNotEquals(partnerAgentId, null);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct User", url, username, password, true));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
	
		pdp = customerService.searchSKU("PreCondition: Search with bto SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add Bto product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Click on mini cart icon and go to cart button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "Create New Quote' page should be displayed");
		Assert.assertNotEquals(createNewQuote, null);
		
		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all mandatory details and Click on 'Save Quote' button",
				"Quote should be created successfully and navigated to Quote detail page.","QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote number should be fetched successfully.");
		Assert.assertNotEquals(createNewQuote, null);
		
		/** Pre-Condition ends **/
		
		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 1: Click on quotes under Orders and Quotes tab in Home Page", 
				"Quote listing page should be displayed with list of quotes created", "Quotes"));
		
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
		String quoteName = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
			
		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 2: Search quote number", "Quote should be searched successfully","Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 2.1: Click on newly 'Serached Quote number'", "Quote details page should be displayed with below details : <br>"
				+ "Print <br>"
				+ "Export <br>"
				+ "Email <br>"
				+ "Share quote <br>"
				+ "Order Information <br>"
				+ "Billing address <br>"
				+ "Shipping address", quoteNumber));

		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser1,true));
		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser2,true));
		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser3,true));
		
		Assert.assertTrue(quoteDetails.logout("Step 11: Click on logout option in homepage", "User should be logged out successfully","HomePage", true));

		usersAvailability.replace(purchaser1, "Free");
		Assert.assertTrue(login.loginToHP2B("Step 12.1: Login to HP2B with other user and check for shared quote", purchaser1, password, true));


		accountSummary = login.navigateToMyAccount("Step 12.2: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 12.3: Click on 'Quotes' under orders and quotes", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 12.4: Search Quote with quote name", "Quote details should display",
				"Quote Name", quoteName, false));
		
		Assert.assertTrue(quoteDetails.logout("Step 11: Click on logout option in homepage", "User should be logged out successfully","HomePage", true));

		usersAvailability.replace(purchaser2, "Free");
		Assert.assertTrue(login.loginToHP2B("Step 12.1: Login to HP2B with other user and check for shared quote", purchaser2, password, true));


		accountSummary = login.navigateToMyAccount("Step 12.2: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 12.3: Click on 'Quotes' under orders and quotes", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 12.4: Search Quote with quote name", "Quote details should display",
				"Quote Name", quoteName, false));
		
		Assert.assertTrue(quoteDetails.logout("Step 11: Click on logout option in homepage", "User should be logged out successfully","HomePage", true));

		usersAvailability.replace(purchaser3, "Free");
		Assert.assertTrue(login.loginToHP2B("Step 12.1: Login to HP2B with other user and check for shared quote", purchaser3, password, true));


		accountSummary = login.navigateToMyAccount("Step 12.2: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 12.3: Click on 'Quotes' under orders and quotes", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 12.4: Search Quote with quote name", "Quote details should display",
				"Quote Name", quoteName, false));

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
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class,groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE,
			IGroupsTagging.IUserType.CSR,IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C295277_Regression_CE_Quotes_CBNVerifyTheLogsticalServicesInSharedQuote_CSR(String region) throws IOException{

		// Reporting info
		initializeReporting("Verify the Logistical Services in Shared Quote",
				"C295277_Regression_CE_Quotes_CBNVerifyTheLogsticalServicesInSharedQuote_CSR",region, logger);

		// Test data
		String scenarioId = "ID03";
		//LinkedHashMap<String, String> dataIDs = storeDataIdsInMapForApjAndAmsNa(ID14, ID03);
		Map<String, String> regData = scenarioData.get(scenarioId);
	//	Map<String, String> regData = getScenarioData(dataIDs, region);
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
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		AccountSummary accSummary = new AccountSummary(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition: Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

	//	Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
	
		pdp = customerService.searchSKU("PreCondition: Search with bto SKU", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("PreCondition: Add Bto product to cart at PDP", "Product should be added to cart", "pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("PreCondition: Click on mini cart icon and go to cart button",
				"User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("PreCondition: Click on save as quote button", "Create New Quote' page should be displayed");
		Assert.assertNotEquals(createNewQuote, null);
		
		quoteDetails = createNewQuote.createQuote("PreCondition: Enter all mandatory details and Click on 'Save Quote' button",
				"Quote should be created successfully and navigated to Quote detail page.","QuoteValue", purchaser1);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote number should be fetched successfully.");
		Assert.assertNotEquals(createNewQuote, null);	
		
		
		
		
		
		
		
		
		
		
		/*Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog", "Requested catalog should be selected", data,true));

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

		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");*/

		/** Pre-Condition Ends **/
		
		
		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 1: Click on quotes under Orders and Quotes tab in Home Page", 
				"Quote listing page should be displayed with list of quotes created", "Quotes"));
		
		String quoteNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");
	//	String quoteName = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		
		
		
		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 2: Search quote number", "Quote should be searched successfully","Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnQuoteAndVerifyQuoteDetails("Step 2.1: Click on newly 'Serached Quote number'", "Quote details page should be displayed with below details : <br>"
				+ "Print <br>"
				+ "Export <br>"
				+ "Email <br>"
				+ "Share quote <br>"
				+ "Order Information <br>"
				+ "Billing address <br>"
				+ "Shipping address", quoteNumber));
		
	//	Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser1,true));
		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser2,true));
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		

	/*	Assert.assertNotNull(customerService.navigateToMyAccount("Step 1: Click on 'My accounts' in home page", 
				"My accounts page should be displayed"));

		Assert.assertNotNull(accSummary.clickOnQuotesUnderMyAccountSection("Step 2: Click on 'Quotes' under Orders and Quotes", 
				"Quote listing page should be displayed"));

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 3.1: Search quote which is created", "Quote details should display",
				"Quote Number", quoteNumber, false));

		Assert.assertTrue(quoteListing.clickOnGearButton("Step 3.2: Click on Action gear icon", "Clicked on Action gear",
				"Share quote"));

		Assert.assertTrue(quoteListing.clickOnActions("Step 3.3: Click on 'Share Quote'", "Share quote pop up with login id text field and with Cancel & Share quote buttons should be displayed",
				"Share quote"));

//		Assert.assertTrue(quoteListing.enterEmailidAndVerifySharequoteMessage("Step 4: Enter valid login details in 'Login id' field and click on 'Share quote' button",
//				"Quote should be successfully shared to provided login in", purchaser2));
		Assert.assertTrue( quoteDetails.clickOnShareLinkAndEnterEmailIdAndClickOnShareQuoteButton("Share quote with another user","Quote Should be shared successfully",purchaser1,true));*/
//
//		Assert.assertTrue(login.clickOnCustomerServiceLink("Step 5.1: Click on customer service link in home page"
//				, "Clicked on Customer Service link."));
		
		Assert.assertTrue(quoteDetails.logout("Step 11: Click on logout option in homepage", "User should be logged out successfully","HomePage", true));

		usersAvailability.replace(purchaser2, "Free");
		Assert.assertTrue(login.loginToHP2B("Step 12.1: Login to HP2B with other user and check for shared quote", purchaser2, password, true));


		accountSummary = login.navigateToMyAccount("Step 12.2: Click on 'My Account'", "My Account page should be displayed");
		Assert.assertNotEquals(accountSummary, null);

		quoteListing = accountSummary.clickOnQuotesUnderMyAccountSection("Step 12.3: Click on 'Quotes' under orders and quotes", "List of existing quotes with gear icon should be displayed");
		Assert.assertNotEquals(quoteListing, null);

		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 12.4: Search Quote with quote name", "Quote details should display",
				"Quote Name", quoteName, false));
		
		
		
		
		

//		usersAvailability.replace(purchaser3, "Free");
//		users.remove(purchaser3);
//
//		data.put("emailID", purchaser3);
//		System.out.println("Impersonate user 2: "+purchaser3);
//		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Step 5.2: Impersonate user","User is Impersonated.",data));
//
//		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 5.3: Select requested catalog", "Requested catalog should be selected", data,true));
//
//		Assert.assertNotNull(customerService.navigateToMyAccount("Step 5.4: Click on 'My accounts' in home page", 
//				"My accounts page should be displayed"));
//
//		Assert.assertNotNull(accSummary.clickOnQuotesUnderMyAccountSection("Step 5.5: Click on 'Quotes' under Orders and Quotes", 
//				"Quote list page should display with list of existing quotes with gear icon"));
//
//		Assert.assertTrue(quoteListing.searchQuoteByDropdownOption("Step 6.1: Search shared quote", "Shared Quote details should display",
//				"Quote Number", quoteNumber, false));
//
//		Assert.assertTrue(quoteListing.clickOnQuote("Step 6.2 : Click on searched quote", "Should be clicked on searched quote", quoteNumber));

		LinkedHashMap<String,List<String>> logisticSkuDetails = quoteDetails.verifyLogisticalServicesDetails("Step 7: Verify Items,Logistical sku,Product description,Qty,Unit price and Total", 
				"Items,Logistical sku,Product description,Qty,Unit price and Total should display", "QUOTEDETAILS");
		Assert.assertNotEquals(logisticSkuDetails, null);

		Assert.assertTrue(quoteDetails.verifyShippingAndHandlingChargeIsEqualToSumOfTotalPrice("Step 8: Verify the Shipping & handling charges", 
				"Shipping & handling charges should be sum of Logistical sku charges", "QUOTEDETAILS"));

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
	@Test(dataProvider ="region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {IGroupsTagging.ITestType.REGRESSION,
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
		
//		Assert.assertTrue(checkoutPage.verifyAllValuesInShippingOptionsDropdown("Step 7: Verify below Shipping Option on Create new quote page,<br>1.Standard Delivery<br>" + 
//				"2.Two Day<br>" + "3.Next Day,A.M<br>" + "4.Next Day,P.M.","Validate all the shipping options should be displayed.", shippingOptions));
		
	/*	Assert.assertTrue(checkoutPage.selectValueInShippingOptionDropdown("Step 8: Select the Next Day,A.M Delivery option", "Selected Delivery option should be displayed.", shippingMethod));
		
		Assert.assertTrue(checkoutPage.verifyRequestedDeliveryDateCalendar("Step 9: Verify Requested delivery date with MM/DD/YY", "Calendar pop up should be displayed with current month"));
		
		String reqDeliveryDate = createNewQuote.selectRequestedDeliveryDate("Step 10: Select any future date", "The selected date should be displayed on Requested delivery date", 5, true);
		
		quoteDetails = createNewQuote.createQuote("Step 11: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigate to quote confirmation page", quoteName, emailId);
		Assert.assertNotNull(quoteDetails);
	
		Assert.assertTrue(orderConfirmation.verifyShippingMethod("Step 12.1: Verify the Shipping delivery type on Quote Confirmation page.", 
				"Shipping delivery type should be displayed as per the created quote.", shippingMethod));
		
		Assert.assertTrue(quoteDetails.verifyRequestedDeliveryDateOnConfirmationPage("Step 12.2:  Requested delivery date on Quote Confirmation page.", 
				"Requested delivery date as per the created quote.", reqDeliveryDate));*/
		
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
	
}
