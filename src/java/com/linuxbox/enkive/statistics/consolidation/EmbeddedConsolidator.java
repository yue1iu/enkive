package com.linuxbox.enkive.statistics.consolidation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_SUM;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsQuery;

public abstract class EmbeddedConsolidator extends AbstractConsolidator {
	public EmbeddedConsolidator(StatsClient client) {
		super(client);
	}

	private Set<Map<String, Object>> getStatTypeData(String gathererName, String type){
		StatsQuery query = new MongoStatsQuery(gathererName, filterType, type, startDate, endDate);
		Set<Map<String, Object>> result = new HashSet<Map<String,Object>>();
		for(Map<String, Object> statsMap: client.queryStatistics(query)){
			statsMap.remove("_id");//WARNING mongo specific pollution
			statsMap.remove(STAT_TIMESTAMP);
			statsMap.remove(STAT_GATHERER_NAME);
			statsMap.remove(CONSOLIDATION_TYPE);
			
			if(statsMap != null && !statsMap.isEmpty()){
				result.add(statsMap);
			}
		}
		return result;
	}
	
	public List<Set<Map<String, Object>>> gathererFilter(String gathererName) {
		List<Set<Map<String,Object>>> result = new LinkedList<Set<Map<String,Object>>>();
		//consolidated stats (by defintion are always intervals)
		Set<Map<String, Object>> consolidatedData = getStatTypeData(gathererName, STAT_INTERVAL);
		if(!consolidatedData.isEmpty()){
			result.add(consolidatedData);
		} else {
			result.add(null);
		}
		return result;
	}
	
	@Override
	public Set<Map<String, Object>> consolidateData() {
		Set<Map<String, Object>> storageData = new HashSet<Map<String, Object>>();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			List<Set<Map<String, Object>>> serviceData = gathererFilter(name);			
			if (!serviceData.isEmpty()) {
				Map<String, Object> mapToStore = new HashMap<String,Object>();
				
				//Interval
				Set<Map<String, Object>> consolidationData = serviceData.get(0);
				if(consolidationData != null){
					Map<String, Object> consolidationTemplate = new HashMap<String, Object>((Map<String,Object>)consolidationData.iterator().next());
					Map<String, Object> consolidationMapToStore = new HashMap<String, Object>(consolidationTemplate);
					
					generateConsolidatedMap(consolidationTemplate, consolidationMapToStore,
							new LinkedList<String>(), attribute.getKeys(),
							consolidationData);//going to need string for point/consolidation
					mapToStore.putAll(consolidationMapToStore);
				}
																																										
				if(!mapToStore.isEmpty()){
					Map<String, Object> dateMap = new HashMap<String, Object>();
					dateMap.put(CONSOLIDATION_MIN, startDate);
					dateMap.put(CONSOLIDATION_MAX, endDate);
					
					mapToStore.put(STAT_TIMESTAMP, dateMap);
					mapToStore.put(CONSOLIDATION_TYPE, consolidationType);
					mapToStore.put(STAT_GATHERER_NAME, attribute.getName());
					
					storageData.add(mapToStore);
				}
			}
		}
		return storageData;
	}
	
	@Override
	protected void consolidateMaps(Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, ConsolidationKeyHandler keyDef,
			LinkedList<String> dataPath) {
		Map<String, Object> statConsolidatedData = new HashMap<String, Object>();
//TODO
		if (keyDef.getMethods() != null) {
			//loop over stat consolidation methods
			for (String method : keyDef.getMethods()) {
				DescriptiveStatistics statsMaker = new DescriptiveStatistics();
				Object dataVal = null;
				dataVal = null;
				//loop over data for consolidation Method
				LinkedList<String> tempPath = new LinkedList<String>(dataPath);
				if(keyDef.isPoint()){
					tempPath.add(method);
				} else {
					tempPath.add(CONSOLIDATION_SUM);
				}
				double input = -1;
				for (Map<String, Object> dataMap : serviceData) {
					//go to end of path & get variable
					input = -1;
					dataVal = getDataVal(dataMap, tempPath);
					if (dataVal != null) {
						//extract relevant data from end of path
						input = statToDouble(dataVal);

						if (input > -1) {
							//add to stat maker if relevant
							statsMaker.addValue(input);
						}
					}
				}
				//store in map if method is valid
				methodMapBuilder(method, statsMaker,
						statConsolidatedData);
			}

			// store stat methods' data on main consolidated map
			putOnPath(dataPath, consolidatedData, statConsolidatedData);
		}
	}
}