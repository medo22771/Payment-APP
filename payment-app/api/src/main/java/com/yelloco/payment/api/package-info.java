/**
 * <p>
 * Provides API for YelloPay application in order to perform payment transaction by another
 * android application and receive back the payment result.
 * </p>
 * <p>
 * In order to start transaction {@link com.yelloco.payment.api.PaymentRequest} has to be initialized
 * and particular values (amount, currency etc.) set.
 * Using {@link com.yelloco.payment.api.PaymentRequest#toIntent()} method the android intent can be
 * acquired and used to start the YelloPay activity, see
 * <a href="https://developer.android.com/training/basics/intents/result.html">Getting a Result from an Activity</a>.
 * </p>
 * <pre>
 * PaymentRequest request = new PaymentRequest();
 * request.setAmount(new BigDecimal("50.88"));
 * request.setCashBack(new BigDecimal("100.00"));
 * request.setTip(new BigDecimal("0.0"));
 * request.setCurrency(Currency.getInstance("EUR"));
 * request.setEmail("merchant1@shop.fr");
 * request.setSms("00111222333444");
 * request.setMerchantId("Merchant1");
 *
 * Intent intent = request.toIntent();
 * intent.setAction(PaymentRequest.ACTION_PAY);
 * startActivityForResult(intent, PaymentRequest.PAYMENT_REQUEST_CODE);
 * </pre>
 * <p>
 * <h2>Payment request parameters</h2>
 * <table>
 *     <tr>
 *         <th>Name</th>
 *         <th>Mandatory</th>
 *         <th>Type</th>
 *         <th>Link</th>
 *     </tr>
 *     <tr>
 *         <td>Amount</td>
 *         <td>true</td>
 *         <td>BigDecimal</td>
 *         <td>{@link com.yelloco.payment.api.PaymentRequest#amount}</td>
 *     </tr>
 *     <tr>
 *         <td>Currency</td>
 *         <td>true</td>
 *         <td>Currency</td>
 *         <td>{@link com.yelloco.payment.api.PaymentRequest#currency}</td>
 *     </tr>
 *     <tr>
 *         <td>CashBack</td>
 *         <td>false</td>
 *         <td>BigDecimal</td>
 *         <td>{@link com.yelloco.payment.api.PaymentRequest#cashBack}</td>
 *     </tr>
 *     <tr>
 *         <td>Tip</td>
 *         <td>false</td>
 *         <td>BigDecimal</td>
 *         <td>{@link com.yelloco.payment.api.PaymentRequest#tip}</td>
 *     </tr>
 *     <tr>
 *         <td>Email</td>
 *         <td>false</td>
 *         <td>String</td>
 *         <td>{@link com.yelloco.payment.api.PaymentRequest#email}</td>
 *     </tr>
 *     <tr>
 *         <td>Sms</td>
 *         <td>false</td>
 *         <td>String</td>
 *         <td>{@link com.yelloco.payment.api.PaymentRequest#sms}</td>
 *     </tr>
 *     <tr>
 *         <td>MerchantId</td>
 *         <td>false</td>
 *         <td>String</td>
 *         <td>{@link com.yelloco.payment.api.PaymentRequest#merchantId}</td>
 *     </tr>
 *     <tr>
 *         <td>BasketData</td>
 *         <td>false</td>
 *         <td>String</td>
 *         <td>{@link com.yelloco.payment.api.PaymentRequest#basketData}</td>
 *     </tr>
 * </table>
 * </p>
 * <p>
 * YelloPay activity should go to foreground with transaction already initialized and user can follow
 * the payment instructions to finish the payment itself. Afterwards the control is passed back to the
 * calling activity.
 * </p>
 * <p>
 * After receiving the android intent back from payment transaction in onActivityResult callback it
 * can be converted into {@link com.yelloco.payment.api.PaymentResponse} object by using static
 * {@link com.yelloco.payment.api.PaymentResponse#fromIntent(android.content.Intent)}
 * </p>
 * <pre>
 *  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *      if (requestCode == PaymentRequest.PAYMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
 *          PaymentResponse response = PaymentResponse.fromIntent(data);
 *          switch (response.getResult()) {
 *              case APPROVED:
 *                  performTheOrder();
 *                  break;
 *              case DECLINED:
 *                  declineTheOrder();
 *                  break;
 *          }
 *          processReceipt(response.getReceipt());
 *      }
 *  }
 * </pre>
 */
package com.yelloco.payment.api;
