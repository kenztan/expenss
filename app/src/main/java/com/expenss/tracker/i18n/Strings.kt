package com.expenss.tracker.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Mirrors the web app's Transloco setup (frontend-expenss/src/app/app.config.ts):
// available langs en/ja/id, default "en", persisted under the same "lang" concept.
enum class AppLang(val code: String, val label: String) {
    EN("en", "EN"),
    JA("ja", "JA"),
    ID("id", "ID");

    companion object {
        fun fromCode(code: String?): AppLang = entries.find { it.code == code } ?: EN
    }
}

object LocaleManager {
    private const val PREFS = "expenss_prefs"
    private const val KEY_LANG = "lang"

    private val _lang = MutableStateFlow(AppLang.EN)
    val lang: StateFlow<AppLang> = _lang

    @Volatile private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val saved = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, null)
        _lang.value = AppLang.fromCode(saved)
    }

    fun set(context: Context, lang: AppLang) {
        _lang.value = lang
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang.code).apply()
    }
}

@Composable
fun currentLang(): AppLang {
    val lang by LocaleManager.lang.collectAsState()
    return lang
}

/** Translate a key for the current locale. Mirrors web's `t('namespace.key')` from Transloco. */
@Composable
fun t(key: String): String = Strings.t(key, currentLang())

/** Translate with `{{ name }}` placeholder substitution, e.g. t("dashboard.pctUsedTxn", "pct" to 42, "count" to 3). */
@Composable
fun t(key: String, vararg args: Pair<String, Any>): String = Strings.t(key, currentLang(), *args)

object Strings {
    fun t(key: String, lang: AppLang, vararg args: Pair<String, Any>): String {
        var s = MAP[key]?.get(lang) ?: MAP[key]?.get(AppLang.EN) ?: key
        args.forEach { (name, value) ->
            s = s.replace("{{ $name }}", value.toString()).replace("{{$name}}", value.toString())
        }
        return s
    }

    private fun s(en: String, ja: String, id: String): Map<AppLang, String> =
        mapOf(AppLang.EN to en, AppLang.JA to ja, AppLang.ID to id)

