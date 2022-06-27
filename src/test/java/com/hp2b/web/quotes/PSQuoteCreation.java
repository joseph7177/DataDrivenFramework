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

public class PSQuoteCreation extends FrameworkMethods {
	
	Logger logger = Logger.getLogger(PSQuoteCreation.class);

	private String url = "";

	public void setEnvironment(String store) {
		String selector = System.getProperty("env");
		selector = selector + "-PS" + store;
		url = _webcontrols.get().propFileHandler().readProperty("config", selector);
	}
	
	@Test(groups = { IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.PS, 
			IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.STOREFRONT }, priority = 2)
	public void C462668_Regression_PS_Quote_CBN_VerifyUserSelectedDefaultBillingAddressAndShippingAddressInQuoteCreationPage_FederalCSR() {
			
		// Reporting info
		initializeReporting("Verify user selected default Billing address and Shipping address in Quote creation page",
				"C462668_Regression_PS_Quote_CBN_VerifyUserSelectedDefaultBillingAddressAndShippingAddressInQuoteCreationPage_FederalCSR", logger);
		
		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(CSRORFEDCSRUSERPWD);	
		
//		String bto = getProduct(USPS, BTO);
//		Assert.assertNotNull(bto);
		//String bto = getProduct(USPS, BTO);
		String bto = "J6E64AA";
		Assert.assertNotNull(bto);

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

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, user, password, true));

	    Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
			"Impersonated Purchaser user", data));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition : Click on Home Tab", "Clicked on Home Tab", true));

		/** Pre-Condition ends **/

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2  : Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("Delete product", "Product should be deleted", "PS", false));

		pdp = customerService.searchSKU("Step 3: In search box ,search for Product", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 4: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 5: Click on 'Mini cart' icon and Click on 'Go to cart' button", "Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 6: Click on 'Save as Quote' link", "Quote creation page should be displayed");
		Assert.assertNotEquals(createNewQuote, null);

//	OLD	String selectedNewBillingAddress = createNewQuote.clickOnChangeBillingAddressAndSelectNewBillingAddressAndClickOnOk("Step 6 & 7: Click on 'Change Billing Address And select the address that needs to be defaulted and click on ok",
//				"Billing address popup should be displayed & Billing Address section is loaded with the selected address", false);
//		Assert.assertNotEquals(selectedNewBillingAddress,null);
//
//		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 8: Select 'Default Billing Address' checkbox",
//				"Billing address is selected as default", "billing address"));
//
//		String selectedNewShipAddress=createNewQuote.clickOnChangeShippingAddressAndSelectNewShippingAddressAndClickOnOk("Step 9 & 10 : Click on 'Change Shipping Address And select the address that needs to be defaulted and click on ok",
//				"Shipping address popup should be displayed & Shipping Address section is loaded with the selected address", false);
//		Assert.assertNotEquals(selectedNewShipAddress, null);
//
//		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 11: Select 'Default Shipping Address' checkbox",
//	OLD		"Shipping address is selected as default", "shipping address"));
		
		
		String updatedAddress = checkout.selectAlternateBillingAddressInCheckoutAndClickOnOk("Step 7 & 8: Click on change billing address and select the address that needs to be defaulted and click on ok",
			      "Change billing address should be updated on checkout page.");
		Assert.assertNotEquals(updatedAddress,null);
		
		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 9: Select 'Default Billing Address' checkbox",
		"Billing address is selected as default", "billing address"));
		
		String selectedNewShipAddress = createNewQuote
				.clickOnChangeShippingAddressAndSelectNewShippingAddressAndClickOnOk(
						"Step 10 & 11 : Click on 'Change Shipping Address And select the address that needs to be defaulted and click on ok",
						"Shipping address popup should be displayed & Shipping Address section is loaded with the selected address", false);
		Assert.assertNotEquals(selectedNewShipAddress, null);
	
		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("Step 12: Select 'Default Shipping Address' checkbox",
				"Shipping address is selected as default", "shipping address"));
		
		
