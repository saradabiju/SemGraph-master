package com.assemblogue.plr.app.generic.semgraph;

import com.assemblogue.plr.lib.Node;

public class NodeInfo<T extends Node> {
	public T node;
	public String name;

	NodeInfo(T node, String name) {
		this.node = node;
		this.name = name;
	}
	
	void set(T node, String name) {
		this.node = node;
		this.name = name;
	}

	boolean equalName(String name) {
		return this.name.equals(name);
	}

	T getNode() {
		return this.node;
	}
}

