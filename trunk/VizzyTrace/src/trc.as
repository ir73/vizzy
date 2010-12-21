/**
 * Instead of regular "trace" use t("some string");
 * This will allow double clicking on a trace line in Vizzy
 * and opening corresponding source file in your
 * default ActionScript editor. 
 * 
 * Make sure that "Permit debugging" checkbox is ticked
 * in your Fla file project settings.
 * 
 * You can turn Vizzy debug information off by setting
 * setVizzyTraceMode("simple");
 * and t() will act as a native trace()
 * 
 * If you are using FlashDevelop, consider downloading
 * VizzyPlugin. This will allow not just opening proper
 * source file on double click, but also locating and highlighting
 * a proper line
 * 
 * Copyright 2007-2010
 * @author Sergei Ledvanov
 */
package {
	import flash.system.Capabilities;
	
	/**
	 * Traces lines with Vizzy debug information depending 
	 * on VizzyTrace.mode parameter. Syntax is the same as
	 * in native trace() method
	 * @param	...strings 
	 * comma separated objects to trace
	 */
	public function trc(...strings):void {
		if (!Capabilities.isDebugger 
					|| VizzyTrace.mode == VizzyTrace.SIMPLE) {
			trace.apply(null, strings);
			return;
		}
		
		if (strings != null && strings.length > 0) {
			var st:String = new Error().getStackTrace();
			var split:/*String*/Array = st.split("\n");
			if (split.length > 2) {
				strings[0] = "|vft|" + split[2] + "\n" + strings[0];
			}
		}
		
		trace.apply(null, strings);
	}
}