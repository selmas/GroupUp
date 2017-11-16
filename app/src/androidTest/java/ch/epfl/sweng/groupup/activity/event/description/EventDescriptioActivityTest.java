package ch.epfl.sweng.groupup.activity.event.description;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.BuildConfig;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.groupup.R;
import ch.epfl.sweng.groupup.activity.event.creation.EventCreationActivity;
import ch.epfl.sweng.groupup.lib.database.Database;
import ch.epfl.sweng.groupup.object.account.Account;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by alix on 10/28/17.
 */

@RunWith(AndroidJUnit4.class)
public class EventDescriptioActivityTest {
    private final String name = "My beautiful event";

    @Rule
    public final ActivityTestRule<EventCreationActivity> mActivityRule =
            new ActivityTestRule<>(EventCreationActivity.class);


    @Test
    public void createDisplayAndChangeAnEvent() {
        Database.setUpDatabase();
        String name = "My event";
        String endName = " is changed";
        String description = "has an awesome description";
        onView(withId(R.id.ui_edit_event_name)).perform(typeText(name));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.save_button)).perform(click());

        onView(withId(R.id.linear_layout_event_list)).perform(click());
        onView(withId(R.id.event_description_tv_name))
                .check(matches(withText(R.string.event_description_tv_name)));
        onView(withId(R.id.event_description_name))
                .check(matches(withText(name)));

        onView(withId(R.id.event_description_name)).perform(typeText(endName));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.event_description_description)).perform(typeText(description));
        onView(withId(R.id.save)).perform(click());
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.event_description_name)).check(matches(withText(name + endName)));
        onView(withId(R.id.event_description_description)).check(matches(withText(description)));
        Account.shared.clear();
    }

    @Test
    public void CreateAndRemoveAnEvent() {

        onView(withId(R.id.ui_edit_event_name)).perform(typeText(name));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.save_button)).perform(click());

        onView(withId(R.id.linear_layout_event_list)).perform(click());

        onView(withId(R.id.remove_event_button)).perform(click());

        if (BuildConfig.DEBUG && !(Account.shared.getEvents().isEmpty())){
            throw new AssertionError();
        }
        Account.shared.clear();
    }
}
