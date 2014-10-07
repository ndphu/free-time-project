qx.Class.define("app.ui.panel.MyLink", {
	extend : qx.ui.container.Composite,
	
	construct : function() {
		this.base(arguments);
		this.__initLayout();
	},
	
	members : {
		__tableModel : null,
		__selectionModel : null,
		
		__initLayout : function() {
			this.setLayout(new qx.ui.layout.VBox());
			this.__tableModel = new qx.ui.table.model.Simple();
			this.__tableModel.setColumns([ "ID", "Name", "Link", "Desc", "Shared", "Time", "Email" ]);
			var table = new qx.ui.table.Table(this.__tableModel, {
				tableColumnModel : function(obj) {
					return new qx.ui.table.columnmodel.Resize(obj);
				},
				initiallyHiddenColumns : [0, 4, 6]
			});
			table.set({
				columnVisibilityButtonVisible : false,
				showCellFocusIndicator : false,
				statusBarVisible : false
			});
			
			table.addListener("cellDbltap", function(e) {
				this.__editLink();
			}, this);
			
			this.__selectionModel = table.getSelectionModel();
			var tcm = table.getTableColumnModel();
			var resizeBehavior = tcm.getBehavior();
			resizeBehavior.setWidth(1, "25%");
			resizeBehavior.setWidth(2, "25%");
			resizeBehavior.setWidth(3, "25%");
			resizeBehavior.setWidth(4, "25%");
			
			this.add(table, {flex : 1});
			
			var toolbar = new qx.ui.toolbar.ToolBar();
			this.add(toolbar);
			
			var addButton = new qx.ui.basic.Image("app/icons/add.png");
			addButton.set({
				cursor : "pointer",
				padding : 4
			});
			addButton.addListener("click", function(){
				this.__addLink();
			}, this);
			toolbar.add(addButton);
			
			var editButton = new qx.ui.basic.Image("app/icons/edit.png");
			editButton.set({
				cursor : "pointer",
				padding : 4
			});
			editButton.addListener("click", function(){
				this.__editLink();
			}, this);
			toolbar.add(editButton);
			
			var removeButton = new qx.ui.basic.Image("app/icons/delete.png");
			removeButton.set({
				cursor : "pointer",
				padding : 4
			});
			removeButton.addListener("click", function(){
				this.__removeLink();
			}, this);
			toolbar.add(removeButton);
			
			var refreshButton = new qx.ui.basic.Image("app/icons/refresh.png");
			refreshButton.set({
				cursor : "pointer",
				padding : 4
			});
			refreshButton.addListener("click", function(){
				this.refresh();
			}, this);
			toolbar.add(refreshButton);
			
			var openButton = new qx.ui.basic.Image("app/icons/open.png");
			openButton.set({
				cursor : "pointer",
				padding : 4
			});
			openButton.addListener("click", function(){
				this.__openLink();
			}, this);
			toolbar.add(openButton);
			
			this.refresh();
		},
		
		refresh : function() {
			var linksResource = app.rest.resource.Manager.getResourceByType("MYLINK");
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
		},
		
		__addLink : function() {
			var addPanel = new app.ui.panel.AddPanel();
			addPanel.addListener("data-updated", function() {
				this.refresh();
			}, this);
			var winRef = addPanel.getAsWindow();
			winRef.open();
		},
		
		__removeLink : function() {
			if (confirm("Are you sure want to remove the link?")) {
				var selectedIdx = this.__selectionModel.getLeadSelectionIndex();
				var id = this.__tableModel.getData()[selectedIdx][0];
				var linkResource = app.rest.resource.Manager.getResourceByType("LINK");
				linkResource.addListener("success", function(e) {
					this.refresh();
				}, this);
				
				linkResource.addListener("error", function(e) {
					alert("Cannot delete link with id " + id);
				}, this);
				linkResource.del({"id" : id}, null);
			}
		},
		
		__editLink : function() {
			var selectedIdx = this.__selectionModel.getLeadSelectionIndex();
			var id = this.__tableModel.getData()[selectedIdx][0];
			var addPanel = new app.ui.panel.AddPanel("modify", id);
			addPanel.addListener("data-updated", function() {
				this.refresh();
			}, this);
			var winRef = addPanel.getAsWindow();
			winRef.open();
		},
		
		__openLink : function() {
			var selectedIdx = this.__selectionModel.getLeadSelectionIndex();
			var link = this.__tableModel.getData()[selectedIdx][2];
			var win = window.open(link, '_blank');
  			win.focus();
		}
	}
});