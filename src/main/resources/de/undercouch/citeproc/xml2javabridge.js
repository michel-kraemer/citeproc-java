// Copyright 2014 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Define a property on an object
 * @param obj the object
 * @param name the property's name
 * @param f a function that returns the property's value (i.e. a getter)
 */
function __xml2java__defineGetter(obj, name, f) {
	if (typeof obj.__defineGetter__ != 'undefined') {
		obj.__defineGetter__(name, f);
	} else {
		Object.defineProperty(obj, name, { get: f });
	}
}

/**
 * A class that forwards XML DOM calls to Java
 */
var Xml2JavaBridge = function() {};

/**
 * Parses an XML string and returns a DOM object
 * @param str the XML string to parse
 * @return the DOM object
 */
Xml2JavaBridge.prototype.parse = function(str) {
	var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
	this.documentBuilder = factory.newDocumentBuilder();
	var doc = this.documentBuilder.parse(new org.xml.sax.InputSource(
			new java.io.StringReader(str)));
	return new Xml2JavaDocument(doc);
};

/**
 * The base class for DOM nodes
 * @param node the underlying Java DOM node
 */
var Xml2JavaNode = function(node) {
	this.node = node;
	
	__xml2java__defineGetter(this, "attributes", this.getAttributes);
	__xml2java__defineGetter(this, "childNodes", this.getChildNodes);
	__xml2java__defineGetter(this, "firstChild", this.getFirstChild);
	__xml2java__defineGetter(this, "nextSibling", this.getNextSibling);
	__xml2java__defineGetter(this, "nodeName", this.getNodeName);
	__xml2java__defineGetter(this, "ownerDocument", this.getOwnerDocument);
	__xml2java__defineGetter(this, "parentNode", this.getParentNode);
	__xml2java__defineGetter(this, "textContent", this.getTextContent);
};

/**
 * Clones a node
 * @param deep true cloning should be performed recursively
 * @return the clone
 */
Xml2JavaNode.prototype.cloneNode = function(deep) {
	var n = this.node.cloneNode(deep);
	if (n == null) {
		return null;
	}
	return new Xml2JavaElement(n);
};

/**
 * @return a named map containing the node's attributes
 */
Xml2JavaNode.prototype.getAttributes = function() {
	var n = this.node.getAttributes();
	if (n == null) {
		return null;
	}
	return new Xml2JavaNamedNodeMap(n);
};

/**
 * @return a node list containing the node's children
 */
Xml2JavaNode.prototype.getChildNodes = function() {
	var n = this.node.childNodes;
	if (n == null) {
		return null;
	}
	return new Xml2JavaNodeList(n);
};

/**
 * @return the node's first child
 */
Xml2JavaNode.prototype.getFirstChild = function() {
	var n = this.node.firstChild;
	if (n == null) {
		return null;
	}
	return new Xml2JavaElement(n);
};

/**
 * @return the node's next sibling in the DOM tree
 */
Xml2JavaNode.prototype.getNextSibling = function() {
	var n = this.node.nextSibling;
	if (n == null) {
		return null;
	}
	return new Xml2JavaElement(n);
}

/**
 * @return the node's name
 */
Xml2JavaNode.prototype.getNodeName = function() {
	var n = this.node.nodeName;
	if (n == null) {
		return null;
	}
	return String(n);
}

/**
 * @return the document containing the node
 */
Xml2JavaNode.prototype.getOwnerDocument = function() {
	var n = this.node.ownerDocument;
	if (n == null) {
		return null;
	}
	return new Xml2JavaDocument(n);
};

/**
 * @return the node's parent node
 */
Xml2JavaNode.prototype.getParentNode = function() {
	var n = this.node.parentNode;
	if (n == null) {
		return null;
	}
	return new Xml2JavaElement(n);
};

/**
 * @return the node's text content
 */
Xml2JavaNode.prototype.getTextContent = function() {
	var n = this.node.textContent;
	if (n == null) {
		return null;
	}
	return String(n);
};

/**
 * @return true if the node has attributes, false otherwise
 */
Xml2JavaNode.prototype.hasAttributes = function() {
	return this.node.hasAttributes;
};

/**
 * Inserts the node <code>newChild</code> before the existing child
 * node <code>refChild</code>
 * @param newChild the node to insert
 * @param refChild the existing child node
 * @return the inserted node
 */
Xml2JavaNode.prototype.insertBefore = function(newChild, refChild) {
	var nc = newChild;
	if (nc != null) {
		nc = nc.node;
	}
	if (refChild != null) {
		refChild = refChild.node;
	}
	var n = this.node.insertBefore(nc, refChild);
	if (n == null) {
		return null;
	}
	return newChild;
};

/**
 * Replaces the child node <code>oldChild</code> by the given new
 * node <code>newChild</code>
 * @param newChild the new node
 * @param oldChild the node to replace
 * @return the replaced node
 */
