package com.myniyam.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.backend.SupabaseClientProvider
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth

/**
 * Required Google sign-in gate (SP-P3). Sits after Welcome, before onboarding.
 * Uses Supabase compose-auth native Google flow (Credential Manager + nonce).
 */
@Composable
fun SignInScreen(onSignedIn: () -> Unit) {
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val googleAction = SupabaseClientProvider.client.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            busy = false
            when (result) {
                is NativeSignInResult.Success -> onSignedIn()
                is NativeSignInResult.Error -> error = result.message
                is NativeSignInResult.NetworkError -> error = result.message
                is NativeSignInResult.ClosedByUser -> error = null
            }
        }
    )

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.brand_lockup),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    stringResource(R.string.signin_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.signin_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.signin_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(36.dp))
                Button(
                    onClick = { busy = true; error = null; googleAction.startFlow() },
                    enabled = !busy,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(stringResource(R.string.signin_google), style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.signin_privacy_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = NiyamTheme.colors.overlineWarm,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
