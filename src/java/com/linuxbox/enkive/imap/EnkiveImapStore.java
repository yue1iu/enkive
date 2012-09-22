package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.ModSeqProvider;
import org.apache.james.mailbox.store.mail.UidProvider;
import org.apache.james.mailbox.store.mail.model.Mailbox;

public abstract class EnkiveImapStore implements UidProvider<String>,
		ModSeqProvider<String> {

	@Override
	public long nextModSeq(MailboxSession session, Mailbox<String> mailbox)
			throws MailboxException {
		return 1;
	}

	@Override
	public long highestModSeq(MailboxSession session, Mailbox<String> mailbox)
			throws MailboxException {
		return 1;
	}
	
	@Override
	public long nextUid(MailboxSession session, Mailbox<String> mailbox)
			throws MailboxException {
		return lastUid(session, mailbox) + 1;
	}

}
