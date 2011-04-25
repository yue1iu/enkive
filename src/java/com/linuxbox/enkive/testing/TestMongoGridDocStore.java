package com.linuxbox.enkive.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import lemurproject.indri.IndexStatus;

import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.docsearch.SearchService;
import com.linuxbox.enkive.docsearch.contentanalyzer.tika.TikaContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docsearch.indri.IndriSearchService;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.EncodedChainedDocument;
import com.linuxbox.enkive.docstore.EncodedDocument;
import com.linuxbox.enkive.docstore.InMemorySHA1Document;
import com.linuxbox.enkive.docstore.SimpleDocument;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.util.StreamConnector;
import com.mongodb.DB;
import com.mongodb.Mongo;

public class TestMongoGridDocStore {
	private static final String INDRI_REPOSITORY_PATH = "/tmp/enkive-indri";
	private static final String INDRI_TEMP_STORAGE_PATH = "/tmp/enkive-indri-tmp";
	private static final boolean DO_INDEXING = true;

	static DocStoreService docStoreService;
	static SearchService docSearchService;
	static Set<String> indexSet = new HashSet<String>();

	static class RecordedIndexStatus extends IndexStatus {
		public int code;
		public String documentPath;
		public String error;
		public int documentsIndexed;
		public int documentsSeen;

		public void status(int code, String docPath, String error,
				int docsIndexed, int docsSeen) {
			this.code = code;
			this.documentPath = docPath;
			this.error = error;
			this.documentsIndexed = docsIndexed;
			this.documentsSeen = docsSeen;
		}

		public void display(PrintStream out) {
			System.out.println("code: " + code);
			System.out.println("document path: " + documentPath);
			System.out.println("error: " + error);
			System.out.println("documents indexed: " + documentsIndexed);
			System.out.println("documents seen: " + documentsSeen);
		}
	}
	
	private static void index(StoreRequestResult storageResult) throws DocSearchException, DocStoreException {
		if (DO_INDEXING) {
			final String identifier = storageResult.getIdentifier();
			
			if (!storageResult.getAlreadyStored()) {
				System.out.println("START indexing " + identifier);
				docSearchService.indexDocument(identifier);
				System.out.println("END indexing " + identifier);
			}
		}
	}

	private static void archive(String content) throws DocStoreException, DocSearchException {
		StoreRequestResult result  = docStoreService.store(new SimpleDocument(content,
				"text/plain", "txt"));
		index(result);
		indexSet.add(result.getIdentifier());
	}

	private static void archiveAll() {
		final String duplicated = "This is another test.";
		final String[] documents = { "This is a test.", duplicated,
				"This is a third test.", "This is a fourth test.", duplicated };

		for (String doc : documents) {
			try {
				archive(doc);
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	private static void retrieveAll() {
		for (String index : indexSet) {
			System.out.print("retrieving " + index + ": ");
			try {
				Document d = docStoreService.retrieve(index);
				String s = new String(d.getContentBytes());
				System.out.println(s);
			} catch (DocumentNotFoundException e) {
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private final static String inputDir = "../..";

	static class FileRecord {
		String name;
		String mimeType;
		String suffix;
		String encoding;
		String characterSet;

		FileRecord(String a, String b, String c, String d, String e) {
			this.name = a;
			this.mimeType = b;
			this.suffix = c;
			this.encoding = d;
			this.characterSet = e;
		}

		FileRecord(String a, String b, String c, String d) {
			this(a, b, c, d, null);
		}
	}

	private final static FileRecord[] encodedFiles = {
			new FileRecord("1-b64.pdf", "application/pdf", "pdf",
					MimeUtil.ENC_BASE64),
			new FileRecord("2-b64.pdf", "application/pdf", "pdf",
					MimeUtil.ENC_BASE64),
			new FileRecord("3-qp.txt", "text/plain", "txt",
					MimeUtil.ENC_QUOTED_PRINTABLE, "windows-1252") };

	private final static Set<String> encodedIdentifierSet = new HashSet<String>();

	private static void archiveEncoded() {
		for (FileRecord fileRec : encodedFiles) {
			try {
				File f = new File(inputDir + "/" + fileRec.name);
				FileInputStream fileStream = new FileInputStream(f);

				Document d = new InMemorySHA1Document(fileRec.mimeType,
						fileRec.suffix, fileStream);
				fileStream.close();

				EncodedDocument ed = new EncodedChainedDocument(
						fileRec.encoding, d);
				encodedIdentifierSet.add(ed.getIdentifier());
				StoreRequestResult result = docStoreService.store(ed);
				index(result);

				System.out.println("archived encoded " + ed.getIdentifier());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private static void retrieveEncoded() {
		int counter = 0;
		for (String identifier : encodedIdentifierSet) {
			FileOutputStream fileStream = null;
			++counter;
			try {
				Document d = docStoreService.retrieve(identifier);
				File f = new File(inputDir + "/" + counter + "."
						+ d.getSuffix());
				fileStream = new FileOutputStream(f);
				StreamConnector.transferForeground(d.getContentStream(),
						fileStream);
				fileStream.close();
				System.out.println("wrote " + d.getIdentifier() + " to "
						+ f.getCanonicalPath());
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
	}

	private static void searchEncoded() {
		for (String identifier : encodedIdentifierSet) {
			try {
				docSearchService.indexDocument(identifier);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void dropCollection(DB db, String collection) {
		if (db.collectionExists(collection)) {
			db.getCollection(collection).drop();
		}
	}

	private static void dropGridFSCollections(String dbName, String bucket)
			throws UnknownHostException {
		DB db = new Mongo().getDB(dbName);
		dropCollection(db, bucket + ".files");
		dropCollection(db, bucket + ".chunks");
	}

	public static void main(String[] args) {
		System.out.println("Starting");

		try {
			dropGridFSCollections("enkive", "fs");
			docStoreService = new MongoGridDocStoreService("enkive", "fs");

			docSearchService = new IndriSearchService(docStoreService,
					new TikaContentAnalyzer(), INDRI_REPOSITORY_PATH,
					INDRI_TEMP_STORAGE_PATH);
			/*
			 * searchService = new TestDisplaySearchService(docService, new
			 * TikaContentAnalyzer());
			 */

			archiveAll();
			retrieveAll();

			archiveEncoded();
			retrieveEncoded();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}
}
