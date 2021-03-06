/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.linuxbox.enkive.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.stream.MimeConfig;

public class SinglePartHeaderImpl extends AbstractSinglePartHeader implements
		SinglePartHeader {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message");

	public SinglePartHeaderImpl() {
		// empty
	}

	@Override
	public void parseHeaders(String partHeaders) {
		parseHeaders(partHeaders, new MimeConfig());
	}

	@Override
	public void parseHeaders(String partHeaders, MimeConfig config) {
		ByteArrayInputStream dataStream = new ByteArrayInputStream(
				partHeaders.getBytes());

		Message headers = null;
		try {
			DefaultMessageBuilder builder = new org.apache.james.mime4j.message.DefaultMessageBuilder();
			builder.setMimeEntityConfig(config);
			headers = builder.parseMessage(dataStream);
		} catch (Exception e) {
			LOGGER.error("Could not parse headers for message", e);
			// Need headers anyway
			headers = new MessageImpl();
		}

		setContentDisposition(headers.getDispositionType());

		setContentType(headers.getMimeType());
		String fname = headers.getFilename();
		if (fname == null) {
			// dang: mime4j doesn't handle RFC 5978 encoded filenames; it merely stores
			// it as a parameter named "filename*".  As a workaround until a proper fix
			// can be implemented, try and get the contents of the "filename*" parameter
			// if the filename is n't set.
			try {
				ContentDispositionField field = (ContentDispositionField)
						headers.getHeader().getField(FieldName.CONTENT_DISPOSITION);
				fname = field.getParameter("filename*");
				// Try to decode a RFC 5987 encoded filename
				String[] parts = fname.split("'",3);
				if (parts.length != 3) {
					throw new UnsupportedEncodingException("Bad filename encoding");
				}
				fname =  URLDecoder.decode(parts[2], parts[0]);
			} catch (Exception e) {
				fname = null;
			}
		}
		setFileName(fname);
	}

	@Override
	public void pushReconstitutedEmail(Writer output) throws IOException {
		try {
			output.write(getOriginalHeaders());
			output.write(getLineEnding());
			IOUtils.copy(getEncodedContentData().getEncodedContent(), output);
			output.write(getLineEnding());
		} catch (ContentException e) {
			throw new IOException(e);
		} finally {
			output.flush();
		}
	}

	@Override
	public String printSinglePartHeader() throws IOException {
		StringWriter w = new StringWriter();
		pushReconstitutedEmail(w);
		return w.toString();
	}

	@Override
	public Set<String> getAttachmentFileNames() {
		Set<String> result = newSet();
		if (getFilename() != null) {
			result.add(getFilename());
		}
		return result;
	}

	@Override
	public Set<String> getAttachmentTypes() {
		Set<String> result = newSet();
		if (getContentType() != null) {
			result.add(getContentType());
		}
		return result;
	}

	@Override
	public Set<String> getAttachmentUUIDs() {
		Set<String> result = newSet();

		result.add(getEncodedContentData().getUUID());
		return result;
	}

	@Override
	public void generateAttachmentSummaries(List<AttachmentSummary> result,
			Deque<Integer> positionsAbove) {
		final EncodedContentReadData content = getEncodedContentData();
		final AttachmentSummary summary = new AttachmentSummary(
				content.getUUID(), getFilename(), getContentType(),
				positionsAbove);
		result.add(summary);
	}
}
