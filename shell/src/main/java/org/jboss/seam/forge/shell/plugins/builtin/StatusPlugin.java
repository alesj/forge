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
package org.jboss.seam.forge.shell.plugins.builtin;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.model.Dependency;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.facets.MavenFacet;
import org.jboss.seam.forge.shell.Shell;
import org.jboss.seam.forge.shell.command.PluginMetadata;
import org.jboss.seam.forge.shell.command.PluginRegistry;
import org.jboss.seam.forge.shell.plugins.DefaultCommand;
import org.jboss.seam.forge.shell.plugins.Help;
import org.jboss.seam.forge.shell.plugins.MavenPlugin;
import org.jboss.seam.forge.shell.plugins.Option;
import org.jboss.seam.forge.shell.plugins.Plugin;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Named("status")
@Help("Check the current project configuration and display the specified plugin's installed status.")
public class StatusPlugin implements Plugin
{
   @Inject
   private PluginRegistry registry;

   @Inject
   private Shell shell;

   @Inject
   private Project project;

   @DefaultCommand
   public void status(@Option(help = "The name of the plugin.") final String pluginName)
   {
      if ((pluginName == null) || pluginName.isEmpty())
      {
         shell.println("Currently operating on the project located in: " + project.getProjectRoot());
      }
      else
      {
         PluginMetadata meta = registry.getPlugins().get(pluginName);
         if (meta != null)
         {
            Plugin plugin = registry.instanceOf(meta);
            if (plugin instanceof MavenPlugin)
            {
               MavenPlugin installable = (MavenPlugin) plugin;
               if (isInstalledInProject(installable))
               {
                  shell.println("Status: INSTALLED");
               }
               else
               {
                  shell.println("Status: NOT-INSTALLED (you may run \"install " + pluginName
                           + "\" to install this plugin.");
               }
            }
            else
            {
               shell.println("The plugin [" + pluginName
                        + "] is not an installable plugin.");
            }
         }
         else
         {
            shell.println("Could not find a plugin with the name: " + pluginName
                     + "; are you sure that's the correct name?");
         }
      }
   }

   private boolean isInstalledInProject(final MavenPlugin installable)
   {
      for (Dependency d : installable.getDependencies())
      {
         if (!project.getFacet(MavenFacet.class).hasDependency(d))
         {
            return false;
         }
      }
      return true;
   }

}
