/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.web.search;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.exception.EnkiveServletException;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;

public class SaveSearchWebScript extends AbstractWorkspaceWebscript {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8342072157628116473L;

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		String searchId = WebScriptUtils.cleanGetParameter(req, "id");
		String nameOfSavedSearch = WebScriptUtils
				.cleanGetParameter(req, "name");
		try {
			Workspace workspace = workspaceService.getActiveWorkspace();
			workspaceService.saveSearchWithName(workspace, searchId,
					nameOfSavedSearch);

			LOGGER.debug("saved search at id " + searchId + " with name \""
					+ nameOfSavedSearch + "\"");
		} catch (WorkspaceException e) {
			throw new EnkiveServletException("Could not mark search at UUID "
					+ searchId + "as saved", e);
		}
	}
}
