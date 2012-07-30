var gn = context.properties["gn"];
var statName = context.properties["stat"];
var tsMin = context.properties["ts.min"];
var tsMax = context.properties["ts.max"];
var grainType = context.properties["gTyp"];
var statType = context.properties["statType"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("enkive");
// retrieve the web script index page
var queryStr = "/stats/statistics?gn="+gn+"&"+gn+"="+statName+"&ts.min="+tsMin+"&ts.max="+tsMax;
if(grainType != null && grainType != undefined){
	queryStr = queryStr + "&gTyp=" + grainType;
}
var auditEntryListJSON = connector.get("/stats/statistics?gn="+gn+"&"+gn+"="+statName+"&ts.min="+tsMin+"&ts.max="+tsMax+"&gTyp="+grainType);
var auditEntryList = "'" + auditEntryListJSON + "'";
model.result = auditEntryList;
model.gn = gn;
model.statName = statName;
model.queryStr = queryStr;
model.statType = "'" + statType + "'";