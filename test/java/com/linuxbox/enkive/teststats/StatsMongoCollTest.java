package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.linuxbox.enkive.TestingConstants;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bson.BasicBSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.linuxbox.enkive.statistics.StatsMongoCollectionProperties;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@RunWith(value = Parameterized.class)
public class StatsMongoCollTest {
	private static StatsMongoCollectionProperties collStats;
	private static BasicDBObject allStats;
	private static DB db;
	private String collName;

	public StatsMongoCollTest(String collName) {
		this.collName = collName;
	}

	@Parameters
	public static Collection<Object[]> data() {
		Mongo m = null;
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);	
		} catch (MongoException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
		collStats = new StatsMongoCollectionProperties(m, TestingConstants.MONGODB_TEST_DATABASE);
		allStats = collStats.getAllStats();
		Iterator<String> collectionNames = db.getCollectionNames().iterator();
		List<Object[]> data = new ArrayList<Object[]>();
		System.out.println("Empty Collections:");
		while(collectionNames.hasNext()){
			String name = collectionNames.next();
			if(db.getCollection(name).count() > 0){
				Object[] thing = { name };
				data.add(thing);
			}
			else{
				System.out.println(name);
			}
		}
		System.out.println("\nTesting Collections:");
		return data;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test 
	public void pushTest() {
		System.out.println(collName);
	}
	
	@Test
	public void typeTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		String type = (String) obj.get(STAT_TYPE);
		assertNotNull("in " + collName + " (type = null)", type);
		assertTrue(
				"in " + collName + " (type = " + type + ")",
				type.compareTo("collection") == 0
						|| type.compareTo("database") == 0
						|| type.compareTo("runtime") == 0);
	}

	@Test
	public void nameTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		String name = (String) obj.get(STAT_NAME);
		assertNotNull("in " + collName + " (name = null)", name);
		assertTrue("in " + collName + "(name = " + name + ")",
				name.compareTo(collName) == 0);
	}

	@Test
	public void namespaceExistsTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (obj = null)", obj);
		assertTrue(
				"in " + collName + "does not contain field(" + STAT_NS + ")",
				obj.containsField(STAT_NS));
	}

	// GT means 'greater than'
	@Test
	public void numObjsGTZeroTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (numObjs = null)",
				(Integer) obj.get(STAT_NUM_OBJS));
		int numObjs = ((Integer) obj.get(STAT_NUM_OBJS)).intValue();
		assertTrue("in " + collName + " (numObjs = " + numObjs + ") ",
				numObjs > 0);
	}

	@Test
	public void avgObjsGTZeroTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (avgObjs = null)",
				((Double) obj.get(STAT_AVG_OBJ_SIZE)));
		double avgObjs = ((Double) obj.get(STAT_AVG_OBJ_SIZE)).doubleValue();
		assertTrue("in " + collName + " (avgObjs = " + avgObjs + ") ",
				avgObjs > 0);
	}

	@Test
	public void dataGTZeroTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (data = null)",
				((Integer) obj.get(STAT_DATA_SIZE)));
		int data = ((Integer) obj.get(STAT_DATA_SIZE)).intValue();
		assertTrue("in " + collName + " (data = " + data + ") ", data > 0);
	}

	@Test
	public void storageGTZeroTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (storage = null)",
				((Integer) obj.get(STAT_TOTAL_SIZE)));
		int storage = ((Integer) obj.get(STAT_TOTAL_SIZE)).intValue();
		assertTrue("in " + collName + " (storage = " + storage + ") ",
				storage > 0);
	}

	@Test
	public void extentsGTZeroTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (numExtents = null)",
				(Integer) obj.get(STAT_NUM_EXTENT));
		int numExtents = ((Integer) obj.get(STAT_NUM_EXTENT)).intValue();
		assertTrue(collName, numExtents > 0);
	}

	@Test
	public void lastExtentSizeGTZeroTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (lastExtentsSize = null)",
				((Integer) obj.get(STAT_LAST_EXTENT_SIZE)));
		int lastExtentSize = ((Integer) obj.get(STAT_LAST_EXTENT_SIZE))
				.intValue();
		assertTrue(collName, lastExtentSize > 0);
	}

	@Test
	public void numIndexesGTEZeroTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (numIndexes = null)",
				((Integer) obj.get(STAT_NUM_INDEX)));
		int numIndexes = ((Integer) obj.get(STAT_NUM_INDEX)).intValue();
		assertTrue("in " + collName + "(numIndexes = " + numIndexes + ")",
				numIndexes >= 0);
	}

	@Test
	public void indexSizeGTEZeroTest() {
		BasicBSONObject obj = (BasicBSONObject) allStats.get(collName);
		assertNotNull("in " + collName + " (totalIndexSize = null)",
				(Integer) obj.get(STAT_TOTAL_INDEX_SIZE));
		Integer integer = (Integer) obj.get(STAT_TOTAL_INDEX_SIZE);
		int indexSize = integer.intValue();
		assertTrue("in " + collName + "(indexSize = " + indexSize + ")",
				indexSize >= 0);
	}
}