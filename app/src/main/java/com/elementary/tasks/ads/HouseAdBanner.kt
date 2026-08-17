package com.elementary.tasks.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.feature.settings.proversion.rememberGooglePlayMarketLauncher
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.ProBuyClickedEvent
import com.github.naz013.platform.SystemInfo
import org.koin.compose.koinInject

/**
 * Free, self-promoted "PRO" creative shown in an existing banner slot instead of a network ad
 * every Nth refresh (see RotatingBannerAdsProvider) - a zero-cash-cost upsell touchpoint that
 * trades a slice of ad revenue on those impressions for a direct Pro nudge.
 */
@Composable
fun HouseAdBanner(modifier: Modifier = Modifier) {
  val analyticsEventSender = koinInject<AnalyticsEventSender>()
  val googlePlayMarketLauncher = rememberGooglePlayMarketLauncher()

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp))
        .clickable {
          analyticsEventSender.send(ProBuyClickedEvent)
          googlePlayMarketLauncher.launch(
            packageName = SystemInfo.PRO_PACKAGE_NAME,
            referrer = "utm_source=free_app&utm_medium=house_ad",
          )
        }.padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(R.string.house_ad_message),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onTertiaryContainer,
    )
  }
}
