qx.Class.define("app.rest.resource.Manager", {
	statics : {
		getResourceByType : function(type) {
			var desc = undefined;
			switch (type) {
				case "CURRENT_USER" :
					desc = {
						get : {
							method : "GET",
							url : "/rest/current_user"
						}
					};
					break;
				case "LINK":
					desc = {
						post : {
							method : "POST",
							url : "/rest/link"
						},
						put : {
							method : "PUT",
							url : "/rest/link"
						},
						get : {
							method : "GET",
							url : "/rest/link/{id}"
						},
						del : {
							method : "DELETE",
							url : "/rest/link/{id}"
						}
					};
					break;
				case "MYLINK":
					desc = {
						get : {
							method : "GET",
							url : "/rest/mylink"
						}
					}
					break;
				case "SHARED_LINK":
					desc = {
						get : {
							method : "GET",
							url : "/rest/shared_link"
						}
					}
					break;
			}
			var resource = new qx.io.rest.Resource(desc);
			return resource;
		}
	}
});