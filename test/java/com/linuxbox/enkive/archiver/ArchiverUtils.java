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
package com.linuxbox.enkive.archiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

public class ArchiverUtils {

	public static Collection<Object[]> getAllTestFiles(File dir) {
		Collection<Object[]> files = new ArrayList<Object[]>();
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				files.add(new File[] { file });
			} else {
				files.addAll(getAllTestFiles(file));
			}
		}
		return files;
	}

	public static String readMessage(InputStream inputStream)
			throws IOException {
		StringBuffer message = new StringBuffer();
		InputStreamReader reader = new InputStreamReader(inputStream);

		Reader in = new BufferedReader(reader);
		int ch;
		while ((ch = in.read()) > -1) {
			message.append((char) ch);
		}
		in.close();
		return message.toString();
	}

}
