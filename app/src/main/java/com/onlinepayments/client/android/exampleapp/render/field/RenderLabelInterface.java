package com.onlinepayments.client.android.exampleapp.render.field;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem;


/**
 * Defines the rendering of label interface methods
 * 
 * Copyright 2020 Global Collect Services B.V
 *
 */
public interface RenderLabelInterface {
	
	
	/**
	 * Renders a tooltip by the data in the PaymentProductField.
	 * This label is added to the given rowView
	 * 
	 * @param selectedPaymentProduct, the selected PaymentProduct, used for getting the correct translations
	 * @param rowView, the ViewGroup to which the rendered label is added
	 */
    TextView renderLabel(PaymentProductField field, BasicPaymentItem selectedPaymentProduct, ViewGroup rowView);
	
}
