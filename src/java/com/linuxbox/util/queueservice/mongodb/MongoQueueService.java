package com.linuxbox.util.queueservice.mongodb;

import static com.linuxbox.util.mongodb.MongoDBConstants.OBJECT_ID_KEY;

import java.net.UnknownHostException;
import java.util.Date;

import org.bson.types.BSONTimestamp;
import org.bson.types.ObjectId;

import com.linuxbox.util.queueservice.AbstractQueueEntry;
import com.linuxbox.util.queueservice.QueueEntry;
import com.linuxbox.util.queueservice.QueueService;
import com.linuxbox.util.queueservice.QueueServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoQueueService implements QueueService {
	private static final String STATUS_FIELD = "status";
	private static final String CREATED_AT_FIELD = "createdAt";
	private static final String DEQUEUED_AT_FIELD = "dequeuedAt";
	private static final String IDENTIFIER_FIELD = "identifier";
	private static final String NOTE_FIELD = "note";

	private static final int STATUS_INITIAL = 1;
	private static final int STATUS_PENDING = 2;

	// Some standard DBObjects used for queries; only create them once and re-use.

	private static final DBObject QUERY_INITIAL_STATUS = new BasicDBObject(
			STATUS_FIELD, STATUS_INITIAL);
	private static final DBObject UPDATE_TO_PENDING = new BasicDBObject(
			DEQUEUED_AT_FIELD, new BSONTimestamp()).append(STATUS_FIELD,
			STATUS_PENDING);
	private static final DBObject DEQUEUE_FIELDS = new BasicDBObject(
			OBJECT_ID_KEY, 1).append(IDENTIFIER_FIELD, 1).append(NOTE_FIELD, 1)
			.append(CREATED_AT_FIELD, 1);
	private static final DBObject SORT_BY_CREATED_AT = new BasicDBObject(
			CREATED_AT_FIELD, 1);

	@SuppressWarnings("unused")
	private static final int STATUS_COMPLETE = 3;

	private Mongo mongo;
	private DB mongoDB;
	private DBCollection queueCollection;

	/**
	 * Keep track of whether we created the instance of Mongo, as if so, we'll
	 * have to close it.
	 */
	private boolean createdMongo;

	public MongoQueueService(String server, int port, String database,
			String collection) throws UnknownHostException, MongoException {
		this(new Mongo(server, port), database, collection);
		createdMongo = true;
	}

	public MongoQueueService(String database, String collection)
			throws UnknownHostException, MongoException {
		this(new Mongo(), database, collection);
		createdMongo = true;
	}

	public MongoQueueService(Mongo mongo, String database, String collection) {
		this.mongo = mongo;
		this.mongoDB = mongo.getDB(database);
		this.queueCollection = mongoDB.getCollection(collection);

		// For MongoDB multi-key indexes to work the most efficiently, the
		// queried fields should appear before the sorting fields in the
		// indexes.

		// This index is used for finding the earliest entry with a given
		// status.
		final DBObject statusTimestampIndex = new BasicDBObject(STATUS_FIELD, 1)
				.append(CREATED_AT_FIELD, 1);
		queueCollection.ensureIndex(statusTimestampIndex,
				"statusTimestampIndex");

		// This index is used for finding the earliest entry with a given status
		// and identifier.
		final DBObject statusIdentifierTimestampIndex = new BasicDBObject(
				STATUS_FIELD, 1).append(IDENTIFIER_FIELD, 1).append(
				CREATED_AT_FIELD, 1);
		queueCollection.ensureIndex(statusIdentifierTimestampIndex,
				"statusIdentifierTimestampIndex");

		// Make sure data is written out to disk before operation is complete.
		queueCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);
	}

	@Override
	public void startup() throws QueueServiceException {
		// empty
	}

	@Override
	public void shutdown() throws QueueServiceException {
		if (createdMongo) {
			mongo.close();
		}
	}

	@Override
	public void enqueue(String identifier) throws QueueServiceException {
		enqueue(identifier, null);
	}

	@Override
	public void enqueue(String identifier, Object note)
			throws QueueServiceException {
		final DBObject entry = new BasicDBObject(CREATED_AT_FIELD,
				new BSONTimestamp()).append(STATUS_FIELD, STATUS_INITIAL)
				.append(IDENTIFIER_FIELD, identifier).append(NOTE_FIELD, note);
		WriteResult result = queueCollection.insert(entry);
		if (!result.getLastError().ok()) {
			throw new QueueServiceException("could not enqueue \"" + identifier
					+ "\" (note: \"" + note.toString() + "\")", result
					.getLastError().getException());
		}
	}

	@Override
	public QueueEntry dequeue() throws QueueServiceException {
		return dequeueHelper(QUERY_INITIAL_STATUS);
	}

	@Override
	public QueueEntry dequeue(String identifer) throws QueueServiceException {
		final BasicDBObject query = new BasicDBObject();
		query.putAll(QUERY_INITIAL_STATUS);
		query.append(IDENTIFIER_FIELD, identifer);
		return dequeueHelper(query);
	}

	private QueueEntry dequeueHelper(DBObject query) {
		final DBObject result = queueCollection.findAndModify(query,
				DEQUEUE_FIELDS, SORT_BY_CREATED_AT, false, UPDATE_TO_PENDING,
				false, false);

		if (result == null) {
			return null;
		}

		final BSONTimestamp createdAtBSON = (BSONTimestamp) result
				.get(CREATED_AT_FIELD);
		final Date createdAt = new Date(createdAtBSON.getTime() * 1000L);

		return new MongoQueueEntry((ObjectId) result.get(OBJECT_ID_KEY),
				(String) result.get(IDENTIFIER_FIELD), result.get(NOTE_FIELD),
				createdAt);
	}

	@Override
	public void finish(QueueEntry entry) throws QueueServiceException {
		if (entry instanceof MongoQueueEntry) {
			final MongoQueueEntry mongoEntry = (MongoQueueEntry) entry;
			final DBObject query = new BasicDBObject(OBJECT_ID_KEY,
					mongoEntry.mongoID);
			final DBObject result = queueCollection.findAndRemove(query);
			if (result == null) {
				throw new QueueServiceException(
						"could not finish queue entry \""
								+ entry.getIdentifier() + "\" "
								+ entry.getEnqueuedAt());
			}
		} else {
			throw new QueueServiceException(
					"tried to finish a queue entry that was not generated by this queue");
		}
	}

	static class MongoQueueEntry extends AbstractQueueEntry {
		private ObjectId mongoID;

		public MongoQueueEntry(ObjectId mongoID, String identifier,
				Object note, Date enqueuedAt) {
			super(identifier, note, enqueuedAt);
			this.mongoID = mongoID;
		}
	}
}