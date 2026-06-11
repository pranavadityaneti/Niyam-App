package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R

private val APP_CATALOG: List<Pair<String, String>> = listOf(
    "Instagram" to "com.instagram.android",
    "YouTube" to "com.google.android.youtube",
    "Facebook" to "com.facebook.katana",
    "X" to "com.twitter.android",
    "Reddit" to "com.reddit.frontpage",
    "Snapchat" to "com.snapchat.android",
    "TikTok" to "com.zhiliaoapp.musically"
)

@Composable
fun AppsScreen(vm: OnboardingViewModel, onContinue: () -> Unit) {
    val ctx = LocalContext.current
    OnboardingScaffold(
        step = 4,
        title = stringResource(R.string.onb_apps_title),
        ctaEnabled = vm.canContinueFromApps(),
        onContinue = {
            vm.persistApps(ctx)
            onContinue()
        }
    ) {
        APP_CATALOG.forEach { (name, pkg) ->
            SelectableCard(
                text = name,
                selected = pkg in vm.selectedPackages,
                onClick = { vm.togglePackage(pkg) },
                leading = { AppIcon(pkg = pkg, name = name) }
            )
        }
    }
}
