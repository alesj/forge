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

package org.jboss.seam.forge.test.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.forge.parser.JavaParser;
import org.jboss.seam.forge.parser.java.JavaClass;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.facets.JavaSourceFacet;
import org.jboss.seam.forge.project.facets.MavenFacet;
import org.jboss.seam.forge.project.services.ProjectFactory;
import org.jboss.seam.forge.project.util.DependencyBuilder;
import org.jboss.seam.forge.test.project.util.ProjectModelTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@Singleton
@RunWith(Arquillian.class)
public class MavenFacetsTest extends ProjectModelTest
{
   private static final String PKG = MavenFacetsTest.class.getSimpleName().toLowerCase();

   @Inject
   private ProjectFactory projectFactory;

   private static Project thisProject;
   private static Project testProject;

   @Inject
   private DependencyBuilder dependencyBuilder;

   @Before
   @Override
   public void postConstruct() throws IOException
   {
      super.postConstruct();

      if (thisProject == null)
      {
         thisProject = projectFactory.findProjectRecursively(new File(""));
      }
      if (testProject == null)
      {
         testProject = projectFactory.findProjectRecursively(new File("src/test/resources/test-pom"));
      }
   }

   @Test
   public void testCreateDefault() throws Exception
   {
      assertTrue(getProject().exists());
   }

   @Test
   public void testGetDefaultSourceDir() throws Exception
   {
      assertEquals(new File(getProject().getProjectRoot() + "/src/main/java/"),
            getProject().getFacet(JavaSourceFacet.class).getSourceFolder());
   }

   @Test
   public void testGetTestSourceDir() throws Exception
   {
      assertEquals(new File(getProject().getProjectRoot() + "/src/test/java/"),
            getProject().getFacet(JavaSourceFacet.class).getTestSourceFolder());
   }

   @Test
   public void testCreateJavaFile() throws Exception
   {
      String name = "JustCreated";
      JavaClass clazz = JavaParser.createClass().setName(name).setPackage(PKG);
      clazz.getOrigin();
      File file = getProject().getFacet(JavaSourceFacet.class).saveJavaClass(clazz);
      assertEquals(name + ".java", file.getName());

      JavaClass result = JavaParser.parse(file);
      assertEquals(name, result.getName());
      assertEquals(PKG, result.getPackage());
      assertTrue(getProject().delete(file));
      assertEquals(clazz, result);
   }

   @Test
   public void testCreatePOM() throws Exception
   {
      Model pom = getProject().getFacet(MavenFacet.class).getPOM();
      pom.setGroupId("org.jboss.seam");
      pom.setArtifactId("scaffolding");
      pom.setVersion("X-SNAPSHOT");
      getProject().getFacet(MavenFacet.class).setPOM(pom);
      File file = pom.getPomFile();
      assertTrue(file.exists());

      MavenXpp3Reader reader = new MavenXpp3Reader();
      Model result = reader.read(new FileInputStream(file));
      assertEquals(pom.getArtifactId(), result.getArtifactId());
   }

   @Test
   public void testHasDependency() throws Exception
   {
      assertTrue(testProject.getFacet(MavenFacet.class).hasDependency(dependencyBuilder.setGroupId("com.ocpsoft")
               .setArtifactId("prettyfaces-jsf2").setVersion("3.0.2-SNAPSHOT").build()));
   }

   @Test
   public void testAddDependency() throws Exception
   {
      Dependency dependency = dependencyBuilder.setGroupId("org.jboss")
               .setArtifactId("test-dependency").setVersion("1.0.0.Final").build();

      assertFalse(getProject().getFacet(MavenFacet.class).hasDependency(dependency));
      getProject().getFacet(MavenFacet.class).addDependency(dependency);
      assertTrue(getProject().getFacet(MavenFacet.class).hasDependency(dependency));
   }

   @Test
   public void testRemoveDependency() throws Exception
   {
      Dependency dependency = dependencyBuilder.setGroupId("org.jboss")
               .setArtifactId("test-dependency").setVersion("1.0.1.Final").build();

      assertFalse(getProject().getFacet(MavenFacet.class).hasDependency(dependency));
      getProject().getFacet(MavenFacet.class).addDependency(dependency);
      assertTrue(getProject().getFacet(MavenFacet.class).hasDependency(dependency));
      getProject().getFacet(MavenFacet.class).removeDependency(dependency);
      assertFalse(getProject().getFacet(MavenFacet.class).hasDependency(dependency));
   }

   @Test
   public void testProjectIsCurrentProject() throws Exception
   {
      Model pom = thisProject.getFacet(MavenFacet.class).getPOM();
      assertEquals("forge-project-model", pom.getArtifactId());
   }

   @Test
   public void testAbsoluteProjectIsResolvedCorrectly() throws Exception
   {
      Model pom = testProject.getFacet(MavenFacet.class).getPOM();
      assertEquals("socialpm", pom.getArtifactId());
   }

   @Test(expected = FileNotFoundException.class)
   public void testAbsoluteUnknownProjectCannotInstantiate() throws Exception
   {
      File temp = File.createTempFile(PKG, null);
      temp.delete();
      temp.mkdirs();
      projectFactory.findProjectRecursively(temp); // boom
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testAbsoluteUnknownProjectInstantiatesWithCreateTrue() throws Exception
   {
      File temp = File.createTempFile(PKG, null);
      temp.delete();
      temp.mkdirs();
      projectFactory.createProject(temp, MavenFacet.class, JavaSourceFacet.class); // no
                                                                                   // boom
   }
}
