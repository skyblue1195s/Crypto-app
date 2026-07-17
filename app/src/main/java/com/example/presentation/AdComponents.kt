package com.example.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay

// AdMob Unit ID for Banner (Using User Publisher ID)
private const val ADMOB_TEST_BANNER_ID = "ca-app-pub-1887434028195350/6300978111"

data class CustomPromoAd(
    val title: String,
    val description: String,
    val callToAction: String,
    val targetUrl: String,
    val colorStart: Color,
    val colorEnd: Color,
    val badgeText: String = "TÀI TRỢ"
)

val promoAdPool = listOf(
    CustomPromoAd(
        title = "Nhận \$BERA Free trên Berachain!",
        description = "Mạng lưới EVM tương thích Proof-of-Liquidity cực nóng đã ra mắt. Nhận faucet bera miễn phí ngay hôm nay.",
        callToAction = "Claim Bera",
        targetUrl = "https://berachain.com",
        colorStart = Color(0xFF8B4513), // Brown Bear
        colorEnd = CryptoGold
    ),
    CustomPromoAd(
        title = "Giao dịch BTC không tốn phí!",
        description = "Tham gia Binance hôm nay và trải nghiệm giao dịch Spot BTC/USDT hoàn toàn miễn phí giao dịch.",
        callToAction = "Giao Dịch",
        targetUrl = "https://binance.com",
        colorStart = Color(0xFF1E1E1E),
        colorEnd = Color(0xFFF0B90B) // Binance Yellow
    ),
    CustomPromoAd(
        title = "Mở Ví Web3 Coinbase Smart Wallet",
        description = "Tạo ví crypto thế hệ mới an toàn tuyệt đối chỉ với mật khẩu vân tay thiết bị (passkey) trong 5 giây.",
        callToAction = "Tạo Ví",
        targetUrl = "https://coinbase.com",
        colorStart = Color(0xFF0052FF), // Coinbase Blue
        colorEnd = Color(0xFF00C6FF)
    ),
    CustomPromoAd(
        title = "Dự đoán xu hướng FOMC với FRED",
        description = "Theo dõi các dữ liệu lạm phát, việc làm & biểu đồ lãi suất Mỹ trực tiếp từ Cục Dự trữ Liên bang St. Louis.",
        callToAction = "Xem Chỉ Số",
        targetUrl = "https://fred.stlouisfed.org",
        colorStart = Color(0xFF0F2027),
        colorEnd = Color(0xFF2C5364)
    )
)

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    marginDp: Int = 8 // Customizable margin/padding parameter
) {
    var isAdmobFailed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Column to wrap the ad with custom margin spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = marginDp.dp, vertical = (marginDp / 2).dp)
            .testTag("ads_container")
    ) {
        if (!isAdmobFailed) {
            // Live AdMob Banner Wrap
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .background(CryptoSurface, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                factory = { ctx ->
                    com.google.android.gms.ads.AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = ADMOB_TEST_BANNER_ID
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                // Fallback immediately if Google Mobile Ads service fails to fetch
                                isAdmobFailed = true
                            }
                        }
                        // Request test ad
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { adView ->
                    // Optional update block
                }
            )
        } else {
            // Elegant premium Custom Fallback Promotional Banner
            CustomPromoBanner()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomPromoBanner() {
    val context = LocalContext.current
    var currentAdIndex by remember { mutableStateOf(0) }

    // Rotate ads every 8 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000)
            currentAdIndex = (currentAdIndex + 1) % promoAdPool.size
        }
    }

    val currentAd = promoAdPool[currentAdIndex]

    // Pulsing gradient border animation
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentAd.targetUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .testTag("custom_promo_banner"),
        colors = CardDefaults.cardColors(containerColor = CryptoSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            currentAd.colorStart.copy(alpha = 0.25f),
                            currentAd.colorEnd.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ad Badge + Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(CryptoGold.copy(alpha = 0.15f * animatedAlpha), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentAd.badgeText,
                        color = CryptoGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Filled.Campaign,
                    contentDescription = "Ad Icon",
                    tint = CryptoGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Copy and detail
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentAd.title,
                    color = CryptoTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentAd.description,
                    color = CryptoTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // CTA Button with dynamic coloring
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentAd.targetUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = currentAd.colorEnd),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(28.dp)
                    .align(Alignment.CenterVertically),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = currentAd.callToAction,
                    color = CryptoDarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = "Go",
                    tint = CryptoDarkBg,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
