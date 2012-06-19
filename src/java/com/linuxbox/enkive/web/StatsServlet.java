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
 ******************************************************************************/
package com.linuxbox.enkive.web;

import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
public class StatsServlet extends EnkiveServlet {
	final StatsClient retriever = getStatsClient();
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.web.StatsServlet");
	private static final long serialVersionUID = 7062366416188559812L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		LOGGER.info("StatsServlet doGet started");
		try {
			try {
				Date upperDate = null;
				Date lowerDate = null;
				boolean noDate = true;
				if (req.getParameter("upperDate") != null) {
					upperDate = NUMERIC_SEARCH_FORMAT.parse(req
							.getParameter("upperDate"));
					noDate = false;
				}
				if (req.getParameter("lowerDate") != null) {
					lowerDate = NUMERIC_SEARCH_FORMAT.parse(req
							.getParameter("lowerDate"));
					noDate = false;
				}

				String[] serviceNames = req.getParameterValues(STAT_SERVICE_NAME);
				Map<String, Map<String, Object>> bigMap = new HashMap<String, Map<String, Object>>();

				resp.getWriter().write("serviceNames: " + serviceNames + "\n");

				for (String serviceName : serviceNames) {
					resp.getWriter().write(
							"serviceName: " + serviceName + "\n");
					Map<String, Object> map = new HashMap<String, Object>();
					GathererAttributes attribute = retriever.getAttributes(serviceName);
					for(String statName: attribute.getKeys().keySet()){
						map.put(statName, req.getParameter(serviceName));
					}
					bigMap.put(serviceName, map);	
				}
				//TODO for testing
				resp.getWriter().write("bigMap: " + bigMap + "\n");
				// resp.getWriter().write("lowerDate: " + lowerDate + "\n");
				// resp.getWriter().write("upperDate: " + upperDate + "\n");

				
				Set<Map<String, Object>> stats;
				if(noDate){
					Map<String, String[]> gatheringStats = new HashMap<String, String[]>();
					for(String name: bigMap.keySet()){
						gatheringStats.put(name, (String[])bigMap.get(name).keySet().toArray());
					}
					stats = retriever.gatherData(gatheringStats);
				}
				else{
					if(bigMap.isEmpty()){
						stats = retriever.queryStatistics(null, lowerDate, upperDate);
					}
					else{
						stats = retriever.queryStatistics(bigMap, lowerDate, upperDate);
					}
				}
				//TODO for testing
				resp.getWriter().write("stats: " + stats + "\n");

				try {
					JSONArray statistics = new JSONArray(stats.toArray());
					resp.getWriter().write(statistics.toString());
				} catch (IOException e) {
					respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							null, resp);
					throw new CannotRetrieveException(
							"could not create JSON for message attachment", e);
				} catch (JSONException e) {
					respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							null, resp);
					throw new CannotRetrieveException(
							"could not create JSON for message attachment", e);
				}
			} catch (ParseException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				LOGGER.error("Error Parsing Data", e);
			} catch (CannotRetrieveException e) {
				respondError(HttpServletResponse.SC_UNAUTHORIZED, null, resp);
				if (LOGGER.isErrorEnabled())
					LOGGER.error("CannotRetrieveException", e);
			} catch (NullPointerException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				LOGGER.error("NullException thrown", e);
			}
		} catch (IOException e) {
			LOGGER.error("IOException thrown", e);
		}
		LOGGER.info("StatsServlet doGet finished");
	}
}