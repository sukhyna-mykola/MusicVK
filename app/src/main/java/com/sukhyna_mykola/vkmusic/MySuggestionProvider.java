package com.sukhyna_mykola.vkmusic;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by mikola on 06.11.2016.
 */

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}