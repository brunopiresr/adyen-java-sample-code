package com.adyen.examples.payout;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import com.adyen.services.payment.BankAccount;
import com.adyen.services.payment.Recurring;
import com.adyen.services.payout.PayoutPortType;
import com.adyen.services.payout.PayoutService;
import com.adyen.services.payout.StoreDetailRequest;
import com.adyen.services.payout.StoreDetailResponse2;

/**
 * Store Payout Details (SOAP)
 * 
 * Besides storing Payout details using the normal payment flow (see Payout Manual), a separate webservice call can be
 * done to store Payout details for a specific shopper. For example if you collect the Payout details on your own
 * website. This example shows how to store the payout details (in this case the bank account of a shopper) using SOAP.
 * 
 * Please note: the Payout functionality is set up as a 2-step process. Because of this there are two additional
 * Webservice User accounts needed to use the Payout process. Please request the Payout permission for a specific user
 * account with Adyen Support. The password can be set in Adyen CA >> Settings >> Users.
 * 
 * @link /9.Payout/Soap/StorePayoutDetails
 * @author Created by Adyen - Payments Made Easy
 */

@WebServlet(urlPatterns = { "/9.Payout/Soap/StorePayoutDetails" })
public class StorePayoutDetails extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/**
		 * SOAP settings
		 * - wsdl: the WSDL url you are using (Test/Live)
		 * - wsUser: your web service user to store Payout details, and to initiate a Payout
		 * - wsPassword: your web service user's password
		 */
		String wsdl = "https://pal-test.adyen.com/pal/servlet/soap/Payout?wsdl";
		String wsUser = "YourWSUser";
		String wsPassword = "YourWSPassword";

		/**
		 * Create SOAP client, using classes in adyen-wsdl-cxf.jar library (generated by wsdl2java tool, Apache CXF).
		 * 
		 * @see WebContent/WEB-INF/lib/adyen-wsdl-cxf.jar
		 */
		PayoutService service = new PayoutService(new URL(wsdl));
		PayoutPortType client = service.getPayoutHttpPort();

		// Set HTTP Authentication
		((BindingProvider) client).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, wsUser);
		((BindingProvider) client).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, wsPassword);

		/**
		 * To submit the payout details, call the storeDetail action on the Payout Service. The storeDetail request has
		 * the following fields:
		 * 
		 * <pre>
		 * - recurring
		 *     - contract       : The contract value of the recurring object should be present and contain value PAYOUT.
		 * - merchantAccount    : The merchant account you want to process this payout with.
		 * - shopperEmail       : The email address of the shopper.
		 * - shopperReference   : The reference to the shopper. This value identifies an unique shopper. Therefore you have to make sure that this value is different for multiple shoppers.
		 * - bank
		 *     - iban           : The complete IBAN number of the shopper.
		 *     - bic            : The associated bic number of the bank.
		 *     - bankName       : The name of the bank from the shopper.
		 *     - countryCode    : The country code of where the bank of the shopper is based.
		 *     - ownerName      : The name which is used to register the bank account number.
		 * </pre>
		 */

		// Create new store details request
		StoreDetailRequest storeDetailsRequest = new StoreDetailRequest();
		storeDetailsRequest.setMerchantAccount("YourMerchantAccount");
		storeDetailsRequest.setShopperEmail("test@shopper.com");
		storeDetailsRequest.setShopperReference("ShopperReference");

		// Set recurring contract
		Recurring recurring = new Recurring();
		recurring.setContract("PAYOUT");
		storeDetailsRequest.setRecurring(recurring);

		// Set bank account
		BankAccount bankAccount = new BankAccount();
		bankAccount.setIban("NL13TEST0123456789");
		bankAccount.setBic("TESTNL01");
		bankAccount.setBankName("TestBank");
		bankAccount.setCountryCode("NL");
		bankAccount.setOwnerName("Test Shopper");
		storeDetailsRequest.setBank(bankAccount);

		/**
		 * Send the store details request.
		 */
		StoreDetailResponse2 storeDetailsResult;
		storeDetailsResult = client.storeDetail(storeDetailsRequest);

		/**
		 * If the message is syntactically valid and merchantAccount is correct you will receive a storeDetailResponse
		 * with the following fields:
		 * 
		 * <pre>
		 * - pspReference               : A new reference to uniquely identify this request.
		 * - recurringDetailReference   : The token which you can use later on for submitting the Payout.
		 * - resultCode                 : Should be Success to indicate that the details were stored successfully.
		 * </pre>
		 */
		PrintWriter out = response.getWriter();

		out.println("Store Details Result:");
		out.println("- pspReference: " + storeDetailsResult.getPspReference());
		out.println("- recurringDetailReference: " + storeDetailsResult.getRecurringDetailReference());
		out.println("- resultCode: " + storeDetailsResult.getResultCode());

	}

}
