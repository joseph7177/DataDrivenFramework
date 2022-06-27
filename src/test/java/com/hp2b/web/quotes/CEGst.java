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
import com.hp2b.common.HP2BStaticData;
import com.hp2b.interfaces.testcasetagging.IGroupsTagging;
import com.hp2b.pdf.PDFValidations;import com.hp2b.web.pom.Checkout;
import com.hp2b.web.pom.CreateAddress;
import com.hp2b.web.pom.CreateNewQuote;
import com.hp2b.web.pom.CustomerService;
import com.hp2b.web.pom.Login;
import com.hp2b.web.pom.OrderConfirmation;
import com.hp2b.web.pom.PDP;
import com.hp2b.web.pom.PLP;
import com.hp2b.web.pom.PageGenerics;
import com.hp2b.web.pom.QuoteDetails;
import com.hp2b.web.pom.ShoppingCart;
import com.hp2b.web.pom.gmail.GmailPage;
import com.hp2b.xls.XLSValidations;
import com.hpicorp.hpframework.reporting.Reporting;
import com.relevantcodes.extentreports.LogStatus;

public class CEGst extends FrameworkMethods {

	Logger logger = Logger.getLogger(CEGst.class);
	private String url = "";
	private static final String config = "config";
	private static final String module = "Quotes";
	String LEASE = "Lease";
	String poNumber = "9999";
	String attentionText = "test";
	String phoneNumber = "12345";
	String firstName = "automation";
	String lastName = "User";
	String PO = "Purchase Order";

	public void setEnvironment() {
		if (url.isEmpty()) {			
			url = _webcontrols.get().propFileHandler().readProperty(config, ENVIRONMENT);
		}
	}

	/**
	 * GST_Verify estimated Tax amount in while converting quote to PO, when isSez checkbox is checked
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300496
	 * @since Apr 26, 2020
	 * @author Manpreet & ThomasAn
	 */	
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
	//		IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300496_Regression_CE_GST_VerifyEstimatedTaxAmountInWhileConvertingQuoteToPOWhenIsSezCheckboxIsChecked_CSR() throws IOException {
		// Reporting info
		initializeReporting("GST_Verify estimated Tax amount in while converting quote to PO, when isSez checkbox is checked",
				"C300496_Regression_CE_GST_VerifyEstimatedTaxAmountInWhileConvertingQuoteToPOWhenIsSezCheckboxIsChecked_CSR", logger);
        
		// Test data
		Map<String, String> regData = getScenarioData("ID01");
		Assert.assertNotEquals(regData.size(), 0);

