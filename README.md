# MyWallet

A personal-finance app for Android (Kotlin, MVVM, Room). Your financial data is stored locally on the device;
login and register use Supabase Auth.
Minimum Android 8.0 (API 26).

## Features
Wallets, transactions, budgets, savings goals, recurring bills with reminders, reports,
user profile, and a Free / Premium plan model (payments are **not** connected yet).

## How the code is organised

```
com.wallet.mywallet
├── MyWalletApp.kt, AppContainer.kt   starts the app, creates the database + repositories once
├── db/                               EVERYTHING database-related
│   ├── AppDatabase.kt, DatabaseSeeder.kt
│   ├── entity/                       tables (WalletEntity, TransactionEntity, ...)
│   ├── dao/                          all SQL queries
│   └── model/                        results of queries that join tables
├── repository/                       the only layer ViewModels talk to
├── auth/                             AuthRepository: the only class that talks to Supabase Auth
├── billing/                          plans, Free/Premium feature limits
├── reminder/                         daily bill-reminder notifications (WorkManager)
├── ui/                               one folder per screen: Fragment + ViewModel (+ Adapter)
└── util/                             money and date helpers
```

Data flows one way: `Fragment -> ViewModel -> Repository -> DAO -> SQLite`, and database
changes flow back up as Kotlin `Flow`s, so screens update by themselves.

* **Rotation / process death:** ViewModels survive rotation; what the user picked in a form
  is kept in `SavedStateHandle`; typed text is restored by Android.
* **Threading:** Room `suspend` functions and `Flow`s run off the main thread; ViewModels use
  `viewModelScope`. No manual threads.
* **Money** is always stored as `Long` minor units (cents). Use `MoneyFormatter`.

## Login and register (Supabase)
1. Create a project at supabase.com and open **Project Settings -> API**.
2. Put the **Project URL** and **anon key** in `local.properties` (this file is not committed to Git):
   ```
   SUPABASE_URL=https://xxxxxxxx.supabase.co
   SUPABASE_ANON_KEY=eyJ...
   ```
3. Rebuild. Until you replace the placeholders, the login screen shows "Supabase is not set up yet".
4. Optional: for development, switch off **Authentication -> Providers -> Email -> Confirm email** so new
   accounts can log in immediately. With it on, users must click the link in their inbox first.

`MainActivity` watches the saved session: logged out -> Login screen, logged in -> home. Logging out is on
the **More** tab. Wallets, transactions and budgets stay in Room on the device; only the account is in Supabase.

Building from the command line needs JDK 17-21 (`JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew assembleDebug`).

## Free vs Premium
Change `billing/Feature.kt` (`freeLimit`) to make a feature free or paid. `FeatureAccess`
answers "may the user do this?". The Premium screen currently switches plans locally
("test mode"). To add real payments later, replace `PremiumViewModel.choosePlan` with a
Google Play Billing purchase flow and save the result via `SubscriptionRepository`.

## Build
```
./gradlew assembleDebug        # build
./gradlew testDebugUnitTest    # unit tests
```
Needs JDK 17 and the Android SDK (platform 36). Set `sdk.dir` in `local.properties`.
