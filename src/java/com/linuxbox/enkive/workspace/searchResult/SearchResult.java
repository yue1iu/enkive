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
package com.linuxbox.enkive.workspace.searchResult;

import java.util.HashSet;
import java.util.Set;

import com.linuxbox.enkive.workspace.WorkspaceException;

/**
 * Abstract representation of a search result.  A result consists of the set of
 * message IDs that match the search, and a link to the @ref SearchQuery that
 * generated the results.
 * @author dang
 *
 */
public abstract class SearchResult {

	private String id;
	private Set<String> messageIds;
	protected String searchQueryId;

	public SearchResult() {
		messageIds = new HashSet<String>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<String> getMessageIds() {
		return messageIds;
	}

	public void setMessageIds(Set<String> messageIds) {
		this.messageIds = messageIds;
	}

	public String getSearchQueryId() {
		return searchQueryId;
	}

	public void setSearchQueryId(String searchQueryId) {
		this.searchQueryId = searchQueryId;
	}

	public abstract void saveSearchResult() throws WorkspaceException;

	public abstract void deleteSearchResult() throws WorkspaceException;

	public abstract void sortSearchResultMessages(String sortBy, int sortDir)
			throws WorkspaceException;
}
