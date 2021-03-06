/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.forge.project.facets.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.forge.project.Facet;
import org.jboss.seam.forge.project.PackagingType;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.facets.MavenFacet;
import org.jboss.seam.forge.project.facets.PackagingFacet;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class MavenWebFacet extends AbstractWebResourceFacet
{
   private Project project;

   @Override
   public File getWebRootDirectory()
   {
      return new File(project.getProjectRoot().getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "webapp");
   }

   @Override
   public List<File> getWebRootDirectories()
   {
      List<File> result = new ArrayList<File>();
      result.add(getWebRootDirectory());
      return result;
   }

   @Override
   public Facet init(final Project project)
   {
      this.project = project;
      return this;
   }

   @Override
   public Project getProject()
   {
      return project;
   }

   @Override
   public boolean isInstalled()
   {
      MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
      PackagingType packagingType = project.getFacet(PackagingFacet.class).getPackagingType();

      return getWebRootDirectory().exists() && mavenFacet.isInstalled()
               && packagingType.equals(new PackagingType("war"));
   }

   @Override
   public Facet install()
   {
      if (!this.isInstalled())
      {
         project.getFacet(PackagingFacet.class).setPackagingType(new PackagingType("war"));
         for (File folder : this.getWebRootDirectories())
         {
            folder.mkdirs();
         }
      }
      project.registerFacet(this);
      return this;
   }

   @Override
   public Set<Class<? extends Facet>> getDependencies()
   {
      Set<Class<? extends Facet>> result = new HashSet<Class<? extends Facet>>();
      result.add(MavenFacet.class);
      result.add(PackagingFacet.class);
      return result;
   }

   @Override
   public File getWebResource(final String relativePath)
   {
      return new File(getWebRootDirectory() + File.separator + relativePath);
   }
}
