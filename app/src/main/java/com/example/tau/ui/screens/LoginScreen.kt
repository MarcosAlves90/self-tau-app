package com.example.tau.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.tau.data.api.RetrofitClient
import com.example.tau.data.local.UserDao
import com.example.tau.data.model.User
import com.example.tau.ui.Strings
import com.example.tau.ui.components.AuthField
import com.example.tau.ui.components.AuthTextField
import com.example.tau.ui.components.NavTopBar
import com.example.tau.ui.components.LoadingDialog
import com.example.tau.ui.components.ErrorDialog
import com.example.tau.ui.theme.Dimensions
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    val isFormValid = email.isNotBlank() && password.isNotBlank() && isValidEmail(email)

    fun getValidationMessage(): String {
        return when {
            email.isNotBlank() && !isValidEmail(email) -> "Email inválido"
            email.isNotBlank() && password.isBlank() -> "Senha é obrigatória"
            email.isBlank() -> "Preencha os campos para continuar"
            else -> ""
        }
    }

    val validationMessage = getValidationMessage()

    fun handleLogin() {
        when {
            email.isBlank() -> {
                errorMessage = "Por favor, preencha o email"
                return
            }
            !isValidEmail(email) -> {
                errorMessage = "Por favor, insira um email válido"
                return
            }
            password.isBlank() -> {
                errorMessage = "Por favor, preencha a senha"
                return
            }
        }

        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.login(User(email, password))
                if (response.isSuccessful) {
                    response.body()?.id?.let { userId ->
                        UserDao(context).saveUserId(userId)

                        val disciplineRepository = com.example.tau.data.repository.DisciplineRepository(context)
                        val scheduleRepository = com.example.tau.data.repository.ScheduleRepository(context)
                        val taskRepository = com.example.tau.data.repository.TaskRepository(context)

                        try {
                            disciplineRepository.syncDisciplinesFromRemote(userId)
                            scheduleRepository.syncSchedulesFromRemote(userId)
                            taskRepository.syncTasksFromRemote(userId)
                            taskRepository.syncTasksToRemote(userId)
                        } catch (_: Exception) {}
                    }
                    onLoginSuccess()
                } else {
                    errorMessage = when (response.code()) {
                        401 -> "Email ou senha inválidos"
                        404 -> "Usuário não encontrado"
                        500 -> "Erro no servidor. Tente novamente mais tarde"
                        else -> "Erro ao fazer login: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão: ${e.message ?: "Tente novamente"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NavTopBar(
                title = "Login",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = Strings.LOGIN_TITLE,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            AuthTextField(field = AuthField(label = Strings.EMAIL_LABEL, value = email, onValueChange = { email = it }))
            Spacer(modifier = Modifier.height(16.dp))
            AuthTextField(
                field = AuthField(
                    label = Strings.PASSWORD_LABEL,
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Lock else Icons.Filled.Lock,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Texto de validação
            Text(
                text = validationMessage,
                fontSize = 14.sp,
                color = if (validationMessage.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(20.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { handleLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(Strings.LOGIN_BUTTON, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Strings.LOGIN_LINK,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    if (isLoading) {
        LoadingDialog(
            title = "Entrando...",
            message = "Aguarde enquanto validamos suas credenciais"
        )
    }

    if (errorMessage != null) {
        ErrorDialog(
            title = "Erro no Login",
            message = errorMessage ?: "Erro desconhecido",
            onDismiss = { errorMessage = null },
            buttonText = "Tentar Novamente"
        )
    }
}
