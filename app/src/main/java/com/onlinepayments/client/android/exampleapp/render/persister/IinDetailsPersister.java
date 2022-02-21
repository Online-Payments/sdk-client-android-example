package com.onlinepayments.client.android.exampleapp.render.persister;

import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem;

import java.io.Serializable;
import java.util.List;

/**
 * Will persist IinDetails information
 *
 * Copyright 2020 Global Collect Services B.V
 */
public class IinDetailsPersister implements Serializable {

    private static final long serialVersionUID = -5586773582374607503L;

    private IinDetailsResponse iinDetailsResponse;

    private List<BasicPaymentItem> paymentProducts;

    public void setIinDetailsResponse(IinDetailsResponse iinDetailsResponse) {
        this.iinDetailsResponse = iinDetailsResponse;
    }

    public IinDetailsResponse getIinDetailsResponse() {
        return iinDetailsResponse;
    }

    public List<BasicPaymentItem> getPaymentProducts() {
        return paymentProducts;
    }

}