    // Transcribed verbatim from frontend-expenss/public/assets/i18n/{en,ja,id}.json —
    // only the namespaces the native app actually surfaces (marketing/home pages excluded).
    private val MAP: Map<String, Map<AppLang, String>> = mapOf(

        // ── lang (currently unused directly — AppLang.label covers EN/JA/ID pill labels) ──

        // ── login ──
        "login.title" to s("Welcome back", "おかえりなさい", "Selamat datang kembali"),
        "login.subtitle" to s("Sign in to your Expenss account", "Expenssアカウントにログイン", "Masuk ke akun Expenss Anda"),
        "login.usernameLabel" to s("Username", "ユーザー名", "Nama pengguna"),
        "login.usernamePlaceholder" to s("username", "ユーザー名", "nama pengguna"),
        "login.usernameRequired" to s("Username is required.", "ユーザー名は必須です。", "Nama pengguna wajib diisi."),
        "login.usernameMinLength" to s("At least 3 characters.", "3文字以上入力してください。", "Minimal 3 karakter."),
        "login.passwordLabel" to s("Password", "パスワード", "Kata sandi"),
        "login.forgotPassword" to s("Forgot password?", "パスワードを忘れた？", "Lupa kata sandi?"),
        "login.passwordRequired" to s("Password is required.", "パスワードは必須です。", "Kata sandi wajib diisi."),
        "login.signIn" to s("Sign in", "ログイン", "Masuk"),
        "login.noAccount" to s("Don't have an account?", "アカウントをお持ちでないですか？", "Belum punya akun?"),
        "login.signUp" to s("Sign up", "新規登録", "Daftar"),

        // ── signup ──
        "signup.title" to s("Create your account", "アカウントを作成", "Buat akun Anda"),
        "signup.subtitle" to s("Free forever. No credit card needed.", "永久無料。クレジットカード不要。", "Gratis selamanya. Tidak perlu kartu kredit."),
        "signup.usernameLabel" to s("Username", "ユーザー名", "Nama pengguna"),
        "signup.usernamePlaceholder" to s("username", "ユーザー名", "nama pengguna"),
        "signup.usernameRequired" to s("Username is required.", "ユーザー名は必須です。", "Nama pengguna wajib diisi."),
        "signup.usernameMinLength" to s("At least 3 characters.", "3文字以上入力してください。", "Minimal 3 karakter."),
        "signup.usernameNoSpaces" to s("No spaces allowed.", "スペースは使用できません。", "Spasi tidak diperbolehkan."),
        "signup.emailLabel" to s("Email address", "メールアドレス", "Alamat email"),
        "signup.emailPlaceholder" to s("you@email.com", "you@email.com", "anda@email.com"),
        "signup.emailRequired" to s("Email is required.", "メールアドレスは必須です。", "Email wajib diisi."),
        "signup.emailInvalid" to s("Enter a valid email address.", "有効なメールアドレスを入力してください。", "Masukkan alamat email yang valid."),
        "signup.passwordLabel" to s("Password", "パスワード", "Kata sandi"),
        "signup.passwordPlaceholder" to s("Min. 8 characters", "8文字以上", "Min. 8 karakter"),
        "signup.passwordRequired" to s("Password is required.", "パスワードは必須です。", "Kata sandi wajib diisi."),
        "signup.passwordMinLength" to s("At least 8 characters.", "8文字以上入力してください。", "Minimal 8 karakter."),
        "signup.passwordWeak" to s(
            "Must include uppercase letter and number.",
            "大文字と数字をそれぞれ1文字以上含める必要があります。",
            "Harus mengandung setidaknya satu huruf besar dan satu angka."
        ),
        "signup.strengthWeak" to s("Weak", "弱い", "Lemah"),
        "signup.strengthMedium" to s("Medium", "普通", "Sedang"),
        "signup.strengthStrong" to s("Strong", "強い", "Kuat"),
        "signup.confirmPasswordLabel" to s("Confirm password", "パスワード確認", "Konfirmasi kata sandi"),
        "signup.confirmPasswordPlaceholder" to s("Re-enter your password", "パスワードを再入力", "Masukkan ulang kata sandi"),
        "signup.confirmPasswordRequired" to s("Please confirm your password.", "パスワードを確認してください。", "Silakan konfirmasi kata sandi Anda."),
        "signup.passwordMismatch" to s("Passwords do not match.", "パスワードが一致しません。", "Kata sandi tidak cocok."),
        "signup.termsAgree" to s("I agree to the", "同意します：", "Saya menyetujui"),
        "signup.termsOfService" to s("Terms of Service", "利用規約", "Ketentuan Layanan"),
        "signup.and" to s("and", "と", "dan"),
        "signup.privacyPolicy" to s("Privacy Policy", "プライバシーポリシー", "Kebijakan Privasi"),
        "signup.termsRequired" to s("You must agree to continue.", "利用規約に同意する必要があります。", "Anda harus menyetujui Ketentuan Layanan."),
        "signup.createAccount" to s("Create account", "アカウント作成", "Buat akun"),
        "signup.hasAccount" to s("Already have an account?", "すでにアカウントをお持ちですか？", "Sudah punya akun?"),
        "signup.signIn" to s("Sign in", "ログイン", "Masuk"),
        "signup.checkEmailTitle" to s("Check your email", "メールをご確認ください", "Periksa email Anda"),
        "signup.checkEmailDesc" to s(
            "We sent a verification link to your email address. Click it to activate your account.",
            "確認リンクをメールアドレスに送信しました。リンクをクリックしてアカウントを有効化してください。",
            "Kami telah mengirimkan tautan verifikasi ke alamat email Anda. Klik untuk mengaktifkan akun Anda."
        ),
        "signup.checkEmailHint" to s(
            "Didn't receive it? Check your spam folder.",
            "届いていませんか？迷惑メールフォルダをご確認ください。",
            "Tidak menerima email? Periksa folder spam Anda."
        ),
        "signup.backToLogin" to s("Back to Login", "ログインに戻る", "Kembali ke Masuk"),

        // ── forgotPassword ──
        "forgotPassword.title" to s("Forgot your password?", "パスワードをお忘れですか？", "Lupa kata sandi?"),
        "forgotPassword.subtitle" to s(
            "Enter your email and we'll send you a link to reset your password.",
            "メールアドレスを入力すると、パスワード再設定リンクをお送りします。",
            "Masukkan email Anda dan kami akan mengirimkan tautan untuk mereset kata sandi."
        ),
        "forgotPassword.emailLabel" to s("Email address", "メールアドレス", "Alamat email"),
        "forgotPassword.emailRequired" to s("Email is required.", "メールアドレスを入力してください。", "Email wajib diisi."),
        "forgotPassword.emailInvalid" to s("Enter a valid email address.", "有効なメールアドレスを入力してください。", "Masukkan alamat email yang valid."),
        "forgotPassword.sendLink" to s("Send reset link", "再設定リンクを送信", "Kirim tautan reset"),
        "forgotPassword.checkEmailTitle" to s("Check your email", "メールをご確認ください", "Periksa email Anda"),
        "forgotPassword.checkEmailDesc" to s(
            "We sent a password reset link to your email. Click it to reset your password.",
            "パスワード再設定リンクをメールに送信しました。リンクをクリックしてパスワードを再設定してください。",
            "Kami telah mengirimkan tautan reset kata sandi ke email Anda. Klik untuk mereset kata sandi Anda."
        ),
        "forgotPassword.rememberPassword" to s("Remembered it?", "パスワードを思い出しましたか？", "Sudah ingat kata sandinya?"),
        "forgotPassword.backToLogin" to s("Back to login", "ログインへ戻る", "Kembali ke masuk"),

        // ── resetPassword ──
        "resetPassword.title" to s("Set new password", "新しいパスワードを設定", "Atur kata sandi baru"),
        "resetPassword.subtitle" to s("Choose a strong password for your account.", "アカウントの強力なパスワードを設定してください。", "Pilih kata sandi yang kuat untuk akun Anda."),
        "resetPassword.newPasswordLabel" to s("New password", "新しいパスワード", "Kata sandi baru"),
        "resetPassword.confirmPasswordLabel" to s("Confirm new password", "パスワードの確認", "Konfirmasi kata sandi baru"),
        "resetPassword.passwordRequired" to s("Password is required.", "パスワードを入力してください。", "Kata sandi wajib diisi."),
        "resetPassword.passwordMinLength" to s("At least 8 characters.", "8文字以上必要です。", "Minimal 8 karakter."),
        "resetPassword.passwordWeak" to s(
            "Must include uppercase letter and number.",
            "大文字と数字を少なくとも1つずつ含めてください。",
            "Harus mengandung minimal satu huruf besar dan satu angka."
        ),
        "resetPassword.passwordMismatch" to s("Passwords do not match.", "パスワードが一致しません。", "Kata sandi tidak cocok."),
        "resetPassword.submit" to s("Reset password", "パスワードを再設定", "Reset kata sandi"),
        "resetPassword.successTitle" to s("Password reset!", "パスワードを再設定しました！", "Kata sandi berhasil direset!"),
        "resetPassword.successDesc" to s(
            "Your password has been updated. You can now sign in.",
            "パスワードが更新されました。ログインしてください。",
            "Kata sandi Anda telah diperbarui. Anda sekarang dapat masuk."
        ),
        "resetPassword.goToLogin" to s("Go to login", "ログインへ", "Ke halaman masuk"),
        "resetPassword.invalidTitle" to s("Link invalid or expired", "リンクが無効または期限切れです", "Tautan tidak valid atau kedaluwarsa"),
        "resetPassword.invalidDesc" to s(
            "This password reset link has expired or already been used.",
            "このパスワード再設定リンクは期限切れか、すでに使用済みです。",
            "Tautan reset kata sandi ini telah kedaluwarsa atau sudah digunakan."
        ),
        "resetPassword.requestNew" to s("Request a new link", "新しいリンクを申請", "Minta tautan baru"),
        "resetPassword.noToken" to s("No reset token found in the URL.", "URLにリセットトークンが見つかりません。", "Token reset tidak ditemukan di URL."),

        // ── verifyEmail ──
        "verifyEmail.verifying" to s("Verifying your email...", "メールアドレスを確認中...", "Memverifikasi email Anda..."),
        "verifyEmail.verifyingDesc" to s(
            "Please wait while we confirm your email address.",
            "メールアドレスを確認しています。しばらくお待ちください。",
            "Harap tunggu sementara kami mengonfirmasi alamat email Anda."
        ),
        "verifyEmail.successTitle" to s("Email verified!", "メール確認完了！", "Email terverifikasi!"),
        "verifyEmail.successDesc" to s("Your account is ready. You can now sign in.", "アカウントの準備ができました。ログインしてください。", "Akun Anda siap. Anda sekarang dapat masuk."),
        "verifyEmail.goToLogin" to s("Go to login", "ログインへ", "Ke halaman masuk"),
        "verifyEmail.failedTitle" to s("Verification failed", "確認に失敗しました", "Verifikasi gagal"),
        "verifyEmail.backToSignup" to s("Back to signup", "新規登録へ戻る", "Kembali ke pendaftaran"),
        "verifyEmail.noToken" to s(
            "No verification token found in the URL.",
            "URLに確認トークンが見つかりません。",
            "Token verifikasi tidak ditemukan di URL."
        ),

        // ── onboarding ──
        "onboarding.step0Title" to s("What's your currency?", "通貨を選択してください", "Apa mata uangmu?"),
        "onboarding.step0Subtitle" to s(
            "Choose the currency you use for your daily expenses.",
            "日常の支出に使用する通貨を選んでください。",
            "Pilih mata uang yang kamu gunakan untuk pengeluaran sehari-hari."
        ),
        "onboarding.step1Title" to s("How do you track spending?", "支出の追跡方法を選んでください", "Bagaimana kamu melacak pengeluaran?"),
        "onboarding.step1Subtitle" to s(
            "Pick the mode that matches how you manage money.",
            "お金の管理スタイルに合ったモードを選択してください。",
            "Pilih mode yang sesuai dengan cara kamu mengatur keuangan."
        ),
        "onboarding.monthly" to s("Monthly", "月別", "Bulanan"),
        "onboarding.monthlyDesc" to s("Track by calendar month", "カレンダーの月で追跡", "Lacak per bulan kalender"),
        "onboarding.monthlyExample" to s("Jan 1 → Jan 31", "1月1日 → 1月31日", "1 Jan → 31 Jan"),
        "onboarding.payday" to s("Payday (Recommended)", "給料日 (オススメ)", "Gajian (Direkomendasikan)"),
        "onboarding.paydayDesc" to s("Track from payday to payday", "給料日から次の給料日まで追跡", "Lacak dari gajian ke gajian"),
        "onboarding.paydayExample" to s("e.g. 20th → 19th", "例: 20日 → 19日", "mis. tgl 20 → tgl 19"),
        "onboarding.continue" to s("Continue", "続ける", "Lanjutkan"),
        "onboarding.step2Title" to s("When do you get paid?", "給料日はいつですか？", "Kapan kamu gajian?"),
        "onboarding.step2Subtitle" to s(
            "We'll use this to calculate your spending cycles.",
            "この日付を基に支出サイクルを計算します。",
            "Kami akan menghitung siklus pengeluaranmu dari tanggal ini."
        ),
        "onboarding.dayLabel" to s("Day of the month", "毎月の給料日", "Tanggal gajian"),
        "onboarding.ofEveryMonth" to s("of every month", "日", "setiap bulan"),
        "onboarding.getStarted" to s("Get started", "始める", "Mulai"),
        "onboarding.back" to s("Back", "戻る", "Kembali"),

        // ── errors (exact backend-message mapping, see auth-error.util.ts) ──
        "errors.invalidCredentials" to s("Invalid username or password.", "ユーザー名またはパスワードが正しくありません。", "Nama pengguna atau kata sandi salah."),
        "errors.emailNotVerified" to s(
            "Please verify your email before logging in.",
            "ログインする前にメールアドレスを確認してください。",
            "Harap verifikasi email Anda sebelum masuk."
        ),
        "errors.usernameTaken" to s("Username is already taken.", "このユーザー名はすでに使用されています。", "Nama pengguna sudah digunakan."),
        "errors.emailTaken" to s("Email is already taken.", "このメールアドレスはすでに使用されています。", "Email sudah digunakan."),
        "errors.linkExpired" to s(
            "Verification link has already been used or expired.",
            "認証リンクはすでに使用済みか期限切れです。",
            "Tautan verifikasi sudah digunakan atau kedaluwarsa."
        ),
        "errors.linkInvalid" to s("Invalid or expired verification link.", "無効または期限切れの認証リンクです。", "Tautan verifikasi tidak valid atau kedaluwarsa."),
        "errors.resetLinkExpired" to s(
            "Password reset link has already been used or expired.",
            "パスワード再設定リンクはすでに使用済みか期限切れです。",
            "Tautan reset kata sandi sudah digunakan atau kedaluwarsa."
        ),
        "errors.resetLinkInvalid" to s(
            "Invalid or expired password reset link.",
            "無効または期限切れのパスワード再設定リンクです。",
            "Tautan reset kata sandi tidak valid atau kedaluwarsa."
        ),
        "errors.tooManyRequests" to s(
            "Too many requests. Please wait before trying again.",
            "リクエストが多すぎます。しばらくしてからもう一度お試しください。",
            "Terlalu banyak permintaan. Silakan tunggu sebelum mencoba lagi."
        ),
        "errors.unexpected" to s("Something went wrong. Please try again.", "エラーが発生しました。もう一度お試しください。", "Terjadi kesalahan. Silakan coba lagi."),

        // ── dashboard ──
        "dashboard.nav.dashboard" to s("Dashboard", "ダッシュボード", "Dasbor"),
        "dashboard.nav.goals" to s("Goals", "夢のアイテム", "Target"),
        "dashboard.nav.savings" to s("Savings", "貯蓄", "Tabungan"),
        "dashboard.nav.analytics" to s("Analytics", "分析", "Analitik"),
        "dashboard.budget" to s("Budget", "予算", "Anggaran"),
        "dashboard.spent" to s("Spent", "支出", "Pengeluaran"),
        "dashboard.remaining" to s("Remaining", "残り", "Sisa"),
        "dashboard.used" to s("used", "使用済み", "terpakai"),
        "dashboard.daysLeft" to s("days left", "日残り", "hari tersisa"),
        "dashboard.dailyAvg" to s("Daily avg", "1日平均", "Rata-rata harian"),
        "dashboard.perDay" to s("per day", "/ 日", "per hari"),
        "dashboard.transactions" to s("Transactions", "件の支出", "Transaksi"),
        "dashboard.topCategory" to s("Top category", "最多カテゴリ", "Kategori terbesar"),
        "dashboard.addExpense" to s("Add Expense", "支出を追加", "Tambah Pengeluaran"),
        "dashboard.editExpense" to s("Edit Expense", "支出を編集", "Edit Pengeluaran"),
        "dashboard.manageBudget" to s("Manage Budget", "予算管理", "Kelola Anggaran"),
        "dashboard.expenses" to s("Expenses", "支出一覧", "Pengeluaran"),
        "dashboard.noExpenses" to s("No expenses yet", "まだ支出がありません", "Belum ada pengeluaran"),
        "dashboard.noExpensesDesc" to s(
            "Add your first expense to start tracking your spending.",
            "最初の支出を追加して、家計の記録を始めましょう。",
            "Tambahkan pengeluaran pertama Anda untuk mulai melacak."
        ),
        "dashboard.changePassword" to s("Change password", "パスワード変更", "Ubah kata sandi"),
        "dashboard.contact" to s("Report a bug", "バグ報告", "Laporkan bug"),
        "dashboard.logout" to s("Log out", "ログアウト", "Keluar"),
        "dashboard.language" to s("Language", "言語", "Bahasa"),
        "dashboard.currency" to s("Currency", "通貨", "Mata uang"),
        "dashboard.personal" to s("Personal", "個人", "Pribadi"),
        "dashboard.pctUsedTxn" to s("{{ pct }}% used · {{ count }} txn", "{{ pct }}% 使用済み · {{ count }} 件", "{{ pct }}% digunakan · {{ count }} transaksi"),
        "dashboard.pctOfSpendingTxn" to s(
            "{{ pct }}% of total spending · {{ count }} txn",
            "総支出の {{ pct }}% · {{ count }} 件",
            "{{ pct }}% dari total · {{ count }} transaksi"
        ),
        "dashboard.deleteTitle" to s("Delete expense", "支出を削除", "Hapus pengeluaran"),
        "dashboard.deleteDesc" to s("Are you sure you want to delete", "削除してもよろしいですか？", "Apakah Anda yakin ingin menghapus"),
        "dashboard.cancel" to s("Cancel", "キャンセル", "Batal"),
        "dashboard.delete" to s("Delete", "削除", "Hapus"),
        "dashboard.form.name" to s("Name", "名前", "Nama"),
        "dashboard.form.namePlaceholder" to s("e.g. Lunch", "例：食料品の買い物", "cth. Lunch"),
        "dashboard.form.amount" to s("Amount", "金額", "Jumlah"),
        "dashboard.form.category" to s("Category", "カテゴリ", "Kategori"),
        "dashboard.form.date" to s("Date", "日付", "Tanggal"),
        "dashboard.form.note" to s("Note", "メモ", "Catatan"),
        "dashboard.form.notePlaceholder" to s("Add a note", "任意のメモ...", "Add a note"),
        "dashboard.form.optional" to s("optional", "（任意）", "opsional"),
        "dashboard.form.save" to s("Save", "支出を保存", "Simpan"),
        "dashboard.form.cancel" to s("Cancel", "キャンセル", "Batal"),
        "dashboard.cat.food" to s("Food & Dining", "食費", "Makanan"),
        "dashboard.cat.housing" to s("Housing", "住居", "Tempat Tinggal"),
        "dashboard.cat.transport" to s("Transport", "交通費", "Transportasi"),
        "dashboard.cat.shopping" to s("Shopping", "ショッピング", "Belanja"),
        "dashboard.cat.entertainment" to s("Entertainment", "娯楽", "Hiburan"),
        "dashboard.cat.taxes" to s("Taxes", "税金", "Pajak"),
        "dashboard.cat.investment" to s("Investment", "投資", "Investasi"),
        "dashboard.cat.savings" to s("Savings", "貯蓄", "Tabungan"),
        "dashboard.cat.other" to s("Other", "その他", "Lainnya"),

        // ── goals ──
        "goals.addGoal" to s("Add Goal", "目標を追加", "Tambah Tujuan"),
        "goals.editGoal" to s("Edit Goal", "目標を編集", "Edit Tujuan"),
        "goals.saveGoal" to s("Save Goal", "目標を保存", "Simpan Tujuan"),
        "goals.updateGoal" to s("Update Goal", "目標を更新", "Perbarui Tujuan"),
        "goals.emptyTitle" to s("No dream goal set", "目標がありません", "Belum ada tujuan"),
        "goals.emptyDesc" to s(
            "Set a savings target and track your progress",
            "貯蓄目標を設定して進捗を管理しましょう。",
            "Tetapkan tujuan tabungan dan pantau perkembanganmu."
        ),
        "goals.savingsPool" to s("Savings pool", "貯蓄プール", "Dana Tabungan"),
        "goals.fromSavingsCategory" to s("From savings category", "貯蓄カテゴリより", "Dari kategori tabungan"),
        "goals.funded" to s("Funded", "達成", "Tercapai"),
        // Web derives this via `t('goals.ofTarget').replace('of','').trim() || 'of goal'` —
        // transcribed as the literal rendered result per locale (en falls back to "of goal").
        "goals.ringSubLabel" to s("of goal", "/", "dari"),
        "goals.goalName" to s("Goal name", "目標名", "Nama Tujuan"),
        "goals.namePlaceholder" to s("e.g. New laptop", "例：緊急資金", "cth. New laptop"),
        "goals.nameRequired" to s("Name is required.", "名前は必須です。", "Nama wajib diisi."),
        "goals.targetAmount" to s("Target amount", "目標金額", "Target Jumlah"),
        "goals.amountRequired" to s("Enter a valid amount.", "金額は必須です。", "Masukkan jumlah yang valid."),
        "goals.deleteTitle" to s("Delete Goal", "目標を削除", "Hapus Tujuan"),
        "goals.deleteDesc" to s("Are you sure you want to delete", "本当に削除してもよろしいですか？", "Apakah kamu yakin ingin menghapus"),
        "goals.dreamItemLabel" to s("Dream Goal", "夢のアイテム", "Impian"),
        "goals.saved" to s("Saved", "貯蓄済み", "Tersimpan"),
        "goals.target" to s("Target", "目標金額", "Target"),
        "goals.remaining" to s("Remaining", "残り", "Sisa"),
        "goals.estTimeLabel" to s("Estimated completion", "目標達成までの推定期間：", "Estimasi waktu mencapai tujuan"),
        "goals.estTimeHint" to s(
            "Based on your average monthly savings",
            "月平均貯蓄額に基づく · 目安です",
            "Berdasarkan rata-rata tabungan bulanan"
        ),

        // ── savings ──
        "savings.totalSavings" to s("Total Savings", "総貯蓄額", "Total Tabungan"),
        "savings.includesRemaining" to s("Includes remaining", "今月の残高を含む", "Termasuk sisa bulan ini"),
        "savings.thisMonthRemaining" to s("This Month Remaining", "今月の残高", "Sisa Bulan Ini"),
        "savings.autoFromBudget" to s("Auto from budget", "現在のサイクルにおける今月の残り予算", "Otomatis dari anggaran"),
        "savings.monthlyCommitment" to s("Monthly Commitment", "月間コミットメント", "Komitmen Bulanan"),
        "savings.commitmentDesc" to s(
            "How much do you plan to save each month? This helps estimate when you'll reach your goals.",
            "毎月いくら貯蓄する予定ですか？目標達成時期の見積もりに使用されます。",
            "Berapa banyak yang kamu rencanakan untuk ditabung setiap bulan? Ini membantu memperkirakan kapan kamu akan mencapai tujuan."
        ),
        "savings.avgMonthlySavings" to s("Avg Monthly Savings", "月平均貯蓄額", "Rata-rata Tabungan Bulanan"),
        "savings.avgDesc" to s("Historical average", "全記録をもとにした月平均貯蓄額", "Rata-rata historis"),
        "savings.logSavings" to s("Log Savings", "貯蓄を記録", "Catat Tabungan"),
        "savings.amount" to s("Amount", "金額", "Jumlah"),
        "savings.date" to s("Date", "日付", "Tanggal"),
        "savings.note" to s("Note", "メモ", "Catatan"),
        "savings.notePlaceholder" to s("e.g. Bonus from work", "例：仕事のボーナス", "cth: Bonus dari pekerjaan"),
        "savings.addRecord" to s("Add Record", "記録を追加", "Tambah Catatan"),
        "savings.history" to s("History", "貯蓄履歴", "Riwayat"),
        "savings.emptyTitle" to s("No savings records yet", "まだ記録がありません", "Belum ada catatan"),
        "savings.emptyDesc" to s(
            "Log your first savings to get started",
            "貯蓄を記録して進捗を確認しましょう。",
            "Mulai mencatat tabunganmu untuk memantau kemajuan."
        ),
        "savings.savingsDeposit" to s("Savings Deposit", "貯蓄入金", "Setoran Tabungan"),
        "savings.deleteTitle" to s("Delete Record", "記録を削除", "Hapus Catatan"),
        "savings.deleteDesc" to s(
            "Are you sure you want to delete this record of",
            "この記録を削除してもよろしいですか？",
            "Apakah kamu yakin ingin menghapus catatan ini sebesar"
        ),
        "savings.setCommitment" to s("Set Monthly Commitment", "月間コミットメントを設定", "Atur Komitmen Bulanan"),
        "savings.monthlyAmount" to s("Monthly amount", "月額", "Jumlah Bulanan"),
        "savings.save" to s("Save", "保存", "Simpan"),

        // ── analytics ──
        "analytics.heroTitle" to s("Your Financial Insights", "財務インサイト", "Wawasan Keuangan Kamu"),
        "analytics.heroSubtitle" to s(
            "Get a deeper look at your spending patterns, category trends, and monthly comparisons.",
            "支出パターン、カテゴリ別のトレンド、月別比較を詳しく確認しましょう。",
            "Lihat lebih dalam pola pengeluaran, tren kategori, dan perbandingan bulanan Anda."
        ),
        "analytics.spendingOverTime" to s("Spending Over Time", "支出の推移", "Pengeluaran dari Waktu ke Waktu"),
        "analytics.categoryBreakdown" to s("Category Breakdown", "カテゴリ別内訳", "Rincian Kategori"),
        "analytics.topCategories" to s("Top Categories", "トップカテゴリ", "Kategori Teratas"),
        "analytics.monthlyComparison" to s("Monthly Comparison", "月次比較", "Perbandingan Bulanan"),
        "analytics.noData" to s("No expense data yet", "まだ支出データがありません", "Belum ada data pengeluaran"),
        "analytics.last6Months" to s("Last 6 months", "過去6ヶ月", "6 bulan terakhir"),

        // ── legal.contact ──
        "legal.contactTitle" to s("Contact the Developer", "開発者へのお問い合わせ", "Hubungi Pengembang"),
        "legal.contactIntro" to s(
            "Have a question, found a bug, or just want to say hi? Reach out below — I will respond as soon as I can.",
            "ご質問やバグ報告、ちょっとした挨拶でも構いません。下記よりご連絡ください。できるだけ早く返信いたします。",
            "Punya pertanyaan, menemukan bug, atau hanya ingin menyapa? Hubungi saya di bawah ini — saya akan merespons secepat mungkin."
        ),
        "legal.yourEmail" to s("Your Email", "メールアドレス", "Email Anda"),
        "legal.emailPlaceholder" to s("you@example.com", "you@example.com", "you@example.com"),
        "legal.emailRequired" to s("Email is required", "メールアドレスは必須です", "Email wajib diisi"),
        "legal.emailInvalid" to s("Enter a valid email", "有効なメールアドレスを入力してください", "Masukkan email yang valid"),
        "legal.messageLabel" to s("Message", "メッセージ", "Pesan"),
        "legal.messagePlaceholder" to s("What's on your mind?", "ご用件をお書きください", "Apa yang ingin Anda sampaikan?"),
        "legal.messageRequired" to s("Message is required", "メッセージは必須です", "Pesan wajib diisi"),
        "legal.messageTooLong" to s("Message is too long (max 1000 characters)", "メッセージが長すぎます（最大1000文字）", "Pesan terlalu panjang (maks 1000 karakter)"),
        "legal.sendMessage" to s("Send Message", "送信する", "Kirim Pesan"),
        "legal.sending" to s("Sending...", "送信中...", "Mengirim..."),
    )
}
