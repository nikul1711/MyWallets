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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.ActivityMainBinding

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
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible = destination.id in topLevelScreens
        }

        askForNotificationPermission()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

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