Xml2JavaNode.prototype.replaceChild = function(newChild, oldChild) {
	if (newChild != null) {
		newChild = newChild.node;
	}
	var oc = oldChild;
	if (oc != null) {
		oc = oc.node;
	}
	var n = this.node.replaceChild(newChild, oc);
	if (n == null) {
		return null;
	}
	return oc;
};

/**
 * A DOM document
 * @param the underlying Java DOM document
 */
var Xml2JavaDocument = function(doc) {
	Xml2JavaNode.call(this, doc);
};

/**
 * Inherit from Xml2JavaNode
 */
Xml2JavaDocument.prototype = new Xml2JavaNode();

/**
 * Returns a node list of all elements in the document having the
 * given node name
 * @param name the node name
 * @return the list of elements
 */
Xml2JavaDocument.prototype.getElementsByTagName = function(name) {
	var nl = this.node.getElementsByTagName(name);
	if (nl == null) {
		return null;
	}
	return new Xml2JavaNodeList(nl);
};

/**
 * Imports a node from another document into this one
 * @param importedNode the node to import
 * @param deep true if the node's subtree should be imported recursively
 * @return the imported node that now belongs to this document
 */
Xml2JavaDocument.prototype.importNode = function(importedNode, deep) {
	if (importedNode != null) {
		importedNode = importedNode.node;
	}
	var n = this.node.importNode(importedNode, deep);
	if (n == null) {
		return null;
	}
	return new Xml2JavaElement(n);
};

/**
 * A DOM element
 * @param elem the underlying Java DOM element
 */
var Xml2JavaElement = function(elem) {
	Xml2JavaNode.call(this, elem);
	
	__xml2java__defineGetter(this, "tagName", this.getTagName);
};

/**
 * Inherit from Xml2JavaNode
 */
Xml2JavaElement.prototype = new Xml2JavaNode();

/**
 * Returns the value of the attribute with the given name
 * @param name the attribute's name
 * @return the attribute's value or null if there is no such attribute
 */
Xml2JavaElement.prototype.getAttribute = function(name) {
	var r = this.node.getAttribute(name);
	if (r == null) {
		return null;
	}
	return String(r);
};

/**
 * Returns a list of all descendant elements with the given name
 * @param name the name
 * @return a list of matching elements
 */
Xml2JavaElement.prototype.getElementsByTagName = function(name) {
	var nl = this.node.getElementsByTagName(name);
	if (nl == null) {
		return null;
	}
	return new Xml2JavaNodeList(nl);
}

/**
 * @return the element's name
 */
Xml2JavaElement.prototype.getTagName = function() {
	var tn = this.node.getTagName();
	if (tn == null) {
		return null;
	}
	return String(tn);
};

/**
 * Sets an attribute's value
 * @param name the attribute's name
 * @param value the new value
 */
Xml2JavaElement.prototype.setAttribute = function(name, value) {
	this.node.setAttribute(name, value);
};

/**
 * A list of DOM nodes
 * @param nodeList the underlying Java node list
 */
var Xml2JavaNodeList = function(nodeList) {
	this.length = nodeList.length;
	for (var i = 0; i < this.length; ++i) {
		this[i] = new Xml2JavaElement(nodeList.item(i));
	}
};

/**
 * Returns the list item at the given index
 * @param index the index
 * @return the item at this index or null if the index is invalid
 */
Xml2JavaNodeList.prototype.item = function(index) {
	return this[index];
};

/**
 * @return the list's length
 */
Xml2JavaNodeList.prototype.getLength = function(index) {
	return this.length;
};

/**
 * A named map of DOM nodes
 * @param namedNodeMap the underlying Java node map
 */
var Xml2JavaNamedNodeMap = function(namedNodeMap) {
	this.length = namedNodeMap.length;
	for (var i = 0; i < this.length; ++i) {
		this[i] = new Xml2JavaAttr(namedNodeMap.item(i));
	}
};

/**
 * @return the number of elements in the map
 */
Xml2JavaNamedNodeMap.prototype.getLength = function() {
	return this.length;
}

/**
 * A DOM attribute
 * @param attr the underlying Java DOM attribute
 */
var Xml2JavaAttr = function(attr) {
	Xml2JavaNode.call(this, attr);
	this.name = String(attr.name);
	this.value = String(attr.value);
};

/**
 * Inherit from Xml2JavaNode
 */
Xml2JavaAttr.prototype = new Xml2JavaNode();

/**
 * @return the attribute's name
 */
Xml2JavaAttr.prototype.getName = function() {
	return this.name;
};

/**
 * @return the attribute's value
 */
Xml2JavaAttr.prototype.getValue = function() {
	return this.value;
};

/**
 * A class that pretends to be an ActiveX MSXML.DomDocument
 */
var ActiveXObject = function(name) {};

/**
 * Inherit from Xml2JavaDocument
 */
ActiveXObject.prototype = Xml2JavaDocument.prototype;

/**
 * Parses the given XML and replaces this object's underlying
 * document by the parsed one
 */
ActiveXObject.prototype.loadXML = function(str) {
	var doc = new Xml2JavaBridge().parse(str);
	this.node = doc.node;
};
