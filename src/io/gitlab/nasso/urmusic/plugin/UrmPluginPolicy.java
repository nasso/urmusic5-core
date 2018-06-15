package io.gitlab.nasso.urmusic.plugin;

import java.io.FilePermission;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;

import io.gitlab.nasso.urmusic.Urmusic;

public class UrmPluginPolicy extends Policy {
	public PermissionCollection getPermissions(CodeSource src) {
		// TODO: An android-like permission management system would be safer than this.
		Permissions p = new Permissions();
		p.add(new FilePermission("<<ALL FILES>>", "read"));
		
		if(src.getLocation() == null || !src.getLocation().getPath().startsWith(Urmusic.getPluginFolder().toString()))
			p.add(new AllPermission());
		
		return p;
	}
	
	public void refresh() {
	}
}
