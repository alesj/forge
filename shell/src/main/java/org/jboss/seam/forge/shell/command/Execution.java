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
package org.jboss.seam.forge.shell.command;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.seam.forge.shell.Shell;
import org.jboss.seam.forge.shell.exceptions.CommandExecutionException;
import org.jboss.seam.forge.shell.exceptions.NoSuchCommandException;
import org.jboss.seam.forge.shell.plugins.Option;
import org.jboss.seam.forge.shell.plugins.Plugin;
import org.mvel2.DataConversion;
import org.mvel2.util.ParseTools;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Execution
{
   private final BeanManager manager;
   private final Shell shell;


   private CommandMetadata command;
   private Object[] parameterArray;
   private String originalStatement;

   @Inject
   public Execution(BeanManager manager, Shell shell)
   {
      this.manager = manager;
      this.shell = shell;
   }

   @SuppressWarnings("unchecked")
   public void perform()
   {
      if (command != null)
      {
         Class<? extends Plugin> pluginType = command.getPluginMetadata().getType();
         Set<Bean<?>> beans = manager.getBeans(pluginType);
         Bean<?> bean = manager.resolve(beans);

         Method method = command.getMethod();

         Class<?>[] parmTypes = method.getParameterTypes();
         Annotation[][] parmAnnotations = method.getParameterAnnotations();
         Object[] paramStaging = new Object[parameterArray.length];

         for (int i = 0; i < parmTypes.length; i++)
         {
            try
            {
               paramStaging[i] = DataConversion.convert(parameterArray[i], parmTypes[i]);
               if (isBooleanOption(parmTypes[i]) && (null == paramStaging[i]))
               {
                  paramStaging[i] = false;
               }
            } catch (Exception e)
            {
               throw new CommandExecutionException(command, "command option '"
                     + command.getOrderedOptionByIndex(i).getDescription()
                     + "' must be of type '" + parmTypes[i].getSimpleName() + "'");
            }
         }

         Plugin plugin;
         if (bean != null)
         {
            CreationalContext<? extends Plugin> context = (CreationalContext<? extends Plugin>) manager
                  .createCreationalContext(bean);
            if (context != null)
            {
               plugin = (Plugin) manager.getReference(bean, pluginType, context);

               try
               {
                  command.getMethod().invoke(plugin, paramStaging);
               } catch (Exception e)
               {
                  throw new CommandExecutionException(command, e);
               }
            }
         }
      } else
      {
         // TODO it would be nice if this delegated to the system shell
         throw new NoSuchCommandException(command, "No such command: " + originalStatement);
      }
   }

   private static Option getOptionMetadata(Annotation[] annos)
   {
      for (Annotation a : annos)
      {
         if (a instanceof Option)
         {
            return (Option) a;
         }
      }

      return null;
   }

   private static boolean isBooleanOption(Class<?> type)
   {
      return ParseTools.unboxPrimitive(type) == boolean.class;
   }

   public CommandMetadata getCommand()
   {
      return command;
   }

   public void setCommand(final CommandMetadata command)
   {
      this.command = command;
   }

   public Object[] getParameterArray()
   {
      return parameterArray;
   }

   public void setParameterArray(final Object... parameters)
   {
      this.parameterArray = parameters;
   }

   public String getOriginalStatement()
   {
      return originalStatement;
   }

   public void setOriginalStatement(final String originalStatement)
   {
      this.originalStatement = originalStatement;
   }

}
