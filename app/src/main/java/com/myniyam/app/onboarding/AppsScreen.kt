package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.AppCatalog

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
        AppCatalog.APPS.forEach { app ->
            SelectableCard(
                text = app.name,
                selected = app.pkg in vm.selectedPackages,
                onClick = { vm.togglePackage(app.pkg) },
                leading = { AppIcon(pkg = app.pkg, name = app.name, logoSlug = app.logoSlug) }
            )
        }
    }
}
