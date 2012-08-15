package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;

import java.text.ParseException;
import java.util.List;

import org.quartz.CronExpression;

import com.linuxbox.enkive.statistics.KeyConsolidationHandler;

public class GathererAttributes {
	protected List<KeyConsolidationHandler> keys;
	protected CronExpression schedule;
	protected String serviceName;
	protected String humanName;

	public GathererAttributes(String serviceName, String humanName, String schedule,
			List<KeyConsolidationHandler> keys) throws ParseException {
		this.humanName = humanName;
		this.serviceName = serviceName;
		this.schedule = new CronExpression(schedule);
		this.keys = keys;
		//serviceName and Timestamp must always be specified
		keys.add(new KeyConsolidationHandler(STAT_GATHERER_NAME + "::Gatherer Name:"));
		keys.add(new KeyConsolidationHandler(STAT_TIMESTAMP + "::Time Stamp:"));
	}

	/**
	 * @return the consolidation handlers cooresponding to this gatherer
	 */
	public List<KeyConsolidationHandler> getKeys() {
		return keys;
	}

	/**
	 * @return the name of the gatherer this attributes class belongs to
	 */
	public String getName() {
		return serviceName;
	}

	/**
	 * @return the human-readable name of the gatherer this attributes class belongs to
	 */
	public String getHumanName() {
		return humanName;
	}
	
	/**
	 * @return the cronExpression schedule this gatherer runs on
	 */
	public CronExpression getSchedule() {
		return schedule;
	}
}
