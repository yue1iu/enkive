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
package com.linuxbox.enkive;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.tool.mongodb.MongoDBIndexManager;
import com.linuxbox.util.DirectoryManagement;
import com.linuxbox.util.dbmigration.DBMigrationService;
import com.linuxbox.util.dbmigration.DBMigrationService.UpToDateException;

public abstract class Main {
	protected static final Log LOGGER;
	private static final String USER = AuditService.USER_SYSTEM;
	private static final String DESCRIPTION = "com.linuxbox.enkive.Main.main";

	protected String[] versionCheckConfigFiles = { "enkive-version-check.xml" };

	protected String[] configFiles;

	private AbstractApplicationContext context;

	protected abstract void doEventLoop(ApplicationContext context);

	protected abstract void startup();

	protected abstract void shutdown();

	static {
		try {
			DirectoryManagement.verifyDirectory(
					GeneralConstants.DEFAULT_LOG_DIRECTORY,
					"default logging directory");
		} catch (IOException e) {
			throw new Error(e);
		}
		LOGGER = LogFactory.getLog("com.linuxbox.enkive");
	}

	public Main(String[] configFiles, String[] arguments) {
		this.configFiles = configFiles;
	}

	public void run() throws IOException {
		try {
			AbstractApplicationContext versionCheckingContext = new ClassPathXmlApplicationContext(
					versionCheckConfigFiles);
			Map<String, DBMigrationService> migrationServices = versionCheckingContext
					.getBeansOfType(DBMigrationService.class);
			for (Entry<String, DBMigrationService> service : migrationServices
					.entrySet()) {
				service.getValue().isUpToDate();
			}
			versionCheckingContext.close();

			/* IF WE GET HERE, THE DATABASE IS APPARENTLY UP TO DATE */

			startup();

			context = new ClassPathXmlApplicationContext(configFiles);
			context.registerShutdownHook();

			try {
				final AuditService auditService = context.getBean(
						"AuditLogService", AuditService.class);

				auditService.addEvent(AuditService.SYSTEM_STARTUP, USER,
						DESCRIPTION);

				final MongoDBIndexManager mongoIndexManager = context
						.getBean(MongoDBIndexManager.class);
				mongoIndexManager.runCheckAndAutoEnsure();

				doEventLoop(context);

				auditService.addEvent(AuditService.SYSTEM_SHUTDOWN, USER,
						DESCRIPTION);
			} catch (AuditServiceException e) {
				LOGGER.error(
						"received AuditServiceException: " + e.getMessage(), e);
			}

			context.close();

			shutdown();
		} catch (UpToDateException e) {
			LOGGER.fatal("database is apparently not up to date", e);
		}
	}
}
