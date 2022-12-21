package com.onlinepayments.client.android.exampleapp.activities;

import static com.onlinepayments.sdk.client.android.configuration.Constants.GOOGLE_PAY_TOKEN_FIELD_ID;
import static com.onlinepayments.sdk.client.android.configuration.Constants.PAYMENTPRODUCTID_GOOGLEPAY;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.onlinepayments.client.android.exampleapp.R;
import com.onlinepayments.client.android.exampleapp.configuration.CheckCommunication;
import com.onlinepayments.client.android.exampleapp.configuration.Constants;
import com.onlinepayments.client.android.exampleapp.model.ShoppingCart;
import com.onlinepayments.client.android.exampleapp.util.GooglePay;
import com.onlinepayments.client.android.exampleapp.view.selectionview.ProductSelectionView;
import com.onlinepayments.client.android.exampleapp.view.selectionview.ProductSelectionViewImpl;
import com.onlinepayments.sdk.client.android.Util;
import com.onlinepayments.sdk.client.android.asynctask.BasicPaymentItemsAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PaymentProductAsyncTask;
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicatorConfiguration;
import com.onlinepayments.sdk.client.android.manager.AssetManager;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest;
import com.onlinepayments.sdk.client.android.model.Size;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.session.Session;
import com.onlinepayments.sdk.client.android.session.SessionEncryptionHelper;

import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;


/**
 * Activity that lists all the available payment options
 *
 * Copyright 2020 Global Collect Services B.V
 */
