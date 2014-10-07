qx.Class.define("app.Banner", {
	extend : qx.ui.container.Composite,
	construct : function() {
		this.base(arguments);
		this.__initLayout();
	},
	members : {
		__initLayout : function() {
			this.set({
				minHeight : 50,
				backgroundColor : "#DF7401"
			});
			this.setLayout(new qx.ui.layout.Canvas());
			var appTitle = new qx.ui.basic.Label();
			appTitle.set({
				padding : 5,
				rich : true
			});
			
			appTitle.setValue("<font size='5' style='color: #fff;'>Open This!!!</font>");
			this.add(appTitle, {
				top : 5,
				left : 5,
				bottom : 5
			});
			
			var currentUser = qx.core.Init.getApplication().currentUser;
			
			var userContainer = new qx.ui.container.Composite(new qx.ui.layout.VBox().set({
				alignX : "right",
				alignY : "middle"
			}));
			
			var userEmail= new qx.ui.basic.Label("<font size='2' style='color: #fff;'>Welcome " + currentUser.email + "</font>");
			userEmail.setRich(true);
			userContainer.add(userEmail);
			
			var logoutLink = new qx.ui.basic.Label("<a href='" + currentUser.logoutUrl + "'>Logout</a>");
			logoutLink.setRich(true);
			userContainer.add(logoutLink);
			
			this.add(userContainer, {
				right : 5,
				top : 1,
				bottom : 1
			});
			
			
		}
	}
});