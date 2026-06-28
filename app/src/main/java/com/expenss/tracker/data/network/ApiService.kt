package com.expenss.tracker.data.network

import retrofit2.http.*

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val message: String, val access_token: String)
data class RegisterRequest(val username: String, val email: String, val password: String, val agreedToTerms: Boolean = true)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val token: String, val newPassword: String)
data class ContactRequest(val email: String, val message: String)
data class ContactResponse(val message: String)
data class SetTrackingModeRequest(val currency: String, val trackingMode: String, val cycleStartDay: Int? = null)

data class UserProfile(
    val userId: Int? = null,
    val username: String,
    val currency: String,
    val trackingMode: String?,
    val cycleStartDay: Int?,
    val monthlyCommitment: Double? = null,
    val onboardingCompleted: Boolean = false
)

data class Expense(
    val id: Int,
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String? = null
)

data class CreateExpenseRequest(
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String? = null
)

data class SetCurrencyRequest(val currency: String)
data class SetCommitmentRequest(val amount: Double)

data class SetBudgetRequest(val month: Int, val year: Int, val amount: Double)
data class SetCategoryBudgetRequest(val month: Int, val year: Int, val category: String, val amount: Double)
data class DeleteCategoryBudgetRequest(val month: Int, val year: Int, val category: String)

data class Budget(
    val id: Int,
    val month: Int,
    val year: Int,
    val amount: Double
)

data class Saving(
    val id: Int,
    val amount: Double,
    val date: String,
    val note: String?
)

data class SavingSummary(
    val totalSavings: Double,
    val avgMonthlySavings: Double,
    val monthlyRemaining: Double,
    val monthlyCommitment: Double
)

data class CreateSavingRequest(
    val amount: Double,
    val date: String,
    val note: String?
)

data class Dream(
    val id: Int,
    val name: String,
    val target_amount: Double,
    val currency: String?
)

data class CreateDreamRequest(
    val name: String,
    val target_amount: Double
)

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun register(@Body body: RegisterRequest): Any

    @POST("auth/forgot-pass")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Any

    @POST("auth/reset-pass")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Any

    @GET("auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Any

    @POST("contact")
    suspend fun sendContact(@Body body: ContactRequest): ContactResponse

    @GET("auth/me")
    suspend fun getMe(): UserProfile

    @PUT("users/setup")
    suspend fun setTrackingMode(@Body body: SetTrackingModeRequest): Any

    @PUT("users/currency")
    suspend fun setCurrency(@Body body: SetCurrencyRequest): Any

    @GET("expenses")
    suspend fun getExpenses(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): List<Expense>

    @GET("expenses/payday")
    suspend fun getPaydayExpenses(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): List<Expense>

    @POST("expenses")
    suspend fun createExpense(@Body body: CreateExpenseRequest): Expense

    @PUT("expenses/{id}")
    suspend fun updateExpense(@Path("id") id: Int, @Body body: CreateExpenseRequest): Expense

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: Int): Any

    @GET("budgets")
    suspend fun getBudget(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Budget?

    @GET("budgets/categories")
    suspend fun getCategoryBudgets(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Map<String, Double>

    @PUT("budgets")
    suspend fun setBudget(@Body body: SetBudgetRequest): Any

    @PUT("budgets/category")
    suspend fun setCategoryBudget(@Body body: SetCategoryBudgetRequest): Any

    @DELETE("budgets")
    suspend fun deleteMonthlyBudget(@Query("month") month: Int, @Query("year") year: Int): Any

    @HTTP(method = "DELETE", path = "budgets/category", hasBody = true)
    suspend fun deleteCategoryBudget(@Body body: DeleteCategoryBudgetRequest): Any

    @GET("savings")
    suspend fun getSavings(): List<Saving>

    @GET("savings/summary")
    suspend fun getSavingsSummary(@Query("monthlyRemaining") monthlyRemaining: Double): SavingSummary

    @POST("savings")
    suspend fun createSaving(@Body body: CreateSavingRequest): Saving

    @DELETE("savings/{id}")
    suspend fun deleteSaving(@Path("id") id: Int): Any

    @PATCH("users/commitment")
    suspend fun setCommitment(@Body body: SetCommitmentRequest): Any

    @GET("dream")
    suspend fun getDream(): Dream?

    @POST("dream")
    suspend fun createDream(@Body body: CreateDreamRequest): Dream

    @DELETE("dream")
    suspend fun deleteDream(): Any
}