//			Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("PreCondition : Select default Billing and Shipping address checkbox", "Billing and Shipping address checkbox Should be selected","Billing and Shipping Address"));
//
//			Assert.assertTrue(createNewQuote.clickOnOkButtonInsideDefaultAddressConfirmationPopup("PreCondition : Click on ok button inside default address confirmation popup", "Ok button should be selected",false));

		LinkedHashMap<String, String> billingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"createNewQuote","billing");
		Assert.assertNotEquals(billingInfoInCreateNewQuotePage, null);

		LinkedHashMap<String, String>shippingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"createNewQuote","shipping");
		Assert.assertNotEquals(shippingInfoInCreateNewQuotePage, null);
		
		quoteDetails = createNewQuote.createQuote("Step 13: Provide all details and click on save quote",
				"Quote should created successfully", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		pdp = customerService.searchSKU("Step 14: In search box ,search for Product", "PDP of searched product should be displayed", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 15: Click on 'Add to cart' button", "Product should be added to cart Successfully","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 16:Click on Mini cart and click Go to cart button",
				"User Should navigate to shopping cart page successfully");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 17: Click on save as quote button", "User should  navigate to quote creation page");
		Assert.assertNotEquals(createNewQuote, null);

//	Assert.assertTrue(checkout.verifyShppingAndBillingAddressDetails("Step 17: Verify the defaulted Billing Address is shown\r\n"
//				+ "Note: Billing address as selected in Step 7", "Default Billing address verified successfully", selectedNewBillingAddress, true));

//		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsSelected("Step 18: Verify 'Default Billing Address' checkbox is selected",
//				"'Default Billing Address' checkbox is selected", true));
//
//		Assert.assertTrue(checkout.verifyShppingAndBillingAddressDetails("Step 19: Verify the defaulted Shipping Address is shown\r\n"
//				+ "Note: Shipping address as selected in Step 9", "Default Shipping address verified successfully", selectedNewShipAddress, false));
//
//		Assert.assertTrue(createNewQuote.verifyDefaultShippingAddressCheckboxIsSelected("Step 20: Verify 'Default Shipping Address' checkbox is selected",
//			"'Default Shipping Address' checkbox is selected", true));
		
		
		LinkedHashMap<String, String> billingInfoInCreateNewQuotePage2= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"createNewQuote","billing");
		Assert.assertTrue(billingInfoInCreateNewQuotePage2.size()>0);

		LinkedHashMap<String, String>shippingInfoInCreateNewQuotePage2= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"createNewQuote","shipping");
		Assert.assertTrue(shippingInfoInCreateNewQuotePage2.size()>0);
		
		Assert.assertTrue( createNewQuote.compareTwoHashMap("Step 18 :Ensure that previously selected 'Default Billing Address' must be displayed","Default Billing address verified successfully", billingInfoInCreateNewQuotePage, billingInfoInCreateNewQuotePage2));

		Assert.assertTrue(createNewQuote.verifyDefaultBillingAddressCheckboxIsSelected("Step 19: Verify 'Default billing Address' checkbox is selected.","'Default Billing Address' checkbox should be selected by default",true));

		Assert.assertTrue( createNewQuote.compareTwoHashMap("Step 20 :Ensure that previously selected 'Default Shipping Address' must be displayed","Default Shipping address verified successfully", shippingInfoInCreateNewQuotePage, shippingInfoInCreateNewQuotePage2));
		
		Assert.assertTrue(createNewQuote.verifyDefaultShippingAddressCheckboxIsSelected("Step 21: Verify 'Default Shipping Address' checkbox is selected.","'Default Shipping Address' checkbox should be selected by default",true));

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
		
		/*
		// Test Data
		Map<String, String> regData = getScenarioData(ID04);
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
		String purchaser = getUser(ID04, PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
	
		
		//String bto = getProduct(USPS, BTO);
		String bto = "J6E64AA";
		Assert.assertNotNull(bto);
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);
		data.put("emailID", purchaser);
		
		
		// Get URL
		setEnvironment("-CSR");
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
	/*	Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, csr, password, true));

	    Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
			"Impersonated Purchaser user", data));
	    
		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));
		
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

//		Assert.assertTrue(createNewQuote.selectPaymentMethodFromPaymentDropDown("Step 8: Select Payment method as 'Purchase Order'", 
//				"Payment method should be selected", "Purchase Order"));
		String updatedAddress = checkout.selectAlternateBillingAddressInCheckoutAndClickOnOk("Step 9: Select any other billing address other than default.Click on OK",
			      "Change billing address should be updated on checkout page.");
			Assert.assertNotEquals(updatedAddress,null);
			
			String shippingAddressValue = checkout.selectAlternateShippingAddressAndClickOnOk("Step 11: Select any address other than default shipping address",
//				"Change shipping address should be updated in checkout page", true);
//			Assert.assertNotEquals(shippingAddressValue, null);
			
			String selectedNewShipAddress = createNewQuote
					.clickOnChangeShippingAddressAndSelectNewShippingAddressAndClickOnOk(
							"Step 10 & 11 : Click on 'Change Shipping Address And select the address that needs to be defaulted and click on ok",
							"Shipping address popup should be displayed & Shipping Address section is loaded with the selected address", false);
			Assert.assertNotEquals(selectedNewShipAddress, null);
			
		Assert.assertTrue(createNewQuote.selectDefaultBillingAndShippingAddressCheckbox("PreCondition : Select default Billing and Shipping address checkbox", "Billing and Shipping address checkbox Should be selected","Billing and Shipping Address"));

		Assert.assertTrue(createNewQuote.clickOnOkButtonInsideDefaultAddressConfirmationPopup("PreCondition : Click on ok button inside default address confirmation popup", "Ok button should be selected",false));

		LinkedHashMap<String, String> billingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Billing address","Billing  address Should be fetched",true,"createNewQuote","billing");
		Assert.assertNotEquals(billingInfoInCreateNewQuotePage, null);

		LinkedHashMap<String, String>shippingInfoInCreateNewQuotePage= createNewQuote.fetchAddress("PreCondition : Fetch Shipping address","Shipping address Should be fetched",true,"createNewQuote","shipping");
		Assert.assertNotEquals(shippingInfoInCreateNewQuotePage, null);

		Assert.assertNotNull(createNewQuote.createQuote("Step 8: Enter all mandatory details and click on Save quote button",
				"Quote should be created successfully and navigated to quote confirmation page", "AutQuote", csr));

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotNull(createNewQuote);
		String quoteName = "AutQuote" + quoteDetails.getQuoteData(quoteDetailsList, "Quote Name");
	
	
		//Assert.assertTrue(customerService.clickOnHomeTab("Precondition: Click on Home tab", "Home page should display", true));
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
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");	*/
   
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.STOREFRONT }, priority = 2)
	public void C462670_Regression_PS_Quote_VerifySPCInQuoteCreationAndQuoteConfirmationPage_FederalCSR(){
		
		// Reporting info
		initializeReporting("Verify SPC in Quote creation and Quote confirmation page",
				"C462670_Regression_PS_Quote_VerifySPCInQuoteCreationAndQuoteConfirmationPage_FederalCSR", logger);
	
		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String password = passwords.get(CSRORFEDCSRUSERPWD);
	//	String bto = getProduct(USPS, BTO);
		String bto =	"E0X96AA";
		Assert.assertNotNull(bto);
	//	String bto2 = getProduct(USPS, BTO);
		String bto2 ="J6E64AA";
		Assert.assertNotNull(bto);
	    String spc = regData.get("SPC");
	
		// Waiting for user availability
	    ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(FEDERALCSR);
		Assert.assertNotNull(csr);
		userSet.add(csr);
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
		QuoteListing quoteListing = new QuoteListing(_webcontrols);
		data.put("emailID", purchaser);
		System.out.println(purchaser);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition : Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("PreCondition : Impersonate user","User is Impersonated.",data));

    	Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Select requested catalog", "Requested catalog should be selected", data,true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "PS", false));

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
	@Test( groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT, IGroupsTagging.IModuleType.QUOTES }, priority = 1)
	
    public void C462667_Regression_PS_Quote_VerifyB2biCheckboxDisplayInCreateQuotePageWhenViewAllQuoteRoleIsEnabledForUser_Direct(){
		
		// Reporting info
		initializeReporting("Verify B2bi’ checkbox display in create quote page when 'View all quote' role is enabled for user",
				"C462667_Regression_PS_Quote_VerifyB2biCheckboxDisplayInCreateQuotePageWhenViewAllQuoteRoleIsEnabledForUser_Direct", logger);
	
		// Test data
		Map<String, String> regData = getScenarioData(ID04);
		Assert.assertNotEquals(regData.size(), 0);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);		

		// Waiting for user availability
		//String purchaser = getUser(ID04, PURCHASER);
		String purchaser = "hp2bfeautomation+699375086P01@gmail.com";
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));

		Map<String, String> data = new HashMap<String, String>();
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		// Get URL
		setEnvironment("-CSR");
		String url = this.url;
			
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
		
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with Purchaser", url, purchaser, password, true));
	
		Assert.assertTrue(customerService.selectOrganizationAndContract("STep 1 & 2: Select org & contract", "Requested org & contract should be selected", data, true));
	
		Assert.assertTrue(customerService.clickOnHomeTab("Precondition: Click on Home tab", "Home page should display", true));
		
		Assert.assertTrue(home.deleteProducts("Precondition: Delete products", "Products should be deleted", "PS", true));
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
		
//		Assert.assertTrue(quoteDetails.logout("PreCondition:click on Log out button.", "User should be landed in signin page", "Quote Details Page", false));
//
//		Reporting.getLogger().log(LogStatus.INFO, "<b>Pre-Condition</b>", "<b>Quote Creation By Partner Agent is Completed.</b>");
//
//		_webcontrols.get().launchUrl("Step 1 : Launch B2Bi OCI URL.", b2biurl);
//
//		Assert.assertTrue(login.loginToB2Bi("Step 2 : Select No, Thanks. I will continue with generic access and Click on " + "Login button.", "Home page should display."));
//
//		Assert.assertTrue(login.clickOnOrderAndQuotesTabAndNavigate("Step 3 : Click on Orders and Quotes link & Select Quotes from dropdown.",
//				"User should redirect to Quote Listing Page.", "Quotes"));
//
//		Assert.assertTrue(quoteListing.searchNewlyCreatedB2BiQuote("Step 4 : Verify quote details created by Partner Agent on Quote listing page.",
//				"B2Bi Quote created by Partner Agent should be displayed in Quote Listing Page.", quoteRefrenceNumber));
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
  }	
}
			