public class PaymentProductSelectionActivity extends ShoppingCartActivity implements BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener,
                                                                                     PaymentProductAsyncTask.PaymentProductCallListener,
                                                                                     SessionEncryptionHelper.OnPaymentRequestPreparedListener,
                                                                                     DialogInterface.OnClickListener {

    private static final String TAG = PaymentProductSelectionActivity.class.getName();

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 42;

    // The view belonging to this activity
    private ProductSelectionView selectionView;

    private ShoppingCart shoppingCart;

    // The session object that is used to connect to the API
    private Session session;

    // Parameters used to initialize the connection
    private String clientSessionId;
    private String customerId;
    private String merchantId;
    private String merchantName;
    private String clientApiUrl;
    private String assetUrl;
    private boolean environmentIsProduction;

    // Variables required to retrieve the payment items that are available for payment
    private PaymentContext paymentContext;
    private boolean groupPaymentProducts;
    private PaymentProduct paymentProduct;

    // Loaded payment product and selected Account On File information
    private BasicPaymentItems paymentItems;
    private AccountOnFile accountOnFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_payment_product);
        // Initialize the shoppingcart
        super.initialize(this);

        selectionView = new ProductSelectionViewImpl(this, R.id.payment_product_selection_view_layout);

        // This activity won't work without internet, so show an error message if there is no
        // connection
        if (!CheckCommunication.isOnline(this)) {
            selectionView.showNoInternetDialog(this);
            return;
        }

        loadIntentData();

        retrieveClientSessionAndCustomerId();

        if (savedInstanceState != null) {
            initializeSavedInstanceStateData(savedInstanceState);
        }

        if (paymentItems == null) {
            try {
                // Instantiate the Session
                session = C2sCommunicatorConfiguration.initWithClientSessionId(clientSessionId, customerId, clientApiUrl, assetUrl, environmentIsProduction, Constants.APPLICATION_IDENTIFIER);
            } catch (InvalidParameterException e) {
                Log.e(TAG, e.getMessage());
                selectionView.showTechnicalErrorDialog(this);
                return;
            }

            selectionView.showLoadingIndicator();
            session.getBasicPaymentItems(getApplicationContext(), paymentContext, this, groupPaymentProducts);
        } else {
            selectionView.renderDynamicContent(paymentItems);
        }
    }

    private void retrieveClientSessionAndCustomerId() {
        // Send a call to your payment Server to have it retrieve a ClientSession and CustomerId via
        // the createSession call. These ID's are needed to initialize the Session that
        // communicates with the API.
        // In order to make the call via the Server2Server API, please include the Metadata that is
        // rendered with the method below.
        @SuppressWarnings("unused")
        Map<String, String> metadata = Util.getMetadata(getApplicationContext(), Constants.APPLICATION_IDENTIFIER, null);

        // We will not make the call here, but use the values that were provided on the
        // start-screen instead.
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        clientSessionId = intent.getStringExtra(Constants.MERCHANT_CLIENT_SESSION_IDENTIFIER);
        customerId = intent.getStringExtra(Constants.MERCHANT_CUSTOMER_IDENTIFIER);
        clientApiUrl = intent.getStringExtra(Constants.MERCHANT_CLIENT_API_URL);
        merchantId = intent.getStringExtra(Constants.MERCHANT_MERCHANT_IDENTIFIER);
        merchantName = intent.getStringExtra(Constants.MERCHANT_NAME);
        assetUrl = intent.getStringExtra(Constants.MERCHANT_ASSET_URL);
        environmentIsProduction = intent.getBooleanExtra(Constants.MERCHANT_ENVIRONMENT_IS_PRODUCTION, false);
        paymentContext = (PaymentContext) intent.getSerializableExtra(Constants.INTENT_PAYMENT_CONTEXT);
        groupPaymentProducts = intent.getBooleanExtra(Constants.INTENT_GROUP_PAYMENTPRODUCTS, false);
        shoppingCart = (ShoppingCart) intent.getSerializableExtra(Constants.INTENT_SHOPPINGCART);
    }

    private void initializeSavedInstanceStateData(Bundle savedInstanceState) {
        paymentItems = (BasicPaymentItems) savedInstanceState.getSerializable(Constants.BUNDLE_PAYMENT_PRODUCTS);
        shoppingCart = (ShoppingCart) savedInstanceState.getSerializable(Constants.BUNDLE_SHOPPING_CART);
        session = (Session) savedInstanceState.getSerializable(Constants.BUNDLE_GC_SESSION);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBasicPaymentItemsCallComplete(@NonNull BasicPaymentItems paymentItems) {
        if (paymentItems.getBasicPaymentItems() != null && !paymentItems.getBasicPaymentItems().isEmpty()) {

            this.paymentItems = paymentItems;

            selectionView.renderDynamicContent(paymentItems);

            updateLogos(session.getAssetUrl(), paymentItems.getBasicPaymentItems());
        } else {
            selectionView.showTechnicalErrorDialog(this);
        }
        selectionView.hideLoadingIndicator();
    }

    @Override
    public void onBasicPaymentItemsCallError(ErrorResponse error) {
        selectionView.showTechnicalErrorDialog(this);
        selectionView.hideLoadingIndicator();
    }

    private void updateLogos(String assetUrl, List<BasicPaymentItem> basicPaymentItems) {
        // if you want to specify the size of the logos you update, set resizedLogo to a size (width, height)
        Size resizedLogo = new Size(100, 100);

        // if you just want to get the default images, set
        // resizedLogo = null;

        AssetManager manager = AssetManager.getInstance(getApplicationContext());
        manager.updateLogos(assetUrl, basicPaymentItems, resizedLogo);
    }

    // The callback method for when a user selects a payment product
    public void onPaymentProductSelected(View v) {
        selectionView.showLoadingIndicator();
        if (v.getTag() instanceof BasicPaymentProduct) {
            String paymentProductId = ((BasicPaymentProduct) v.getTag()).getId();
            session.getPaymentProduct(getApplicationContext(), paymentProductId, paymentContext, this);

        } else if (v.getTag() instanceof AccountOnFile) {
            // Store the Account on file so it can be added to the intent later on.
            accountOnFile = (AccountOnFile) v.getTag();
            session.getPaymentProduct(getApplicationContext(), accountOnFile.getPaymentProductId(), paymentContext, this);
        } else {
            throw new InvalidParameterException("Tag in view is not of a valid type");
        }
    }

    @Override
    public void onPaymentProductCallComplete(PaymentProduct paymentProduct) {
        this.paymentProduct = paymentProduct;
        handlePaymentItemCallBack(paymentProduct);
    }

    @Override
    public void onPaymentProductCallError(ErrorResponse error) {
        // Not implemented
    }

    // Determine what view should be served next, based on whether the product has inputfields.
    // For some products special rules apply, which are also handled here.
    private void handlePaymentItemCallBack(PaymentItem paymentItem) {
        if (paymentItem == null) {
            selectionView.hideLoadingIndicator();
            selectionView.showTechnicalErrorDialog(this);

        } else if (PAYMENTPRODUCTID_GOOGLEPAY.equals(paymentItem.getId())) {
            // Google pay requires a different flow even though it has fields. The fields should
            // not be shown to the user, but they will be filled after the user has completed the
            // Google pay flow.
            startGooglePay((PaymentProduct) paymentItem);

        } else if (paymentItem.getPaymentProductFields().isEmpty()) {
            // For payment products that do not have fields configured other actions are required to
            // complete the payment. These actions may be a redirect to an external payment products'
            // website, or showing instructions to complete the payment.
            // Currently this app does not contain examples for these kinds of products. Instead we
            // will go to the result activity directly.
            startNextActivity(new Intent(this, PaymentResultActivity.class));

        } else {
            // Ask the user for its payment details in the next activity
            determineAndStartDetailInputActivity(paymentItem);
        }
    }

    private void startGooglePay(PaymentProduct paymentProduct) {
        if (!merchantId.isEmpty() && !merchantName.isEmpty()) {
            GooglePay googlePay = new GooglePay(this, paymentContext, paymentProduct, merchantId, merchantName);
            googlePay.start(session.isEnvironmentTypeProduction());
        } else {
            Toast
                .makeText(this, "MerchantId & MerchantName cannot be empty when you want to use GooglePay", Toast.LENGTH_LONG)
                .show();
        }
        selectionView.hideLoadingIndicator();
    }

    private void determineAndStartDetailInputActivity(PaymentItem paymentItem) {
        // Determine what DetailInputActivity to load. The base-class just renders the fields and
        // performs default validation on the fields. In some cases this is not enough however. In
        // these cases a subclass of the DetailInputActivity will be loaded that has additional
        // functionality for these specific products/methods.
        Intent detailInputActivityIntent = null;
        if (((PaymentProduct) paymentItem).getPaymentMethod().equals("card")) {
            detailInputActivityIntent = new Intent(this, DetailInputActivityCreditCards.class);

        } else {
            detailInputActivityIntent = new Intent(this, DetailInputActivity.class);

        }
        detailInputActivityIntent.putExtra(Constants.INTENT_SELECTED_ITEM, paymentItem);
        startNextActivity(detailInputActivityIntent);
    }

    private void startNextActivity(Intent detailInputActivityIntent) {
        // Add data to intent for the detail activity input
        detailInputActivityIntent.putExtra(Constants.INTENT_PAYMENT_CONTEXT, paymentContext);
        detailInputActivityIntent.putExtra(Constants.INTENT_SELECTED_ACCOUNT_ON_FILE, accountOnFile);
        detailInputActivityIntent.putExtra(Constants.INTENT_SHOPPINGCART, shoppingCart);
        detailInputActivityIntent.putExtra(Constants.INTENT_GC_SESSION, session);

        // Start the intent
        startActivity(detailInputActivityIntent);
        selectionView.hideLoadingIndicator();
    }

    // Handle the result returned by the Google Pay client. In case the payment is successful,
    // the payment result page is loaded.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectionView.showLoadingIndicator();
        // value passed in AutoResolveHelper
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);

                    String jsonString = paymentData.toJson();
                    try {
                        JSONObject jsonJSON = new JSONObject(jsonString);

                        JSONObject paymentMethodData = jsonJSON.getJSONObject("paymentMethodData");
                        JSONObject tokenizationData = paymentMethodData.getJSONObject("tokenizationData");
                        String token = tokenizationData.getString("token");

                        String encryptedPaymentData = token;

                        PaymentRequest paymentRequest = new PaymentRequest();
                        paymentRequest.setPaymentProduct(paymentProduct);
                        paymentRequest.setValue(GOOGLE_PAY_TOKEN_FIELD_ID, encryptedPaymentData);
                        paymentRequest.validate();

                        session.preparePaymentRequest(paymentRequest, getApplicationContext(), this);
                    } catch (Exception e) {
                        Log.e(TAG, "Could not parse malformed JSON: \"" + jsonString + "\"");
                    }

                    break;
                case RESULT_CANCELED:
                    Log.i(TAG, "Google Pay payment was cancelled");
                    selectionView.hideLoadingIndicator();
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    Log.e(TAG, "Something went wrong whilst making a Google Pay payment; errorCode: " + status);
                    selectionView.hideLoadingIndicator();
                    selectionView.showTechnicalErrorDialog(this);
                    break;
                default:
                    selectionView.hideLoadingIndicator();
                    selectionView.showTechnicalErrorDialog(this);
                    Log.e(TAG, "Something went wrong whilst making a Google Pay payment");
            }
        }
    }

    @Override
    public void onPaymentRequestPrepared(PreparedPaymentRequest preparedPaymentRequest) {
        // Send the PreparedPaymentRequest to the merchant server, this contains a blob of encrypted values + base64encoded metadata
        //
        // Depending on the response from the merchant server, redirect to one of the following pages:
        //
        // - Successful page if the payment is done
        // - Unsuccesful page when the payment result is unsuccessful, you must supply a paymentProductId and an errorcode which will be translated
        // - Webview page to show an instructions page, or to go to a third party payment page
        //
        // Successful and Unsuccessful results have to be redirected to PaymentResultActivity

        selectionView.hideLoadingIndicator();

        // When the the payment result has come back, go to the successful/unsuccessful page:
        Intent paymentResultIntent = new Intent(this, PaymentResultActivity.class);

        //put shopping cart and payment request inside the intent
        paymentResultIntent.putExtra(Constants.INTENT_SHOPPINGCART, shoppingCart);
        paymentResultIntent.putExtra(Constants.INTENT_PAYMENT_CONTEXT, paymentContext);

        // Add errormessage if there was an error
        //String errorCode = "errorCode";
        //paymentResultIntent.putExtra(Constants.INTENT_ERRORMESSAGE, null);

        startActivity(paymentResultIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Constants.BUNDLE_PAYMENT_PRODUCTS, paymentItems);
        outState.putSerializable(Constants.BUNDLE_SHOPPING_CART, shoppingCart);
        outState.putSerializable(Constants.BUNDLE_GC_SESSION, session);

        super.onSaveInstanceState(outState);
    }

    @Override
    // Callback for the the ok button on the error dialogs
    public void onClick(DialogInterface dialogInterface, int which) {
        // When an error has occurred the Activity is no longer valid. Destroy it and go one step back
        finish();
    }
}
