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
package com.linuxbox.enkive.testing;

import java.io.File;
import java.io.FileOutputStream;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.util.StreamConnector;

/*
 * Program to retrieve arbitrary objects from the data store, keyed by their
 * cryptographic identifier.  Borrows extensively from unit test code.
 *
 * export enkiveHome=/opt/enkive
 * export enkiveBuild=/home/matt/enkive/enkive2/
 * 
 * java -cp $enkiveBuild/bin:$enkiveHome/enkive.jar:$enkiveHome/lib/*: \
 *     com.linuxbox.enkive.testing.TestMongoDocRetrieval \
 *     3ba1489afcee26d3e0187777392dcfa5b33b1e84
 * Starting
 * retrieving 3ba1489afcee26d3e0187777392dcfa5b33b1e84 ENCODED to /tmp/3ba1489afcee26d3e0187777392dcfa5b33b1e84_enc.LinuxBoxW_P.120507.gif
 * retrieving 3ba1489afcee26d3e0187777392dcfa5b33b1e84 DECODED to /tmp/3ba1489afcee26d3e0187777392dcfa5b33b1e84_dec.LinuxBoxW_P.120507.gif
 * Done
 * 
 */

public class TestMongoDocRetrieval {
	// FIXME: Since this is defined in the spring configuration, it should be
	// retrieved from there rather than hard-coded
	private final static String DATABASE_NAME = "enkive";

	// FIXME: Since this is defined in the spring configuration, it should be
	// retrieved from there rather than hard-coded
	private final static String GRIDFS_COLLECTION_NAME = "fs";

	private final static String outputDir = "/tmp"; /* cmdline? */

	enum store_how {
		ENCODED, DECODED
	}

	private static void storeAsFile(Document d, store_how how) {
		FileOutputStream fileStream = null;
		File f = null;
		try {
			switch (how) {
			case ENCODED:
				f = new File(outputDir, d.getFilename() + "_enc."
						+ d.getFileExtension());
				fileStream = new FileOutputStream(f);
				StreamConnector.transferForeground(d.getEncodedContentStream(),
						fileStream);
				break;

			case DECODED:
			default:
				f = new File(outputDir, d.getFilename() + "_dec."
						+ d.getFileExtension());
				fileStream = new FileOutputStream(f);
				StreamConnector.transferForeground(d.getDecodedContentStream(),
						fileStream);
				break;
			}
			fileStream.close();
			System.out.println("retrieving " + d.getFilename() + " " + how
					+ " to " + f.getCanonicalPath());
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileStream != null) {
					fileStream.close();
				}
			} catch (Exception e) {
				// empty
			}
		}
	}

	public static String getIdentifierOpt(String[] args) {
		String identifier = args[0];

		return identifier;
	}

	public static void main(String[] args) {
		System.out.println("Starting");

		MongoGridDocStoreService docStoreService = null;

		try {
			docStoreService = new ConvenienceMongoGridDocStoreService(
					DATABASE_NAME, GRIDFS_COLLECTION_NAME);
			docStoreService.startup();

			String identifier = getIdentifierOpt(args);
			if (identifier == null) {
				System.out.println("usage: program <identifier>");
				return;
			}

			Document d = docStoreService.retrieve(identifier);

			storeAsFile(d, store_how.ENCODED);
			storeAsFile(d, store_how.DECODED);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		} finally {
			try {
				if (docStoreService != null) {
					docStoreService.shutdown();
				}
			} catch (Exception e) {
				// empty
			}
		}

		System.out.println("Done");
	}
}
