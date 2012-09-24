package com.linuxbox.enkive.imap.message.mongo;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.enkive.imap.message.EnkiveImapMessageMapper;
import com.linuxbox.enkive.imap.mongo.MongoEnkiveImapConstants;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoEnkiveImapMessageMapper extends EnkiveImapMessageMapper {

	Mongo m;
	DB imapDB;
	DBCollection imapCollection;

	public MongoEnkiveImapMessageMapper(MailboxSession mailboxSession,
			EnkiveImapStore store, MessageRetrieverService retrieverService,
			Mongo m, String enkiveDbName, String imapCollectionName) {
		super(mailboxSession, store, retrieverService);
		imapDB = m.getDB(enkiveDbName);
		imapCollection = imapDB.getCollection(imapCollectionName);
	}

	@Override
	public long countMessagesInMailbox(Mailbox<String> mailbox)
			throws MailboxException {
		long messageCount = 0;
		if (mailbox.getName().equals(MailboxConstants.INBOX))
			messageCount = 1;
		else if (mailbox.getMailboxId() != null) {
			BasicDBObject mailboxQueryObject = new BasicDBObject();
			mailboxQueryObject.put("_id",
					ObjectId.massageToObjectId(mailbox.getMailboxId()));
			DBObject mailboxObject = imapCollection.findOne(mailboxQueryObject);

			Map<String, String> messageIds = null;
			Object messageIdsMap = mailboxObject
					.get(MongoEnkiveImapConstants.MESSAGEIDS);
			if (messageIdsMap instanceof Map) {
				messageIds = (Map<String, String>) messageIdsMap;
			}

			if (messageIds != null)
				messageCount = messageIds.size();
		}
		return messageCount;
	}

	@Override
	public SortedMap<Long, String> getMailboxMessageIds(Mailbox<String> mailbox,
			long fromUid, long toUid) {
		SortedMap<Long, String> messageIds = new TreeMap<Long, String>();
		if (mailbox.getName().equals(MailboxConstants.INBOX)) {
			if (fromUid <= 1)
				messageIds.put((long) 1,
						"e826e2fb14162842d9caf023163338d20e2820bb");
		} else if (mailbox.getMailboxId() != null) {
			BasicDBObject mailboxQueryObject = new BasicDBObject();
			mailboxQueryObject.put("_id",
					ObjectId.massageToObjectId(mailbox.getMailboxId()));
			DBObject mailboxObject = imapCollection.findOne(mailboxQueryObject);
			Object messageIdsMap = mailboxObject.get("msgids");

			Map<String, String> tmpMsgIds = null;
			if (messageIdsMap instanceof HashMap) {
				tmpMsgIds = (HashMap<String, String>) messageIdsMap;
			}

			if (tmpMsgIds == null || tmpMsgIds.isEmpty())
				return messageIds;

			if (fromUid > tmpMsgIds.size()) {
				// Do Nothing
			} else if (toUid < 0){
				TreeSet<Long> sortedUids = new TreeSet<Long>();
				for (String key : tmpMsgIds.keySet())
					sortedUids.add(Long.parseLong(key));
				SortedSet<Long> sortedSubSet = sortedUids
						.tailSet(fromUid, true);
				for (Long key : sortedSubSet)
					messageIds.put(key, tmpMsgIds.get(key.toString()));
			} else if (fromUid == toUid) {
				messageIds.put(fromUid, tmpMsgIds.get(String.valueOf(fromUid)));
			} else {
				TreeSet<Long> sortedUids = new TreeSet<Long>();
				for (String key : tmpMsgIds.keySet())
					sortedUids.add(Long.parseLong(key));
				SortedSet<Long> sortedSubSet = sortedUids
						.subSet(fromUid, toUid);
				for (Long key : sortedSubSet)
					messageIds.put(key, tmpMsgIds.get(key.toString()));
			}
		}
		return messageIds;
	}
}
