qx.Class.define("app.ui.panel.ContentTabView", {
	extend : qx.ui.container.Composite,
	
	construct : function() {
		this.base(arguments);
		this.__initLayout();
	},
	
	members : {
		__initLayout : function() {
			this.setLayout(new qx.ui.layout.VBox());
			var tabView = new qx.ui.tabview.TabView();
			
			var myLinkPage = new qx.ui.tabview.Page("My Link");
			myLinkPage.setLayout(new qx.ui.layout.VBox());
			var myLink = new app.ui.panel.MyLink();
			myLinkPage.add(myLink, {"height" : "100%"});
			tabView.add(myLinkPage);
			
			var sharedLinkPage = new qx.ui.tabview.Page("Shared Link");
			sharedLinkPage.setLayout(new qx.ui.layout.VBox());
			var sharedLink = new app.ui.panel.SharedLink();
			sharedLinkPage.add(sharedLink, {"height" : "100%"});
			tabView.add(sharedLinkPage);
			
			this.add(tabView,{"height" : "100%"});
		}
	}
});