		String password = commonData.get(CSRORFEDCSRUSERPWD);
		String mdcpId = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String contract = regData.get("Contract");
		String billingType = "IGST";	   
	    String poNumber = regData.get("Orders");
		String phoneNumber = HP2BStaticData.phoneNumber;
		String attentionText = HP2BStaticData.attentionText;
		String PO = HP2BStaticData.PO;		  	
		  
		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpId);
		data.put("actionOnUsers", "Impersonate user");
		data.put("OrgName", orgName);
		data.put("catalogName", contract);

		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("poNumber", poNumber);
		mandatoryData.put("phoneNumber", phoneNumber);
		mandatoryData.put("attentionText", attentionText);
		mandatoryData.put("paymentMethod", PO);
		mandatoryData.put("MDCPID", mdcpId);
		mandatoryData.put("catalogName", contract);
		mandatoryData.put("actionOnUsers", actionOnUsers);
		mandatoryData.put("OrgName", orgName);
		
		// Waiting For Users Availability
		ArrayList<String> userSet= new ArrayList<>();
		String user = getUser(CSR);
		Assert.assertNotEquals(user, null);
		String purchaser = getUser("ID01", PURCHASER);
		Assert.assertNotNull(purchaser);
		userSet.add(user);
		userSet.add(purchaser);
		data.put("emailID", purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);
		
		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PLP plp = new PLP(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		XLSValidations xlsPage= new XLSValidations(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);
		OrderConfirmation orderConfirmation = new OrderConfirmation(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		String category = (plp.selectCategoryInProductsAndServices(
				"Step 1 & 2 : Click On Product and Services tab and select Services Category",
				"User should be landed on Product Listing Page", "Services"));
		Assert.assertNotEquals(category, null);

		Assert.assertTrue(plp.addProductToCart("Step 3: Add any BTO product in the Cart",
				"Product should be added to cart", "plp"));

		ShoppingCart shoppingCart = plp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 5: Click on 'Save as Quote link' to navigate to Create quote page", "User must be navigated to Create quote page"));

		Assert.assertTrue(createNewQuote.verifyBillingTypeHaveValue("Step 6: Ensure that Billing type label hold some value and it should not be blank",
				"Billing type should be hold value", "IGST CGST+SGST UGST+CGST"));

		//		Assert.assertTrue(createNewQuote.checkisSEZCheckBox("Step 7.1: Click on isSez Checkbox"," isSez should be checked"));
		//		Assert.assertTrue(createNewQuote.verifyBillingType("Step 7.2: Verify Billing type value changed to IGST",
		//				"Billing type must be changed to IGST", "IGST"));

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 7.1 & 7.2 : Click on isSez Checkbox and verify Billing type value changed to IGST", "isSez checkbox should be checked and billing type value should be changed to IGST"));

		String gstIdSupplier = createNewQuote.getGSTID("Pre-condition: Getting the GST ID for Supplier", "fetched the GST ID","Supplier");
		Assert.assertNotEquals(gstIdSupplier,null);

		String gstIdBillingShipping = createNewQuote.getGSTID("Pre-condition: Getting the GST ID for Shipping", "fetched the GST ID","Shipping");
		Assert.assertNotEquals(gstIdBillingShipping,null);

		Assert.assertTrue(createNewQuote.verifyEstimatedTAX("Step 8: Go to Cart summary Module and verify that estimated Tax amount is INR:0.00", "'Estimated Tax' must display tax amount as INR 0.00", "INR 0.00"));

		String quoteName = "Aut_Quote_";

//		QuoteDetails quoteDetails = createNewQuote.createQuote("Step 9.1 : Fill in other quote details", "Filled in other details", quoteName, emailID[0]);
//		Assert.assertNotEquals(quoteDetails, null);
		
		QuoteDetails quoteDetails = createNewQuote.createQuote("Step 9.1 : Fill in other quote details", "Filled in other details", quoteName,purchaser );
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 9.2: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		Assert.assertTrue(quoteDetails.verifyBillingType("Step 10: Verify Billing type in quote details page",
				"Billing type must be displayed as IGST", "IGST"));

		Assert.assertTrue(quoteDetails.verifyIsSezCheckboxIsChecked("Step 11: Verify Is Sez checkbox is checked",
				"Is Sez checkbox should be checked"));

		Assert.assertTrue(quoteDetails.verifyEstimatedTaxValueInQuoteDetails("Step 12: Go to Cart summary Module and verify that estimated Tax amount is INR:0.00", "'Estimated Tax' must display tax amount as INR 0.00", "0.00"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 13.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 13.2: Select requested file type as xls. ",
				"Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 13.3: Click on export button to export file.", "XLS should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 13.4 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 13.5: Select requested file type as pdf. ",
				"Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 13.6: Click on export button to export file.", "PDF should get exported successfully"));

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Step 13.7: Get Estimated Tax Value", "Estimated tax price is fetched", true);
		Assert.assertNotEquals(estimatedTax, null);

		Assert.assertTrue(xlsPage.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching(
				"Step 14.1: Open xls file", "HTML Embedded xls file should be opened", newQuote + ".xls"));
		Assert.assertTrue(xlsPage.verifyEstimatedTaxInHtmlEmbeddedXls("Step 14.2: Verify Estimated tax value<br>",
				"Estimated tax line item should be displayed with all other details of quote", newQuote, estimatedTax));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.3: Verify Billing informaiton module:<br>Billing type-IGST"
				, "Billing type- IGST is verified", newQuote, "Billing type", "", "IGST"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.4: Verify Billing informaiton module: <br>GST ID "
				, "", quoteName, "GST ID", "Billing", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.5: Verify Supplier address section: <br>GST ID "
				, "", quoteName, "GST ID", "Supplier", gstIdSupplier));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.6: Verify shipping informaiton section: <br>GST ID "
				, "", quoteName, "GST ID", "Shipping", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.7: Verify isSEZ checkbox value: <br>GST ID "
				, "", quoteName, "isSEZ", "", "Yes"));

		Assert.assertTrue(xlsPage.navigateToPreviousPage("quoteDetails"));

		String pdfContent = pdfPage.readPdfFileInDownloads("Step 15.1: Read PDF file", "Pdf file content is read", newQuote+".pdf ", true);
		Assert.assertNotEquals(pdfContent, "");

		Assert.assertTrue(pdfPage.verifyisSEZIsDisplayedInPDF("Step 15.2: Verify isSEZ checkbox value","isSEZ should be No",pdfContent,"Yes"));

		Assert.assertTrue(pdfPage.verifyEstimatedTaxInPDF("Step 15.3: Verify estimated tax",
				"Estimated tax should be verified",estimatedTax,pdfContent));

		Assert.assertTrue(pdfPage.verifyBillingTypeInPDF("Step 15.4: Verify billing typE","Billing type should be displayed","IGST",pdfContent));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 15.5: Click on open PDF attachment", "User must be able to find the " +
				"Supplier address section:<br>" +
				"GST ID<br>"
				, pdfContent, "Supplier",gstIdSupplier));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 15.6: Click on open PDF attachment", "User must be able to find the " +
				"Billing informaiton module<br>" +
				"Billing type-IGST, GST ID<br>"
				, pdfContent, "Shipping",gstIdBillingShipping));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 15.7: Click on open PDF attachment", "User must be able to find the " +
				"Shipping Information - GST ID<br>"
				, pdfContent, "Shipping",gstIdBillingShipping));

		Checkout checkout = quoteDetails.navigateToCheckoutPage("Step 16: Click on Checkout button", "User must be navigated to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertNotNull(checkout.verifyFieldInAddressInfoSection("Step 17.1: Verify checkout page contains Billing Type Field", 
				"Checkout page contains Billing Type Field", "Billing", "Billing Type", billingType, true));

		String billingGstId = checkout.verifyFieldInAddressInfoSection("Step 17.2: Scroll up to 'Billing Information' section and ensure that GST ID is present and have appropriate associated value", 
				"Billing information section must have GST ID", "Billing", "GST ID", "", true);
		Assert.assertNotEquals(billingGstId, null);

		String shippingGstId = checkout.verifyFieldInAddressInfoSection("Step 17.3: In the Shipping address section verify GST ID", 
				"Shipping information section must have GST ID", "Shipping", "GST ID", "", true);
		Assert.assertNotEquals(shippingGstId, null);

		String supplierGstId = checkout.verifyFieldInAddressInfoSection("Step 17.4: In the supplier address section verify GST ID", 
				"Supplier section must have GST ID", "Supplier", "GST ID", "", false);
		Assert.assertNotEquals(supplierGstId, null);

		//check box not checked in checkout page
		Assert.assertTrue(checkout.verifyIsSezCheckboxIsChecked("Step 17.5: Verify Is Sez checkbox is checked",
				"Is Sez checkbox should be checked"));

		Assert.assertNotNull(checkout.verifyEstimatedTaxIsDisplayedInCheckout("Step 17.5: Verify Estimated Taxes Line Item on Checkout page", "Validate Estimated Tax with price", true));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 18: Fill in mandatory details in checkout page", "Fill in mandatory details in checkout page", mandatoryData));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 19.1: Click on Purchase order button", "Clicked on Purchase order button", true));

		Assert.assertTrue(orderConfirmation.verifyFieldInAddressInfoSection("Step 19.2: Verify that Billing information module has GST ID", 
				"Billing information must have GST ID", "Billing", "GST ID", billingGstId, true));

		Assert.assertTrue(orderConfirmation.verifyFieldInAddressInfoSection("Step 19.3: Verify that Billing information module has Billing Type", 
				"Billing information must have Billing Type", "Billing", "Billing Type", billingType, true));

		Assert.assertTrue(orderConfirmation.verifyFieldInAddressInfoSection("Step 19.4: Verify that Supplier information module has GST ID", 
				"Supplier information must haveGST ID", "Supplier", "GST ID", supplierGstId, true));

		Assert.assertTrue(orderConfirmation.verifyFieldInAddressInfoSection("Step 19.5: Verify that Shipping information module has GST ID", 
				"Shipping information must haveGST ID", "Shipping", "GST ID", shippingGstId, true));

		Assert.assertTrue(orderConfirmation.verifyIsSezCheckboxIsChecked("Step 19: Verify Is Sez checkbox is checked",
				"Is Sez checkbox should be checked"));

		String estTax = checkout.verifyEstimatedTaxIsDisplayedInCheckout("Step 19.6: Verify Estimated Taxes Line Item on Order Confirmation page", 
				"Validate the Estimated Tax on Order confirmation page", true);
		Assert.assertNotNull(estTax);

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * GST_Verify estimated Tax amount in while converting quote to PO, when isSez checkbox is unchecked
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300497
	 * @param region APJ
	 * @since Apr 26, 2020
	 * @author Manpreet & ThomasAn
	 */

	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
	//		IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300497_Regression_CE_GST_VerifyEstimatedTaxAmountInWhileConvertingQuoteToPOWhenIsSezCheckboxIsUnChecked_CSR() throws IOException {

		// Reporting info
		initializeReporting("GST_Verify estimated Tax amount in while converting quote to PO, when isSez checkbox is unchecked",
				"C300497_Regression_CE_GST_VerifyEstimatedTaxAmountInWhileConvertingQuoteToPOWhenIsSezCheckboxIsUnChecked_CSR", logger);

		// Test data		
		HashMap<String, String> regData = getTestData("CE","DT002_APJ");
		Assert.assertNotEquals(regData.size(), 0);

		Assert.assertNotEquals(regData.size(), 0);
		String catalogName = regData.get("Contract");
		String orgName = regData.get("OrganizationName");
		String mdcpid = regData.get("MDCPID");
		String emailID[] = regData.get("Purchaser").split("_");
		String bto = regData.get("BTO").split("_")[0];
		String poNumber = regData.get("Orders");
		String phoneNumber = HP2BStaticData.phoneNumber;
		String attentionText = HP2BStaticData.attentionText;
		String PO = HP2BStaticData.PO;
		HashMap<String, String> csrAndPassword = getTestData("CE", module);
		Assert.assertNotEquals(csrAndPassword.size(), 0);

		String[] userData = csrAndPassword.get("CSRUsersForCE").split("_");
		String password = csrAndPassword.get("CSRAndFedCSRPassword");
		Assert.assertNotEquals(csrAndPassword.size(), 0);
		String billingType = "CGST+SGST";

		Map<String, String> data = new HashMap<String, String>();
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", "Impersonate user");
		data.put("OrgName", orgName);
		data.put("catalogName", catalogName);

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		PLP plp = new PLP(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		XLSValidations xlsPage= new XLSValidations(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
		OrderConfirmation orderConfirmation = new OrderConfirmation(_webcontrols);

		// Waiting For Users Availability		
		ArrayList<String> userSet= new ArrayList<>();
		String user = waitForUserAvailability(userData);
		Assert.assertNotEquals(user, null);
		String purchaser = waitForUserAvailability(emailID);
		Assert.assertNotEquals(purchaser, null);
		data.put("emailID", purchaser);
		userSet.add(purchaser);
		userSet.add(user);
		usersMappedToThreadID.put(Thread.currentThread().getId(), userSet);

		Map<String, String> mandatoryData = new HashMap<String, String>();
		mandatoryData.put("poNumber", poNumber);
		mandatoryData.put("phoneNumber", phoneNumber);
		mandatoryData.put("attentionText", attentionText);
		mandatoryData.put("paymentMethod", PO);
		mandatoryData.put("MDCPID", mdcpid);
		mandatoryData.put("catalogName", catalogName);
		mandatoryData.put("actionOnUsers", actionOnUsers);
		mandatoryData.put("OrgName", orgName);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, user, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user", "Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		/** Pre-Condition ends **/

		String category = (plp.selectCategoryInProductsAndServices(
				"Step 1 & 2 : Click On Product and Services tab and select Services Category",
				"User should be landed on Product Listing Page", "Services"));
		Assert.assertNotEquals(category, null);

		Assert.assertTrue(plp.addProductToCart("Step 3: Add any BTO product in the Cart",
				"Product should be added to cart", "plp"));

		ShoppingCart shoppingCart = plp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", 
				"Shopping cart page should be displayed");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 5: Click on 'Save as Quote link' to navigate to Create quote page", "User must be navigated to Create quote page"));

		String stateOrProvinceName = checkout.verifyFieldInAddressInfoSection("Precondition: Getting Billing address State/Province","State/Province should be fetched","Billing","State/Province:","",false);
		Assert.assertNotEquals(stateOrProvinceName, null);

		Assert.assertTrue(checkout.clickOnChangeSupplierAddress("Step 6.1: Now click on 'Change Supplier address' Button", "'Supplier addresses' popup should appears", true));

		Assert.assertNotNull(checkout.selectSupplierAddressBasedOnBillingInfoStateName(
				"Step 6.2: Select the address and click on OK button","Address must be saved and Page must refresh",stateOrProvinceName, "Same"));

		Assert.assertTrue(createNewQuote.verifyIsSezCheckboxUncheckedInCreateQuote("Step 7: Verify IsSez checkbox displayed as unchecked ","IsSez checkbox should be displayed as unchecked"));

		String gstIdSupplier = createNewQuote.getGSTID("Pre-condition: Getting the GST ID for Supplier", "fetched the GST ID","Supplier");
		Assert.assertNotNull(gstIdSupplier,null);

		String gstIdBillingShipping = createNewQuote.getGSTID("Pre-condition: Getting the GST ID for Shipping", "fetched the GST ID","Shipping");
		Assert.assertNotNull(gstIdBillingShipping,null);

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 8: Go to Cart summary Module and verify that estimated Tax amount shows some amount", "'Estimated Tax' should be displayed some tax amount", true));

		String quoteName = "Aut_Quote_";

		QuoteDetails quoteDetails = createNewQuote.createQuote("Step 9: Fill other quote details", "Other quote details should be filled", quoteName, emailID[0]);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Pre-condition: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		System.out.println(newQuote);

		Assert.assertTrue(quoteDetails.verifyBillingType("Step 10: Verify Billing type in quote details page",
				"Billing type must be displayed as IGST", "CGST+SGST"));

		Assert.assertTrue(quoteDetails.verifyIsSezCheckboxUncheckedInQuoteDetails("Step 11 : Verify IsSez checkbox displayed as unchecked ","IsSez checkbox should be displayed as unchecked"));
		Assert.assertTrue(createNewQuote.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 12: Go to Cart summary Module and verify that estimated Tax amount shows some amount", "'Estimated Tax' should be displayed some tax amount", true));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 13.1 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 13.2: Select requested file type as xls. ",
				"Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 13.3: Click on export button to export file.", "XLS should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 13.4 : Click on 'Export catalog' button",
				"Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 13.5: Select requested file type as pdf. ",
				"Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup(
				"Step 13.6: Click on export button to export file.", "PDF should get exported successfully"));

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Pre-condition: Get Estimated Tax Value", "Estimated tax price is fetched", true);
		Assert.assertNotEquals(estimatedTax, null);

		Assert.assertTrue(xlsPage.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching(
				"Step 14.1: Open xls file", "HTML Embedded xls file should be opened", newQuote + ".xls"));
		Assert.assertTrue(xlsPage.verifyEstimatedTaxInHtmlEmbeddedXls("Step 14.2: Verify Estimated tax value<br>",
				"Estimated tax line item should be displayed with all other details of quote", newQuote, estimatedTax));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.3: Verify Billing informaiton module:<br>Billing type-IGST"
				, "Billing type- IGST is verified", newQuote, "Billing type", "", "SGST+CGST"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.4: Verify Billing informaiton module: <br>GST ID "
				, "", quoteName, "GST ID", "Billing", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.5: Verify Supplier address section: <br>GST ID "
				, "", quoteName, "GST ID", "Supplier", gstIdSupplier));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.6: Verify shipping informaiton section: <br>GST ID "
				, "", quoteName, "GST ID", "Shipping", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 14.7: Verify isSEZ checkbox value: <br>GST ID "
				, "", quoteName, "isSEZ", "", "No"));

		Assert.assertTrue(xlsPage.navigateToPreviousPage("quoteDetails"));

		String pdfContent = pdfPage.readPdfFileInDownloads("Step 15.1: Read PDF file", "Pdf file content is read", newQuote+".pdf ", true);
		Assert.assertNotEquals(pdfContent, "");

		Assert.assertTrue(pdfPage.verifyisSEZIsDisplayedInPDF("Step 15.2: Verify isSEZ checkbox value","isSEZ should be No",pdfContent,"No"));

		Assert.assertTrue(pdfPage.verifyEstimatedTaxInPDF("Step 15.3: Verify estimated tax",
				"Estimated tax should be verified",estimatedTax,pdfContent));

		Assert.assertTrue(pdfPage.verifyBillingTypeInPDF("Step 15.4: Verify billing type","Billing type should be verified","SGST+CGST",pdfContent));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 15.5: Click on open PDF attachment", "User must be able to find the " +
				"Supplier address section:<br>" +
				"GST ID<br>"
				, pdfContent, "Supplier",gstIdSupplier));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 15.6: Click on open PDF attachment", "User must be able to find the " +
				"Billing informaiton module<br>" +
				"Billing type-IGST, GST ID<br>"
				, pdfContent, "Shipping",gstIdBillingShipping));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 15.7: Click on open PDF attachment", "User must be able to find the " +
				"Shipping Information - GST ID<br>"
				, pdfContent, "Shipping",gstIdBillingShipping));

		checkout = quoteDetails.navigateToCheckoutPage("Step 16: Click on Checkout button", "User must be navigated to checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertNotNull(checkout.verifyFieldInAddressInfoSection("Step 17.1: Verify checkout page contains Billing Type Field", 
				"Checkout page contains Billing Type Field", "Billing", "Billing Type", billingType, true));

		String billingGstId = checkout.verifyFieldInAddressInfoSection("Step 17.2: Scroll up to 'Billing Information' section and ensure that GST ID is present and have appropriate associated value", 
				"Billing information section must have GST ID", "Billing", "GST ID", "", true);
		Assert.assertNotEquals(billingGstId, null);

		String shippingGstId = checkout.verifyFieldInAddressInfoSection("Step 17.3: In the Shipping address section verify GST ID", 
				"Shipping information section must have GST ID", "Shipping", "GST ID", "", true);
		Assert.assertNotEquals(shippingGstId, null);

		String supplierGstId = checkout.verifyFieldInAddressInfoSection("Step 17.4: In the supplier address section verify GST ID", 
				"Supplier section must have GST ID", "Supplier", "GST ID", "", false);
		Assert.assertNotEquals(supplierGstId, null);

		Assert.assertTrue(checkout.verifyIsSezCheckboxIsUnchecked("Step 17.5: Verify Is Sez checkbox is unchecked",
				"Is Sez checkbox should be unchecked"));

		Assert.assertNotNull(checkout.verifyEstimatedTaxIsDisplayedInCheckout("Step 17.5: Verify Estimated Taxes Line Item on Checkout page", "Validate Estimated Tax with price", true));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 18: Fill in mandatory details in checkout page", "Fill in mandatory details in checkout page", mandatoryData));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 19.1: Click on Purchase order button", "Clicked on Purchase order button", true));

		Assert.assertTrue(orderConfirmation.verifyFieldInAddressInfoSection("Step 19.2: Verify that Billing information module has GST ID", 
				"Billing information must have GST ID", "Billing", "GST ID", billingGstId, true));

		Assert.assertTrue(orderConfirmation.verifyFieldInAddressInfoSection("Step 19.3: Verify that Billing information module has Billing Type", 
				"Billing information must have Billing Type", "Billing", "Billing Type", billingType, true));

		Assert.assertTrue(orderConfirmation.verifyFieldInAddressInfoSection("Step 19.4: Verify that Supplier information module has GST ID", 
				"Supplier information must haveGST ID", "Supplier", "GST ID", supplierGstId, true));

		Assert.assertTrue(orderConfirmation.verifyFieldInAddressInfoSection("Step 19.5: Verify that Shipping information module has GST ID", 
				"Shipping information must haveGST ID", "Shipping", "GST ID", shippingGstId, true));

		Assert.assertTrue(orderConfirmation.verifyIsSezCheckboxIsUnchecked("Step 19: Verify Is Sez checkbox is unchecked",
				"Is Sez checkbox should be unchecked"));

		String estTax = checkout.verifyEstimatedTaxIsDisplayedInCheckout("Step 19.6: Verify Estimated Taxes Line Item on Order Confirmation page", 
				"Validate the Estimated Tax on Order confirmation page", true);
		Assert.assertNotNull(estTax);

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify tax amount when tax code =M in S4 Flow-Quote to checkout
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/359257
	 * @throws IOException -
	 * @since May 19, 2021 10:34:24 AM
	 * @author ThomasAn
	 */	
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C359257_Regression_CE_GST_VerifyTaxAmountWhenTaxCodeMInS4FlowQuoteToCheckout_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify tax amount when tax code =M in S4 Flow-Quote to checkout",
				"C359257_Regression_CE_GST_VerifyTaxAmountWhenTaxCodeMInS4FlowQuoteToCheckout_Direct", logger);
        
		// Test data
		Map<String, String> regData = getScenarioData(ID19);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String catalogName = regData.get("Contract");
	    String orgName = regData.get("Org Name");    	       
	       
        String password = passwords.get(DIRECTUSERPWD);        
        
        Map<String, String> data = new HashMap<String, String>();
		data.put("poNumber", poNumber);
		data.put("phoneNumber", phoneNumber);
		data.put("attentionText", attentionText);
		data.put("paymentMethod", PO);
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		String quoteName = "MyQuote";
						
		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(0);			
			
		String bto =  getProductByDataSetID(APJ, ID19, BTO);
		Assert.assertNotNull(bto);
		 
		String purchaser = getUser("ID19", PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));
		
		// Get URL
		setEnvironment();
		String url = this.url;
				
		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		GmailPage gmailPage = new GmailPage(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
	
		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Purchaser", url, purchaser, password, true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));
 
		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/
		
		pdp = customerService.searchSKU("Step 1.1: Search for a Bto", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 1.2: Add Bto product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 1.3: Click on mini cart icon and click on Go to cart button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 2: Click on save as quote button", "Create quote Page should be displayed to the user"));

		LinkedHashMap<String, String> selectedS4ContractAddressDetails =createNewQuote.clickOnChangeBillingAddressEnterContractIDandVerifySelectedContractID(
				"Step 3.1: Scroll down to billing information section and select Tax class M S4 Contract from billing address"
						+ " popup a value", "Selected Billing address must be displayed",contractID);
		Assert.assertNotEquals(selectedS4ContractAddressDetails, null);

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 3.2: Verify Billing type in Billing Information",
				"Billing type must be displayed as IGST", "IGST"));

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 4 : Scroll down to find Estimated Tax label in summary", "'Estimated tax label must be displayed with some amount", true));
		quoteDetails = createNewQuote.createQuote("Step 5: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		quoteNameValue = quoteName + quoteNameValue;
	
		String estimatedTax = quoteDetails.getEstimatedTaxValue("STep 6.1: Verify estimated tax is displayed", "Estimated tax should be displayed", true);
		Assert.assertNotEquals(estimatedTax, null);

		Assert.assertTrue(quoteDetails.verifyBillingType("Step 6.2: Verify Billing type in Billing Information", "Billing type must be displayed as IGST", "IGST"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 7.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 7.2: Select requested file type as pdf. ", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 7.3: Click on export button to export file.", "PDF should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 7.4 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 7.5: Select requested file type as xls. ", "Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 7.6: Click on export button to export file.", "XLS should get exported successfully"));

		String pdfValue = pdfValidations.readPdfFileInDownloads("Precondition: Getting PDF content", "PDF content should be fetched", quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyBillingTypeInPDF("Step 8.1: Click to open PDF format", "Billing type should display as IGST", "Billing type: IGST", pdfValue));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxIsDisaplayedInPDF("Step 8.2: Click to open PDF format", "Estimated tax has to be displayed", pdfValue));

		estimatedTax = quoteDetails.getEstimatedTaxValue("Precondition: Get Estimated Tax Value", "Estimated tax price is fetched", true);
		Assert.assertNotEquals(estimatedTax, null);

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 9.1: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp(
				"Step 9.2: Enter valid email in email address field and Select PDF radio button and click on Email button",
				"Your quote was sent successfully message should be displayed", "PDF", purchaser));

		String pdfFile = gmailPage.getAttachmentInDownloads("Precondition: Login to the emailed account and download quote PDF",
				"Quote PDF should be fetched from email", quoteNameValue + ".pdf", 12);
		Assert.assertNotEquals(pdfFile, null);

		String pdfContent = pdfValidations.readPdfFileInDownloads("Precondition: Getting PDF content", "PDF content should be fetched", quoteNameValue + ".pdf", true);
		Assert.assertNotEquals(pdfContent, "");

		Assert.assertTrue(pdfValidations.verifyBillingTypeInPDF("Step 10.1: Click to open PDF format",
				"Billing type should display as IGST", "Billing type: IGST", pdfValue));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxIsDisaplayedInPDF("Step 10.2: Click to open PDF format", "Estimated tax  has to be displayed", pdfValue));

		checkout = quoteDetails.navigateToCheckoutPage("Step 11: Click on Check out button", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 12.1: Verify Billing type in Billing Information", "Billing type must be displayed as IGST", "IGST"));

		Assert.assertNotNull(checkout.verifyEstimatedTaxIsDisplayedInCheckout("Step 12.2: Verify Estimated Taxes displayed on Checkout page",
						"Estimated tax should be displayed", true));
		
		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 13.1: Enter all the Mandatory fields", "Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder("Step 13.2: Click on Create purchase Order button to place Place an Order",
				"User successfully Placed Purchase Order and landed to Order confirmation Page", true));

		PageGenerics.deleteAFile(quoteNameValue + ".pdf ");

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify billing type label IGST in Quote detail page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300495
	 * @param region APJ
	 * @since May 5, 2021 3:52:39 PM
	 * @author Vijay
	 * @throws IOException 
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, 
	//		IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300495_Regression_CE_GST_VerifyBillingTypeLabelIGSTInQuoteDetailPage_CSR() throws IOException {

		// Reporting info
		initializeReporting("Verify billing type label IGST in Quote detail page",
				"C300495_Regression_CE_GST_VerifyBillingTypeLabelIGSTInQuoteDetailPage_CSR", logger);

		// Test Data
		String scenariodId = "ID01";
		Map<String, String> regData = getScenarioData(scenariodId);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = commonData.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability		
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotEquals(csr, "");
		String purchaser = getUser(scenariodId, PURCHASER);
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

		String quoteName = "Aut_Quote_";

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		CreateAddress createAddress=new CreateAddress(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PLP plp = new PLP(_webcontrols);
		XLSValidations xlsPage= new XLSValidations(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		data.put("emailID", purchaser);

		HashMap<String, String> shippingAdress = new HashMap<String, String>();
		shippingAdress.put("Company", "hp2b");
		shippingAdress.put("AttentionText", attentionText);
		shippingAdress.put("City", "banglor");
		shippingAdress.put("Phone", phoneNumber);
		shippingAdress.put("Email", purchaser);
		String addressLine = createAddress.systemDate();
		shippingAdress.put("Addressline1", addressLine);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition: Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		Assert.assertNotEquals(customerService.selectCategoryInProductsAndServices("Step 1: Click on Products & Services & Select Category." +
				"<br>Step 2: Click on any one category",
				"All the catalogs assigned for that organization must be displayed<br>User must be landed in PLP", "Accessories"), null);

		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart.", "Product must get added to cart", "plp"));

		shoppingCart = plp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertNotEquals(shoppingCart.navigateToQuoteCreationPage("Step 5: In the shipping cart page,Click on 'Save as Quote'link to navigate to Checkout page",
				"User must be navigated to Create Quote page"), null);

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 6: Scroll down to Supplier Address section and check for GST ID",
				"GST ID must be present", "GST ID","Supplier"));

		String stateOrProvinceName = checkout.verifyFieldInAddressInfoSection("Precondition: Getting Billing address State/Province", 
				"State/Province should be fetched","Billing","State/Province:","",false);
		Assert.assertNotEquals(stateOrProvinceName, null);

		Assert.assertTrue(checkout.clickOnChangeSupplierAddress("Step 7: Now click on 'Change Supplier address' Button", "'Supplier addresses' popup should appears", true));

		Assert.assertNotNull(checkout.selectSupplierAddressBasedOnBillingInfoStateName(
				"Step 8.1: Select the address and click on OK button","Address must be saved and Page must refresh",stateOrProvinceName, "Different"));

		String gstIdSupplier = createNewQuote.getGSTID("Step 8.2: Getting the GST ID for Supplier", "fetched the GST ID","Supplier");
		Assert.assertNotNull(gstIdSupplier,null);

		String gstIdBillingShipping = createNewQuote.getGSTID("Step 8.3: Getting the GST ID for Shipping", "Fetched the GST ID","Shipping");
		Assert.assertNotNull(gstIdBillingShipping,null);

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 9.1: Scroll up to 'Billing Information' section and ensure that Below Labels are present and they have appropriate associated values\r\n" + 
				"GST ID<br>Billing Type : IGST","Billing information section must haveGST ID<br>" + 
						"Billing Type as IGST", "GST ID","Supplier"));

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 9.2: Verify Billing type in Billing Information" + 
				"<br>Billing Type : IGST","Billing information section must have <br>" + "Billing Type as IGST", "IGST"));

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 10.1: In the Shipping address section verify below entities\r\n" + 
				"GST ID<br>SEZ Disclaimer text<br>Please note, For SEZ location, your bill to and ship to must be identical." + 
				"GST ID<br>Billing Type : IGST","All the below entities must be displayed in the Shipping information module.<br>" + 
						"GST ID\r\n" + 
						"SEZ Disclaimer text-->\r\n" + 
						"Please note, For SEZ location, your bill to and ship to must be identical.", "isSEZ","Shipping"));

		Assert.assertTrue(createNewQuote.verifyIsSezCheckboxDisplayed("Step 10.2: Verify isSez checkbox", "isSez Checkbox is displayed."));

		quoteDetails = createNewQuote.createQuote("Step 11: Fill in other details and click on Save Quote Button",
				"Quote detail page appears",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 11.2: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		String estimatedTax = quoteDetails.getEstimatedTaxValue("Step 16.1: Get Estimated Tax Value", "Estimated tax price is fetched", true);

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 12.1: Verify that Billing information module has below fields\r\n" + 
				"GST ID", "Billing information must have below fields" +  "GST ID", "GST ID", "Billing"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 12.2: Verify that Billing information module has below fields\r\n" + 
				"Billing Type", "Billing information must have below fields" + "Billing Type", "Billing Type", "Billing Type"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 13: Verify that Supplier information module has below fields\r\n" + 
				"GST ID", "Billing information must have below fields <br>GST ID", "GST ID", "Supplier"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 14.1: Verify that Shipping information module has below fields\r\n" + 
				"GST ID", "Shipping information module should have below fields <br>GST ID", "GST ID", "Shipping"));

		Assert.assertTrue(quoteDetails.verifyIsSezCheckboxIsChecked("Step 14.2: Verify Is Sez checkbox in not enabled", "Is Sez checkbox in not enabled",false));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.2: Select requested file type as CSV. ", "Requested file type should be selected", "csv"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup( "Step 15.3: Click on export button to export file.", "CSV should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.4 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.5: Select requested file type as xls. ", "Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup( "Step 15.6: Click on export button to export file.", "XLS should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.7 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.8: Select requested file type as pdf. ", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup( "Step 15.9: Click on export button to export file.", "PDF should get exported successfully"));

		Assert.assertTrue(xlsPage.verifyEstimatedTaxInHtmlEmbeddedXls("Step 16.1: Verify Estimated tax value<br>",
				"Estimated tax line item should be displayed with all other details of quote", newQuote, estimatedTax));

		Assert.assertTrue(xlsPage.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching(
				"Step 16.2: Open xls file", "HTML Embedded xls file should be opened", newQuote + ".xls"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.3: Verify Billing informaiton module:<br>Billing type-IGST"
				, "Billing type- IGST is verified", newQuote, "Billing type", "", "IGST"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.4: Verify Billing informaiton module: <br>GST ID "
				, "", quoteName, "GST ID", "Billing", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.5: Verify Billing informaiton module: <br>GST ID "
				, "", quoteName, "GST ID", "Shipping", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.6: Verify isSEZ checkbox value: <br>GST ID "
				, "", quoteName, "isSEZ", "", "No"));

		String pdfContent = pdfPage.readPdfFileInDownloads("Step 17.1: Read PDF file", "Pdf file content is read", newQuote+".pdf ", true);

		Assert.assertTrue(pdfPage.verifyBillingTypeInPDF("Step 17.2: Verify Billing type as IGST",
				"Billing type should display as IGST","Billing type: IGST",pdfContent));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 17.3: Verify GST ID for Supplier in pdf", "User must be able to find the " + 
				"Supplier address section:<br>" + "GST ID<br>" , pdfContent, "Supplier",gstIdSupplier));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 17.4: Verify GST ID for Shipping in pdf", "User must be able to find the " + 
				"Shipping Information - GST ID<br>" , pdfContent, "Shipping",gstIdBillingShipping));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 17.4: Verify GST ID for Billing in pdf", "User must be able to find the " + 
				"Billing Information - GST ID<br>" , pdfContent, "Billing",gstIdBillingShipping));

		Assert.assertTrue(pdfPage.verifyisSEZIsDisplayedInPDF("Step 17.5: Verify isSEZ checkbox value","isSEZ should be No",pdfContent,"No"));

		Assert.assertTrue(pdfPage.verifyEstimatedTaxInPDF("Step 17.6: Verify estimated tax","Estimated tax should be verified",estimatedTax,pdfContent));
	}

	/**
	 * Verify tax amount when tax code =N in S4 Flow-Quote to checkout
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/359258
	 * @since May 10, 2021 6:39:52 PM
	 * @author rajoriap
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C359258_Regression_CE_GST_VerifyTaxAmountWhenTaxCodeEqualsNInS4FlowQuoteToCheckout_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify tax amount when tax code =N in S4 Flow-Quote to checkout",
				"C359258_Regression_CE_GST_VerifyTaxAmountWhenTaxCodeEqualsNInS4FlowQuoteToCheckout_Direct", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID01);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String quoteName = "MyQuote";

		Reporting.getLogger().log(LogStatus.INFO, "<b>Needed S4 Contract ID with Tax class N</b>", "<b>For all 5 S4 contracts under Billing information pop up estimated tax is not becoming 0</b>");
		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(0);

		//Need to use bundle
		String bundle =  getProductsByProductType(getRegion(APJ, ID01),BUNDLE).get(0);

		Map<String, String> data = new HashMap<String, String>();
		data.put("poNumber", "9999");
		data.put("phoneNumber", "9898989898");
		data.put("attentionText", "test");
		data.put("paymentMethod", "Purchase Order");
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser(ID01, PURCHASER);
		Assert.assertNotNull(purchaser);
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
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		XLSValidations xlsValidations = new XLSValidations(_webcontrols);
		GmailPage gmailPage = new GmailPage(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("PreCondition: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		/** Pre-Condition ends **/

		pdp = customerService.searchSKU("Step 1.1: Search for a Bundle", "Requested product PDP should load", bundle);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 1.2: Add Bundle product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 1.3: Click on mini cart icon and click on Go to cart button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.verifyCheckoutButtonInCartPage("Step 1.4: Verify checkout button is displayed in cart page","Checkout button should appear in Shopping cart page"));

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 2: Click on save as quote button", "Create quote Page will be displayed to the user"));

		LinkedHashMap<String, String> selectedS4ContractAddressDetails =createNewQuote.clickOnChangeBillingAddressEnterContractIDandVerifySelectedContractID(
				"Step 3.1: Scroll down to billing information section and select Tax class N S4 Contract from billing address"
						+ " popup a value", "Selected Billing address must be displayed",contractID);
		Assert.assertNotEquals(selectedS4ContractAddressDetails, null);

		Assert.assertTrue(createNewQuote.verifySelectedS4ContractBillingAddress("Step 3.2: Verify selected Contract address "
				+ "details in Billing information section", "Selected Address should display under Billing information section",selectedS4ContractAddressDetails));

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 3.3: Verify Billing type in Billing Information",
				"Billing type must be displayed as IGST", "IGST"));

		Assert.assertTrue(createNewQuote.verifyEstimatedTAX("Step 4: Scroll down to find Estimated Tax label in summary", 
				"Estimated Tax =0 must be displayed", "INR 0.00"));

		quoteDetails = createNewQuote.createQuote("Step 5: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		quoteNameValue = quoteName + quoteNameValue;

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 6.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 6.2: Select requested file type as pdf. ", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopUpAndVerifyExportedFileAvailability(
				"Step 6.3: Click on export button to export file.", "PDF should get exported successfully",quoteNameValue + ".pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 6.4 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 6.5: Select requested file type as xls. ", "Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopUpAndVerifyExportedFileAvailability(
				"Step 6.6: Click on export button to export file.", "XLS should get exported successfully",quoteNameValue + ".xls"));

		String pdfValue = pdfValidations.readPdfFileInDownloads("Precondition: Getting PDF content", "PDF content should be fetched",quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyBillingTypeInPDF("Step 7.1: Click to open PDF format",
				"Billing type should display as IGST","Billing type: IGST",pdfValue));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxInPDF("Step 7.2: Click to open PDF format",
				"Estimated tax =0 has to be displayed","Estimated Tax INR 0.00",pdfValue));

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Precondition: Get Estimated Tax Value", "Estimated tax price is fetched", true);
		Assert.assertNotEquals(estimatedTax, null);

		Assert.assertTrue(xlsValidations.verifyXLSFileContent("Step 8: Click to open XLS format", 
				"Estimated tax =0 has to be displayed", quoteNameValue, "Estimated Tax:", "0.00"));

		PageGenerics.deleteAFile(quoteNameValue + ".pdf ");
		PageGenerics.deleteAFile(quoteNameValue + ".xls");

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 10.1: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 10.2: Enter valid email in email address field and Select PDF radio button and click on Email button", 
				"Your quote was sent successfully message should be displayed", "PDF", purchaser));

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 10.3: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 10.4: Enter valid email in email address field and Select PDF radio button and click on Email button", 
				"Your quote was sent successfully message should be displayed", "XLS", purchaser));

		String pdfFile = gmailPage.getAttachmentInDownloads("Precondition: Login to the emailed account and download quote PDF", 
				"Quote PDF should be fetched from email", quoteNameValue + ".pdf", 10);
		Assert.assertNotEquals(pdfFile, null);

		String xlsFile =gmailPage.getAttachmentInDownloads("Precondition: Login to the emailed account and download quote PDF", 
				"Quote XLS should be fetched from email", quoteNameValue + ".xls", 10);
		Assert.assertNotEquals(xlsFile, null);

		String pdfContent = pdfValidations.readPdfFileInDownloads("Precondition: Getting PDF content", "PDF content should be fetched", quoteNameValue + ".pdf", true);
		Assert.assertNotEquals(pdfContent, "");

		Assert.assertTrue(pdfValidations.verifyBillingTypeInPDF("Step 11.1: Click to open PDF format",
				"Billing type should display as IGST","Billing type: IGST",pdfValue));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxInPDF("Step 11.2: Click to open PDF format",
				"Estimated tax =0 has to be displayed","Estimated Tax INR 0.00",pdfValue));

		Assert.assertTrue(xlsValidations.verifyXLSFileContent("Step 12: Click to open XLS format", 
				"Estimated tax =0 has to be displayed", quoteNameValue, "Estimated Tax:", "0.00"));

		checkout = quoteDetails.navigateToCheckoutPage("Step 13: Click on Check out button", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null);

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 14.1: Verify Billing type in Billing Information",
				"Billing type must be displayed as IGST", "IGST"));

		Assert.assertTrue(createNewQuote.verifyEstimatedTAX("Step 14.2: Scroll down to find Estimated Tax label in summary", 
				"Estimated Tax =0 must be displayed", "INR 0.00"));

		Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 15.1: Enter all the Mandatory fields",
				"Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder(
				"Step 15.2: Click on Create purchase Order button to place Place an Order","User successfully Placed Purchase Order and landed to Order confirmation Page",
				true));

		PageGenerics.deleteAFile(quoteNameValue + ".pdf ");
		PageGenerics.deleteAFile(quoteNameValue + ".xls");

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}

	/**
	 * Verify billing type label CGST+SGST in Quote detail page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/300493
	 * @param region APJ
	 * @since May 5, 2021 3:52:39 PM
	 * @author Keshav
	 * @throws IOException 
	 */
	//@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
	//		IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C300493_Regression_CE_GST_VerifyBillingTypeLabelCGSTSGSTInQuoteDetailPage_CSR() throws IOException {

		// Reporting info
		initializeReporting("Verify billing type label CGST+SGST in Quote detail page",
				"C300493_Regression_CE_GST_VerifyBillingTypeLabelCGSTSGSTInQuoteDetailPage_CSR", logger);

		//Test Data
		Map<String, String> regData = getScenarioData("ID01");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String poNumber = regData.get("Orders");
		String phoneNumber = "12345";
		String attentionText = "test";
		String PO = "Purchase Order";

		// String bto = getProductsByProductTypeAndCategory(getRegion("APJ", "ID04"),"BTO","Laptops").get(0);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
        
		// Get user
		ArrayList<String> users = new ArrayList<String>();
		String partnerAgent = getUser("ID01", PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		users.add(partnerAgent);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser = getUser("ID01", PURCHASER);
		Assert.assertNotNull(purchaser);
		users.add(purchaser);
		String CSRUser = getUser(CSR);		
		Assert.assertNotNull(CSRUser);
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser);

		String quoteName = "Aut_Quote_";		

		HashMap<String, String> shippingAdress = new HashMap<String, String>();
		shippingAdress.put("Company", "hp2b");
		shippingAdress.put("AttentionText", attentionText);
		shippingAdress.put("City", "banglore");
		shippingAdress.put("Phone", phoneNumber);
		shippingAdress.put("Email", purchaser);		

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		CreateAddress createAddress=new CreateAddress(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);		
		XLSValidations xlsPage= new XLSValidations(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);
		
		String addressLine = createAddress.systemDate();
		shippingAdress.put("Addressline1", addressLine);

		/** Pre-Condition starts **/
		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("PreCondition : Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		/** Pre-Condition ends **/

		String redirectedPageName = customerService.selectCategoryInProductsAndServices("Step 1: Click on Products & Services & Select Category." +
				"<br>Step 2: Click on any one category",
				"All the catalogs assigned for that organization must be displayed<br>User must be landed in PLP", "Monitors");
		Assert.assertEquals("Monitors", redirectedPageName);

		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart.", "Product must get added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User must be landed in 'Shopping Cart'Page showing the list of products that were added");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: In the shipping cart page,Click on 'Save as Quote'link to navigate to Checkout page",
				"User must be navigated to Create Quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 6: Scroll down to Supplier Address section and check for GST ID",
				"GST ID must be present", "GST ID","Supplier"));

		Assert.assertTrue(createNewQuote.selectBillingAddressByStateCode("Step 7: Now click on 'Change Supplier address' Button<br>"+
				"Step 8: Select the address and click on OK button.", "Supplier addresses' popup appears<br>Address must be saved and Page must refresh",
				"Supplier popup appears<br>Address Saved with page refresh", "supplier", "MH"));

		String gstIdSupplier = createNewQuote.getGSTID("Step 7.3: Getting the GST ID for Supplier", "fetched the GST ID","Supplier");
		Assert.assertNotNull(gstIdSupplier,null);

		String gstIdBillingShipping = createNewQuote.getGSTID("Step 7.4: Getting the GST ID for Shipping", "fetched the GST ID","Shipping");
		Assert.assertNotNull(gstIdBillingShipping,null);

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 9.1: Scroll up to 'Billing Information' section and ensure that Below Labels are present and they have appropriate associated values\r\n" + 
				"GST ID<br>Billing Type : SGST+CGST","Billing information section must haveGST ID<br>" + 
						"Billing Type as SGST+CGST", "GST ID","Supplier"));

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 9.2: Verify Billing type in Billing Information" + 
				"<br>Billing Type : SGST+CGST","Billing information section must have <br>" + 
						"Billing Type as SGST+CGST", "SGST+CGST"));

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 10.1: In the Shipping address section verify below entities\r\n" + 
				"GST ID<br>SEZ Disclaimer text<br>Please note, For SEZ location, your bill to and ship to must be identical." + 
				"GST ID<br>Billing Type : SGST+CGST","All the below entities must be displayed in the Shipping information module.<br>" + 
						"GST ID\r\n" + 
						"SEZ Disclaimer text--><br>" + 
						"Please note, For SEZ location, your bill to and ship to must be identical.", "isSEZ","Shipping"));

		Assert.assertTrue(createNewQuote.verifyIsSezCheckboxDisplayed("Step 10.2: Verify isSez checkbox", "isSez Checkbox is displayed."));

		quoteDetails = createNewQuote.createQuote("Step 11: Fill in other details and click on Save Quote Button", "Quote detail page appears",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 11.2: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 12.1: Verify that Billing information module has below fields\r\n" + 
				"GST ID", "Billing information must have below fields" +  "GST ID", "GST ID", "Billing"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 12.2: Verify that Billing information module has below fields\r\n" + 
				"Billing Type", "Billing information must have below fields" +  "Billing Type", "Billing Type", "Billing Type"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 13: Verify that Supplier information module has below fields\r\n" + 
				"GST ID", "Supplier information must have below fields <br>GST ID", "GST ID", "Supplier"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 14.1: Verify that Shipping information module has below fields\r\n" + 
				"GST ID", "Shipping information module should have below fields <br>GST ID", "GST ID", "Shipping"));

		Assert.assertTrue(quoteDetails.verifyIsSezCheckboxIsChecked("Step 14.2: Verify Is Sez checkbox in not enabled",
				"Is Sez checkbox in not enabled",false));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.2: Select requested file type as xls. ", "Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 15.3: Click on export button to export file.", "XLS should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.4: Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.5: Select requested file type as pdf. ", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 15.6: Click on export button to export file.", "PDF should get exported successfully"));

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Precondition: Get Estimated Tax Value", "Estimated tax price is fetched", true);

		Assert.assertTrue(xlsPage.verifyEstimatedTaxInHtmlEmbeddedXls("Step 16.2: Verify Estimated tax value in XLS<br>",
				"Estimated tax line item should be displayed with all other details of quote", newQuote, estimatedTax));

		Assert.assertTrue(xlsPage.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching(
				"Step 16.3: Open xls file", "HTML Embedded xls file should be opened", newQuote + ".xls"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.4: Verify Billing informaiton module:<br>Billing type-CGST+SGST  in XLS"
				, "Billing type- SGST+CGST is verified", newQuote, "Billing type", "", "SGST+CGST"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.5: Verify Supplier informaiton module  in XLS: <br>GST ID "
				, "", quoteName, "GST ID", "Supplier", gstIdSupplier));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.6: Verify Billing informaiton module  in XLS: <br>GST ID "
				, "", quoteName, "GST ID", "Billing", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.7: Verify Shipping informaiton module  in XLS: <br>GST ID "
				, "", quoteName, "GST ID", "Shipping", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.8: Verify isSEZ checkbox value  in XLS: <br>GST ID "
				, "", quoteName, "isSEZ", "", "No"));

		String pdfContent = pdfPage.readPdfFileInDownloads("Step 17.1: Read PDF file", "Pdf file content is read", newQuote+".pdf ", true);

		Map<String,List<String>> expectedValues = new HashMap<>();
		expectedValues.put("Supplier", Arrays.asList(gstIdSupplier,estimatedTax.replace(" ", ""),"Quote Summary"));
		expectedValues.put("Billing", Arrays.asList(gstIdBillingShipping,"SGST+CGST"));
		expectedValues.put("Shipping", Arrays.asList(gstIdBillingShipping,"No"));
		Assert.assertTrue(pdfPage.verifyGSTBillingTypeInPDF("Step 17.2: Click on open PDF attachment", "User must be able to find the following:<br>" + 
				"Billing informaiton module:<br>" + 
				"Billing type-CGST+SGST, GST ID<br>" + 
				"Supplier address section:<br>" + 
				"GST ID<br>" + 
				"Shipping information section:<br>" + 
				"GST ID:<br>" + 
				"isSEZ: No<br>" + 
				"Quote Summary:<br>" + 
				"Estimated Tax", pdfContent, expectedValues));

	}
	
	// New TestCases with different IDs after changes	
	
	/**
	 * Verify tax amount when tax code =M [SEZ]in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/C457880
	 * @throws IOException -
	 * @since Mar 4, 2022 7:34:24 PM
	 * @author KatamBha
	 */	
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C457880_Regression_CE_GST_VerifyTaxAmountWhenTaxcodeMSEZInQuoteCreationAndQuoteConfirmationPage_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify tax amount when tax code =M [SEZ]in Quote creation and Quote confirmation page",
				"C457880_Regression_CE_GST_VerifyTaxAmountWhenTaxcodeMSEZInQuoteCreationAndQuoteConfirmationPage_Direct", logger);
        
		// Test data
		Map<String, String> regData = getScenarioData(ID19);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String catalogName = regData.get("Contract");
	    String orgName = regData.get("Org Name");    	       
	       
        String password = passwords.get(DIRECTUSERPWD);        
        
        Map<String, String> data = new HashMap<String, String>();
		data.put("poNumber", poNumber);
		data.put("phoneNumber", phoneNumber);
		data.put("attentionText", attentionText);
		data.put("paymentMethod", PO);
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		String quoteName = "MyQuote";
						
		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(0);			
			
		String bto =  getProductByDataSetID(APJ, ID19, BTO);
		Assert.assertNotNull(bto);
		 
		String purchaser = getUser("ID19", PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));
		
		// Get URL
		setEnvironment();
		String url = this.url;
				
		// Page Objects
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		GmailPage gmailPage = new GmailPage(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);
	
		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Purchaser", url, purchaser, password, true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Select requested catalog","Requested catalog should be selected", data,true));
 
		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));
		
		pdp = customerService.searchSKU("Step 3.1: Search for a Bto", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 3.2: Add Bto product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on mini cart icon and click on Go to cart button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 5: Click on save as quote button", "Create quote Page should be displayed to the user"));

		LinkedHashMap<String, String> selectedS4ContractAddressDetails =createNewQuote.clickOnChangeBillingAddressEnterContractIDandVerifySelectedContractID(
				"Step 6: Scroll down to billing information section and select Tax class M S4 Contract from billing address"
						+ " popup a value", "Selected Billing address must be displayed",contractID);
		Assert.assertNotEquals(selectedS4ContractAddressDetails, null);

		Assert.assertTrue(createNewQuote.verifyEstimatedTaxIsDisplayedAndIncludedInTotal("Step 7: Scroll down to find Estimated Tax label in summary", "'Estimated tax label must be displayed with some amount", true));
		
		quoteDetails = createNewQuote.createQuote("Step 8: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details", "Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		quoteNameValue = quoteName + quoteNameValue;
	
		String estimatedTax = quoteDetails.getEstimatedTaxValue("STep 9.1: Verify estimated tax is displayed", "Estimated tax should be displayed", true);
		Assert.assertNotEquals(estimatedTax, null);

		Assert.assertTrue(quoteDetails.verifyBillingType("Step 9.2: Verify Billing type in Billing Information", "Billing type must be displayed as IGST", "IGST"));

		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	/**
	 * Verify tax amount when tax code =N [SEZ]in Quote creation and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/C457881
	 * @since Mar 7, 2022 12:39:52 PM
	 * @author KatamBha
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C457881_Regression_CE_GST_VerifyTaxAmountWhenTaxCodeNSEZInQuoteCreationAndQuoteConfirmationPage_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify tax amount when tax code =N [SEZ]in Quote creation and Quote confirmation page",
				" C457881_Regression_CE_GST_VerifyTaxAmountWhenTaxCodeNSEZInQuoteCreationAndQuoteConfirmationPage_Direct", logger);

		// Test data
		Map<String, String> regData = getScenarioData(ID01);
		Assert.assertNotEquals(regData.size(), 0);
//		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = passwords.get(DIRECTUSERPWD);
		String quoteName = "MyQuote";

//		Reporting.getLogger().log(LogStatus.INFO, "<b>Needed S4 Contract ID with Tax class N</b>", "<b>For all 5 S4 contracts under Billing information pop up estimated tax is not becoming 0</b>");
//		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
//		Assert.assertNotEquals(regData.size(), 0);
		String contractID = "0170002232";               //contractIDs.get(0);

		//Need to use kit
		String kit =  getProductsByProductType(getRegion(APJ, ID01),KIT).get(1);

		Map<String, String> data = new HashMap<String, String>();
		data.put("poNumber", "9999");
		data.put("phoneNumber", "9898989898");
		data.put("attentionText", "test");
		data.put("paymentMethod", "Purchase Order");
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		//Waiting for user availability
		String purchaser = getUser(ID01, PURCHASER);
		Assert.assertNotNull(purchaser);
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
		//PDFValidations pdfValidations = new PDFValidations(_webcontrols);
		//XLSValidations xlsValidations = new XLSValidations(_webcontrols);
		//Gm9ailPage gmailPage = new GmailPage(_webcontrols);
		//Checkout checkout = new Checkout(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 3.1: Search for a Bundle", "Requested product PDP should load", kit);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 3.2: Add kit product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 4: Click on mini cart icon and click on Go to cart button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

	//	Assert.assertTrue(shoppingCart.verifyCheckoutButtonInCartPage("Step 1.4: Verify checkout button is displayed in cart page","Checkout button should appear in Shopping cart page"));

		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 5: Click on save as quote button", "Create quote Page will be displayed to the user"));

		LinkedHashMap<String, String> selectedS4ContractAddressDetails =createNewQuote.clickOnChangeBillingAddressEnterContractIDandVerifySelectedContractID(
				"Step 6: Scroll down to billing information section and select Tax class N S4 Contract from billing address"
						+ " popup a value", "Selected Billing address must be displayed",contractID);
		Assert.assertNotEquals(selectedS4ContractAddressDetails, null);

	/*	Assert.assertTrue(createNewQuote.verifySelectedS4ContractBillingAddress("Step 3.2: Verify selected Contract address "
				+ "details in Billing information section", "Selected Address should display under Billing information section",selectedS4ContractAddressDetails));

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 3.3: Verify Billing type in Billing Information",
				"Billing type must be displayed as IGST", "IGST")); */

		Assert.assertTrue(createNewQuote.verifyEstimatedTAX("Step 7: Scroll down to find Estimated Tax label in summary", 
				"Estimated Tax =0 must be displayed", "INR 0.00"));

		quoteDetails = createNewQuote.createQuote("Step 8: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

	/*	ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("PreCondition: Getting Quote details",
				"Quote details should be fetched");
		Assert.assertNotEquals(createNewQuote, null);

		String quoteNameValue = quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
		Assert.assertNotEquals(quoteNameValue, "");

		quoteNameValue = quoteName + quoteNameValue;

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 6.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 6.2: Select requested file type as pdf. ", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopUpAndVerifyExportedFileAvailability(
				"Step 6.3: Click on export button to export file.", "PDF should get exported successfully",quoteNameValue + ".pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 6.4 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 6.5: Select requested file type as xls. ", "Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopUpAndVerifyExportedFileAvailability(
				"Step 6.6: Click on export button to export file.", "XLS should get exported successfully",quoteNameValue + ".xls"));

		String pdfValue = pdfValidations.readPdfFileInDownloads("Precondition: Getting PDF content", "PDF content should be fetched",quoteNameValue + ".pdf ", false);
		Assert.assertNotEquals(pdfValue, "");

		Assert.assertTrue(pdfValidations.verifyBillingTypeInPDF("Step 7.1: Click to open PDF format",
				"Billing type should display as IGST","Billing type: IGST",pdfValue));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxInPDF("Step 7.2: Click to open PDF format",
				"Estimated tax =0 has to be displayed","Estimated Tax INR 0.00",pdfValue));

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Precondition: Get Estimated Tax Value", "Estimated tax price is fetched", true);
		Assert.assertNotEquals(estimatedTax, null);

		Assert.assertTrue(xlsValidations.verifyXLSFileContent("Step 8: Click to open XLS format", 
				"Estimated tax =0 has to be displayed", quoteNameValue, "Estimated Tax:", "0.00"));

		PageGenerics.deleteAFile(quoteNameValue + ".pdf ");
		PageGenerics.deleteAFile(quoteNameValue + ".xls");

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 10.1: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 10.2: Enter valid email in email address field and Select PDF radio button and click on Email button", 
				"Your quote was sent successfully message should be displayed", "PDF", purchaser));

		Assert.assertTrue(quoteDetails.clickOnEmailButton("Step 10.3: Click on 'Email' option", "Pop up with email options should be displayed"));

		Assert.assertTrue(quoteDetails.fillMandatoryFieldsAndClickOnEmailButtonInEmailPopUp("Step 10.4: Enter valid email in email address field and Select PDF radio button and click on Email button", 
				"Your quote was sent successfully message should be displayed", "XLS", purchaser));

		String pdfFile = gmailPage.getAttachmentInDownloads("Precondition: Login to the emailed account and download quote PDF", 
				"Quote PDF should be fetched from email", quoteNameValue + ".pdf", 10);
		Assert.assertNotEquals(pdfFile, null);

		String xlsFile =gmailPage.getAttachmentInDownloads("Precondition: Login to the emailed account and download quote PDF", 
				"Quote XLS should be fetched from email", quoteNameValue + ".xls", 10);
		Assert.assertNotEquals(xlsFile, null);

		String pdfContent = pdfValidations.readPdfFileInDownloads("Precondition: Getting PDF content", "PDF content should be fetched", quoteNameValue + ".pdf", true);
		Assert.assertNotEquals(pdfContent, "");

		Assert.assertTrue(pdfValidations.verifyBillingTypeInPDF("Step 11.1: Click to open PDF format",
				"Billing type should display as IGST","Billing type: IGST",pdfValue));

		Assert.assertTrue(pdfValidations.verifyEstimatedTaxInPDF("Step 11.2: Click to open PDF format",
				"Estimated tax =0 has to be displayed","Estimated Tax INR 0.00",pdfValue));

		Assert.assertTrue(xlsValidations.verifyXLSFileContent("Step 12: Click to open XLS format", 
				"Estimated tax =0 has to be displayed", quoteNameValue, "Estimated Tax:", "0.00"));

		checkout = quoteDetails.navigateToCheckoutPage("Step 13: Click on Check out button", "User should be navigated to Checkout page");
		Assert.assertNotEquals(checkout, null); */

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 9.1: Verify Billing type in Billing Information",
				"Billing type must be displayed as IGST", "IGST"));

		Assert.assertTrue(createNewQuote.verifyEstimatedTAX("Step 9.2: Scroll down to find Estimated Tax label in summary", 
				"Estimated Tax =0 must be displayed", "INR 0.00"));

	/*	Assert.assertTrue(checkout.fillMandatoryPurchaseOrderDetails("Step 15.1: Enter all the Mandatory fields",
				"Details entered successfully", data));

		Assert.assertTrue(checkout.clickOnCreatePurchaseOrder(
				"Step 15.2: Click on Create purchase Order button to place Place an Order","User successfully Placed Purchase Order and landed to Order confirmation Page",
				true));

		PageGenerics.deleteAFile(quoteNameValue + ".pdf ");
		PageGenerics.deleteAFile(quoteNameValue + ".xls"); */

		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	/**
	 * Verify billing type label IGST in Quote Creation page and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/C457884
	 * @param region APJ
	 * @since Mar 7, 2022 11:52:39 AM
	 * @author KatamBha
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, 
			IGroupsTagging.IUserType.CSR, IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C457884_Regression_CE_GST_VerifyBillingTypeLabelIGSTInQuoteCreationPageAndQuoteConfirmationPage_CSR() throws IOException {

		// Reporting info
		initializeReporting("Verify billing type label IGST in Quote Creation page and Quote confirmation page",
				"C457884_Regression_CE_GST_VerifyBillingTypeLabelIGSTInQuoteCreationPageAndQuoteConfirmationPage_CSR", logger);

		// Test Data
		String scenariodId = "ID01";
		Map<String, String> regData = getScenarioData(scenariodId);
		Assert.assertNotEquals(regData.size(), 0);
		String mdcpid = regData.get("MDCP ID");
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String password = commonData.get(CSRORFEDCSRUSERPWD);

		// Waiting for user availability		
		ArrayList<String> userSet= new ArrayList<>();
		String csr = getUser(CSR);
		Assert.assertNotEquals(csr, "");
		String purchaser = getUser(scenariodId, PURCHASER);
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

		String quoteName = "Aut_Quote_";

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		CreateAddress createAddress=new CreateAddress(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		PLP plp = new PLP(_webcontrols);
		XLSValidations xlsPage= new XLSValidations(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		data.put("emailID", purchaser);

		HashMap<String, String> shippingAdress = new HashMap<String, String>();
		shippingAdress.put("Company", "hp2b");
		shippingAdress.put("AttentionText", attentionText);
		shippingAdress.put("City", "banglor");
		shippingAdress.put("Phone", phoneNumber);
		shippingAdress.put("Email", purchaser);
		String addressLine = createAddress.systemDate();
		shippingAdress.put("Addressline1", addressLine);

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, csr, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 & 2: Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		Assert.assertNotEquals(customerService.selectCategoryInProductsAndServices("PreCondition: Click on Products & Services & Select Category." +
				"<br>PreCondition: Click on any one category",
				"All the catalogs assigned for that organization must be displayed<br>User must be landed in PLP", "Accessories"), null);

		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart.", "Product must get added to cart", "plp"));

		shoppingCart = plp.navigateToShoppingCartThroughHeader("Step 4: Click on 'Mini cart' icon and Click on 'Go to cart' button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);

		Assert.assertNotEquals(shoppingCart.navigateToQuoteCreationPage("Step 5: In the shipping cart page,Click on 'Save as Quote'link to navigate to Checkout page",
				"User must be navigated to Create Quote page"), null);

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 6: Scroll down to Supplier Address section and check for GST ID",
				"GST ID must be present", "GST ID","Supplier"));

		String stateOrProvinceName = checkout.verifyFieldInAddressInfoSection("Precondition: Getting Billing address State/Province", 
				"State/Province should be fetched","Billing","State/Province:","",false);
		Assert.assertNotEquals(stateOrProvinceName, null);

		Assert.assertTrue(checkout.clickOnChangeSupplierAddress("Step 7: Now click on 'Change Supplier address' Button", "'Supplier addresses' popup should appears", true));

		Assert.assertNotNull(checkout.selectSupplierAddressBasedOnBillingInfoStateName(
				"Step 8.1: Select the address and click on OK button","Address must be saved and Page must refresh",stateOrProvinceName, "Different"));

		String gstIdSupplier = createNewQuote.getGSTID("Step 8.2: Getting the GST ID for Supplier", "fetched the GST ID","Supplier");
		Assert.assertNotNull(gstIdSupplier,null);

		String gstIdBillingShipping = createNewQuote.getGSTID("Step 8.3: Getting the GST ID for Shipping", "Fetched the GST ID","Shipping");
		Assert.assertNotNull(gstIdBillingShipping,null);

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 9.1: Scroll up to 'Billing Information' section and ensure that Below Labels are present and they have appropriate associated values\r\n" + 
				"GST ID<br>Billing Type : IGST","Billing information section must haveGST ID<br>" + 
						"Billing Type as IGST", "GST ID","Supplier"));

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 9.2: Verify Billing type in Billing Information" + 
				"<br>Billing Type : IGST","Billing information section must have <br>" + "Billing Type as IGST", "IGST"));

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 10.1: In the Shipping address section verify below entities\r\n" + 
				"GST ID<br>SEZ Disclaimer text<br>Please note, For SEZ location, your bill to and ship to must be identical." + 
				"GST ID<br>Billing Type : IGST","All the below entities must be displayed in the Shipping information module.<br>" + 
						"GST ID\r\n" + 
						"SEZ Disclaimer text-->\r\n" + 
						"Please note, For SEZ location, your bill to and ship to must be identical.", "isSEZ","Shipping"));

		Assert.assertTrue(createNewQuote.verifyIsSezCheckboxDisplayed("Step 10.2: Verify isSez checkbox", "isSez Checkbox is displayed."));

		quoteDetails = createNewQuote.createQuote("Step 11: Fill in other details and click on Save Quote Button",
				"Quote detail page appears",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 11.2: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
		String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");
	//	String estimatedTax = quoteDetails.getEstimatedTaxValue("Step 16.1: Get Estimated Tax Value", "Estimated tax price is fetched", true);

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 12.1: Verify that Billing information module has below fields\r\n" + 
				"GST ID", "Billing information must have below fields" +  "GST ID", "GST ID", "Billing"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 12.2: Verify that Billing information module has below fields\r\n" + 
				"Billing Type", "Billing information must have below fields" + "Billing Type", "Billing Type", "Billing Type"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 13: Verify that Supplier information module has below fields\r\n" + 
				"GST ID", "Billing information must have below fields <br>GST ID", "GST ID", "Supplier"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 14.1: Verify that Shipping information module has below fields\r\n" + 
				"GST ID", "Shipping information module should have below fields <br>GST ID", "GST ID", "Shipping"));

		Assert.assertTrue(quoteDetails.verifyIsSezCheckboxIsChecked("Step 14.2: Verify Is Sez checkbox in not enabled", "Is Sez checkbox in not enabled",false));

	/*	Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.2: Select requested file type as CSV. ", "Requested file type should be selected", "csv"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup( "Step 15.3: Click on export button to export file.", "CSV should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.4 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.5: Select requested file type as xls. ", "Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup( "Step 15.6: Click on export button to export file.", "XLS should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.7 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.8: Select requested file type as pdf. ", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup( "Step 15.9: Click on export button to export file.", "PDF should get exported successfully"));

		Assert.assertTrue(xlsPage.verifyEstimatedTaxInHtmlEmbeddedXls("Step 16.1: Verify Estimated tax value<br>",
				"Estimated tax line item should be displayed with all other details of quote", newQuote, estimatedTax));

		Assert.assertTrue(xlsPage.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching(
				"Step 16.2: Open xls file", "HTML Embedded xls file should be opened", newQuote + ".xls"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.3: Verify Billing informaiton module:<br>Billing type-IGST"
				, "Billing type- IGST is verified", newQuote, "Billing type", "", "IGST"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.4: Verify Billing informaiton module: <br>GST ID "
				, "", quoteName, "GST ID", "Billing", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.5: Verify Billing informaiton module: <br>GST ID "
				, "", quoteName, "GST ID", "Shipping", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.6: Verify isSEZ checkbox value: <br>GST ID "
				, "", quoteName, "isSEZ", "", "No"));

		String pdfContent = pdfPage.readPdfFileInDownloads("Step 17.1: Read PDF file", "Pdf file content is read", newQuote+".pdf ", true);

		Assert.assertTrue(pdfPage.verifyBillingTypeInPDF("Step 17.2: Verify Billing type as IGST",
				"Billing type should display as IGST","Billing type: IGST",pdfContent));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 17.3: Verify GST ID for Supplier in pdf", "User must be able to find the " + 
				"Supplier address section:<br>" + "GST ID<br>" , pdfContent, "Supplier",gstIdSupplier));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 17.4: Verify GST ID for Shipping in pdf", "User must be able to find the " + 
				"Shipping Information - GST ID<br>" , pdfContent, "Shipping",gstIdBillingShipping));

		Assert.assertTrue(pdfPage.verifyGSTIDShippingBillingAndSupplierSectionInPDF("Step 17.4: Verify GST ID for Billing in pdf", "User must be able to find the " + 
				"Billing Information - GST ID<br>" , pdfContent, "Billing",gstIdBillingShipping));

		Assert.assertTrue(pdfPage.verifyisSEZIsDisplayedInPDF("Step 17.5: Verify isSEZ checkbox value","isSEZ should be No",pdfContent,"No"));

		Assert.assertTrue(pdfPage.verifyEstimatedTaxInPDF("Step 17.6: Verify estimated tax","Estimated tax should be verified",estimatedTax,pdfContent));
		*/
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");
	}
	/**
	 * Verify billing type label CGST+SGST in Quote creation page and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/C457883
	 * @param region APJ
	 * @since May 5, 2021 3:52:39 PM
	 * @author Keshav
	 * @throws IOException 
	 */
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.CSR,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C457883_Regression_CE_GST_VerifyBillingTypeLabelCGSTSGSTInQuoteCreationPageAndQuoteConfirmationPage_CSR() throws IOException {

		// Reporting info
		initializeReporting("Verify billing type label CGST+SGST in Quote creation page and Quote confirmation page",
				"C457883_Regression_CE_GST_VerifyBillingTypeLabelCGSTSGSTInQuoteCreationPageAndQuoteConfirmationPage_CSR", logger);

		//Test Data
		Map<String, String> regData = getScenarioData("ID01");
		String catalogName = regData.get("Contract");
		String orgName = regData.get("Org Name");
		String mdcpid = regData.get("MDCP ID");
		String poNumber = regData.get("Orders");
		String phoneNumber = "12345";
		String attentionText = "test";
		String PO = "Purchase Order";

		// String bto = getProductsByProductTypeAndCategory(getRegion("APJ", "ID04"),"BTO","Laptops").get(0);
		String password = passwords.get(CSRORFEDCSRUSERPWD);
        
		// Get user
		ArrayList<String> users = new ArrayList<String>();
		String partnerAgent = getUser("ID01", PARTNERAGENT);
		Assert.assertNotNull(partnerAgent);
		users.add(partnerAgent);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);
		String purchaser = getUser("ID01", PURCHASER);
		Assert.assertNotNull(purchaser);
		users.add(purchaser);
		String CSRUser = getUser(CSR);		
		Assert.assertNotNull(CSRUser);
		users.add(CSRUser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), users);

		Map<String, String> data = new HashMap<String, String>();
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);
		data.put("MDCPID", mdcpid);
		data.put("actionOnUsers", actionOnUsers);
		data.put("emailID", purchaser);

		String quoteName = "Aut_Quote_";		

		HashMap<String, String> shippingAdress = new HashMap<String, String>();
		shippingAdress.put("Company", "hp2b");
		shippingAdress.put("AttentionText", attentionText);
		shippingAdress.put("City", "banglore");
		shippingAdress.put("Phone", phoneNumber);
		shippingAdress.put("Email", purchaser);		

		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		CustomerService customerService = new CustomerService(_webcontrols);
		Login login = new Login(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		CreateAddress createAddress=new CreateAddress(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);		
		XLSValidations xlsPage= new XLSValidations(_webcontrols);
		PDFValidations pdfPage = new PDFValidations(_webcontrols);
		
		String addressLine = createAddress.systemDate();
		shippingAdress.put("Addressline1", addressLine);

		Assert.assertTrue(login.loginToHP2B("Precondition: Login to HP2B with CSR user", url, CSRUser, password, true));

		Assert.assertTrue(customerService.impersonateOrBuyOnBehalfUser("Precondition: Impersonate Purchaser user",
				"Impersonated Purchaser user", data));

		Assert.assertTrue(login.selectOrganizationAndContract("Step 1 & 2: Select requested org & catalog",
				"Requested org & catalog should be selected", data, true));

		Assert.assertTrue(login.deleteProducts("PreCondition :Delete product", "Product should be deleted", "CE", false));

		String redirectedPageName = customerService.selectCategoryInProductsAndServices("Precondition: Click on Products & Services & Select Category." +
				"<br>Precondition: Click on any one category",
				"All the catalogs assigned for that organization must be displayed<br>User must be landed in PLP", "Monitors");
		Assert.assertEquals("Monitors", redirectedPageName);

		Assert.assertTrue(customerService.addProductToCart("Step 3: Add BTO product to the cart.", "Product must get added to cart", "plp"));

		shoppingCart = customerService.navigateToShoppingCartThroughHeader("Step 4: Click on Mini cart or Go to cart button",
				"User must be landed in 'Shopping Cart'Page showing the list of products that were added");
		Assert.assertNotEquals(shoppingCart, null);

		createNewQuote = shoppingCart.navigateToQuoteCreationPage("Step 5: In the shipping cart page,Click on 'Save as Quote'link to navigate to Checkout page",
				"User must be navigated to Create Quote page");
		Assert.assertNotEquals(createNewQuote, null);

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 6: Scroll down to Supplier Address section and check for GST ID",
				"GST ID must be present", "GST ID","Supplier"));

		Assert.assertTrue(createNewQuote.selectBillingAddressByStateCode("Step 7: Now click on 'Change Supplier address' Button<br>"+
				"Step 8: Select the address and click on OK button.", "Supplier addresses' popup appears<br>Address must be saved and Page must refresh",
				"Supplier popup appears<br>Address Saved with page refresh", "supplier", "KA"));

	/*	String gstIdSupplier = createNewQuote.getGSTID("Step 7.3: Getting the GST ID for Supplier", "fetched the GST ID","Supplier");
		Assert.assertNotNull(gstIdSupplier,null);

		String gstIdBillingShipping = createNewQuote.getGSTID("Step 7.4: Getting the GST ID for Shipping", "fetched the GST ID","Shipping");
		Assert.assertNotNull(gstIdBillingShipping,null); */

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 9.1: Scroll up to 'Billing Information' section and ensure that Below Labels are present and they have appropriate associated values\r\n" + 
				"GST ID<br>Billing Type : SGST+CGST","Billing information section must haveGST ID<br>" + 
						"Billing Type as SGST+CGST", "GST ID","Supplier"));

		Assert.assertTrue(createNewQuote.verifyBillingType("Step 9.2: Verify Billing type in Billing Information" + 
				"<br>Billing Type : SGST+CGST","Billing information section must have <br>" + 
						"Billing Type as SGST+CGST", "SGST+CGST"));

		Assert.assertTrue(createNewQuote.verifyFieldsOnCreateQuote("Step 10.1: In the Shipping address section verify below entities\r\n" + 
				"GST ID<br>SEZ Disclaimer text<br>Please note, For SEZ location, your bill to and ship to must be identical." + 
				"GST ID<br>Billing Type : SGST+CGST","All the below entities must be displayed in the Shipping information module.<br>" + 
						"GST ID\r\n" + 
						"SEZ Disclaimer text--><br>" + 
						"Please note, For SEZ location, your bill to and ship to must be identical.", "isSEZ","Shipping"));

		Assert.assertTrue(createNewQuote.verifyIsSezCheckboxDisplayed("Step 10.2: Verify isSez checkbox", "isSez Checkbox is displayed."));

		quoteDetails = createNewQuote.createQuote("Step 11: Fill in other details and click on Save Quote Button", "Quote detail page appears",quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);

		ArrayList<String> quoteDetailsList = quoteDetails.getQuoteDetailsForVerification("Step 11.2: Getting Quote details",
				"Quote Should be created Successfully and navigate to quote details page");
		Assert.assertNotEquals(createNewQuote, null);
	//	String newQuote = quoteName+quoteDetails.getQuoteData(quoteDetailsList,"Quote Name");

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 12.1: Verify that Billing information module has below fields\r\n" + 
				"GST ID", "Billing information must have below fields" +  "GST ID", "GST ID", "Billing"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 12.2: Verify that Billing information module has below fields\r\n" + 
				"Billing Type", "Billing information must have below fields" +  "Billing Type", "Billing Type", "Billing Type"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 13: Verify that Supplier information module has below fields\r\n" + 
				"GST ID", "Supplier information must have below fields <br>GST ID", "GST ID", "Supplier"));

		Assert.assertTrue(quoteDetails.verifyFieldsOnQuoteDetails("Step 14.1: Verify that Shipping information module has below fields\r\n" + 
				"GST ID", "Shipping information module should have below fields <br>GST ID", "GST ID", "Shipping"));

		Assert.assertTrue(quoteDetails.verifyIsSezCheckboxIsChecked("Step 14.2: Verify Is Sez checkbox in not enabled",
				"Is Sez checkbox in not enabled",false));

	/*	Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.1 : Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.2: Select requested file type as xls. ", "Requested file type should be selected", "xls"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 15.3: Click on export button to export file.", "XLS should get exported successfully"));

		Assert.assertTrue(quoteDetails.clickOnExportButton("Step 15.4: Click on 'Export catalog' button", "Export catalog popup should be displayed."));

		Assert.assertTrue(quoteDetails.selectFileTypeToExport("Step 15.5: Select requested file type as pdf. ", "Requested file type should be selected", "pdf"));

		Assert.assertTrue(quoteDetails.clickOnExportButtonInPopup("Step 15.6: Click on export button to export file.", "PDF should get exported successfully"));

		String estimatedTax = quoteDetails.getEstimatedTaxValue("Precondition: Get Estimated Tax Value", "Estimated tax price is fetched", true);

		Assert.assertTrue(xlsPage.verifyEstimatedTaxInHtmlEmbeddedXls("Step 16.2: Verify Estimated tax value in XLS<br>",
				"Estimated tax line item should be displayed with all other details of quote", newQuote, estimatedTax));

		Assert.assertTrue(xlsPage.readingHTMLEmbeddedFileContentWrittingInFileAndLaunching(
				"Step 16.3: Open xls file", "HTML Embedded xls file should be opened", newQuote + ".xls"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.4: Verify Billing informaiton module:<br>Billing type-CGST+SGST  in XLS"
				, "Billing type- SGST+CGST is verified", newQuote, "Billing type", "", "SGST+CGST"));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.5: Verify Supplier informaiton module  in XLS: <br>GST ID "
				, "", quoteName, "GST ID", "Supplier", gstIdSupplier));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.6: Verify Billing informaiton module  in XLS: <br>GST ID "
				, "", quoteName, "GST ID", "Billing", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.7: Verify Shipping informaiton module  in XLS: <br>GST ID "
				, "", quoteName, "GST ID", "Shipping", gstIdBillingShipping));

		Assert.assertTrue(xlsPage.verifyXLSContent("Step 16.8: Verify isSEZ checkbox value  in XLS: <br>GST ID "
				, "", quoteName, "isSEZ", "", "No"));

		String pdfContent = pdfPage.readPdfFileInDownloads("Step 17.1: Read PDF file", "Pdf file content is read", newQuote+".pdf ", true);

		Map<String,List<String>> expectedValues = new HashMap<>();
		expectedValues.put("Supplier", Arrays.asList(gstIdSupplier,estimatedTax.replace(" ", ""),"Quote Summary"));
		expectedValues.put("Billing", Arrays.asList(gstIdBillingShipping,"SGST+CGST"));
		expectedValues.put("Shipping", Arrays.asList(gstIdBillingShipping,"No"));
		Assert.assertTrue(pdfPage.verifyGSTBillingTypeInPDF("Step 17.2: Click on open PDF attachment", "User must be able to find the following:<br>" + 
				"Billing informaiton module:<br>" + 
				"Billing type-CGST+SGST, GST ID<br>" + 
				"Supplier address section:<br>" + 
				"GST ID<br>" + 
				"Shipping information section:<br>" + 
				"GST ID:<br>" + 
				"isSEZ: No<br>" + 
				"Quote Summary:<br>" + 
				"Estimated Tax", pdfContent, expectedValues)); */
		
		logger.info("End Test case");
		Reporting.getLogger().log(LogStatus.INFO, "<b>End test case</b>", "<b>End test case</b>");

	}
	/**
	 * Verify billing type CGST+SGST and tax amount when tax code = Z or null [Non SEZ] in Quote creation page and Quote confirmation page
	 * @TestCaseLink https://hpitdce.testrail.net/index.php?/cases/view/C458186
	 * @since March 15, 2022 3:52:39 PM
	 * @author RamanatM
	 * @throws IOException 
	 */
	
	@Test(groups = {IGroupsTagging.ITestType.REGRESSION, IGroupsTagging.IStoreType.CE, IGroupsTagging.IUserType.DIRECT,
			IGroupsTagging.IModuleType.QUOTES }, priority = 2)
	public void C458186_Regression_Quotes_CE_GST_VerifyTaxAmountWhenTaxCodeNSEZInQuoteCreationAndQuoteConfirmationPage_Direct() throws IOException{

		// Reporting info
		initializeReporting("Verify billing type CGST+SGST and tax amount when tax code = Z or null [Non SEZ] in Quote creation page and Quote confirmation page",
				" C458186_Regression_Quotes_CE_GST_VerifyBillingTypeCGST_SGSTAndTaxAmountWhenTaxCodeIsZOrNull_NonSEZInQuoteCreationPageAndQuoteConfirmationPage_Direct", logger);

		// Test data
		Map<String, String> regData = scenarioData.get(ID15);
		Assert.assertNotEquals(regData.size(), 0);
		String password = passwords.get(DIRECTUSERPWD);
		String orgName = regData.get("Org Name");
		String catalogName = regData.get("Contract");
		String mdcpid = regData.get("MDCP ID");
		ArrayList<String> contractIDs = getS4ContractIDFromS4AndWslSheet(APJ,mdcpid,catalogName,"Purchase Order","No");
		Assert.assertNotEquals(regData.size(), 0);
		String contractID = contractIDs.get(0);
		String quoteName = "MyQuote";
		
		//Need to use bto
		String bto =  getProductsByProductType(getRegion(APJ, ID15),BTO).get(1);

		Map<String, String> data = new HashMap<String, String>();
		data.put("poNumber", "9999");
		data.put("phoneNumber", "9898989898");
		data.put("attentionText", "test");
		data.put("paymentMethod", "Purchase Order");
		data.put("catalogName", catalogName);
		data.put("OrgName", orgName);

		
		// Waiting for user availability
		String purchaser = getUser(ID19, PURCHASER);
		Assert.assertNotNull(purchaser);
		usersMappedToThreadID.put(Thread.currentThread().getId(), Arrays.asList(purchaser));
					
		Map<String, String> orderDetails = new HashMap<String, String>();
		orderDetails.put("attentionText", attentionText);
		orderDetails.put("phoneNumber", phoneNumber);
		orderDetails.put("paymentMethod", PO);
		orderDetails.put("emailID", purchaser);		
		
		// Get URL
		setEnvironment();
		String url = this.url;

		// Page Objects
		Login login = new Login(_webcontrols);
		//PLP plp = new PLP(_webcontrols);
		PDP pdp = new PDP(_webcontrols);
		ShoppingCart shoppingCart = new ShoppingCart(_webcontrols);
		CustomerService customerService = new CustomerService(_webcontrols);
		CreateNewQuote createNewQuote = new CreateNewQuote(_webcontrols);
		QuoteDetails quoteDetails = new QuoteDetails(_webcontrols);
		Checkout checkout = new Checkout(_webcontrols);

		Assert.assertTrue(login.loginToHP2B("PreCondition: Login to HP2B with Direct user", url, purchaser, password, true));

		Assert.assertTrue(customerService.clickOnHomeTab("PreCondition: Click on Home Tab","Clicked on Home Tab", true));

		Assert.assertTrue(customerService.selectOrganizationAndContract("Step 1 & 2: Select requested catalog","Requested catalog should be selected", data,true));

		Assert.assertTrue(customerService.deleteProducts("PreCondition: Delete product", "Product should be deleted", "CE", false));

		pdp = customerService.searchSKU("Step 3.1: Search for a Bto", "Requested product PDP should load", bto);
		Assert.assertNotEquals(pdp, null);

		Assert.assertTrue(pdp.addProductToCart("Step 3.2: Add bto product to cart at PDP", "Product should be added to cart","pdp"));

		shoppingCart = pdp.navigateToShoppingCartThroughHeader("Step 3.3: Click on mini cart icon and click on Go to cart button", "User should navigate to shopping cart page");
		Assert.assertNotEquals(shoppingCart, null);
		
		Assert.assertTrue(shoppingCart.clickOnSaveAsQuote("Step 5: Click on save as quote button", "Create quote Page will be displayed to the user"));

		LinkedHashMap<String, String> selectedS4ContractAddressDetails =createNewQuote.clickOnChangeBillingAddressEnterContractIDandVerifySelectedContractID(
				"Step 6: Scroll down to billing information section and select Tax class N S4 Contract from billing address"
						+ " popup a value", "Selected Billing address must be displayed",contractID);
		Assert.assertNotEquals(selectedS4ContractAddressDetails, null);
		
		String stateOrProvinceName = checkout.verifyFieldInAddressInfoSection("Precondition: Getting Billing address State/Province","State/Province should be fetched","Billing","State/Province:","",false);
		Assert.assertNotEquals(stateOrProvinceName, null);
		
		Assert.assertTrue(checkout.clickOnChangeSupplierAddress("Step 6.1: Now click on 'Change Supplier address' Button", "'Supplier addresses' popup should appears", true));

		Assert.assertNotNull(checkout.selectSupplierAddressBasedOnBillingInfoStateName(
				"Step 6.2: Select the address and click on OK button","Address must be saved and Page must refresh",stateOrProvinceName, "Same"));

		Assert.assertTrue(quoteDetails.verifyBillingType("Step 10: Verify Billing type in quote details page",
				"Billing type must be displayed as IGST", "CGST+SGST"));

		Assert.assertTrue(quoteDetails.verifyIsSezCheckboxNotDisplayed("Step 11: Verify Is Sez checkbox is checked",
				"Is Sez checkbox should be checked", true));
		
		String estimatedTax = quoteDetails.getEstimatedTaxValue("Get Estimated Tax Value", "Estimated tax price is fetched", true);
		Assert.assertNotEquals(estimatedTax, null);
		
		quoteDetails = createNewQuote.createQuote("Step 5: Enter all the mandatory details and click on save a quote",
				"Quote Should be created Successfully and navigate to quote details page", quoteName, purchaser);
		Assert.assertNotEquals(quoteDetails, null);
		
		
	
	}
}