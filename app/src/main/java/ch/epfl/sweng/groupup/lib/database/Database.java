package ch.epfl.sweng.groupup.lib.database;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.groupup.object.account.Account;
import ch.epfl.sweng.groupup.object.account.Member;
import ch.epfl.sweng.groupup.object.event.Event;

public final class Database {

    static final String EMPTY_FIELD = "EMPTY_FIELD";

    private static final String NODE_EVENTS_LIST = "events";

    private static FirebaseDatabase database;
    private static DatabaseReference databaseRef;

    private Database() {
        // Not instantiable.
    }

    public static void setUpDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }

        databaseRef = database.getReference();
    }

    public static void setUpEventListener() {
        databaseRef.child(NODE_EVENTS_LIST).addValueEventListener(getEventsListener());
    }

    public static void updateDatabase() {
        for (Event event : Account.shared.getFutureEvents()) {
            storeEvent(event);
        }
        for (Event event : Account.shared.getPastEvents()) {
            storeEvent(event);
        }
        if (!Account.shared.getCurrentEvent().isEmpty()) {
            storeEvent(Account.shared.getCurrentEvent().get());
        }
    }

    private static void storeEvent(Event event) {
        HashMap<String, DatabaseUser> uuidToUserMap = new HashMap<>();

        for (Member memberToStore : event.getEventMembers()) {
            DatabaseUser databaseUser =
                    new DatabaseUser(memberToStore.getGivenName().getOrElse(EMPTY_FIELD),
                                     memberToStore.getFamilyName().getOrElse(EMPTY_FIELD),
                                     memberToStore.getDisplayName().getOrElse(EMPTY_FIELD),
                                     memberToStore.getEmail().getOrElse(EMPTY_FIELD),
                                     memberToStore.getUUID().getOrElse(EMPTY_FIELD));

            uuidToUserMap.put(databaseUser.uuid, databaseUser);
        }

        DatabaseEvent eventToStore = new DatabaseEvent(event.getEventName(),
                                                       event.getDescription(),
                                                       event.getStartTime().toString(),
                                                       event.getEndTime().toString(),
                                                       event.getUUID(),
                                                       uuidToUserMap);
        storeEvent(eventToStore);
    }

    private static void storeEvent(DatabaseEvent databaseEvent) {
        DatabaseReference events = databaseRef.child(NODE_EVENTS_LIST);
        DatabaseReference currentEvent = events.child(databaseEvent.uuid);

        currentEvent.setValue(databaseEvent);
    }

    private static ValueEventListener getEventsListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    boolean needToUpdateMyself = false;

                    DatabaseEvent event = eventSnapshot.getValue(DatabaseEvent.class);
                    if (event != null && !event.uuid.equals(Database.EMPTY_FIELD)) {

                        List<String> uuids = new ArrayList<>();
                        Set<String> uuidss = event.members.keySet();
                        for (DatabaseUser user : event.members.values()) {
                            uuids.add(user.uuid);
                        }

                        if (uuids.contains(Account.shared.getUUID().getOrElse(EMPTY_FIELD))) {

                            // TODO: remove
                            Log.e("###", event.uuid);
                            // TODO: remove

                            List<Member> members = new ArrayList<>();

                            for (DatabaseUser user : event.members.values()) {
                                Member memberToAdd = new Member(user.uuid,
                                                                user.display_name,
                                                                user.given_name,
                                                                user.family_name,
                                                                user.email);

                                if (user.uuid.equals(Account.shared.getUUID().getOrElse
                                        (EMPTY_FIELD))) {
                                    Member mySelf = new Member(Account.shared.getUUID()
                                                                       .getOrElse(EMPTY_FIELD),
                                                               Account.shared.getDisplayName()
                                                                       .getOrElse(EMPTY_FIELD),
                                                               Account.shared.getGivenName()
                                                                       .getOrElse(EMPTY_FIELD),
                                                               Account.shared.getFamilyName()
                                                                       .getOrElse(EMPTY_FIELD),
                                                               Account.shared.getEmail()
                                                                       .getOrElse(EMPTY_FIELD));

                                    if (!memberToAdd.equals(mySelf)) {
                                        memberToAdd = mySelf;
                                        needToUpdateMyself = true;
                                    }
                                }

                                members.add(memberToAdd);
                            }

                            Event tempEvent =
                                    new Event(event.uuid,
                                              event.name,
                                              LocalDateTime.parse(event.datetime_start),
                                              LocalDateTime.parse(event.datetime_end),
                                              event.description, members);
                            Account.shared.addEvent(tempEvent);

                            if (needToUpdateMyself) {
                                Database.updateDatabase();
                            }

                            // TODO: remove
                            for (Event event1 : Account.shared.getPastEvents()) {
                                Log.e("###", event1.toString());
                            }
                            for (Event event1 : Account.shared.getFutureEvents()) {
                                Log.e("###", event1.toString());
                            }
                            Log.e("###", Account.shared.getCurrentEvent().toString());
                            // TODO: remove
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: uawz
            }
        };
    }
}
