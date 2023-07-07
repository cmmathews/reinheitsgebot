package org.apache.maven.enforcer.rules.dependency;

import org.apache.maven.execution.MavenSession;

public abstract class ReinheitsgebotBannedDependencyBase extends BannedDependenciesBase {
    public ReinheitsgebotBannedDependencyBase(MavenSession session, ResolverUtil resolverUtil) {
        super(session, resolverUtil);
    }
}