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

import org.jboss.seam.forge.parser.java.JavaClass;
import org.jboss.seam.forge.parser.java.util.Formatter;
import org.jboss.seam.forge.project.facets.JavaSourceFacet;
import org.jboss.seam.forge.project.util.Packages;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractJavaSourceFacet implements JavaSourceFacet
{
   // TODO the impl part of the project model API needs to be split into a
   // separate package so that plugin authors see a clean API
   private File saveJavaFile(final File sourceFolder, final JavaClass clazz)
   {
      String path = sourceFolder.getAbsolutePath() + File.separator + Packages.toFileSyntax(clazz.getPackage());
      File file = new File(path + File.separator + clazz.getName() + ".java");

      getProject().writeFile(Formatter.format(clazz).toCharArray(), file);
      // TODO event.fire(Created new Java file);
      return file;
   }

   @Override
   public File saveJavaClass(final JavaClass clazz)
   {
      return saveJavaFile(getSourceFolder(), clazz);
   }

   @Override
   public File saveTestJavaClass(final JavaClass clazz)
   {
      return saveJavaFile(getTestSourceFolder(), clazz);
   }
}
