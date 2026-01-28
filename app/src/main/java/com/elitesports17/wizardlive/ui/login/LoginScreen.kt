package com.elitesports17.wizardlive.ui.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elitesports17.wizardlive.R
import com.elitesports17.wizardlive.ui.util.setAppLocale
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.elitesports17.wizardlive.ui.util.UserSession
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import android.util.Log
import androidx.compose.ui.geometry.Offset

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val api = com.elitesports17.wizardlive.data.remote.RetrofitClient.api
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) }
    var rememberMe by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    val forgotViewModel = remember {
        ForgotPasswordViewModel(api)
    }
    val forgotState by forgotViewModel.state.collectAsState()
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel()

    val registerViewModel = remember {
        com.elitesports17.wizardlive.ui.login.RegisterViewModel(api)
    }
    val registerState by registerViewModel.state.collectAsState()
    var isRegisterError by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var registerSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {

                val success = uiState as LoginUiState.Success

                Log.d("LOGIN_UI", "Login SUCCESS | token=${success.token.take(10)}...")

                UserSession.saveSession(
                    context = context,
                    token = success.token,
                    userId = success.userId,
                    role = success.role,
                    rememberMe = rememberMe
                )

                onLoginSuccess()
            }

            is LoginUiState.Error -> {
                val rawMessage = (uiState as LoginUiState.Error).message

                errorMessage = when {
                    rawMessage.contains("401") ||
                            rawMessage.contains("Unauthorized", ignoreCase = true) -> {
                        context.getString(R.string.error_invalid_credentials)
                    }

                    else -> {
                        context.getString(R.string.error_generic)
                    }
                }


                showErrorDialog = true
                Log.e("LOGIN_UI", "Login ERROR: $rawMessage")
            }


            else -> Unit
        }
    }

    LaunchedEffect(forgotState) {
        if (forgotState is ForgotPasswordState.Success) {
            kotlinx.coroutines.delay(1500)
            showForgotDialog = false
        }
    }

    LaunchedEffect(registerState) {
        when (registerState) {

            is RegisterUiState.Success -> {
                registerSuccess = true
                showRegisterDialog = true

                // limpiamos campos
                username = ""
                password = ""
                confirmPassword = ""
            }

            is RegisterUiState.Error -> {
                registerSuccess = false
                showRegisterDialog = true
            }

            else -> Unit
        }
    }


    // 🔴 Animación del punto LIVE
    val infiniteTransition = rememberInfiniteTransition(label = "liveDot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )


    Box(modifier = Modifier.fillMaxSize()) {

        LoginAnimatedBackground()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🧙 LOGO
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp)
                )

                // 📝 Wizard + 🔴 Live
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Wizard",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(18.dp))

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = dotAlpha))
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Live",
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 🪟 CARD LOGIN
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF1A1A1D).copy(alpha = 0.9f))
                        .padding(20.dp)
                ) {

                    if (!isLogin) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            enabled = uiState !is LoginUiState.Loading,
                            label = { Text(stringResource(R.string.username)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFB983FF),
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color(0xFFB983FF),
                                focusedLabelColor = Color(0xFFB983FF),
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }


                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        enabled = uiState !is LoginUiState.Loading,
                        label = { Text(stringResource(R.string.email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFB983FF),
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color(0xFFB983FF),
                            focusedLabelColor = Color(0xFFB983FF),
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        enabled = uiState !is LoginUiState.Loading,
                        label = { Text(stringResource(R.string.password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { showPassword = !showPassword }
                            ) {
                                Icon(
                                    imageVector = if (showPassword)
                                        Icons.Filled.VisibilityOff
                                    else
                                        Icons.Filled.Visibility,
                                    contentDescription = null, // sin strings
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFB983FF),
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Color(0xFFB983FF),
                            focusedLabelColor = Color(0xFFB983FF),
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    if (!isLogin) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            enabled = uiState !is LoginUiState.Loading,
                            label = { Text(stringResource(R.string.confirm_password)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFB983FF),
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color(0xFFB983FF),
                                focusedLabelColor = Color(0xFFB983FF),
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // ✅ RECUÉRDAME (IZQUIERDA)
                        if (isLogin) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    modifier = Modifier.padding(0.dp) // 👈 elimina padding extra
                                )

                                Spacer(modifier = Modifier.width(0.dp)) // 👈 mucho más pegado

                                Text(
                                    text = stringResource(R.string.remember_me),
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // ❓ OLVIDÉ MI CONTRASEÑA (DERECHA)
                        if (isLogin) {
                            Text(
                                text = stringResource(R.string.forgot_password),
                                color = Color(0xFFB983FF),
                                fontSize = 11.sp,
                                maxLines = 1,              // 👈 fuerza una sola línea
                                softWrap = false,          // 👈 evita salto
                                modifier = Modifier
                                    .clickable {
                                        forgotEmail = ""
                                        showForgotDialog = true
                                    }
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(25.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() ||
                                        password.isBlank() ||
                                        (!isLogin && username.isBlank()) -> {
                                    errorMessage = context.getString(R.string.error_missing_fields)
                                    isRegisterError = !isLogin
                                    showErrorDialog = true

                                }

                                !isLogin && password.length < 8 -> {
                                    errorMessage = context.getString(R.string.error_password_length)
                                    showErrorDialog = true
                                    isRegisterError = true
                                }

                                !isLogin && password != confirmPassword -> {
                                    errorMessage =
                                        context.getString(R.string.error_password_mismatch)
                                    showErrorDialog = true
                                    isRegisterError = true
                                }

                                isLogin -> {
                                    viewModel.login(email.trim(), password)
                                }

                                else -> {
                                    registerViewModel.register(
                                        username = username.trim(),
                                        email = email.trim(),
                                        password = password,
                                        confirmPassword = confirmPassword
                                    )
                                }
                            }
                        },


                        enabled = uiState !is LoginUiState.Loading,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF7B2FF7),
                                            Color(0xFFE040FB)
                                        )
                                    ),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState is LoginUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = if (isLogin)
                                        stringResource(R.string.login)
                                    else
                                        stringResource(R.string.register),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isLogin)
                            stringResource(R.string.no_account)
                        else
                            stringResource(R.string.have_account),
                        color = Color(0xFFB983FF),
                        fontSize = 12.sp,//fontsize
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { isLogin = !isLogin }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.terms),
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    softWrap = false,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )


                Spacer(modifier = Modifier.height(32.dp))


            }

            LanguageSelector(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) { lang ->
                setAppLocale(context, lang)
            }

        }




        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                containerColor = Color(0xFF1A1A1D),
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(
                        text = if (isRegisterError)
                            stringResource(R.string.register_failed_title)
                        else
                            stringResource(R.string.login_failed_title),
                        color = Color(0xFFB983FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },

                text = {
                    Text(
                        text = errorMessage,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF7B2FF7),
                                            Color(0xFFE040FB)
                                        )
                                    )
                                )
                                .clickable { showErrorDialog = false }
                                .padding(horizontal = 22.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "OK",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            )
        }

        if (showRegisterDialog) {
            AlertDialog(
                onDismissRequest = { showRegisterDialog = false },
                containerColor = Color(0xFF1A1A1D),
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(
                        text = if (registerSuccess)
                            stringResource(R.string.register_success_title)
                        else
                            stringResource(R.string.register_failed_title),
                        color = Color(0xFFB983FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = if (registerSuccess)
                            stringResource(R.string.register_success_message)
                        else
                            stringResource(R.string.register_failed_message),
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF7B2FF7),
                                            Color(0xFFE040FB)
                                        )
                                    )
                                )
                                .clickable {
                                    showRegisterDialog = false
                                    if (registerSuccess) {
                                        isLogin = true
                                    }
                                }
                                .padding(horizontal = 22.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "OK",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            )
        }

        if (showForgotDialog) {
            AlertDialog(
                onDismissRequest = { showForgotDialog = false },
                containerColor = Color(0xFF1A1A1D),
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(
                        text = stringResource(R.string.forgot_password),
                        color = Color(0xFFB983FF),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text(stringResource(R.string.email)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFB983FF),
                                unfocusedBorderColor = Color.DarkGray,
                                cursorColor = Color(0xFFB983FF),
                                focusedLabelColor = Color(0xFFB983FF),
                                unfocusedLabelColor = Color.Gray
                            )
                        )


                        Spacer(Modifier.height(8.dp))

                        when (forgotState) {
                            is ForgotPasswordState.Error -> {
                                Text(
                                    text = (forgotState as ForgotPasswordState.Error).message,
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }

                            is ForgotPasswordState.Success -> {
                                Text(
                                    text = (forgotState as ForgotPasswordState.Success).message,
                                    color = Color.Green,
                                    fontSize = 12.sp
                                )
                            }

                            else -> Unit
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            forgotViewModel.recover(forgotEmail.trim())
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.send),
                            color = Color(0xFFB983FF),
                            fontWeight = FontWeight.SemiBold
                        )

                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotDialog = false }) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = Color.LightGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }


    }

}

@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val currentLang = Locale.getDefault().language
    val languageLabel = when (currentLang) {
        "es" -> "Español"
        "en" -> "English"
        else -> "English"
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1A1A1D).copy(alpha = 0.85f))
                .clickable { expanded = true }
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = "Language",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = languageLabel,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Español") },
                onClick = {
                    expanded = false
                    onLanguageSelected("es")
                }
            )
            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    expanded = false
                    onLanguageSelected("en")
                }
            )
        }
    }
}





