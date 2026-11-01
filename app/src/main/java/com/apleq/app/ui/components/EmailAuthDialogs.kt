package com.apleq.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * Pantalla de verificación del correo: no se puede cerrar tocando fuera.
 * "Ya lo he verificado", "Reenviar correo" y "Usar otra cuenta".
 */
@Composable
fun VerifyEmailDialog(
    email: String,
    onCheckVerified: (onResult: (Boolean) -> Unit) -> Unit,
    onResend: (onResult: (Boolean) -> Unit) -> Unit,
    onUseOtherAccount: () -> Unit
) {
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { },
        title = { Text(I18n.verifyEmailTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(I18n.verifyEmailBody(email), style = MaterialTheme.typography.bodyMedium)
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (messageIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        message = null
                        onResend { ok ->
                            busy = false
                            messageIsError = !ok
                            message = if (ok) I18n.verifyEmailResent else I18n.verifyEmailResendFailed
                        }
                    }
                ) { Text(I18n.verifyEmailResend) }
                TextButton(
                    enabled = !busy,
                    onClick = onUseOtherAccount
                ) { Text(I18n.verifyEmailUseOther) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    busy = true
                    message = null
                    onCheckVerified { ok ->
                        busy = false
                        if (!ok) {
                            messageIsError = true
                            message = I18n.verifyEmailNotYet
                        }
                    }
                }
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.height(16.dp).width(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(I18n.verifyEmailDone)
            }
        }
    )
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

