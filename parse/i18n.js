
var i18nHash=require('./resource').i18nHash;
var findXMLAttribute=require('./xml').findXMLAttribute;
exports.setup = function(Core){
	Core.beforeI18n =processI18N;
	Core.parseI18n =processI18N;

}
function processI18N(node){
	if (node.nodeType == 2) {
		var el = node.ownerElement;
		el.removeAttribute(node.name);
		this.next(el);
	}else if (node.nodeType == 1){
		this.parse(node.childNodes);
	}
}
function processI18N2(node){
	var i18nKey = findXMLAttribute(node,'i18n');
	var uri = this.currentURI;
	var path = uri.scheme == 'lite'? uri.path: String(uri);
	if(node.nodeType == 1){
		var begin = this.mark();
		_parseChild(this,node);
		var content = this.reset(begin);
		
		i18nKey = i18nHash(path,i18nKey,node.textContent);
		//
		this.parse("${I18N."+i18nKey+"}");
	}else{
		var el = node.ownerElement;
		var node2 = el.cloneNode(true)||el;
		var begin = this.mark();
		this.parse(el.textContent);
		var content = this.reset(begin);

		i18nKey = i18nHash(path,i18nKey,el.textContent);
		node2.textContent = "${I18N."+i18nKey+"}";
		node2.removeAttribute(node.name);
		node2.setAttribute('data-i18n-key',i18nKey)
		this.next(node2);
	}
	addI18NData(this,i18nKey,content);
}
function seekI18N(text){
	
}


//TODO:?>>
function parsePHP(node){
	var value = node.textContent || node.text;
	this.appendPlugin(PLUGIN_NATIVE,'{"type":"php"}');
	parseChildRemoveAttr(this,node);
	this.appendEnd();
}
function parseJS(node){
	var value = node.textContent || node.text;
	this.appendPlugin(PLUGIN_NATIVE,'{"type":"js"}');
	parseChildRemoveAttr(this,node);
	this.appendEnd();
}



function addI18NData(context,i18nKey,content){
	if(typeof content != 'string' && content.length == 1){
		content = content[0];
	}
	var i18nSource = context.getAttribute("#i18n-source");
	var i18nObject = context.getAttribute("#i18n-object");
	if(!i18nObject){
		i18nObject = {};
		context.setAttribute("#i18n-object",i18nObject);
	}
	if(i18nKey in i18nObject){
		i18nObject[i18nKey] = content;
		i18nSource = JSON.stringify(i18nObject)
	}else{
		if(i18nSource){
			i18nSource = i18nSource.slice(0,-1)+',';
		}else{
			i18nSource = '{';
		}
		i18nSource = i18nSource + '"'+i18nKey+'":' +JSON.stringify(content)+'}';
	}
	
	context.setAttribute("#i18n-data",i18nSource);
}
