package com.wallet.mywallet.db.entity

/**
 * Small enums that are stored in the database.
 * Room saves an enum as its name (for example "EXPENSE"), so the data stays readable.
 * Never rename a value after release without writing a migration.
 */

/** Is the money coming in or going out? */
enum class TransactionType { INCOME, EXPENSE }

/** How often a recurring bill repeats. */
enum class RecurrenceInterval { DAILY, WEEKLY, MONTHLY, YEARLY }

/** Which plan the user is on. FREE is the default. */
enum class PlanType { FREE, PREMIUM_MONTHLY, PREMIUM_YEARLY, LIFETIME }

/** State of the user's subscription. */
enum class SubscriptionStatus { ACTIVE, GRACE_PERIOD, CANCELED, EXPIRED }
