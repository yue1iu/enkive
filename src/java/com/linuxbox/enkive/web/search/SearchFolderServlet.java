/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
package com.linuxbox.enkive.web.search;

import static com.linuxbox.enkive.web.WebConstants.DATA_TAG;
import static com.linuxbox.enkive.web.WebConstants.ITEM_TOTAL_TAG;
import static com.linuxbox.enkive.web.WebConstants.RESULTS_TAG;
import static com.linuxbox.enkive.web.WebConstants.SEARCH_ID_TAG;
import static com.linuxbox.enkive.web.WebPageInfo.PAGE_POSITION_PARAMETER;
import static com.linuxbox.enkive.web.WebPageInfo.PAGE_SIZE_PARAMETER;
import static com.linuxbox.enkive.web.WebPageInfo.PAGING_LABEL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.exception.EnkiveServletException;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.enkive.web.EnkiveServlet;
import com.linuxbox.enkive.web.WebPageInfo;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

/**
 * This webscript is run when a user wants to see the results of a prior search,
 * either recent or saved
 */
public class SearchFolderServlet extends EnkiveServlet {

	public static String VIEW_SEARCH_FOLDER = "view";
	public static String REMOVE_SEARCH_FOLDER_MESSAGE = "remove_message";
	public static String ADD_SEARCH_FOLDER_MESSAGE = "add_message";
	public static String EXPORT_SEARCH_FOLDER = "export";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1226107681645083623L;

	protected static final Log LOGGER = LogFactory

	.getLog("com.linuxbox.enkive.webscripts.search.folder");
	protected WorkspaceService workspaceService;
	protected MessageRetrieverService archiveService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.workspaceService = getWorkspaceService();
		this.archiveService = getMessageRetrieverService();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		try {
			String searchFolderId = WebScriptUtils.cleanGetParameter(req, "id");
			String action = WebScriptUtils.cleanGetParameter(req, "action");

			WebPageInfo pageInfo = new WebPageInfo(
					WebScriptUtils.cleanGetParameter(req,
							PAGE_POSITION_PARAMETER),
					WebScriptUtils.cleanGetParameter(req, PAGE_SIZE_PARAMETER));

			JSONObject dataJSON = new JSONObject();
			JSONObject jsonResult = new JSONObject();
			dataJSON.put(SEARCH_ID_TAG, searchFolderId);

			if (LOGGER.isInfoEnabled())
				LOGGER.info("Loading " + searchFolderId);

			SearchFolder searchFolder = workspaceService
					.getSearchFolder(searchFolderId);

			JSONArray resultsJson = new JSONArray();

			if (action.equalsIgnoreCase(ADD_SEARCH_FOLDER_MESSAGE)) {
				String searchResultId = WebScriptUtils.cleanGetParameter(req,
						"searchResultId");
				String messageidlist = WebScriptUtils.cleanGetParameter(req,
						"messageids");
				Collection<String> messageIds = new HashSet<String>(
						Arrays.asList(messageidlist.split(",")));
				addSearchFolderMessages(searchFolder, searchResultId,
						messageIds);
			} else if (action.equalsIgnoreCase(EXPORT_SEARCH_FOLDER)) {
				exportSearchFolder(searchFolderId);
			} else if (action.equalsIgnoreCase(REMOVE_SEARCH_FOLDER_MESSAGE)) {
				String messageidlist = WebScriptUtils.cleanGetParameter(req,
						"messageids");
				Collection<String> messageIds = new HashSet<String>(
						Arrays.asList(messageidlist.split(",")));
				removeSearchFolderMessages(searchFolder, messageIds);
			} else if (action.equalsIgnoreCase(VIEW_SEARCH_FOLDER)) {
				resultsJson = viewSearchFolder(searchFolder, pageInfo);
			}

			dataJSON.put(ITEM_TOTAL_TAG, pageInfo.getItemTotal());

			dataJSON.put(RESULTS_TAG, resultsJson);
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Returning search folder messages for folder id "
						+ searchFolderId);

			jsonResult.put(DATA_TAG, dataJSON);
			jsonResult.put(PAGING_LABEL, pageInfo.getPageJSON());
			res.getWriter().write(jsonResult.toString());
		} catch (WorkspaceException e) {
			respondError(HttpServletResponse.SC_UNAUTHORIZED, null, res);
			throw new EnkiveServletException(
					"Could not login to repository to retrieve search", e);
		} catch (JSONException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new EnkiveServletException("Unable to serialize JSON", e);
		} catch (CannotRetrieveException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new EnkiveServletException(
					"Unable to retrieve search folder messages", e);
		} finally {

		}
	}

	protected void removeSearchFolderMessages(SearchFolder searchFolder,
			Collection<String> messageIds) throws WorkspaceException {
		searchFolder.removeMessageIds(messageIds);

	}

	protected void addSearchFolderMessages(SearchFolder searchFolder,
			String searchResultId, Collection<String> messageIds)
			throws WorkspaceException {
		SearchResult searchResult = workspaceService
				.getSearchResult(searchResultId);
		Set<String> messageIdSet = new HashSet<String>(messageIds);
		searchResult.setMessageIds(messageIdSet);
		searchFolder.addSearchResult(searchResult);
	}

	protected JSONArray viewSearchFolder(SearchFolder searchFolder,
			WebPageInfo pageInfo) throws CannotRetrieveException, JSONException {
		List<String> messageIds = new ArrayList<String>(
				searchFolder.getMessageIds());
		@SuppressWarnings("unchecked")
		List<MessageSummary> messageSummaries = archiveService
				.retrieveSummary((List<String>) pageInfo.getSubList(messageIds));
		pageInfo.setItemTotal(messageIds.size());

		JSONArray jsonMessageSummaryList = SearchResultsBuilder
				.getMessageListJSON((Collection<MessageSummary>) messageSummaries);
		return jsonMessageSummaryList;
	}

	protected void exportSearchFolder(String searchFolderId) {

	}
}
