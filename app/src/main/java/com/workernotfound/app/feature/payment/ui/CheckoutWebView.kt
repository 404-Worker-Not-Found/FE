package com.workernotfound.app.feature.payment.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import com.workernotfound.app.R
import com.workernotfound.app.core.designsystem.*
import com.workernotfound.app.core.designsystem.component.AppTopBar
import com.workernotfound.app.feature.payment.domain.*
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled") // Toss SDK requires JS; no native JS bridge or auth headers are exposed.
@Composable
internal fun CheckoutWebView(checkout: Checkout, onResult: (CheckoutReturn) -> Unit,
    onCancel: () -> Unit, onInvalid: () -> Unit) {
    val context = LocalContext.current
    val result by rememberUpdatedState(onResult)
    val invalid by rememberUpdatedState(onInvalid)
    var finished by remember(checkout.nonce) { mutableStateOf(false) }
    val description = stringResource(R.string.checkout_order_name, checkout.jobId)
    val unavailable = stringResource(R.string.checkout_app_unavailable)
    var externalError by remember { mutableStateOf(false) }
    val webView = remember(checkout.nonce) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.setSupportMultipleWindows(false)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val uri = request.url
                    if (uri.host == "appassets.androidplatform.net") {
                        if (request.isForMainFrame && !finished) {
                            finished = true
                            try { checkout.parseReturn(uri.toString())?.let(result) ?: invalid() }
                            catch (_: IllegalArgumentException) { invalid() }
                            catch (_: java.net.URISyntaxException) { invalid() }
                        }
                        return true
                    }
                    if (uri.scheme == "https") return false
                    if (request.isForMainFrame) openPaymentApp(uri)
                    return true
                }
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    // Never send local return URLs (including paymentKey) to a remote host.
                    return if (request.url.host == "appassets.androidplatform.net")
                        WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0))) else null
                }
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (request.isForMainFrame && !finished) { finished = true; invalid() }
                }
                private fun openPaymentApp(uri: Uri) {
                    try {
                        val parsed = if (uri.scheme == "intent") Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME) else Intent(Intent.ACTION_VIEW, uri)
                        val data = parsed.data ?: return
                        if (data.scheme !in PAYMENT_SCHEMES) { externalError = true; return }
                        val safe = Intent(Intent.ACTION_VIEW, data).addCategory(Intent.CATEGORY_BROWSABLE)
                        parsed.`package`?.let { safe.setPackage(it) }
                        context.startActivity(safe)
                    } catch (_: android.content.ActivityNotFoundException) { externalError = true }
                    catch (_: java.net.URISyntaxException) { externalError = true }
                    catch (_: SecurityException) { externalError = true }
                }
            }
            loadDataWithBaseURL("$CHECKOUT_ORIGIN/payment/index.html", checkoutHtml(checkout, description), "text/html", "UTF-8", null)
        }
    }
    DisposableEffect(webView) { onDispose { webView.stopLoading(); webView.removeAllViews(); webView.destroy() } }
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding()) {
                AppTopBar(stringResource(R.string.checkout_title), onBack = onCancel)
                Text(stringResource(R.string.checkout_test_notice), color = AppColors.TextSub,
                    modifier = Modifier.padding(AppDimens.screenPadding))
                if (externalError) Text(unavailable, color = AppColors.Danger)
                AndroidView(factory = { webView }, modifier = Modifier.fillMaxWidth().weight(1f))
            }
        }
    }
}
internal val PAYMENT_SCHEMES = setOf("supertoss", "ispmobile", "kakaotalk", "kakaobank", "kb-acp", "liivbank", "newliiv", "kbbank",
    "nhappcardansimclick", "nhallonepayansimclick", "nonghyupcardansimclick", "lottesmartpay", "lotteappcard",
    "mpocket.online.ansimclick", "mpocket.ansimclick.cert", "vguardstart", "samsungpay", "monimopay", "monimopayauth",
    "shinhan-sr-ansimclick", "smshinhanansimclick", "com.wooricard.wcard", "newsmartpib", "citispay", "citicardappkr", "citimobileapp",
    "cloudpay", "hanawalletmembers", "hdcardappcardansimclick", "smhyundaiansimclick", "shinsegaeeasypayment", "payco", "lpayapp")

internal fun checkoutHtml(checkout: Checkout, description: String): String {
    val gson = Gson()
    val options = gson.toJson(mapOf("method" to "CARD", "amount" to mapOf("currency" to "KRW", "value" to checkout.amount),
        "orderId" to checkout.orderId, "orderName" to description, "successUrl" to checkout.successUrl, "failUrl" to checkout.failUrl))
    return """<!doctype html><html lang="ko"><head><meta name="viewport" content="width=device-width, initial-scale=1">
        <script src="https://js.tosspayments.com/v2/standard"></script></head>
        <body><script>
        window.addEventListener('load', async () => {try{
          const toss=TossPayments(${gson.toJson(checkout.clientKey)});
          await toss.payment({customerKey:TossPayments.ANONYMOUS}).requestPayment($options);
        }catch(e){window.location.replace(${gson.toJson(checkout.failUrl)});}}, {once:true});
        </script></body></html>""".trimIndent()
}
