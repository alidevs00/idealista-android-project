package com.idealista.challenge.presentation.common

/**
 * One-off feedback event emitted right after a favorite toggle, so a screen can
 * show a confirmation (e.g. a Snackbar) without smuggling a transient UI event
 * into the screen's ViewModel state - see the architecture notes in CLAUDE.md
 * §3 ("one-off events go through a separate SharedFlow/Channel").
 */
enum class FavoriteToggleEvent { ADDED, REMOVED }
