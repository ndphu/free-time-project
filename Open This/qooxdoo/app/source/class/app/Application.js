/* ************************************************************************

   Copyright:

   License:

   Authors:

 ************************************************************************ */

/**
 * This is the main application class of your custom application "app"
 * 
 * @asset(app/*)
 */
qx.Class.define("app.Application", {
	extend : qx.application.Standalone,

	/*
	 * ****************************************************************************
	 * MEMBERS
	 * ****************************************************************************
	 */

	members : {
		__mainContainer : null,
		__navPane : null,
		__contentPane : null,
		__mainToolBar : null,
		__addPanel : null,
		currentUser : null,
		main : function() {
			this.base(arguments);
			
			if (qx.core.Environment.get("qx.debug")) {
				qx.log.appender.Native;
				qx.log.appender.Console;
			}
			
			var userRest = app.rest.resource.Manager.getResourceByType("CURRENT_USER");
			userRest.addListener("success", function(e) {
				if (e.getData().email == undefined) {
					window.location.replace('/login');
				} else {
					this.currentUser = e.getData();
					this.__init();
				}
			}, this);
			userRest.addListener("error", function(e) {
				window.location.replace('/login');
			}, this);
			userRest.get();
		},
		
		__init : function() {
			var doc = this.getRoot();
			var scroll = new qx.ui.container.Scroll();
			scroll.set({
				backgroundColor : "#000",
				padding : 2
			});
			doc.add(scroll, {
				left : 0,
				top : 0,
				width : "100%",
				height : "100%"
			});
			this.__mainContainer = new qx.ui.container.Composite();
			this.__mainContainer.setLayout(new qx.ui.layout.VBox());
			this.__mainContainer.set({
				"backgroundColor" : "white"
			});
			scroll.add(this.__mainContainer);
			// Create the banner
			this.__initBanner();
			// Create the main toolbar
			// TODO: temporary remove toolbar
			// this.__initToolBar();
			// Create the 2 pane
			var contentContainer = new qx.ui.container.Composite();
			contentContainer.setLayout(new qx.ui.layout.HBox());
			this.__mainContainer.add(contentContainer, {flex : 1});
			
			this.__navPane = new qx.ui.container.Composite();
			this.__navPane.setLayout(new qx.ui.layout.VBox());
			contentContainer.add(this.__navPane, {"width" : "30%"});
			
			this.__contentPane = new qx.ui.container.Composite();
			this.__contentPane.setLayout(new qx.ui.layout.VBox());
			this.__contentPane.set({
				backgroundColor : "white"
			});
			contentContainer.add(this.__contentPane, {flex : 1});
			this.__initNavPane();
			this.__initContentPane();
		},
		
		__initBanner : function() {
			this.__banner = new app.Banner();
			this.__mainContainer.add(this.__banner);
		},

		__initToolBar : function() {
			this.__mainToolBar = new qx.ui.toolbar.ToolBar();
			// TODO: Temporary disable nav pane behavior
			/*
			this.__showMenuBtn = new qx.ui.toolbar.Button(">>>>");
			this.__showMenuBtn.addListener("execute", function() {
				this.__navPane.show();
				this.__showMenuBtn.exclude();
				this.__hideMenuBtn.show();
			}, this);
			this.__hideMenuBtn = new qx.ui.toolbar.Button("<<<<");
			this.__hideMenuBtn.addListener("execute", function() {
				this.__navPane.exclude();
				this.__showMenuBtn.show();
				this.__hideMenuBtn.exclude();
			}, this);
			this.__mainToolBar.add(this.__showMenuBtn);
			this.__mainToolBar.add(this.__hideMenuBtn);
			this.__showMenuBtn.exclude();
			*/

			this.__mainToolBar.addSpacer();

			this.__userNameLabel = new qx.ui.basic.Label(this.currentUser.email);
			this.__userNameLabel.set({
				alignY : "middle"
			});
			this.__mainToolBar.add(this.__userNameLabel);
			this.__logoutBtn = new qx.ui.toolbar.Button("Logout");
			this.__logoutBtn.addListener("execute", function() {
				if (confirm("Are you sure want to logout?")) {
					window.location.replace(this.currentUser.logoutUrl);
				}
			}, this);
			this.__mainToolBar.add(this.__logoutBtn);

			this.__mainContainer.add(this.__mainToolBar);
		},
		
		__initNavPane : function() {
			this.__addPanel = new app.ui.panel.AddPanel();
			this.__navPane.add(this.__addPanel);
			// TODO: temporary hide the navpane
			this.__navPane.exclude();
		},
		
		__initContentPane : function() {
			var contentTabView = new app.ui.panel.ContentTabView();
			this.__contentPane.add(contentTabView, {"height": "100%"});
		}
	}
});
