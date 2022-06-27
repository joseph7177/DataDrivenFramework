package com.hp2b.web.quotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.hp2b.common.FrameworkMethods;
import com.hp2b.common.HP2BDataProvider;
import com.hp2b.interfaces.testcasetagging.IGroupsTagging;
import com.hp2b.web.pom.Checkout;
import com.hp2b.web.pom.CreateNewQuote;
import com.hp2b.web.pom.CustomerService;
import com.hp2b.web.pom.Login;
import com.hp2b.web.pom.PDP;
import com.hp2b.web.pom.QuoteDetails;
import com.hp2b.web.pom.ShoppingCart;
import com.hpicorp.hpframework.reporting.Reporting;
import com.relevantcodes.extentreports.LogStatus;

public class CEQuoteToCheckout extends FrameworkMethods {
	
    Logger logger = Logger.getLogger(CEQuoteToCheckout.class);
	
	private String url = "";	
	private static final String config = "config";
	private static final String module = "Quotes";

	public void setEnvironment() {
		if (url.isEmpty()) {			
			url = _webcontrols.get().propFileHandler().readProperty(config, ENVIRONMENT);
		}
	}
	
	/**
	 * Verify quote creation for change in billingAddress
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300030
	 * @param region APJ,EMEA,AMS-US,AMS-LA
	 * @since Mar 15, 2022 
	 * @author Vijay R
	 */		
	@Test(dataProvider = "emeaAndApjRegion_data-provider", dataProviderClass = HP2BDataProvider.class, groups = {
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

		Assert.assertTrue(pdp.addToCartAtPDPNoPopup("Step 2.2: Add product to cart at PDP", "Product should be added to cart"));
		
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


}
