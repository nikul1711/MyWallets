package com.wallet.mywallet.ui

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.wallet.mywallet.MyWalletApp
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * The only Activity. It hosts the toolbar, the bottom navigation and the NavHost
 * that swaps the fragments (screens) described in res/navigation/nav_graph.xml.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Android 13+ asks the user before an app may show notifications (bill reminders).
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* nothing to do */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app has a light theme, so keep the status/navigation bar icons dark.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep content out from under the status bar, navigation bar and keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        val authScreens = setOf(R.id.loginFragment, R.id.registerFragment)
        val topLevelScreens = setOf(
            R.id.transactionsFragment,
            R.id.budgetsFragment,
            R.id.reportsFragment,
            R.id.walletsFragment,
            R.id.moreFragment
        )

        setSupportActionBar(binding.toolbar)
        appBarConfiguration = AppBarConfiguration(topLevelScreens)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        // Only the five main screens show the bottom bar; forms get the full screen.
        // The login and register screens have their own headings, so they get no toolbar either.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = destination.id in topLevelScreens
            binding.toolbar.isVisible = destination.id !in authScreens
        }

        routeByLoginState(navController, authScreens)

        askForNotificationPermission()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    /**
     * Shows Login or the home screen depending on the saved session, and keeps watching it:
     * logging in opens the home screen, logging out (or an ended session) opens Login.
     */
    private fun routeByLoginState(navController: NavController, authScreens: Set<Int>) {
        val authRepository = (application as MyWalletApp).container.authRepository
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authRepository.isSignedIn.collect { signedIn ->
                    if (navController.currentDestinationOrNull() == null) {
                        // First start: choose the start screen. (After rotation the graph is restored by itself.)
                        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
                        graph.setStartDestination(if (signedIn) R.id.transactionsFragment else R.id.loginFragment)
                        navController.graph = graph
                    } else {
                        val onAuthScreen = navController.currentDestination?.id in authScreens
                        if (signedIn && onAuthScreen) navController.navigate(R.id.action_global_home)
                        if (!signedIn && !onAuthScreen) navController.navigate(R.id.action_global_login)
                    }
                }
            }
        }
    }

    private fun NavController.currentDestinationOrNull() =
        runCatching { currentDestination }.getOrNull()

    override fun onSupportNavigateUp(): Boolean {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
