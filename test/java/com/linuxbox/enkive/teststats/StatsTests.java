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
 ******************************************************************************/
package com.linuxbox.enkive.teststats;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ StatsMongoAttachTest.class, StatsClientTest.class,
		StatsMongoCollTest.class, StatsMongoDbTest.class,
		StatsRemovalTest.class, StatsMsgTest.class, StatsRuntimeTest.class,
		StatsMongoStorageAndRetrievalTest.class, StatsHourGrainTest.class,
		StatsDayGrainTest.class, StatsWeekGrainTest.class,
		StatsMonthGrainTest.class })
public class StatsTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(StatsTests.class.getName());
		// $JUnit-BEGIN$
		// $JUnit-END$
		return suite;
	}
}
