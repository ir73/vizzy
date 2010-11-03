package  {
	
	/**
	 * Copyright 2008-2010
	 * @author Sergei Ledvanov
	 */
	public class VizzyTrace {
		
		public static const SIMPLE:String = "simple"; // forces to use native trace
		public static const VIZZY:String = "vizzy"; // adds Vizzy metadata about source file and line number
		
		public static var mode:String = VizzyTrace.VIZZY;
		
	}

}