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

import static com.linuxbox.util.Version.Type.PRODUCTION;
import static com.linuxbox.util.Version.Type.RELEASE_CANDIDATE;

import com.linuxbox.util.Version;

public interface ProductInfo {
	/*
	 * History of released versions with monotonically increasing ordinals;
	 * maintaining this history allows for DB migrations.
	 */
	final static Version V1_2P = new Version(1, 2, PRODUCTION, 0);
	final static Version V1_3P = new Version(1, 3, PRODUCTION, 5);
	final static Version V1_3_1RC1 = new Version("1.3.1 RC 1", 6);
	final static Version V1_3_1P = new Version(1, 3, PRODUCTION, 7, "1.3.1");
	final static Version V1_4RC = new Version(1, 4, RELEASE_CANDIDATE, 8);
	final static Version V1_4RC2 = new Version("1.4 RC 2", 9);
	final static Version V1_4P = new Version(1, 4, PRODUCTION, 10);
	/*
	 * IMPORTANT: be certain to add software versions and associated database
	 * versions to EnkiveDbVersionManager as well.
	 */

	final static String PRODUCT = "Enkive CE";
	final static String COPYRIGHT = "Copyright 2013 The Linux Box Corporation; all rights "
			+ "reserved except those granted under license.";
	final static String LICENSE = "Licensed under the GNU Affero General Public License "
			+ "version 3 or later.";
	final static Version VERSION = V1_4P;
}
