qx.Class.define("app.ui.field.BasicField", {
	extend : qx.ui.container.Composite,
	construct : function(fieldName, fieldValue) {
		this.base(arguments);
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.__initLayout();
	},
	members : {
		fieldName : null,
		fieldValue : null,
		__linkLabel : null,
		__linkValue : null,
		__initLayout : function() {
			this.setLayout(new qx.ui.layout.HBox());
			this.__linkLabel =  new qx.ui.basic.Label(this.fieldName);
			this.__linkValue = new qx.ui.form.TextField(this.fieldValue);
			this.add(this.__linkLabel, {"width" : "50%"});
			this.add(this.__linkValue, {flex : 1});
		},
		getValue : function() {
			return this.__linkValue.getValue();
		}
	}
});