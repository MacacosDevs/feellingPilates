package com.feelingpilates.pagos.caracterizacion;

import com.stripe.exception.*;
import com.stripe.model.*;
import com.stripe.net.*;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;

/** Intercepts SDK ApiRequest directly; every unexpected transport fails closed. */
final class StripeResponseGetterPN14Fake implements StripeResponseGetter {
    final List<ApiRequest> requests = new ArrayList<>();
    final Map<String,PaymentIntent> retrieved = new HashMap<>();
    final Set<String> failingIds = new HashSet<>();
    PaymentIntent created = PN14Fixtures.intent("pi_pn14_created","requires_payment_method");
    boolean failCreate;
    @Override @SuppressWarnings("unchecked")
    public <T extends StripeObjectInterface> T request(ApiRequest request, Type type) throws StripeException {
        requests.add(request);
        if(type != PaymentIntent.class) throw new AssertionError("Unexpected SDK type: "+type);
        if(request.getMethod()==ApiResource.RequestMethod.POST && request.getPath().equals("/v1/payment_intents")) {
            if(failCreate) throw new ApiException("pn14 create rejected",null,null,500,null);
            return (T)created;
        }
        if(request.getMethod()==ApiResource.RequestMethod.GET && request.getPath().startsWith("/v1/payment_intents/")) {
            String id=request.getPath().substring("/v1/payment_intents/".length());
            if(failingIds.contains(id)) throw new ApiException("pn14 retrieve rejected",null,null,500,null);
            PaymentIntent pi=retrieved.get(id);
            if(pi==null)throw new AssertionError("Unexpected retrieve: "+id);
            return (T)pi;
        }
        throw new AssertionError("Unexpected SDK request: "+request.getMethod()+" "+request.getPath());
    }
    @Override public <T extends StripeObjectInterface> T request(BaseAddress b, ApiResource.RequestMethod m,
            String p, Map<String,Object> params, Type t, RequestOptions o, ApiMode mode) {
        throw new AssertionError("Unexpected legacy transport");
    }
    @Override public InputStream requestStream(BaseAddress b, ApiResource.RequestMethod m, String p,
            Map<String,Object> params, RequestOptions o, ApiMode mode) { throw new AssertionError("Unexpected stream"); }
    @Override public InputStream requestStream(ApiRequest r) { throw new AssertionError("Unexpected stream"); }
    @Override public StripeResponse rawRequest(RawApiRequest r) { throw new AssertionError("Unexpected raw transport"); }
}
