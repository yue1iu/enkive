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
/*
 * 
 */
package com.linuxbox.enkive.workspace;

import java.util.ArrayList;
import java.util.Collection;

import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;

/**
 * Wrapper for another @ref WorkspaceService that enforces permissions by checking an
 * @ref AuthenticationService before returning anything.  Currently, this wraps
 * @ref MongoWorkspaceService
 *
 * @author dang
 *
 */
public class PermissionsEnforcingWorkspaceService implements WorkspaceService {

	protected WorkspaceService workspaceService;
	protected AuthenticationService authenticationService;
	protected PermissionService permissionService;

	@Override
	public Workspace getActiveWorkspace(String userId)
			throws WorkspaceException {
		Workspace workspace = workspaceService.getActiveWorkspace(userId);
		if (canReadWorkspace(userId, workspace.getWorkspaceUUID()))
			return workspace;
		else
			throw new WorkspaceException(
					"Could not get permissions to access workspace. User: "
							+ userId + " Workspace: " + workspace.getWorkspaceUUID());
	}

	@Override
	public Workspace getWorkspace(String workspaceUUID)
			throws WorkspaceException {
		try {
			if (canReadWorkspace(authenticationService.getUserName(),
					workspaceUUID))
				return workspaceService.getWorkspace(workspaceUUID);
			else
				throw new WorkspaceException(
						"Could not get permissions to access workspace. User: "
								+ authenticationService.getUserName()
								+ " Workspace: " + workspaceUUID);
		} catch (AuthenticationException e) {
			throw new WorkspaceException(
					"Could not determine user attempting to access workspace "
							+ workspaceUUID);
		}
	}

	public boolean canReadWorkspace(String userId, String workspaceId)
			throws WorkspaceException {
		try {
			if (permissionService.isAdmin())
				return true;
			Workspace workspace = workspaceService.getWorkspace(workspaceId);
			return (workspace.getCreator().equals(userId));
		} catch (CannotGetPermissionsException e) {
			throw new WorkspaceException(
					"Could not get permissions for user to access workspace", e);
		}

	}

	public boolean canSaveWorkspace(String userId, String workspaceId)
			throws WorkspaceException {
		// If we can read, we can save
		return canReadWorkspace(userId, workspaceId);
	}

	public boolean canDeleteWorkspace(String userId, String workspaceId)
			throws WorkspaceException {
		// If we can read, we can delete
		return canReadWorkspace(userId, workspaceId);
	}

	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	public void setWorkspaceService(WorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public Collection<Workspace> getUserWorkspaces(String userId)
			throws WorkspaceException {
		ArrayList<Workspace> workspaces = new ArrayList<Workspace>();
		for (Workspace workspace : workspaceService.getUserWorkspaces(userId))
			if (canReadWorkspace(userId, workspace.getWorkspaceUUID()))
				workspaces.add(workspace);
		return workspaces;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@Override
	public SearchQuery getSearch(String searchId)
			throws WorkspaceException {
		return workspaceService.getSearch(searchId);
	}

	@Override
	public SearchQueryBuilder getSearchQueryBuilder() {
		return workspaceService.getSearchQueryBuilder();
	}

	@Override
	public void setSearchQueryBuilder(SearchQueryBuilder searchQueryBuilder) {
		workspaceService.setSearchQueryBuilder(searchQueryBuilder);
	}

/*	@Override
	public SearchFolder getSearchFolder(String searchFolderId)
			throws WorkspaceException {
		return workspaceService.getSearchFolder(searchFolderId);
	}*/

}
