/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.workspace.searchResult.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHQUERYID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Implementation of @ref SearchResultBuilder in MongoDB.  Creates or finds @ref
 * MongoSearchResult objects
 * @author dang
 *
 */
public class MongoSearchResultBuilder implements SearchResultBuilder {
	DBCollection searchResultsColl;
	MongoSearchResultUtils searchResultUtils;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoSearchResultBuilder(MongoClient m, String searchResultsDBName,
			String searchResultsCollName) {
		this(m.getDB(searchResultsDBName).getCollection(searchResultsCollName));
	}

	public MongoSearchResultBuilder(MongoDbInfo searchResultsInfo) {
		this(searchResultsInfo.getCollection());
	}

	public MongoSearchResultBuilder(DBCollection searchResultsColl) {
		this.searchResultsColl = searchResultsColl;
	}

	@Override
	public SearchResult getSearchResult() throws WorkspaceException {
		MongoSearchResult searchResult = new MongoSearchResult(searchResultsColl);
		searchResult.setSearchResultUtils(searchResultUtils);
		return searchResult;
	}

	public SearchResult getSearchResult(String searchResultId)
			throws WorkspaceException {
		MongoSearchResult result = new MongoSearchResult(searchResultsColl);
		DBObject searchResultObject = searchResultsColl.findOne(ObjectId
				.massageToObjectId(searchResultId));
		if (searchResultObject == null) {
			LOGGER.error("SearchResultObject is null for searchResultId: "
					+ searchResultId);
		}
		result.setId(searchResultId);

		BasicDBList searchResults = (BasicDBList) searchResultObject
				.get(SEARCHRESULTS);

		Set<String> searchResultUUIDs = new HashSet<String>();
		Iterator<Object> searchResultsIterator = searchResults.iterator();
		while (searchResultsIterator.hasNext())
			searchResultUUIDs.add((String) searchResultsIterator.next());

		result.setMessageIds(searchResultUUIDs);

		result.setSearchQueryId((String) searchResultObject.get(SEARCHQUERYID));

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Retrieved Search Results - " + result.getId());
		result.setSearchResultUtils(searchResultUtils);

		return result;
	}

	@Override
	public Collection<SearchResult> getSearchResults(
			Collection<String> searchResultUUIDs) {

		Collection<SearchResult> results = new HashSet<SearchResult>();

		BasicDBObject query = new BasicDBObject();
		BasicDBList idList = new BasicDBList();
		for (String searchResultUUID : searchResultUUIDs)
			idList.add(ObjectId.massageToObjectId(searchResultUUID));
		query.put("$in", idList);
		DBCursor searchResult = searchResultsColl.find(new BasicDBObject(UUID,
				query));
		while (searchResult.hasNext()) {
			MongoSearchResult result = new MongoSearchResult(searchResultsColl);
			DBObject searchResultObject = searchResult.next();
			result.setId(((ObjectId) searchResultObject.get(UUID)).toString());

			BasicDBList searchResults = (BasicDBList) searchResultObject
					.get(SEARCHRESULTS);

			Set<String> searchResultMessageUUIDs = new HashSet<String>();
			Iterator<Object> searchResultsIterator = searchResults.iterator();
			while (searchResultsIterator.hasNext())
				searchResultMessageUUIDs.add((String) searchResultsIterator
						.next());

			result.setMessageIds(searchResultMessageUUIDs);

			result.setSearchQueryId((String) searchResultObject
					.get(SEARCHQUERYID));
			result.setSearchResultUtils(searchResultUtils);
			results.add(result);

		}
		return results;
	}

	public MongoSearchResultUtils getSearchResultUtils() {
		return searchResultUtils;
	}

	public void setSearchResultUtils(MongoSearchResultUtils searchResultUtils) {
		this.searchResultUtils = searchResultUtils;
	}

}
