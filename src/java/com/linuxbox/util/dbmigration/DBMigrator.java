package com.linuxbox.util.dbmigration;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBMigrator {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.dbmigration.DBMigrator");
	Map<Integer, DBMigration> migrations = new HashMap<Integer, DBMigration>();
	String migratorName;
	DBInfo db;

	public DBMigrator(String migratorName, DBInfo db) {
		this.migratorName = migratorName;
		this.db = db;
	}

	public void registerMigration(DBMigration migration)
			throws DBMigrationException {
		if (migrations.containsValue(migration.fromVersion)) {
			throw new DBMigrationException("already have migration for "
					+ migratorName + " from version " + migration.fromVersion);
		}
		migrations.put(migration.fromVersion, migration);
	}

	public int runThrough(int fromVersion, int toVersion)
			throws DBMigrationException {
		while (fromVersion < toVersion) {
			Integer newVersion = runNext(fromVersion);
			if (newVersion == null) {
				throw new DBMigrationException(migratorName + " could not find the migration for version " + fromVersion);
			}
			fromVersion = newVersion;
		}
		return toVersion;
	}

	@PostConstruct
	public void init(){
		LOGGER.info("Running " + migratorName);
//		System.out.println("Running " + migratorName);
		try {
			runAll(db.getCurrentVersion());
		} catch (DBMigrationException e) {
			//TODO not sure what to do in this case
			e.printStackTrace();
		}
	}
	
	public int runAll(int fromVersion) throws DBMigrationException {
		Integer lastVersion = fromVersion;
		Integer newVersion = runNext(lastVersion);
		while (newVersion != null) {
			lastVersion = newVersion;
			newVersion = runNext(lastVersion);
		}
		return lastVersion;
	}

	Integer runNext(int fromVersion) throws DBMigrationException {
		DBMigration nextMigration = migrations.get(fromVersion);
		if (nextMigration == null) {
			return null;
		} else {
			nextMigration.migrate(db);
			// NOAH: is this the best place to update the stored version number?
			return nextMigration.toVersion;
		}
	}
}
