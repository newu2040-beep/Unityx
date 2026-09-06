package com.example.integration

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val permissionManifest: String?,
    val roleName: String? = null,
    val isGranted: Boolean,
    val whyNeeded: String,
    val category: String
)

class PermissionManager(private val context: Context) {

    fun getPermissionsList(): List<PermissionItem> {
        val pm = context.packageManager
        val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
        } else null

        val isMicGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val isCallGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val isContactsGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val isSmsGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val isNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        val isGalleryGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        val isAssistantRole = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
            roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)
        } else false

        val isDialerRole = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
            roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else false

        val isSmsRole = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager != null) {
            roleManager.isRoleHeld(RoleManager.ROLE_SMS)
        } else false

        val isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true

        return listOf(
            PermissionItem(
                id = "overlay",
                title = "Display Over Other Apps",
                description = "Enables floating assistant circle on home screen and over apps",
                permissionManifest = null,
                isGranted = isOverlayGranted,
                whyNeeded = "Required for the small floating circle to run in background outside the app with smooth gesture controls.",
                category = "System Overlay"
            ),
            PermissionItem(
                id = "mic",
                title = "Microphone",
                description = "Offline voice capture and speech-to-text",
                permissionManifest = Manifest.permission.RECORD_AUDIO,
                isGranted = isMicGranted,
                whyNeeded = "Required to capture spoken natural language instructions and convert them offline to actions.",
                category = "Core Voice"
            ),
            PermissionItem(
                id = "phone",
                title = "Phone & Calls",
                description = "Direct call placing and dialer launch",
                permissionManifest = Manifest.permission.CALL_PHONE,
                isGranted = isCallGranted,
                whyNeeded = "Allows UNITYX to initiate voice calls to contacts upon user confirmation.",
                category = "Communication"
            ),
            PermissionItem(
                id = "contacts",
                title = "Contacts",
                description = "Match contact names and phone numbers",
                permissionManifest = Manifest.permission.READ_CONTACTS,
                isGranted = isContactsGranted,
                whyNeeded = "Enables entity extraction to match spoken names ('Call Rahul') with stored phone numbers.",
                category = "Communication"
            ),
            PermissionItem(
                id = "sms",
                title = "SMS Messaging",
                description = "Send and format quick text messages",
                permissionManifest = Manifest.permission.SEND_SMS,
                isGranted = isSmsGranted,
                whyNeeded = "Allows UNITYX to send drafted messages directly after explicit user confirmation.",
                category = "Communication"
            ),
            PermissionItem(
                id = "notifications",
                title = "Notifications",
                description = "Status alerts and workflow completion",
                permissionManifest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null,
                isGranted = isNotificationsGranted,
                whyNeeded = "Displays workflow status, automation progress, and scheduled reminder alerts.",
                category = "System"
            ),
            PermissionItem(
                id = "gallery",
                title = "Gallery & Photos",
                description = "Image processing and file access",
                permissionManifest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                },
                isGranted = isGalleryGranted,
                whyNeeded = "Enables assistant photo tasks and visual reference selection.",
                category = "Media"
            ),
            PermissionItem(
                id = "assistant_role",
                title = "Default Assistant Role",
                description = "Android system-level assistant integration",
                permissionManifest = null,
                roleName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) RoleManager.ROLE_ASSISTANT else null,
                isGranted = isAssistantRole,
                whyNeeded = "Allows long-press home or power button to invoke UNITYX from any screen.",
                category = "Android Roles"
            ),
            PermissionItem(
                id = "dialer_role",
                title = "Default Dialer Role",
                description = "InCallService call management",
                permissionManifest = null,
                roleName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) RoleManager.ROLE_DIALER else null,
                isGranted = isDialerRole,
                whyNeeded = "Enables hands-free incoming call answering and silence controls.",
                category = "Android Roles"
            ),
            PermissionItem(
                id = "sms_role",
                title = "Default SMS Role",
                description = "Direct local SMS inbox and reply capability",
                permissionManifest = null,
                roleName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) RoleManager.ROLE_SMS else null,
                isGranted = isSmsRole,
                whyNeeded = "Required for reading incoming SMS messages aloud and automated message handling.",
                category = "Android Roles"
            )
        )
    }

    fun getCoreRuntimePermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return permissions.toTypedArray()
    }

    fun areAllCorePermissionsGranted(): Boolean {
        return getCoreRuntimePermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }
}
