qx.Class.define("app.ui.panel.AddPanel", {
	extend : qx.ui.container.Composite,
	construct : function(method, linkId) {
		this.base(arguments);
		this.method = method;
		this.linkId = linkId;
		this.__initLayout();
	},
	
	events : {
		"data-updated" : "qx.event.type.Data"	
	},
	
	members : {
		__name : null,
		__link : null,
		__desc : null,
		__shared : null,
		__status : null,
		method : null,
		linkId : null,
		__initLayout : function() {
			this.setLayout(new qx.ui.layout.VBox(2));
			this.set({
				padding : 4
			});
			
			var lbName = new qx.ui.basic.Label("Name:");
			lbName.set({
				font : "bold",
				alignY : "middle"
			});
			this.add(lbName);
			this.__name = new qx.ui.form.TextField();
			this.add(this.__name);
			
			var lbLink = new qx.ui.basic.Label("Link:");
			lbLink.set({
				font : "bold",
				alignY : "middle"
			});
			this.add(lbLink);
			this.__link = new qx.ui.form.TextField();
			this.add(this.__link);
			
			var lbDesc = new qx.ui.basic.Label("Description:");
			lbDesc.set({
				font : "bold",
				alignY : "middle"
			});
			this.add(lbDesc);
			this.__desc = new qx.ui.form.TextArea();
			this.add(this.__desc, {flex : 1});
			
			this.__shared = new qx.ui.form.CheckBox("Share");
			this.add(this.__shared);
			
			this.__initToolbar();
			this.__status = new qx.ui.basic.Label();
			this.__status.set({
				rich : true
			});
			this.add(this.__status);
			
			if (this.method === 'modify') {
				this.loadLinkData();
			}
		},
		
		__initToolbar : function() {
			this.__toolBar = new qx.ui.toolbar.ToolBar();
			this.add(this.__toolBar);
			this.__toolBar.set({
				padding : 2
			});
			
			var submit = new qx.ui.toolbar.Button("Save", "app/icons/save.png");
			this.__toolBar.add(submit);
			
			this.__toolBar.addSpacer();
			var clear = new qx.ui.toolbar.Button("Close", "app/icons/remove.png");
			this.__toolBar.add(clear);
			
			submit.addListener("execute", function() {
				this.__doSubmit();
			}, this);
			
			clear.addListener("execute", function() {
				if (this.winRef == undefined) {
					this.__doClearForm();
				} else {
					this.winRef.close();
				}
				this.__doClearForm();			
			}, this);
			
			
		},
		
		__doSubmit : function() {
			var name = this.__name.getValue();
			var link = this.__link.getValue();
			var desc = this.__desc.getValue();
			var shared = this.__shared.getValue();
			var postData = {
				"name" : name,
				"link" : link,
				"desc" : desc,
				"shared" : shared
			};
			var linkResource = app.rest.resource.Manager.getResourceByType("LINK");
			linkResource.configureRequest(function(req) {
				req.setRequestHeader("Content-Type", "application/json");
			});
			
			linkResource.addListener("success", function(e) {
				this.__doClearForm();
				this.fireDataEvent("data-updated");
				if (this.winRef != undefined) {
					this.winRef.close();
				}
			}, this);
			
			linkResource.addListener("error", function(e) {
				this.setStatus(1, "Failed to add new link");
			}, this);
			if (this.method === 'modify') {
				postData.id = this.linkId;
				linkResource.put(null, postData);
			} else {
				linkResource.post(null, postData);
			}
		},
		
		__doClearForm : function() {
			this.__name.setValue("");
			this.__link.setValue("");
			this.__desc.setValue("");
			this.__shared.setValue(false);
		},
		
		setStatus : function(type, message) {
			var color = "black";
			if (type == 0) {
				color = "green";
			} else if (type == 1) {
				color = "red";
			}
			
			var html = "<a style='color : " + color + "'>" + message + "</a>";
			this.__status.setValue(html);
		},
		
		getAsWindow : function() {
			var title = this.method === 'modify' ? "Edit Link" : "Add New Link";
			this.winRef = new qx.ui.window.Window(title);
			var w = window,
		    d = document,
		    e = d.documentElement,
		    g = d.getElementsByTagName('body')[0],
		    winWidth = w.innerWidth || e.clientWidth || g.clientWidth,
		    winHeight = w.innerHeight|| e.clientHeight|| g.clientHeight;
			this.winRef.set({
				showMinimize : false,
				showMaximize : false,
				width : winWidth / 2,
				height : winHeight / 2 
			});
			this.winRef.center();
			this.winRef.setLayout(new qx.ui.layout.VBox());
			this.winRef.add(this, {"height" : "100%"});
			return this.winRef;
		},
		
		loadLinkData : function() {
			var linkResource = app.rest.resource.Manager.getResourceByType("LINK");
			linkResource.addListener("success", function(e) {
				var linkObj = e.getData();
				this.__name.setValue(linkObj.name);
				this.__link.setValue(linkObj.link);
				this.__desc.setValue(linkObj.desc);
				this.__shared.setValue(linkObj.shared);
			}, this);
			
			linkResource.addListener("error", function(e) {
				this.setStatus(1, "Failed to load link data with id: " + this.linkId);
			}, this);
			linkResource.get({id : this.linkId}, null);
		}
	}
});