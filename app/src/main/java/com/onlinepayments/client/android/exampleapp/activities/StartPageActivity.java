package com.onlinepayments.client.android.exampleapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.onlinepayments.client.android.exampleapp.R;
import com.onlinepayments.client.android.exampleapp.view.ValidationEditText;
import com.onlinepayments.client.android.exampleapp.configuration.Constants;
import com.onlinepayments.client.android.exampleapp.dialog.DialogUtil;
import com.onlinepayments.client.android.exampleapp.model.ShoppingCart;
import com.onlinepayments.client.android.exampleapp.model.ShoppingCartItem;
import com.onlinepayments.sdk.client.android.model.AmountOfMoney;
import com.onlinepayments.sdk.client.android.model.CountryCode;
import com.onlinepayments.sdk.client.android.model.CurrencyCode;
import com.onlinepayments.sdk.client.android.model.PaymentContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Dummy startpage to start payment
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class StartPageActivity extends Activity {

	ValidationEditText clientSessionIdentifierEditText;
	ValidationEditText customerIdentifierEditText;
	ValidationEditText clientApiUrlEditText;
	ValidationEditText assetUrlEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startpage);
		clientSessionIdentifierEditText = findViewById(R.id.client_session_identifier);
		customerIdentifierEditText = findViewById(R.id.customer_identifier);
		clientApiUrlEditText = findViewById(R.id.client_api_url);
		assetUrlEditText = findViewById(R.id.asset_url);

		loadData();
	}

	public void payButtonPressed(View view) {

		if (!clientSessionIdentifierEditText.isValid()) {
			return;
		}
		String clientSessionIdentifier = clientSessionIdentifierEditText.getValue();

		if (!customerIdentifierEditText.isValid()) {
			return;
		}
		String customerId = customerIdentifierEditText.getValue();

		EditText merchantIdentifierEditText = findViewById(R.id.merchant_identifier);
		String merchantId = merchantIdentifierEditText.getText().toString();

		EditText merchantNameText = findViewById(R.id.merchant_name);
		String merchantName = merchantNameText.getText().toString();

		ValidationEditText clientApiUrlEditText = findViewById(R.id.client_api_url);
		if (!clientApiUrlEditText.isValid()) {
			return;
		}
		String clientApiUrl = clientApiUrlEditText.getValue();

		if (!assetUrlEditText.isValid()) {
			return;
		}
		String assetUrl = assetUrlEditText.getValue();

		ValidationEditText amountEditText = findViewById(R.id.amount);
		if (!amountEditText.isValid()) {
			return;
		}
		String amount = amountEditText.getValue();

		CountryCode countryCode = CountryCode.valueOf(((Spinner) findViewById(R.id.country_code)).getSelectedItem().toString());
		CurrencyCode currencyCode = CurrencyCode.valueOf(((Spinner) findViewById(R.id.currency_code)).getSelectedItem().toString());

		boolean isRecurring = ((CheckBox) findViewById(R.id.payment_is_recurring)).isChecked();
		boolean environmentIsProduction = ((CheckBox) findViewById(R.id.environment_is_production)).isChecked();

		ShoppingCart cart = new ShoppingCart();
		cart.addItemToShoppingCart(new ShoppingCartItem("Something", Long.parseLong(amount), 1));

		// Create the PaymentContext object
		AmountOfMoney amountOfMoney = new AmountOfMoney(cart.getTotalAmount(), currencyCode);
		PaymentContext paymentContext = new PaymentContext(amountOfMoney, countryCode, isRecurring);

		// and show the PaymentProductSelectionActivity
		Intent paymentIntent = new Intent(this, PaymentProductSelectionActivity.class);

		// Attach the following objects to the paymentIntent
		paymentIntent.putExtra(Constants.INTENT_PAYMENT_CONTEXT, paymentContext);
		paymentIntent.putExtra(Constants.INTENT_SHOPPINGCART, cart);
		paymentIntent.putExtra(Constants.MERCHANT_CLIENT_SESSION_IDENTIFIER, clientSessionIdentifier);
		paymentIntent.putExtra(Constants.MERCHANT_CUSTOMER_IDENTIFIER, customerId);
		paymentIntent.putExtra(Constants.MERCHANT_MERCHANT_IDENTIFIER, merchantId);
		paymentIntent.putExtra(Constants.MERCHANT_NAME, merchantName);
		paymentIntent.putExtra(Constants.MERCHANT_CLIENT_API_URL, clientApiUrl);
		paymentIntent.putExtra(Constants.MERCHANT_ASSET_URL, assetUrl);
		paymentIntent.putExtra(Constants.MERCHANT_ENVIRONMENT_IS_PRODUCTION, environmentIsProduction);

		// Start paymentIntent
		startActivity(paymentIntent);
	}

	public void parseJsonButtonPressed(View view) {
		DialogUtil.showJsonAlertDialog(this, sessionDetails -> {

			assetUrlEditText.setText(sessionDetails.assetUrl);
			clientApiUrlEditText.setText(sessionDetails.clientApiUrl);
			clientSessionIdentifierEditText.setText(sessionDetails.clientSessionId);
			customerIdentifierEditText.setText(sessionDetails.customerId);

			assetUrlEditText.isValid();
			clientApiUrlEditText.isValid();
			clientSessionIdentifierEditText.isValid();
			customerIdentifierEditText.isValid();
		});
	}


	private void loadData() {
		// Get all values for CountryCode spinner
		List<CountryCode> spinnerArrayCountry = new ArrayList<>(EnumSet.allOf(CountryCode.class));
		Collections.sort(spinnerArrayCountry);

		// Make adapters of list and put it inside spinner
		ArrayAdapter<CountryCode> adapterCountry = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArrayCountry);
		adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinnerCountry = findViewById(R.id.country_code);
		spinnerCountry.setAdapter(adapterCountry);
		spinnerCountry.setSelection(adapterCountry.getPosition(CountryCode.NL));

		// Get all values for CurrencyCode spinner
		List<CurrencyCode> spinnerArrayCurrency = new ArrayList<>(EnumSet.allOf(CurrencyCode.class));
		Collections.sort(spinnerArrayCurrency);

		// Make adapters of list and put it inside spinner
		ArrayAdapter<CurrencyCode> adapterCurrency = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArrayCurrency);
		adapterCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinnerCurrency = findViewById(R.id.currency_code);
		spinnerCurrency.setAdapter(adapterCurrency);
		spinnerCurrency.setSelection(adapterCurrency.getPosition(CurrencyCode.EUR));
	}
}
