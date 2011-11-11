package com.linuxbox.enkive.web.search;

import java.util.List;

import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;

public class RecentSearchListServlet extends AbstractSearchListServlet {
	private static final long serialVersionUID = 7489338160172966335L;

	@Override
	List<SearchResult> getSearches(Workspace workspace)
			throws WorkspaceException {
		return workspaceService.getRecentSearches(workspace.getWorkspaceUUID());
	}



}