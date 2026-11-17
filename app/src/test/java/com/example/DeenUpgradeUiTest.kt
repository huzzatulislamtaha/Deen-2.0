package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeenUpgradeUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `home shortcuts location selection timezone font settings quran reflection and notebook work end to end`() {
        composeRule.waitForIdle()

        // 1. Open Location Selection from Home -> filter Sylhet division -> pick Sunamganj -> Confirm
        composeRule.onNodeWithTag("select_city_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location_lazy_list").performScrollToNode(hasTestTag("division_chip_div_sylhet"))
        composeRule.onNodeWithTag("division_chip_div_sylhet").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location_lazy_list").performScrollToNode(hasTestTag("district_row_dist_sunamganj"))
        composeRule.onNodeWithTag("district_row_dist_sunamganj").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location_lazy_list").performScrollToNode(hasTestTag("confirm_bangladesh_location_button"))
        composeRule.onNodeWithTag("confirm_bangladesh_location_button").performClick()
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithText("Sunamganj", substring = true).fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("সুনামগঞ্জ", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Open Timezone Selection from Home pill -> search London -> select -> reset to Asia/Dhaka
        composeRule.onNodeWithTag("home_timezone_pill").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("timezone_lazy_list").performScrollToNode(hasTestTag("timezone_search_input"))
        composeRule.onNodeWithTag("timezone_search_input").performTextInput("Europe/London")
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithTag("timezone_item_Europe/London").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("timezone_lazy_list").performScrollToNode(hasTestTag("timezone_item_Europe/London"))
        composeRule.onNodeWithTag("timezone_item_Europe/London").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("timezone_lazy_list").performScrollToNode(hasTestTag("timezone_bd_default_button"))
        composeRule.onNodeWithTag("timezone_bd_default_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("timezone_back_button").performClick()
        composeRule.waitForIdle()

        // 3. Open Quran from Home Dashboard -> reflect in Notebook -> save -> return Home
        composeRule.onNodeWithTag("home_lazy_list").performScrollToNode(hasTestTag("home_shortcut_quran"))
        composeRule.onNodeWithTag("home_shortcut_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("quran_lazy_list").performScrollToNode(hasTestTag("quran_reflect_quran_fatiha"))
        composeRule.onNodeWithTag("quran_reflect_quran_fatiha").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_lazy_list").performScrollToNode(hasTestTag("editor_rtl_toggle"))
        composeRule.onNodeWithTag("editor_rtl_toggle").performScrollTo().performClick()
        composeRule.onNodeWithTag("editor_preview_toggle").performScrollTo().performClick()
        composeRule.onNodeWithTag("editor_save_done_button").performClick()
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithTag("notebook_add_note_fab").fetchSemanticsNodes().isNotEmpty()
        }

        // 4. Verify note appears in Notebook, pin + favorite, toggle Dark theme via Settings
        composeRule.onNodeWithTag("notebook_add_note_fab").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_lazy_list").performScrollToNode(hasTestTag("editor_title_input"))
        composeRule.onNodeWithTag("editor_title_input").performTextInput("Dua Note")
        composeRule.onNodeWithTag("editor_lazy_list").performScrollToNode(hasTestTag("editor_content_input"))
        composeRule.onNodeWithTag("editor_content_input").performTextInput("রাব্বানা আতিনা — ربنا آتنا")
        composeRule.onNodeWithTag("editor_pin_button").performClick()
        composeRule.onNodeWithTag("editor_fav_button").performClick()
        composeRule.onNodeWithTag("editor_save_done_button").performClick()
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithText("Dua Note").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("notebook_back_button").performClick()
        composeRule.waitForIdle()
        if (composeRule.onAllNodesWithTag("quran_back_button").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("quran_back_button").performClick()
            composeRule.waitForIdle()
        }

        // 5. Open Settings -> Dark Mode -> Font Settings (verify live Arabic/Bengali/English preview) -> Reset
        composeRule.onNodeWithTag("home_lazy_list").performScrollToNode(hasTestTag("open_settings_button"))
        composeRule.onNodeWithTag("open_settings_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_lazy_list").performScrollToNode(hasTestTag("theme_option_dark"))
        composeRule.onNodeWithTag("theme_option_dark").performClick()
        composeRule.onNodeWithTag("settings_lazy_list").performScrollToNode(hasTestTag("settings_notebook_autosave_switch"))
        composeRule.onNodeWithTag("settings_notebook_autosave_switch").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_lazy_list").performScrollToNode(hasTestTag("settings_font_row"))
        composeRule.onNodeWithTag("settings_font_row").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Your Islamic Daily Companion").assertIsDisplayed()
        composeRule.onNodeWithText("আপনার দৈনন্দিন ইসলামিক সঙ্গী").assertIsDisplayed()
        composeRule.onNodeWithText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ").assertIsDisplayed()

        composeRule.onNodeWithTag("font_lazy_list").performScrollToNode(hasTestTag("font_preset_large"))
        composeRule.onNodeWithTag("font_preset_large").performClick()
        composeRule.onNodeWithTag("font_lazy_list").performScrollToNode(hasTestTag("latin_font_inter"))
        composeRule.onNodeWithTag("latin_font_inter").performClick()
        composeRule.onNodeWithTag("font_lazy_list").performScrollToNode(hasTestTag("font_reset_default_button"))
        composeRule.onNodeWithTag("font_reset_default_button").performClick()
        composeRule.onNodeWithTag("font_settings_back_button").performClick()
        composeRule.waitForIdle()
    }
}
