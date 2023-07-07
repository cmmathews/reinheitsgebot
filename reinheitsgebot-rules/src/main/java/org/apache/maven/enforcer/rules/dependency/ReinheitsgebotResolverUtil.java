package org.apache.maven.enforcer.rules.dependency;

import org.apache.maven.execution.MavenSession;
import org.eclipse.aether.RepositorySystem;

public abstract class ReinheitsgebotResolverUtil extends ResolverUtil{
    public ReinheitsgebotResolverUtil(RepositorySystem repositorySystem, MavenSession session) {
        super(repositorySystem, session);
    }
}
