package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Registration : Screen("registration")
    object Dashboard : Screen("dashboard")
    object SosConfirmation : Screen("sos_confirmation")
    object ActiveSos : Screen("active_sos")
    object ContactsList : Screen("contacts_list")
    object AddEditContact : Screen("add_edit_contact?contactId={contactId}") {
        fun createRoute(contactId: Long? = null): String {
            return if (contactId != null) "add_edit_contact?contactId=$contactId" else "add_edit_contact"
        }
    }
    object UserProfile : Screen("user_profile")
    object LiveLocationMap : Screen("live_location_map")
    object EmergencyHistory : Screen("emergency_history")
    object SosDetail : Screen("sos_detail/{sosId}") {
        fun createRoute(sosId: Long): String = "sos_detail/$sosId"
    }
    object Settings : Screen("settings")
    object PermissionsSafety : Screen("permissions_safety")
    object AboutHelp : Screen("about_help")
}
