/**
 * 
 * @author Shivank-NUDM
 * 
 * HDFC CollectNow (Razorpay Embedded Checkout)
 *
 * Exports: startHdfcPayment(createOrderResponse)
 *
 * Behavior:
 * - Accepts the response returned from backend (create-order/create-citizen-reciept)
 * - Handles both:
 *    1) backend returns a `razorpay` object: { orderId, keyId, amount, currency, callbackUrl, prefill }
 *    2) backend returns a redirectUrl (legacy) — then simply navigates to it
 * - Builds a form and POSTs to Razorpay Embedded checkout endpoint.
 *
 */

export function startHdfcPayment(createOrderResponse) {
  console.log("Starting HDFC CollectNow Payment with response:", createOrderResponse);
  debugger
  // Defensive checks
  if (!createOrderResponse || !createOrderResponse.Transaction) {
    console.error("Invalid create order response for HDFC", createOrderResponse);
    throw new Error("Invalid create order response for HDFC");
  }

  const txn = createOrderResponse.Transaction;
  console.log("HDFC Transaction Data:", txn);
  // Option A: backend returned a `razorpay` object with explicit fields
  const razorpay = txn.razorpay || txn.razorPay || txn.razorPayData;
  console.log("Razorpay Data:", razorpay);
  
  if (razorpay && (razorpay.orderId || razorpay.order_id)) {
    const orderId = razorpay.orderId || razorpay.order_id;
    const keyId = razorpay.keyId || razorpay.key_id || razorpay.key;
    const amount = String(razorpay.amount || razorpay.txnAmount || '');
    const currency = razorpay.currency || 'INR';
    const callbackUrl = razorpay.callbackUrl || razorpay.callback_url || txn.callbackUrl || txn.redirectUrl;
    const prefill = razorpay.prefill || {};

    // Build form to post to Razorpay embedded endpoint
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = 'https://api.razorpay.com/v1/checkout/embedded';
    form.style.display = 'none';
    form.target = '_top';

    const addInput = (name, value) => {
      const inp = document.createElement('input');
      inp.type = 'hidden';
      inp.name = name;
      inp.value = value ?? '';
      form.appendChild(inp);
    };

    // Required fields per Embedded Checkout
    if (keyId) addInput('key_id', keyId);
    addInput('order_id', orderId);
    if (amount) addInput('amount', amount); // paise
    addInput('currency', currency);
    if (callbackUrl) addInput('callback_url', callbackUrl);

    // Optional prefill
    if (prefill.name) addInput('prefill[name]', prefill.name);
    if (prefill.email) addInput('prefill[email]', prefill.email);
    if (prefill.contact) addInput('prefill[contact]', prefill.contact);

    // Any additional notes provided by backend
    if (razorpay.notes && typeof razorpay.notes === 'object') {
      Object.entries(razorpay.notes).forEach(([k, v]) => addInput(`notes[${k}]`, v));
    }

    document.body.appendChild(form);
    form.submit();
    return;
  }

  // Option B: backend returned a redirectUrl (older/alternate flow)
  const redirectUrl = txn.redirectUrl || txn.redirecturl || txn.redirect;
  if (redirectUrl) {
    // If backend already created a ready-to-redirect URL, just navigate
    window.location = redirectUrl;
    return;
  }

  throw new Error("Unsupported response format from backend for HDFC/Razorpay payment.");
}
