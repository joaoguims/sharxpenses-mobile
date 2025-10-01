package com.sharxpenses.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sharxpenses.MainActivity

object NotificationRouter {
    const val EXTRA_ROUTE = "deep_link_route"

    fun resolveRoute(data: Map<String, String>): String {
        val type = data["type"]
        return when (type) {
            "expense" -> {
                val id = data["expenseId"] ?: return "home"
                "expense/"
            }
            "group" -> {
                val id = data["groupId"] ?: return "home"
                "group/"
            }
            else -> "home"
        }
    }

    fun buildPendingIntent(context: Context, route: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROUTE, route)
        }
        return PendingIntent.getActivity(
            context,
            route.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}