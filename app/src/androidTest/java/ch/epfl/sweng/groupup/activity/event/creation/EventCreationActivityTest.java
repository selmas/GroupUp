package ch.epfl.sweng.groupup.activity.event.creation;

import android.location.Location;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.BuildConfig;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.rule.ActivityTestRule;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.hamcrest.Matchers;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.groupup.R;
import ch.epfl.sweng.groupup.lib.Optional;
import ch.epfl.sweng.groupup.lib.database.Database;
import ch.epfl.sweng.groupup.object.account.Account;
import ch.epfl.sweng.groupup.object.account.Member;
import ch.epfl.sweng.groupup.object.event.Event;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class EventCreationActivityTest {

    @Rule
    // third parameter is set to true which means the activity is started automatically
    public ActivityTestRule<EventCreationActivity> mActivityRule =
            new ActivityTestRule<>(EventCreationActivity.class, true, true);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup(){
        Database.setUpDatabase();
    }


    /**
     * Tests if an event is well generated when the user enters specified inputs.
     * Important ! The test could fail because of the start and end date and time.
     */
    @Test
    public void testEventWellGenerated() {

        String eventName = "My event";

        Member emptyMember = new Member(Optional.<String>empty(), Optional.<String>empty(), Optional.<String>empty(),
                Optional.<String>empty(), Optional.<String>empty(), Optional.<Location>empty());
        List<Member> expectedMembers = new ArrayList<>();
        expectedMembers.add(emptyMember.withUUID("0"));
        expectedMembers.add(emptyMember.withUUID("1"));
        expectedMembers.add(emptyMember.withUUID("2"));
        expectedMembers.add(emptyMember.withUUID("3"));
        expectedMembers.add(emptyMember.withUUID("4"));
        expectedMembers.add(emptyMember.withUUID(Member.UNKNOWN_USER + "1").withEmail("swenggroupup@gmail.com"));
        expectedMembers.add(emptyMember.withUUID(Account.shared.getUUID().getOrElse("Default UUID")));

        addEventName(eventName);

        Espresso.closeSoftKeyboard();

        addDescription("My description");

        Espresso.closeSoftKeyboard();

        LocalDateTime start = LocalDateTime.now().plusHours(1).plusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusHours(1).plusMinutes(6);

        onView(withId(R.id.button_start_date)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(start.getYear(), start.getMonthOfYear(), start.getDayOfYear()));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.button_end_date)).perform(click());

        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(end.getYear(), end.getMonthOfYear(), end.getDayOfYear()));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.button_start_time)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(start.getHourOfDay(), start.getMinuteOfHour()));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.button_end_time)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(end.getHourOfDay(), end.getMinuteOfHour()));
        onView(withId(android.R.id.button1)).perform(click());

        addMembers();
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.save_button)).perform(click());
        Event expected = new Event(eventName, start, end, "My description", expectedMembers,false);

        Event found = findEvent(eventName);
        if (BuildConfig.DEBUG && !(found.equals(expected))){
            throw new AssertionError();
        }
        Account.shared.clear();
    }

    @Test
    public void noEventCreatedOnEmptyEventName(){

        addEventName("");
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.save_button)).perform(click());
        onView(withId(R.id.ui_edit_event_name))
                .check(matches(hasErrorText(
                        getTargetContext().getString(R.string.event_creation_toast_non_empty_event_name))));
    }

    @Test
    public void noEventCreatedOnTooLongName() {
        addEventName("This event name should be way too long for the event creator to accept it"+
                "I should not be able to tell my life in the event name");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 5, 4, 5);
        setEndDate(2100, 5, 5, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        onView(withId(R.id.ui_edit_event_name))
                .check(matches(hasErrorText(
                        getTargetContext().getString(R.string.event_creation_toast_event_name_too_long))));
         }

    @Test
    public void dateWellComparedYear1(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 5, 5, 5);
        setEndDate(2099, 5, 5, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        onView(withText(R.string.event_creation_toast_event_end_before_begin))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity()
                        .getWindow()
                        .getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void dateWellComparedYear2(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2099, 5, 5, 5, 5);
        setEndDate(2100, 5, 5, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        //intended(hasComponent(EventListingActivity.class.getName()));
        Account.shared.clear();
    }

    @Test
    public void dateWellComparedMonth1(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 5, 5, 5);
        setEndDate(2100, 4, 5, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        onView(withText(R.string.event_creation_toast_event_end_before_begin))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity()
                        .getWindow()
                        .getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void dateWellComparedMonth2(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 4, 5, 5, 5);
        setEndDate(2100, 5, 5, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        //intended(hasComponent(EventListingActivity.class.getName()));
        Account.shared.clear();
    }

    @Test
    public void dateWellComparedDay1(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 5, 5, 5);
        setEndDate(2100, 5, 4, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        onView(withText(R.string.event_creation_toast_event_end_before_begin))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity()
                        .getWindow()
                        .getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void dateWellComparedDay2(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 4, 5, 5);
        setEndDate(2100, 5, 5, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        //intended(hasComponent(EventListingActivity.class.getName()));
        Account.shared.clear();
    }

    @Test
    public void dateWellComparedHour1(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 5, 5, 5);
        setEndDate(2100, 5, 5, 4, 5);
        onView(withId(R.id.save_button)).perform(click());
        onView(withText(R.string.event_creation_toast_event_end_before_begin))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity()
                        .getWindow()
                        .getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void dateWellComparedHour2(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 5, 4, 5);
        setEndDate(2100, 5, 5, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        //intended(hasComponent(EventListingActivity.class.getName()));
        Account.shared.clear();
    }

    @Test
    public void dateWellComparedMinute1(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 5, 5, 5);
        setEndDate(2100, 5, 5, 5, 4);
        onView(withId(R.id.save_button)).perform(click());
        onView(withText(R.string.event_creation_toast_event_end_before_begin))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity()
                        .getWindow()
                        .getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void atLeastOneMinuteBetweenStartAndEndDate(){
        addEventName("My event");
        Espresso.closeSoftKeyboard();
        setStartDate(2100, 5, 5, 5, 5);
        setEndDate(2100, 5, 5, 5, 5);
        onView(withId(R.id.save_button)).perform(click());
        onView(withText(R.string.event_craeation_toast_event_last_1_minute))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity()
                        .getWindow()
                        .getDecorView()))))
                .check(matches(isDisplayed()));
     }

    @Test
    public void noEventCreationOnPastStartDate(){
        LocalDateTime now = LocalDateTime.now();
        int day = now.getDayOfMonth();
        int month = now.getMonthOfYear();
        int year = now.getYear() - 1;

        addEventName("My event");
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.button_start_date)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(year, month, day));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.save_button)).perform(click());
        onView(withText(R.string.event_creation_toast_event_start_before_now))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity()
                        .getWindow()
                        .getDecorView()))))
                .check(matches(isDisplayed()));
    }

    /**
     * Test QR Scanner
     */
    @Test
    public void stateRestoredAfterCameraOpened(){
        String eventName = "testEventName";
        // Enter event details
        addEventName(eventName);
        Espresso.closeSoftKeyboard();
        addMembers();
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.button_add_members)).perform(click());
        Espresso.closeSoftKeyboard();
        // Click scan button
        //onView(withId(R.id.buttonScanQR)).perform(click());
        // Click back
        //Espresso.pressBack();
        onView(withId(R.id.save_button)).perform(click());
        // Check event details
        onView(withId(R.id.ui_edit_event_name)).check(matches(withText(eventName)));
        Account.shared.clear();
    }

    /**
     * Helper functions
     */

    private void setStartDate(int year, int month, int day, int hour, int minute){
        onView(withId(R.id.button_start_date)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(year, month, day));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.button_start_time)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(hour, minute));
        onView(withId(android.R.id.button1)).perform(click());
    }

    private void setEndDate(int year, int month, int day, int hour, int minute){
        onView(withId(R.id.button_end_date)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(year, month, day));
        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.button_end_time)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName())))
                .perform(PickerActions.setTime(hour, minute));
        onView(withId(android.R.id.button1)).perform(click());
    }

    private Event findEvent(String eventName){
        List<Event> accountEvents = Account.shared.getFutureEvents();
        Event found = null;
        for(Event e : accountEvents){
            if(e.getEventName().equals(eventName)){
                found = e;
                break;
            }
        }
        return found;
    }

    private void addMembers(){
        onView(withId(R.id.button_add_members)).perform(click());
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.edit_text_add_member)).perform(typeText("0"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.image_view_add_member)).perform(click());
        onView(withId(R.id.edit_text_add_member)).perform(typeText("1"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.image_view_add_member)).perform(click());
        onView(withId(R.id.edit_text_add_member)).perform(typeText("2"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.image_view_add_member)).perform(click());
        onView(withId(R.id.edit_text_add_member)).perform(typeText("3"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.image_view_add_member)).perform(click());
        onView(withId(R.id.edit_text_add_member)).perform(typeText("4"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.image_view_add_member)).perform(click());
        onView(withId(R.id.edit_text_add_member)).perform(typeText("swenggroupup@gmail.com"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.image_view_add_member)).perform(click());
        onView(withId(R.id.save_button)).perform(click());
    }

    private void addEventName(String name){
        onView(withId(R.id.ui_edit_event_name)).perform(typeText(name));
    }

    private void addDescription(String description){
        onView(withId(R.id.edit_text_description)).perform(typeText(description));
    }
}
