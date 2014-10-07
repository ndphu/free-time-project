qx.Class.define("app.ui.panel.SharedLink", {
	extend : qx.ui.container.Composite,
	
	construct : function() {
		this.base(arguments);
		this.__initLayout();
	},
	
	members : {
		__tableModel : null,
		
		__initLayout : function() {
			this.setLayout(new qx.ui.layout.VBox());
			this.__tableModel = new qx.ui.table.model.Simple();
			this.__tableModel.setColumns([ "ID", "Name", "Link", "Desc", "Shared", "Time", "Email" ]);
			var table = new qx.ui.table.Table(this.__tableModel, {
				tableColumnModel : function(obj) {
					return new qx.ui.table.columnmodel.Resize(obj);
				},
				initiallyHiddenColumns : [0, 4]
			});
			table.set({
				columnVisibilityButtonVisible : false,
				statusBarVisible : false
			});
			var tcm = table.getTableColumnModel();
			var resizeBehavior = tcm.getBehavior();
			resizeBehavior.setWidth(1, "20%");
			resizeBehavior.setWidth(2, "20%");
			resizeBehavior.setWidth(3, "20%");
			resizeBehavior.setWidth(5, "20%");
			resizeBehavior.setWidth(6, "20%");
			
			this.add(table, {"height" : "100%"});
			this.refresh();
		},
		
		refresh : function() {
			var linksResource = app.rest.resource.Manager.getResourceByType("SHARED_LINK");
			linksResource.addListener("success", function(e) {
				var links = e.getData();
				var tableData = [];
				for (var i = 0; i < links.length; ++i) {
					var linkJson = links[i];
					tableData[i] = [linkJson["id"], linkJson["name"], linkJson["link"], linkJson["desc"], linkJson["shared"], linkJson["timeStamp"], linkJson["email"]];
				}
				this.__tableModel.setData(tableData);
			}, this);
			
			linksResource.addListener("error", function(e) {
			
			}, this);
			linksResource.get();
		}
	}
});