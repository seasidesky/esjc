package com.github.msemys.esjc;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.github.msemys.esjc.matcher.RecordedEventListMatcher.hasItems;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.*;

public class ITIterateAllEventsBackward extends AbstractIntegrationTest {

    @Override
    protected EventStore createEventStore() {
        return eventstoreSupplier.get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsToIterateWhenBatchSizeIsZero() {
        eventstore.iterateAllEventsBackward(Position.START, 0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsToIterateWhenBatchSizeOutOfRange() {
        eventstore.iterateAllEventsBackward(Position.START, 4097, false);
    }

    @Test
    public void returnsEmptyIteratorIfAskedToIterateFromStart() {
        final String stream = generateStreamName();

        eventstore.appendToStream(stream, ExpectedVersion.NO_STREAM, newTestEvents(20)).join();

        Iterator<ResolvedEvent> iterator = eventstore.iterateAllEventsBackward(Position.START, 1, false);

        assertFalse(iterator.hasNext());
    }

    @Test
    public void iteratesFirstEvent() {
        final String stream = generateStreamName();

        eventstore.appendToStream(stream, ExpectedVersion.NO_STREAM, newTestEvents(20)).join();

        List<ResolvedEvent> firstEvents = eventstore.readAllEventsForward(Position.START, 2, false).join().events;

        Position position = firstEvents.get(1).originalPosition;

        Iterator<ResolvedEvent> iterator = eventstore.iterateAllEventsBackward(position, 1, false);

        assertTrue(iterator.hasNext());
        assertEquals(firstEvents.get(0).event.eventId, iterator.next().event.eventId);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void returnsEventsInReversedOrderComparedToWritten() {
        final String stream = generateStreamName();

        List<EventData> events = newTestEvents(20);
        eventstore.appendToStream(stream, ExpectedVersion.NO_STREAM, events).join();

        Iterator<ResolvedEvent> iterator = eventstore.iterateAllEventsBackward(Position.END, 6, false);

        List<RecordedEvent> result = new ArrayList<>();
        range(0, 20).forEach(i -> result.add(iterator.next().event));

        List<EventData> reversedEvents = reverse(events);

        range(0, 20).forEach(i -> assertEquals(reversedEvents.get(i).eventId, result.get(i).eventId));
    }

    @Test
    public void iteratesAllEventsOneByOneUntilEndOfStream() {
        final String stream = generateStreamName();

        List<EventData> events = newTestEvents(20);
        eventstore.appendToStream(stream, ExpectedVersion.NO_STREAM, events).join();

        List<RecordedEvent> allEvents = new ArrayList<>();
        eventstore.iterateAllEventsBackward(Position.END, 1, false).forEachRemaining(e -> allEvents.add(e.event));

        assertThat(allEvents.stream().limit(events.size()).collect(toList()), hasItems(reverse(events)));
    }

    @Test
    public void iteratesAllEventsUntilEndOfStream() {
        final String stream = generateStreamName();

        List<EventData> events = newTestEvents(20);
        eventstore.appendToStream(stream, ExpectedVersion.NO_STREAM, events).join();

        List<RecordedEvent> allEvents = new ArrayList<>();
        eventstore.iterateAllEventsBackward(Position.END, 5, false).forEachRemaining(e -> allEvents.add(e.event));

        assertThat(allEvents.stream().limit(events.size()).collect(toList()), hasItems(reverse(events)));
    }

    @Test
    public void iteratesAllEventsUntilEndOfStreamUsingMaxBatchSize() {
        final String stream = generateStreamName();

        List<EventData> events = newTestEvents(20);
        eventstore.appendToStream(stream, ExpectedVersion.NO_STREAM, events).join();

        List<RecordedEvent> allEvents = new ArrayList<>();
        eventstore.iterateAllEventsBackward(Position.END, 4096, false).forEachRemaining(e -> allEvents.add(e.event));

        assertThat(allEvents.stream().limit(events.size()).collect(toList()), hasItems(reverse(events)));
    }

}
