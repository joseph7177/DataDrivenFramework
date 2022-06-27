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
import com.hp2b.web.pom.Checkout;
import com.hp2b.web.pom.CreateAddress;
import com.hp2b.web.pom.CreateNewQuote;
import com.hp2b.web.pom.CustomerService;
import com.hp2b.web.pom.HomePage;
import com.hp2b.web.pom.Login;
import com.hp2b.web.pom.PDP;
import com.hp2b.web.pom.PLP;
import com.hp2b.web.pom.QuoteDetails;
import com.hp2b.web.pom.QuoteListing;
import com.hp2b.web.pom.ShoppingCart;
import com.hpicorp.hpframework.reporting.Reporting;
import com.relevantcodes.extentreports.LogStatus;

public class CEQuoteCreation extends FrameworkMethods {
	
    Logger logger = Logger.getLogger(CEQuoteCreation.class);
	
	private String url = "";
	
	private static final String config = "config";
	private static final String module = "Quotes";

	public void setEnvironment() {
		if (url.isEmpty()) {			
			url = _webcontrols.get().propFileHandler().readProperty(config, ENVIRONMENT);
		}
	}

	/**
	* Verify SPC in Quote creation and Quote confirmation page
	* @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/438419
	* @param region 
	* @since 
	* @author ThomasAn
	*/
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.STOREFRONT }, priority = 2)
	public void C461583_Regression_CE_Quote_VerifySPCInQuoteCreationAndQuoteConfirmationPage_CSR(String region){
		
		// Reporting info
		initializeReporting("Verify SPC in Quote creation and Quote confirmation page",
				"C461583_Regression_CE_Quote_VerifySPCInQuoteCreationAndQuoteConfirmationPage_CSR",region, logger);
	
		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01, ID03, ID02,ID05);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
	    String bto2 = getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);
	    Assert.assertNotNull(bto);
	    String spc = regData.get("SPC");
	
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
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user","User is Impersonated.",data));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 3: Search for a BTO product", "PDP of BTO product must be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 4:  Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		pdp = customerService.searchSKU("Step 5 : Search for another BTO product", "PDP of BTO product must be displayed", bto2);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 6:  Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));
		
		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 7: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 8: Click on 'save as quote' link", "Create New Quote' page should be displayed");
		Assert.assertNotEquals(createNewQuote, null);
		
		Assert.assertTrue(shoppingCart.enterTextInSpecialPricingCodeTextBox("Step 9.1: Enter valid 'Special pricing code' in cart summary section", 
				"Special pricing code' Should be applied successfully", spc, true));

		Assert.assertTrue(shoppingCart.clickOnApplyLink("Step 9.2: Click on apply link", 
				"'Special pricing code' Should be applied successfully and product price should be reduced.", true, true));

		quoteDetails = createNewQuote.createQuote("Step 10:Enter all the mandatory details and click on save a quote",
				"Quote should be created successfully and navigated to Quote confirmation page","QuoteValue", purchaser);
		Assert.assertNotEquals(quoteDetails, null);
		
		Assert.assertTrue(quoteDetails.verifySPCInQuoteConfirmationPage("Step 11: Verify SPC in quote confirmation page", "Special pricing code is verified in quote confirmation page",spc));
	
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.STOREFRONT }, priority = 2)
	public void C359230_Regression_CE_Quote_CBN_VerifyUserSelectedDefaultBillingAddressAndShippingAddressInQuoteCreationPage_CSR(String region){
		
		// Reporting info
		initializeReporting("Verify user selected default Billing address and Shipping address in Quote creation page",
				"C359230_Regression_CE_Quote_CBN_VerifyUserSelectedDefaultBillingAddressAndShippingAddressInQuoteCreationPage_CSR",region, logger);
	
		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01,ID08, ID02,ID05);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
		String bto = getProductByDataSetID(region, dataIDs.get(region), BTO, MONITORS);
	    Assert.assertNotNull(bto);
	    //3TQ40AA 2SC67AA
	    String bto2 = getProductByDataSetID(region, dataIDs.get(region), BTO, ACCESSORIES);
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
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);

		String quoteName = "Test_Automation_Quote_";
		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("poNumber", "12345");
		mandatoryData.put("phoneNumber", "12345");
		mandatoryData.put("attentionText", "test");

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

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		/** Pre-Condition ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2 : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 3: Search with BTO SKU", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(login.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully", "pdp"));

		Assert.assertNotNull(login.navigateToShoppingCartThroughHeader("Step 5: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed"));

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 6: Click on save as quote button",
				"User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);
		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 7: Select payment method from dropdown", 
				"Payment should be selected", "Purchase Order"));
//		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 6: Select payment method from dropdown", 
//				"Payment should be selected", "Credit Card"));

		String selectedNewBillingAddress = createNewQuote.clickOnChangeBillingAddressAndSelectNewBillingAddressAndClickOnOk("Step 8 & 9 : Click on 'Change Billing Address And select the address that needs to be defaulted and click on ok",
				"Billing address popup should be displayed & Billing Address section is loaded with the selected address", false);
		Assert.assertNotEquals(selectedNewBillingAddress, null);

	
		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 12: Select 'Default Billing Address' checkbox",
				"Billing address is selected as default", "billing address"));

		Assert.assertTrue(createNewQuote.clickOnOkButtonInsideDefaultAddressConfirmationPopup("Step 13 : Click on ok button inside default address confirmation popup", "Ok button should be selected",false));
	//	Assert.assertTrue(createNewQuote.clickOnOkButtonInsideDefaultAddressConfirmationPopup("Step 10.2 : Click on ok button inside default address confirmation popup", "Ok button should be selected",false));
		
//		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 9.1: Select 'Default Billing Address' checkbox",
//				"Billing address is selected as default", "billing address"));
		
	    String selectedNewShipAddress=createNewQuote.clickOnChangeShippingAddressAndSelectNewShippingAddressAndClickOnOk("Step 10 & 11 : Click on 'Change Shipping Address And select the address that needs to be defaulted and click on ok",
				"Shipping address popup should be displayed & Shipping Address section is loaded with the selected address", false);
		Assert.assertNotEquals(selectedNewShipAddress, null);


	   Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 12: Select 'Default Shipping Address' checkbox",
				"Shipping address is selected as default", "shipping adddress"));
	   Assert.assertTrue(createNewQuote.clickOnOkButtonInsideDefaultAddressConfirmationPopup("Step 13 : Click on ok button inside default address confirmation popup", "Ok button should be selected",false));
	
		
		LinkedHashMap<String, String> billingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"createNewQuote","billing");
		Assert.assertNotEquals(billingInfoInCreateNewQuotePage, null);

	   LinkedHashMap<String, String>shippingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"createNewQuote","shipping");
		Assert.assertNotEquals(shippingInfoInCreateNewQuotePage, null);
		
		quoteDetails = createNewQuote.createQuote("Step 14: Provide all details and click on save quote",
				"Quote should created successfully", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		pdp = customerService.searchSKU("Step 15: In search box ,search for Product", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 16: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 17:Click on Mini cart and click Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 18: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);
		
		Assert.assertTrue(createNewQuote.verifyDefaultSelectedPaymentMethod("Step 19:Verify default payment method","Default payment method should be displayed","PurchaseOrder"));

//		Assert.assertTrue(checkout.verifyShppingAndBillingAddressDetails("Step 17: Verify the defaulted Billing Address is shown\r\n"
//				+ "Note: Billing address as selected in Step 7", "Default Billing address verified successfully", selectedNewBillingAddress, true));

//		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsSelected("Step 18: Verify 'Default Billing Address' checkbox is selected",
//				"'Default Billing Address' checkbox is selected", true));
//
//		Assert.assertTrue(checkout.verifyShppingAndBillingAddressDetails("Step 19: Verify the defaulted Shipping Address is shown\r\n"
//				+ "Note: Shipping address as selected in Step 9", "Default Shipping address verified successfully", selectedNewShipAddress, false));
//
//		Assert.assertTrue(createNewQuote.verifyDefaultShippingAddressCheckboxIsSelected("Step 20: Verify 'Default Shipping Address' checkbox is selected",
//				"'Default Shipping Address' checkbox is selected", true));
		
	
		LinkedHashMap<String, String> billingInfoInCreateNewQuotePage2= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"createNewQuote","billing");
		Assert.assertTrue(billingInfoInCreateNewQuotePage2.size()>0);

		LinkedHashMap<String, String>shippingInfoInCreateNewQuotePage2= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"createNewQuote","shipping");
		Assert.assertTrue(shippingInfoInCreateNewQuotePage2.size()>0);
		
		Assert.assertTrue( createNewQuote.compareTwoHashMap("Step 20 :Ensure that previously selected 'Default Billing Address' must be displayed","Default Billing address verified successfully", billingInfoInCreateNewQuotePage, billingInfoInCreateNewQuotePage2));

		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsSelected("Step 21: Verify 'Default billing Address' checkbox is selected.","'Default Billing Address' checkbox should be selected by default",true));

		Assert.assertTrue( createNewQuote.compareTwoHashMap("Step 22 :Ensure that previously selected 'Default Shipping Address' must be displayed","Default Shipping address verified successfully", shippingInfoInCreateNewQuotePage, shippingInfoInCreateNewQuotePage2));
		
		//Assert.assertTrue(createNewQuote.verifyDefaultShippingAddressCheckboxIsSelected("Step 22: Verify 'Default Shipping Address' checkbox is selected.","'Default Shipping Address' checkbox should be selected by default",true));

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
		logger.info("End Test case");
		

	}
	
	@Test(dataProvider = "region_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
			IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT, IGroupsTagging.IModuleType.QUOTES }, priority = 1)
	public void C459056_Regression_CE_Quote_VerifyB2biCheckboxDisplayInCreateQuotePageWhenViewAllQuoteRoleIsEnabledForUser_Direct(String region){
		
		// Reporting info
		initializeReporting("Verify B2bi’ checkbox display in create quote page when 'View all quote' role is enabled for user",
				"C459056_Regression_CE_Quote_VerifyB2biCheckboxDisplayInCreateQuotePageWhenViewAllQuoteRoleIsEnabledForUser_Direct",region, logger);
	
		// Test data
		LinkedHashMap<String, String> dataIDs = storeDataIdsInMap(ID01, ID03, ID02,ID05);
		Map<String, String> regData = getScenarioData(dataIDs, region);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);	
		

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		
		// Waiting for user availability
	//	String purchaser = getUser(dataIDs.get(region), PURCHASER);	
		String purchaser = "hp2bfeautomation+94292970P011@gmail.com";
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));
			
	    //Page Objects
		Login login = new Login(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		HomePage home = new HomePage(_webcontrols);
		PLP plp = new PLP(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		
		
		ShoppingCart cartPage = new ShoppingCart(_webcontrols);
		CreateNewQuote createQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		AccountSummary accountSummary = new AccountSummary(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteListing quoteListing = new QuoteListing(_webcontrols);			
		//Get URL
		setEnvironment();
		String url = this.url;
		String b2biRegion= "";
		if(region.equalsIgnoreCase("AMS-NA")) {
			b2biRegion = region.replace("-NA", "");
		}else {
			b2biRegion = region;
		}
		String b2biurl = _webcontrols.get().propFileHandler().readProperty(config, ENVIRONMENT + "-CE-B2Bi-" + b2biRegion);
		
		Assert.assertTrue(customerService.checkUrlAvailablity("Precondition : Check url avaliablity for region " + region, "URL should be avaliable", b2biurl));
		
	
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Purchaser", url, purchaser, password, true));
		
		Assert.assertTrue(customerService.selectOrganizationAndContract("STep 1 & 2: Select org & contract", "Requested org & contract should be selected", data, true));
	
		Assert.assertTrue(customerService.clickOnHomeTab("Precondition: Click on Home tab", "Home page should display", true));
		
		Assert.assertTrue(home.deleteProducts("Precondition: Delete products", "Products should be deleted", "CE", true));
		String bto = getProduct(USPS, BTO);
		Assert.assertNotNull(bto);
		pdp = customerService.searchSKU("Step 3: Search for a BTO product", "PDP of BTO product must be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.enterFirstProductQuantityinPDP("Step 4: Enter Qty as 100 and update", "Quantity should be entered", "100"));
		
		Assert.assertTrue(pdp.addProductToCart("Step 5:  Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));
		
		Assert.assertNotEquals(shoppingCart, null);
		
		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 6: Click on Mini cart and click Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 7: Click on 'save as quote' link", "Create New Quote' page should be displayed");
		Assert.assertNotEquals(createNewQuote, null);
		
		Assert.assertTrue(createQuote.verifyAndSelectB2BiQuoteCheckBox("Step 8 & 9 : Verify & Select B2Bi checkbox.",
				"B2Bi quote checkbox should verified and selected."));

		quoteDetails = createQuote.createQuote("Step 10 : Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page.", data.get("quoteName"), purchaser);
		Assert.assertNotEquals(null, quoteDetails);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification(
				"Pre-Condition: Fetching Quote Reference Number to verify B2Bi checkbox in Quote Listing Page.", "Get Quote Reference number.");
		Assert.assertNotEquals(quoteDetailsList, null);
		String quoteRefrenceNumber = quoteDetails.getQuoteData(quoteDetailsList,"Quote Number");				

		Assert.assertTrue(quoteDetails.verifyB2BiQuotecheckboxCheckedOrNot("Step 11 : Verify B2Bi checkbox in Quote confirmation page.",
				"B2Bi checkbox should be displayed as checked.", ""));
		
		Assert.assertTrue(quoteDetails.logout("PreCondition:click on Log out button.", "User should be landed in signin page", "Quote Details Page", false));

		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition</b>", "<b>Quote Creation By Partner Agent is Completed.</b>");

		_webcontrols.get().launchUrl("Step 1 : Launch B2Bi OCI URL.", b2biurl);

		Assert.assertTrue(login.loginToB2Bi("Step 2 : Select No, Thanks. I will continue with generic access and Click on " + "Login button.", "Home page should display."));

		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 3 : Click on Orders and Quotes link & Select Quotes from dropdown.",
				"User should redirect to Quote Listing Page.", "Quotes"));
		Assert.assertTrue(quoteListing.searchNewlyCreatedB2BiQuote("Step 4 : Verify quote details created by Partner Agent on Quote listing page.",
				"B2Bi Quote created by Partner Agent should be displayed in Quote Listing Page.", quoteRefrenceNumber));
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
  }
}

