package com.onlinepayments.client.android.exampleapp.render.field;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onlinepayments.client.android.exampleapp.translation.Translator;
import com.onlinepayments.client.android.exampleapp.R;
import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem;


/** 
 * This class implements the RenderLabelInterface and 
 * handles the rendering of the label for one paymentproductfield
 * 
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class RenderLabel implements RenderLabelInterface {

	@Override
	public TextView renderLabel(PaymentProductField field, BasicPaymentItem selectedPaymentProduct, ViewGroup rowView) {

		TextView label = new TextView(rowView.getContext());

		// Get the labeltext
		Translator translator = Translator.getInstance(rowView.getContext());
		String labelText = translator.getPaymentProductFieldLabel(selectedPaymentProduct.getId(), field.getId());
		if (labelText.contains(Constants.LINK_PLACEHOLDER)) {
			String linkLabel = translator.getPaymentProductFieldLabel(selectedPaymentProduct.getId(), field.getId() + ".link");
			String link = "<a href=\"" + field.getDisplayHints().getLink() + "\">" + linkLabel + "</a>";
			labelText = labelText.replace(Constants.LINK_PLACEHOLDER, link);
			label.setMovementMethod(LinkMovementMethod.getInstance());
		}

		// Create new label
		label.setTextAppearance(rowView.getContext(), R.style.ListViewTextView);
		label.setText(Html.fromHtml(labelText));
		rowView.addView(label);

		return label;
	}
	
}
