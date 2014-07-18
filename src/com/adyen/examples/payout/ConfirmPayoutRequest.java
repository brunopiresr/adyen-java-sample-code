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

import com.adyen.services.payout.ModifyRequest;
import com.adyen.services.payout.ModifyResponse;
import com.adyen.services.payout.PayoutPortType;
import com.adyen.services.payout.PayoutService;

/**
 * Confirm Payout Request (SOAP)
 * 
 * Confirming and declining the Payout can be done via a webservice call to the Payout Service or manually via the
 * Customer Area (CA). This example shows how a Payout request can be confirmed using SOAP.
 * 
 * Please note: the Payout functionality is set up as a 2-step process. Because of this there are two additional
 * Webservice User accounts needed to use the Payout process. Please request the Payout permission for a specific user
 * account with Adyen Support. The password can be set in Adyen CA >> Settings >> Users.
 * 
 * @link /9.Payout/Soap/ConfirmPayoutRequest
 * @author Created by Adyen - Payments Made Easy
 */

@WebServlet(urlPatterns = { "/9.Payout/Soap/ConfirmPayoutRequest" })
public class ConfirmPayoutRequest extends HttpServlet {

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
		 * You can confirm a Payout by sending a confirm request to the Payout service. The following fields are sent:
		 * 
		 * <pre>
		 * - merchantAccount    : The merchant account used in the Payout payment.
		 * - originalReference  : The PSP reference received after the Payout was submitted.
		 * </pre>
		 */
		ModifyRequest confirmRequest = new ModifyRequest();
		confirmRequest.setMerchantAccount("YourMerchantAccount");
		confirmRequest.setOriginalReference("ThePspReferenceOfThePayout");

		/**
		 * Send the confirm payout request.
		 */
		ModifyResponse confirmResult;
		confirmResult = client.confirm(confirmRequest);

		/**
		 * If the message is syntactically valid and merchantAccount is correct you will receive a confirmResponse with
		 * the following fields:
		 * 
		 * <pre>
		 * - pspReference      : A new reference to uniquely identify this request. This reference will be the
		 *                       pspReference of the payment.
		 * - response          : In case of success, this will be [payout-confirm-received] or, in case of an error,
		 *                       an informational message will be returned.
		 * </pre>
		 * 
		 * The actual result of the confirmed Payout is sent via a notification with eventCode REFUND_WITH_DATA. The
		 * success field indicates if the Payout was successful (true) or not (false). If false, the reason field of the
		 * notification will give a short description why. We send back the PSP reference of the Payout as
		 * originalReference.
		 */
		PrintWriter out = response.getWriter();

		out.println("Confirm Payout Result:");
		out.println("- pspReference: " + confirmResult.getPspReference());
		out.println("- response: " + confirmResult.getResponse());
	}

}
