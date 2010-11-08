package {
	
	/**
	 * Usage:
	 * Instead of regular "trace" use t("some string");
	 * This will allow clicking on a trace line in Vizzy
	 * and opening corresponding source file in your
	 * default ActionScript editor. 
	 * 
	 * Make sure that "Permit debugging" checkbox is ticked
	 * in your Fla file project settings.
	 * 
	 * You can turn Vizzy debug information off by setting
	 * VizzyTrace.mode = VizzyTrace.SIMPLE;
	 * and t() will act as a native "trace"
	 * 
	 * If you are using FlashDevelop, consider downloading
	 * VizzyPlugin. This will allow not just opening proper
	 * source file on click, but also locating and highlighting
	 * a proper line
	 * 
	 * Copyright 2008-2010
	 * @author Sergei Ledvanov
	 */
	 

	public function t(...strings):void {
		if (VizzyTrace.mode == VizzyTrace.SIMPLE) {
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