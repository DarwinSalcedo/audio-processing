import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.audio.test.navigation.Screen
import com.audio.test.screen.ProcessScreen
import com.audio.test.screen.SetupScreen
import com.audio.test.screen.SetupViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: SetupViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Setup.route) {
        composable(Screen.Setup.route) {
            SetupScreen(navController, viewModel)
        }

        composable(
            Screen.Process.route
        )
        {
            ProcessScreen("")
        }
    }
}
