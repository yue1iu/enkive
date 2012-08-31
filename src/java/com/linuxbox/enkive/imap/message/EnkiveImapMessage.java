package com.linuxbox.enkive.imap.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;

import org.apache.james.mailbox.store.mail.model.AbstractMessage;
import org.apache.james.mailbox.store.mail.model.Property;
import org.springframework.util.StringUtils;

import com.linuxbox.enkive.message.Message;

public class EnkiveImapMessage extends AbstractMessage<String> {

	Message message;
	long uid;
	String mailboxId = "";

	public EnkiveImapMessage(Message message) {
		this.message = message;
	}

	@Override
	public Date getInternalDate() {
		return message.getDate();
	}

	@Override
	public String getMailboxId() {
		return mailboxId;
	}

	@Override
	public long getUid() {
		// TODO Auto-generated method stub
		return uid;
	}

	@Override
	public void setUid(long uid) {
		this.uid = uid;

	}

	@Override
	public void setModSeq(long modSeq) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getModSeq() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isAnswered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDraft() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFlagged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRecent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSeen() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setFlags(Flags flags) {
		// TODO Auto-generated method stub

	}

	@Override
	public InputStream getBodyContent() throws IOException {
		// TODO Auto-generated method stub
		System.out.println("get body");
		return new ByteArrayInputStream(""
				.getBytes());
		
	}

	@Override
	public InputStream getFullContent() throws IOException {
		System.out.println("Get Full");
		return new ByteArrayInputStream(message.getReconstitutedEmail()
				.getBytes());
	}
	
	@Override
	public String getMediaType() {
		return "";
	}

	@Override
	public String getSubType() {
		return "";
	}

	@Override
	public long getFullContentOctets() {
		try {
			return message.getReconstitutedEmail().length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public Long getTextualLineCount() {
		long lineCount = 0;
		try {
			lineCount = StringUtils.countOccurrencesOf(
					message.getReconstitutedEmail(), "\n");
			System.out.println("line count is " + lineCount);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lineCount;
	}

	@Override
	public InputStream getHeaderContent() throws IOException {
		return new ByteArrayInputStream(message.getOriginalHeaders().getBytes());
	}

	@Override
	public List<Property> getProperties() {
		// TODO Auto-generated method stub
		ArrayList<Property> properties = new ArrayList<Property>();
		return properties;
	}

	@Override
	protected int getBodyStartOctet() {
		// TODO Auto-generated method stub
		return 0;
	}

}
