package ch.epfl.sweng.groupup.lib.database;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.groupup.activity.main.MainActivity;
import ch.epfl.sweng.groupup.object.account.Account;
import ch.epfl.sweng.groupup.object.account.Member;
import ch.epfl.sweng.groupup.object.event.Event;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseShould {
    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void exposeSetUpMethod() throws Exception {
        Database.setUpDatabase();

        assertTrue(true);
    }

    @Test
    public void exposeSetUpListenerForDefaultAndOwnListener() throws Exception {
        Database.setUpDatabase();

        Database.setUpEventListener(null);
        Database.setUpEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }
        });

        assertTrue(true);
    }

    @Test
    public void exposeAnUpdateMethod() throws Exception {
        Database.setUpDatabase();

        Database.update();

        assertTrue(true);
    }

    @Test
    public void updateDatabaseCorrectlyAccordingToTheUsersAccount() throws Exception {
        // Database set up.
        Database.setUpDatabase();

        // Account initialization.
        String givenName = "Group";
        String familyName = "Up";
        String displayName = "GroupUp";
        String email = "groupup@flyingmonkeys.com";
        String uuid = "myAccountUuidIsVeryComplex";
        Account.shared.withGivenName(givenName).withFamilyName(familyName).withDisplayName
                (displayName).withEmail(email).withUUID(uuid);

        // Add of current event.
        String nameCurrent = "Current Event";
        String descriptionCurrent = "DescriptionCurrent";
        LocalDateTime startCurrent = LocalDateTime.now().minusMinutes(1);
        LocalDateTime endCurrent = LocalDateTime.now().plusDays(1);
        final String uuidCurrent = "uuidCurrent";
        List<Member> membersCurrent = new ArrayList<>();
        String userName01 = "User";
        String userFamilyName01 = "01";
        String userDisplayName01 = "User01";
        String userEmail01 = "user@01.com";
        String userUuid01 = "myUserUuid01";
        membersCurrent.add(new Member(userName01, userFamilyName01, userDisplayName01, userEmail01,
                                      userUuid01));
        final Event eventCurrent = new Event(uuidCurrent,
                                             nameCurrent,
                                             startCurrent,
                                             endCurrent,
                                             descriptionCurrent,
                                             membersCurrent);
        Account.shared.addOrUpdateEvent(eventCurrent);

        // Add of past event.
        String namePast = "Past Event";
        String descriptionPast = "Description Past";
        LocalDateTime startPast = LocalDateTime.now().minusDays(2);
        LocalDateTime endPast = LocalDateTime.now().minusDays(1);
        final String uuidPast = "uuidPast";
        List<Member> membersPast = new ArrayList<>();
        String userName02 = "User";
        String userFamilyName02 = "02";
        String userDisplayName02 = "User02";
        String userEmail02 = "user@02.com";
        String userUuid02 = "myUserUuid02";
        membersPast.add(new Member(userName02, userFamilyName02, userDisplayName02, userEmail02,
                                   userUuid02));
        final Event eventPast = new Event(uuidPast,
                                          namePast,
                                          startPast,
                                          endPast,
                                          descriptionPast,
                                          membersPast);
        Account.shared.addOrUpdateEvent(eventPast);

        // Add of future event.
        String nameFuture = "Future Event";
        String descriptionFuture = "Description Future";
        LocalDateTime startFuture = LocalDateTime.now().plusDays(2);
        LocalDateTime endFuture = LocalDateTime.now().plusDays(3);
        final String uuidFuture = "uuidFuture";
        List<Member> membersFuture = new ArrayList<>();
        String userName03 = "User";
        String userFamilyName03 = "03";
        String userDisplayName03 = "User03";
        String userEmail03 = "user@03.com";
        String userUuid03 = "myUserUuid03";
        membersFuture.add(new Member(userName03, userFamilyName03, userDisplayName03, userEmail03,
                                     userUuid03));
        final Event eventFuture = new Event(uuidFuture,
                                            nameFuture,
                                            startFuture,
                                            endFuture,
                                            descriptionFuture,
                                            membersFuture);
        Account.shared.addOrUpdateEvent(eventFuture);

        Database.update();

        // Event listener set up.
        Database.setUpEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    DatabaseEvent event = eventSnapshot.getValue(DatabaseEvent.class);

                    if (event != null && !event.uuid.equals(Database.EMPTY_FIELD)) {
                        if (event.uuid.equals(uuidCurrent) || event.uuid.equals(uuidPast) ||
                            event.uuid.equals(uuidFuture)) {

                            List<Member> members = new ArrayList<>();
                            for (DatabaseUser user : event.members.values()) {
                                members.add(new Member(user.uuid,
                                                       user.display_name,
                                                       user.given_name,
                                                       user.family_name,
                                                       user.email));
                            }

                            Event tempEvent = new Event(
                                    event.uuid,
                                    event.name,
                                    LocalDateTime.parse(event.datetime_start),
                                    LocalDateTime.parse(event.datetime_end),
                                    event.description, members);

                            switch (event.uuid) {
                                case uuidCurrent:
                                    assertEquals(eventCurrent, tempEvent);
                                    break;
                                case uuidPast:
                                    assertEquals(eventPast, tempEvent);
                                    break;
                                case uuidFuture:
                                    assertEquals(eventFuture, tempEvent);
                                    break;
                                default:
                                    throw new Error("default case in switch statement");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Nothing
            }
        });

        Database.update();

        /*
         This is to ensure that the onDataChange listener had enough time be be called in the
         background and pass all the asserts.
          */
        Thread.sleep(1000);
    }
}