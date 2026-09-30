package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FoodDatabase
import com.example.data.model.AvailabilityFilter
import com.example.data.model.DietaryFilter
import com.example.data.model.Donation
import com.example.data.model.FoodItem
import com.example.data.model.FoodNeedRequest
import com.example.data.model.FoodStatus
import com.example.data.model.MealTypeFilter
import com.example.data.model.ReceiverProfile
import com.example.data.model.ReceiverType
import com.example.data.model.Recipe
import com.example.data.model.RecipeMatch
import com.example.data.model.User
import com.example.data.model.WasteLog
import com.example.data.remote.GeminiRecipeService
import com.example.data.repository.AuthRepository
import com.example.data.repository.DonationRepository
import com.example.data.repository.FoodRepository
import com.example.data.repository.RecipeRepository
import com.example.data.repository.WasteImpactStats
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    DONOR("Donor"),
    RECOVER("Receiver"),
    DELIVERY("Tracker"),
    PANTRY("Pantry"),
    IMPACT("Impact")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FoodDatabase.getDatabase(application)
    val authRepository = AuthRepository(database.userDao(), application)
    val foodRepository = FoodRepository(database.foodItemDao(), database.wasteLogDao(), database.donationDao())
    val recipeRepository = RecipeRepository(database.recipeDao())
    val donationRepository = DonationRepository(database.donationDao(), database.wasteLogDao())

    // Current Tab
    private val _currentTab = MutableStateFlow(AppTab.DONOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Auth State
    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // Notification toast / snackbar
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Inventory State
    private val _activeUserId = MutableStateFlow(1L)

    val inStockItems: StateFlow<List<FoodItem>> = _activeUserId.combine(currentUser) { activeId, user ->
        user?.id ?: activeId
    }.let { userIdFlow ->
        // Dynamically collect in-stock items
        foodRepository.getInStockItems(1L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recipe Recommendation Engine Filters
    private val _recipeSearchQuery = MutableStateFlow("")
    val recipeSearchQuery: StateFlow<String> = _recipeSearchQuery.asStateFlow()

    private val _selectedDietaryFilter = MutableStateFlow(DietaryFilter.ALL)
    val selectedDietaryFilter: StateFlow<DietaryFilter> = _selectedDietaryFilter.asStateFlow()

    private val _selectedMealTypeFilter = MutableStateFlow(MealTypeFilter.ALL)
    val selectedMealTypeFilter: StateFlow<MealTypeFilter> = _selectedMealTypeFilter.asStateFlow()

    private val _selectedAvailabilityFilter = MutableStateFlow(AvailabilityFilter.ALL)
    val selectedAvailabilityFilter: StateFlow<AvailabilityFilter> = _selectedAvailabilityFilter.asStateFlow()

    // Raw Recipe stream
    val allRecipes: StateFlow<List<Recipe>> = recipeRepository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class RecipeFilterCriteria(
        val query: String,
        val dietaryFilter: DietaryFilter,
        val mealTypeFilter: MealTypeFilter,
        val availabilityFilter: AvailabilityFilter
    )

    private val filterCriteriaFlow = combine(
        _recipeSearchQuery,
        _selectedDietaryFilter,
        _selectedMealTypeFilter,
        _selectedAvailabilityFilter
    ) { query, diet, meal, avail ->
        RecipeFilterCriteria(query, diet, meal, avail)
    }

    // Filtered & Ranked Recommended Recipes
    val recommendedRecipes: StateFlow<List<RecipeMatch>> = combine(
        allRecipes,
        inStockItems,
        filterCriteriaFlow
    ) { recipes, inventory, criteria ->
        recipeRepository.recommendRecipes(
            recipes = recipes,
            inventoryItems = inventory,
            dietaryFilter = criteria.dietaryFilter,
            mealTypeFilter = criteria.mealTypeFilter,
            availabilityFilter = criteria.availabilityFilter,
            searchQuery = criteria.query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected recipe for details sheet
    private val _selectedRecipeMatch = MutableStateFlow<RecipeMatch?>(null)
    val selectedRecipeMatch: StateFlow<RecipeMatch?> = _selectedRecipeMatch.asStateFlow()

    // Gemini AI Recipe Recommendations
    private val geminiRecipeService = GeminiRecipeService()
    private val _geminiRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val geminiRecipes: StateFlow<List<Recipe>> = _geminiRecipes.asStateFlow()

    private val _isGeneratingGeminiRecipes = MutableStateFlow(false)
    val isGeneratingGeminiRecipes: StateFlow<Boolean> = _isGeneratingGeminiRecipes.asStateFlow()

    private val _geminiError = MutableStateFlow<String?>(null)
    val geminiError: StateFlow<String?> = _geminiError.asStateFlow()

    // Donations & Waste Logs
    val donations: StateFlow<List<Donation>> = donationRepository.getDonations(1L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCommunityDonations: StateFlow<List<Donation>> = donationRepository.getAllDonations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDeliveries: StateFlow<List<Donation>> = donationRepository.getActiveDeliveries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDeliveryForTracking = MutableStateFlow<Donation?>(null)
    val selectedDeliveryForTracking: StateFlow<Donation?> = _selectedDeliveryForTracking.asStateFlow()

    private val _receiverProfile = MutableStateFlow(ReceiverProfile())
    val receiverProfile: StateFlow<ReceiverProfile> = _receiverProfile.asStateFlow()

    private val _foodNeedRequests = MutableStateFlow(
        listOf(
            FoodNeedRequest(
                id = 1L,
                receiverType = ReceiverType.NGO,
                requesterName = "Hope Harvest Food Bank (NGO)",
                title = "Need 50 Warm Dinner Meals for Tonight",
                peopleCount = 50,
                urgency = "Immediate (<2h)",
                phone = "+1 (555) 789-0123",
                dropAddress = "St. Vincent Community Shelter, 820 Elm Street",
                notes = "Any surplus hot rice, curries, or stews are greatly appreciated.",
                latitude = 37.7785,
                longitude = -122.4150,
                distanceMiles = 1.2,
                category = "Cooked Meals"
            ),
            FoodNeedRequest(
                id = 4L,
                receiverType = ReceiverType.NGO,
                requesterName = "Mercy Meals Kitchen (NGO)",
                title = "Need 45 Hot Lunch Boxes for Seniors",
                peopleCount = 45,
                urgency = "Immediate (<2h)",
                phone = "+1 (555) 321-7788",
                dropAddress = "510 Mission Blvd, Suite 2",
                notes = "Urgent lunch meals required for daily soup kitchen delivery.",
                latitude = 37.7812,
                longitude = -122.4110,
                distanceMiles = 0.8,
                category = "Cooked Meals"
            ),
            FoodNeedRequest(
                id = 6L,
                receiverType = ReceiverType.NGO,
                requesterName = "Tenderloin Family Fellowship (NGO)",
                title = "Need 60 Sandwiches & Fresh Fruit",
                peopleCount = 60,
                urgency = "Today",
                phone = "+1 (555) 901-2345",
                dropAddress = "142 Hyde St, Community Center",
                notes = "Sandwiches, apples, bananas, and bottled water.",
                latitude = 37.7830,
                longitude = -122.4165,
                distanceMiles = 1.8,
                category = "Bakery & Produce"
            ),
            FoodNeedRequest(
                id = 2L,
                receiverType = ReceiverType.INDIVIDUAL,
                requesterName = "Elena Rostova (Family of 5 - Individual)",
                title = "Need Baby Food, Milk & Fresh Groceries",
                peopleCount = 5,
                urgency = "Today",
                phone = "+1 (555) 887-9911",
                dropAddress = "Oak Park Family Center, Apt 2B",
                notes = "Have toddler and senior grandmother at home.",
                latitude = 37.7640,
                longitude = -122.4280,
                distanceMiles = 2.8,
                category = "Groceries & Milk"
            ),
            FoodNeedRequest(
                id = 5L,
                receiverType = ReceiverType.INDIVIDUAL,
                requesterName = "Marcus & Chloe (Senior Couple - Individual)",
                title = "Need Prepared Soft Dinners & Soups",
                peopleCount = 2,
                urgency = "Today",
                phone = "+1 (555) 654-3210",
                dropAddress = "720 16th Street, Unit 4",
                notes = "Low sodium meals preferred for senior health condition.",
                latitude = 37.7660,
                longitude = -122.3990,
                distanceMiles = 3.6,
                category = "Cooked Meals"
            ),
            FoodNeedRequest(
                id = 3L,
                receiverType = ReceiverType.NGO,
                requesterName = "Downtown Youth Care Shelter (NGO)",
                title = "Need 30 Breakfast Bakery Packages & Fruits",
                peopleCount = 30,
                urgency = "This Week",
                phone = "+1 (555) 441-2299",
                dropAddress = "340 Pine Ave, Downtown",
                notes = "Bread loaves, muffins, or fresh apples/bananas.",
                latitude = 37.7910,
                longitude = -122.4020,
                distanceMiles = 4.5,
                category = "Bakery"
            ),
            FoodNeedRequest(
                id = 7L,
                receiverType = ReceiverType.NGO,
                requesterName = "Sunset Community Food Pantry (NGO)",
                title = "Need 80 Bulk Rice, Beans & Produce Boxes",
                peopleCount = 80,
                urgency = "This Week",
                phone = "+1 (555) 776-5544",
                dropAddress = "19th Ave & Judah St",
                notes = "Any pantry surplus dry goods or supermarket surplus produce.",
                latitude = 37.7620,
                longitude = -122.4760,
                distanceMiles = 7.2,
                category = "Produce & Dry Goods"
            ),
            FoodNeedRequest(
                id = 8L,
                receiverType = ReceiverType.NGO,
                requesterName = "Bayview Neighborhood Fellowship (NGO)",
                title = "Need 35 Hot Dinner Platters & Drinks",
                peopleCount = 35,
                urgency = "This Week",
                phone = "+1 (555) 332-1100",
                dropAddress = "Third St & Palou Ave",
                notes = "Family shelter dinner assistance.",
                latitude = 37.7320,
                longitude = -122.3890,
                distanceMiles = 12.5,
                category = "Cooked Meals"
            )
        )
    )
    val foodNeedRequests: StateFlow<List<FoodNeedRequest>> = _foodNeedRequests.asStateFlow()

    private val _selectedMapRequest = MutableStateFlow<FoodNeedRequest?>(null)
    val selectedMapRequest: StateFlow<FoodNeedRequest?> = _selectedMapRequest.asStateFlow()

    fun selectMapRequest(request: FoodNeedRequest?) {
        _selectedMapRequest.value = request
    }

    val wasteLogs: StateFlow<List<WasteLog>> = donationRepository.getWasteLogs(1L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val impactStats: StateFlow<WasteImpactStats> = wasteLogs.combine(inStockItems) { logs, _ ->
        var rescuedVal = 0.0
        var wastedVal = 0.0
        var co2 = 0.0
        var rescuedCount = 0
        var wastedCount = 0

        for (log in logs) {
            if (log.actionType == "RESCUED") {
                rescuedVal += log.costAmount
                co2 += log.co2SavedKg
                rescuedCount++
            } else {
                wastedVal += log.costAmount
                wastedCount++
            }
        }

        WasteImpactStats(
            totalRescuedValue = rescuedVal,
            totalWastedValue = wastedVal,
            totalCo2SavedKg = co2,
            rescuedItemCount = rescuedCount,
            wastedItemCount = wastedCount
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WasteImpactStats(0.0, 0.0, 0.0, 0, 0)
    )

    init {
        viewModelScope.launch {
            authRepository.checkSession()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Authentication Actions ---
    fun login(email: String, password: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            _authSuccessMessage.value = null
            val result = authRepository.login(email, password)
            _isAuthLoading.value = false
            result.onSuccess {
                _activeUserId.value = it.id
                _authSuccessMessage.value = "Welcome back, ${it.name}!"
                onResult(true)
            }.onFailure {
                _authError.value = it.message ?: "Login failed"
                onResult(false)
            }
        }
    }

    fun register(name: String, email: String, password: String, securityAnswer: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            _authSuccessMessage.value = null
            val result = authRepository.register(name, email, password, securityAnswer)
            _isAuthLoading.value = false
            result.onSuccess {
                _activeUserId.value = it.id
                _authSuccessMessage.value = "Account created successfully! Welcome ${it.name}."
                onResult(true)
            }.onFailure {
                _authError.value = it.message ?: "Registration failed"
                onResult(false)
            }
        }
    }

    fun resetPassword(email: String, securityAnswer: String, newPassword: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authRepository.resetPassword(email, securityAnswer, newPassword)
            _isAuthLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "Password updated! You can now log in."
                onResult(true)
            }.onFailure {
                _authError.value = it.message ?: "Failed to reset password"
                onResult(false)
            }
        }
    }

    fun quickDemoLogin() {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = authRepository.quickLoginDemo()
            _isAuthLoading.value = false
            result.onSuccess {
                _activeUserId.value = it.id
                _userMessage.emit("Logged in as Donor (Eco Chef Alex)!")
                setTab(AppTab.DONOR)
            }
        }
    }

    fun quickDemoReceiverLogin() {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = authRepository.quickLoginReceiverDemo()
            _isAuthLoading.value = false
            result.onSuccess {
                _activeUserId.value = it.id
                _userMessage.emit("Logged in as Receiver (Hope Harvest NGO)!")
                setTab(AppTab.RECOVER)
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = authRepository.continueAsGuest()
            _isAuthLoading.value = false
            result.onSuccess {
                _activeUserId.value = it.id
                _userMessage.emit("Continuing as Guest Explorer!")
                setTab(AppTab.DONOR)
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearAuthMessages() {
        _authError.value = null
        _authSuccessMessage.value = null
    }

    // --- Inventory Actions ---
    fun addFoodItem(item: FoodItem) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: 1L
            foodRepository.insertItem(item.copy(userId = userId))
            _userMessage.emit("Added ${item.name} to pantry!")
        }
    }

    fun updateFoodItem(item: FoodItem) {
        viewModelScope.launch {
            foodRepository.updateItem(item)
            _userMessage.emit("Updated ${item.name}!")
        }
    }

    fun deleteFoodItem(item: FoodItem) {
        viewModelScope.launch {
            foodRepository.deleteItem(item)
            _userMessage.emit("Removed ${item.name}")
        }
    }

    fun markItemConsumed(item: FoodItem) {
        viewModelScope.launch {
            foodRepository.markAsConsumed(item, "Consumed fresh")
            _userMessage.emit("🎉 Rescued ${item.name}! Saved \$${"%.2f".format(item.priceEstimate)}")
        }
    }

    fun markItemWasted(item: FoodItem, reason: String) {
        viewModelScope.launch {
            foodRepository.markAsWasted(item, reason)
            _userMessage.emit("Logged ${item.name} as wasted to track habits.")
        }
    }

    fun markItemDonated(item: FoodItem, destination: String, notes: String = "") {
        viewModelScope.launch {
            foodRepository.markAsDonated(item, destination, notes)
            _userMessage.emit("💖 Donated ${item.name} to $destination!")
        }
    }

    // --- Recipe Actions ---
    fun setRecipeSearchQuery(query: String) {
        _recipeSearchQuery.value = query
    }

    fun setDietaryFilter(filter: DietaryFilter) {
        _selectedDietaryFilter.value = filter
    }

    fun setMealTypeFilter(filter: MealTypeFilter) {
        _selectedMealTypeFilter.value = filter
    }

    fun setAvailabilityFilter(filter: AvailabilityFilter) {
        _selectedAvailabilityFilter.value = filter
    }

    fun selectRecipeForDetails(recipeMatch: RecipeMatch?) {
        _selectedRecipeMatch.value = recipeMatch
    }

    fun cookRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val count = foodRepository.cookRecipeAndDeduct(recipe, inStockItems.value)
            _selectedRecipeMatch.value = null
            _userMessage.emit("👨‍🍳 Cooked '${recipe.title}'! Deducted $count ingredients from pantry.")
        }
    }

    fun addCustomRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.insertRecipe(recipe.copy(isCustom = true))
            _userMessage.emit("Added custom recipe '${recipe.title}'!")
        }
    }

    fun generateGeminiRecipes(
        dietaryPreference: String = "All Diets",
        mealType: String = "All Meals"
    ) {
        viewModelScope.launch {
            _isGeneratingGeminiRecipes.value = true
            _geminiError.value = null
            val items = inStockItems.value
            val result = geminiRecipeService.getRecipeRecommendations(items, dietaryPreference, mealType)
            _isGeneratingGeminiRecipes.value = false
            result.onSuccess { recipes ->
                _geminiRecipes.value = recipes
                recipes.forEach { recipe ->
                    recipeRepository.insertRecipe(recipe)
                }
                _userMessage.emit("✨ Gemini recommended ${recipes.size} zero-waste recipes from your pantry!")
            }.onFailure { error ->
                _geminiError.value = error.message ?: "Failed to generate AI recipes"
                _userMessage.emit("Gemini: ${error.message}")
            }
        }
    }

    fun clearGeminiRecommendations() {
        _geminiRecipes.value = emptyList()
        _geminiError.value = null
    }

    // --- Donation & Recovery Actions ---
    fun selectDeliveryForTracking(donation: Donation?) {
        _selectedDeliveryForTracking.value = donation
    }

    fun updateReceiverProfile(profile: ReceiverProfile) {
        _receiverProfile.value = profile
    }

    fun setReceiverType(newType: ReceiverType) {
        val current = _receiverProfile.value
        if (current.type == newType) return
        val defaultName = if (newType == ReceiverType.NGO) "Helping Hands Food Bank (NGO)" else "Alex Rivera (Individual)"
        val defaultId = if (newType == ReceiverType.NGO) "NGO Reg #501C-4421" else "Family of 4"
        _receiverProfile.value = current.copy(
            type = newType,
            name = defaultName,
            identifierOrSize = defaultId
        )
    }

    fun toggleReceiverType() {
        val current = _receiverProfile.value
        val newType = if (current.type == ReceiverType.NGO) ReceiverType.INDIVIDUAL else ReceiverType.NGO
        val defaultName = if (newType == ReceiverType.NGO) "Helping Hands Food Bank (NGO)" else "Alex Rivera (Individual)"
        val defaultId = if (newType == ReceiverType.NGO) "NGO Reg #501C-4421" else "Family of 4"
        _receiverProfile.value = current.copy(
            type = newType,
            name = defaultName,
            identifierOrSize = defaultId
        )
    }

    fun claimDonation(
        donation: Donation,
        deliveryMethod: String = "VOLUNTEER_DELIVERY",
        customNotes: String = ""
    ) {
        viewModelScope.launch {
            val profile = _receiverProfile.value
            donationRepository.claimFoodDonation(
                donation = donation,
                receiverType = profile.type.name,
                receiverName = profile.name,
                receiverPhone = profile.phone,
                receiverDetails = profile.identifierOrSize,
                dropAddress = profile.address,
                deliveryMethod = deliveryMethod
            )
            _selectedDeliveryForTracking.value = donation.copy(
                status = "CLAIMED",
                receiverType = profile.type.name,
                receiverName = profile.name,
                receiverPhone = profile.phone,
                dropAddress = profile.address,
                deliveryMethod = deliveryMethod
            )
            _userMessage.emit("🎉 Claimed '${donation.foodTitle}' for recovery as ${profile.type.label}!")
        }
    }

    fun advanceDelivery(donation: Donation) {
        viewModelScope.launch {
            donationRepository.advanceDelivery(donation)
            val nextStatus = when (donation.status) {
                "CLAIMED" -> "OUT_FOR_PICKUP"
                "OUT_FOR_PICKUP" -> "IN_TRANSIT"
                "IN_TRANSIT" -> "DELIVERED"
                else -> donation.status
            }
            _selectedDeliveryForTracking.value = donation.copy(status = nextStatus)
            if (nextStatus == "DELIVERED") {
                _userMessage.emit("🎉 Delivery completed! Rescued ${donation.quantity} of surplus food.")
            } else {
                _userMessage.emit("Delivery updated: $nextStatus")
            }
        }
    }

    fun postFoodNeedRequest(request: FoodNeedRequest) {
        val current = _foodNeedRequests.value.toMutableList()
        current.add(0, request)
        _foodNeedRequests.value = current
        viewModelScope.launch {
            _userMessage.emit("📢 Food need request broadcasted to local food donors!")
        }
    }

    fun addDonation(donation: Donation) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: 1L
            donationRepository.insertDonation(donation.copy(userId = userId))
            _userMessage.emit("Surplus food posted! Available for NGOs and individuals.")
        }
    }
}
