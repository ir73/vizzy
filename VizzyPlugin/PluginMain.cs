using System;
using PluginCore;
using PluginCore.Managers;
using System.Windows.Forms;
using ScintillaNet.Configuration;

namespace VizzyPlugin
{
    public class PluginMain : IPlugin
	{
        private String pluginName = "VizzyPlugin";
        private String pluginGuid = "3ef17920-59bd-4c5d-9707-270e05075fce";
        private String pluginHelp = "http://code.google.com/p/flash-tracer/";
        private String pluginDesc = "Adds Vizzy Flash Tracer integration with FlashDevelop.";
        private String pluginAuth = "Sergei Ledvanov";

        #region Required Properties

        /// <summary>
        /// Name of the plugin
        /// </summary> 
        public String Name
		{
			get { return this.pluginName; }
		}

        /// <summary>
        /// GUID of the plugin
        /// </summary>
        public String Guid
		{
			get { return this.pluginGuid; }
		}

        /// <summary>
        /// Author of the plugin
        /// </summary> 
        public String Author
		{
			get { return this.pluginAuth; }
		}

        /// <summary>
        /// Description of the plugin
        /// </summary> 
        public String Description
		{
			get { return this.pluginDesc; }
		}

        /// <summary>
        /// Web address for help
        /// </summary> 
        public String Help
		{
			get { return this.pluginHelp; }
		}

        /// <summary>
        /// Object that contains the settings
        /// </summary>
        public Object Settings
        {
            get { return null; }
        }
		
		#endregion
		
		#region Required Methods
		
		/// <summary>
		/// Initializes the plugin
		/// </summary>
		public void Initialize()
		{
            this.AddEventHandlers();
        }
		
		/// <summary>
		/// Disposes the plugin
		/// </summary>
		public void Dispose()
		{
		}
		
		/// <summary>
		/// Handles the incoming events
		/// </summary>
		public void HandleEvent(Object sender, NotifyEvent e, HandlingPriority prority)
		{
            if (e.Type == EventType.Command)
            {
                string cmd = (e as DataEvent).Action;
                if (cmd != "FlashDevelop.StartArgs")
                {
                    return;
                }
            }
            else if (e.Type != EventType.UIStarted)
            {
                return;
            }

            try
            {
                String[] args = PluginBase.MainForm.StartArguments;
                for (int i = 0; i < args.Length; i++)
                {
                    String arg = args[i];
                    if (arg == "-line")
                    {
                        if (args.Length >= i + 1)
                        {
                            Int32 line = Int32.Parse(args[i + 1]) - 1;
                            if (PluginBase.MainForm.CurrentDocument != null)
                            {
                                ScintillaNet.ScintillaControl Sci = PluginBase.MainForm.CurrentDocument.SciControl;
                                int pos = Sci.PositionFromLine(line);
                                int end = Sci.LineEndPosition(line);
                                Sci.BeginInvoke((MethodInvoker)delegate
                                {
                                    Sci.SetSel(pos, end);
                                    Sci.ScrollCaret();
                                });
                            }
                        }
                    }
                }
            }
            catch
            {
            }
		}

		#endregion

        #region Custom Methods

        /// <summary>
        /// Adds the required event handlers
        /// </summary> 
        public void AddEventHandlers()
        {
            EventManager.AddEventHandler(this, EventType.Command | EventType.UIStarted);
        }

		#endregion

    }
}
