package org.jfrog.eclipse.npm;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IWorkspace;

/**
 * @author yahavi
 */
@SuppressWarnings("restriction")
public class NpmProject extends Project {

	public NpmProject(String path, IWorkspace container) {
		super(org.eclipse.core.runtime.Path.fromOSString(path), (Workspace) container);
	}
}
