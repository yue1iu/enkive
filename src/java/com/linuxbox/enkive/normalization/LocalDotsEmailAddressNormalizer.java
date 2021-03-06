package com.linuxbox.enkive.normalization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some emailers such as Gmail, ignore periods ("."s) in email addresses. Thus
 * janedoe@gmail.com is considered the same as jane.doe@gmail.com. This
 * normalizer removes all periods in the local part of an email address to
 * normalize the address.
 * 
 * For more information, see:
 * http://en.wikipedia.org/wiki/Email_address#Local-part_normalization
 */
public class LocalDotsEmailAddressNormalizer extends
		AbstractChainedEmailAddressNormalizer {
	final static Pattern DOT_RE = Pattern.compile("\\.");

	public LocalDotsEmailAddressNormalizer(EmailAddressNormalizer prior) {
		super(prior);
	}

	public LocalDotsEmailAddressNormalizer() {
		this(null);
	}

	@Override
	protected String myMap(String emailAddress) {
		final String[] parts = splitAddress(emailAddress);
		if (parts.length != 2) {
			return emailAddress;
		}
		
		final String localPart = parts[0];
		final String domainPart = parts[1];

		StringBuilder result = new StringBuilder();

		Matcher m = DOT_RE.matcher(localPart);
		result.append(m.replaceAll(""));
		result.append('@');
		result.append(domainPart);

		return result.toString();
	}
}
