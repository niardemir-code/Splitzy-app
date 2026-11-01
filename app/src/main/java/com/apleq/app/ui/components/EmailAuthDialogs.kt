package com.apleq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.apleq.app.data.util.evaluatePassword
import com.apleq.app.ui.util.I18n

/**
 * Diálogo de REGISTRO con correo: nombre + email + contraseña + repetir contraseña.
 */
@Composable
fun EmailRegisterDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onRegister: (name: String, email: String, password: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val passwordsMatch = confirmPassword.isEmpty() || password == confirmPassword
    val rules = evaluatePassword(password)
    val canSubmit = name.trim().isNotEmpty() &&
        email.isNotBlank() &&
        rules.meets &&
        password == confirmPassword &&
        !isLoading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(I18n.emailRegisterTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(I18n.accountNameLabel) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(I18n.emailLabel) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(I18n.passwordLabel) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    supportingText = {
                        Column {
                            PasswordRuleLine(I18n.passwordRuleLength, rules.length, password.isNotEmpty())
                            PasswordRuleLine(I18n.passwordRuleLetter, rules.hasLetter, password.isNotEmpty())
                            PasswordRuleLine(I18n.passwordRuleNumber, rules.hasNumber, password.isNotEmpty())
                            if (rules.basicsMet && !rules.notCommon) {
                                PasswordRuleLine(I18n.passwordRuleNotCommon, false, true)
                            }
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(I18n.confirmPasswordLabel) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    isError = !passwordsMatch,
                    supportingText = {
                        if (!passwordsMatch) Text(I18n.passwordsDoNotMatch)
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = { onRegister(name.trim(), email.trim(), password) }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(16.dp).width(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isLoading) I18n.emailCreating else I18n.emailCreateMyAccount)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(I18n.cancel) }
        }
    )
}

/**
 * Diálogo de INICIO DE SESIÓN con correo: solo email + contraseña.
 */
@Composable
fun EmailLoginDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onForgotPassword: (email: String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(I18n.emailLoginTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(I18n.emailLabel) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(I18n.passwordLabel) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = { onForgotPassword(email.trim()) },
                    enabled = !isLoading
                ) {
                    Text(I18n.forgotPasswordLink)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                onClick = { onLogin(email.trim(), password) }
            ) {
                Text(if (isLoading) I18n.emailLoggingIn else I18n.tabSignIn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text(I18n.cancel) }
        }
    )
}

@Composable
private fun PasswordRuleLine(text: String, ok: Boolean, show: Boolean) {
    val color = when {
        !show -> MaterialTheme.colorScheme.onSurfaceVariant
        ok -> androidx.compose.ui.graphics.Color(0xFF16A34A)
        else -> MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Icon(
            imageVector = if (show && !ok) Icons.Default.Close else Icons.Default.Check,
            contentDescription = null,
            tint = color,
            modifier = Modifier.height(14.dp).width(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = text, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

/**
 * Pantalla de verificación del correo: no se puede cerrar tocando fuera ni con «atrás».
 * «Ya lo he verificado», «Reenviar correo» y «Usar otra cuenta».
 */
@Composable
fun VerifyEmailDialog(
    email: String,
    onCheckVerified: (onResult: (Boolean) -> Unit) -> Unit,
    onResend: (onResult: (Boolean) -> Unit) -> Unit,
    onUseOtherAccount: () -> Unit
) {
    var busy by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icono dentro de un círculo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = I18n.verifyEmailTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Correo destacado, en su propia «píldora»
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = I18n.verifyEmailInstructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = if (messageIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Acción principal
                Button(
                    onClick = {
                        busy = true
                        checking = true
                        message = null
                        onCheckVerified { ok ->
                            busy = false
                            checking = false
                            if (!ok) {
                                messageIsError = true
                                message = I18n.verifyEmailNotYet
                            }
                        }
                    },
                    enabled = !busy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (checking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(10.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(I18n.verifyEmailDone, fontWeight = FontWeight.SemiBold)
                }

                // Reenviar
                OutlinedButton(
                    onClick = {
                        busy = true
                        message = null
                        onResend { ok ->
                            busy = false
                            messageIsError = !ok
                            message = if (ok) I18n.verifyEmailResent else I18n.verifyEmailResendFailed
                        }
                    },
                    enabled = !busy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(I18n.verifyEmailResend)
                }

                // Otra cuenta
                TextButton(
                    onClick = onUseOtherAccount,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(I18n.verifyEmailUseOther)
                }
            }
        }
    }
}

/** Pide el correo y envía el enlace para cambiar la contraseña. */
@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onSend: (email: String, onResult: (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var busy by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(I18n.forgotPasswordTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (sent) {
                    Text(I18n.forgotPasswordSent, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(I18n.forgotPasswordBody, style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(I18n.emailLabel) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (sent) {
                TextButton(onClick = onDismiss) { Text(I18n.close) }
            } else {
                TextButton(
                    enabled = email.isNotBlank() && !busy,
                    onClick = {
                        busy = true
                        onSend(email.trim()) { ok ->
                            busy = false
                            // Mismo mensaje exista o no la cuenta; solo falla si no hay conexión.
                            if (ok) sent = true
                        }
                    }
                ) { Text(I18n.forgotPasswordSend) }
            }
        },
        dismissButton = {
            if (!sent) TextButton(onClick = onDismiss, enabled = !busy) { Text(I18n.cancel) }
        }
    )
}

