# Expenss Android

Native Android app for Expenss, a personal finance tracker for managing expenses, budgets, savings, and goals.

The web version is available at https://expenss.online.

---

## Overview

Expenss helps you take control of your personal finances. You can track daily expenses by category, set monthly or per-category budgets, log savings, and monitor progress toward a financial goal. The app supports two tracking modes — standard monthly and paycycle — so it works regardless of when your pay arrives.

---

## Features

**Expense Tracking**
- Add, edit, and delete expenses with name, amount, category, date, and optional note
- Filter expenses by category
- View daily average spend and days remaining in the period

**Budget Management**
- Set a monthly budget and track how much you have spent and remaining
- Set per-category budgets alongside the monthly total
- Visual progress ring showing percentage used

**Savings**
- Log savings records with date and note
- Set a monthly savings commitment target
- Automatic calculation of remaining budget carried into savings
- Historical average monthly savings

**Goals**
- Set a dream goal with a target amount
- Track progress using your total savings pool
- Estimated completion time based on average monthly savings

**Tracking Modes**
- Monthly: standard calendar month navigation with previous/next controls
- Paycycle: budget period starts on a custom day of the month, independent of the calendar

**Multi-currency**
- USD, JPY, and IDR supported
- Currency formatting matches each locale

**Analytics**
- Coming soon

---

## Download

Pre-built APK available on the [Releases](../../releases) page.

Requires Android 7.0 (API 24) or higher.

---

## Build from Source

Requirements: Android Studio, JDK 11 or higher.

```bash
git clone https://github.com/kenztan/expenss.git
cd expenss
./gradlew assembleDebug
```

The APK will be output to `app/build/outputs/apk/debug/app-debug.apk`.

---

## Account

Accounts are shared with the web app at https://expenss.online. Register there or directly in the Android app. All data is synced via the API.

---

## License

See [LICENSE](LICENSE).
