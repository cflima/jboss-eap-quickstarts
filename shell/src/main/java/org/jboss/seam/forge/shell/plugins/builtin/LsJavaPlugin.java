/*
 * JBoss, by Red Hat.
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

import org.jboss.seam.forge.parser.java.Field;
import org.jboss.seam.forge.parser.java.JavaClass;
import org.jboss.seam.forge.parser.java.Method;
import org.jboss.seam.forge.parser.java.Parameter;
import org.jboss.seam.forge.project.Resource;
import org.jboss.seam.forge.project.resources.builtin.JavaFieldResource;
import org.jboss.seam.forge.project.resources.builtin.JavaMemberResource;
import org.jboss.seam.forge.project.resources.builtin.JavaMethodResource;
import org.jboss.seam.forge.project.resources.builtin.JavaResource;
import org.jboss.seam.forge.shell.Shell;
import org.jboss.seam.forge.shell.color.JavaColorizer;
import org.jboss.seam.forge.shell.plugins.*;
import org.jboss.seam.forge.shell.util.GeneralUtils;
import org.jboss.seam.forge.shell.util.ShellColor;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.seam.forge.shell.util.GeneralUtils.printOutColumns;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Mike Brock
 */
@OverloadedName("ls")
@ResourceScope({JavaResource.class, JavaMethodResource.class, JavaFieldResource.class})
@Topic("File & Resources")
@Help("Prints the contents current Java file")
public class LsJavaPlugin implements Plugin
{
   private static final String DELIM = "::";

   @Inject
   private Shell shell;

   @DefaultCommand
   public void run(@Option(flagOnly = true, name = "all", shortName = "a", required = false) final boolean showAll,
                   @Option(flagOnly = true, name = "list", shortName = "l", required = false) final boolean list,
                   @Option(description = "path", defaultValue = ".") Resource<?>[] paths,
                   final PipeOut out) throws FileNotFoundException
   {

      for (Resource<?> resource : paths)
      {
         if (resource instanceof JavaResource)
         {
            if (showAll)
            {
               out.print(JavaColorizer.format(out, resource.toString()));
            }
            else
            {
               JavaResource javaSource = (JavaResource) resource;
               JavaClass javaClass = javaSource.getJavaClass();
               List<String> output = new ArrayList<String>();

               if (!out.isPiped())
               {
                  out.println();
                  out.println(ShellColor.RED, "[fields]");
               }

               List<Field> fields = javaClass.getFields();

               for (Field field : fields)
               {
                  String entry = out.renderColor(ShellColor.BLUE, field.getVisibility().scope());
                  entry += out.renderColor(ShellColor.GREEN, DELIM + field.getType() + "");
                  entry += DELIM + field.getName() + ";";
                  output.add(entry);
               }

               if (out.isPiped())
               {
                  GeneralUtils.OutputAttributes attr = new GeneralUtils.OutputAttributes(120, 1);
                  printOutColumns(output, ShellColor.NONE, out, attr, null, false);
               }
               else
               {
                  GeneralUtils.printOutColumns(output, out, shell, true);
                  out.println();
               }

               // rinse and repeat for methods
               output = new ArrayList<String>();
               List<Method> methods = javaClass.getMethods();

               if (!out.isPiped())
               {
                  out.println(ShellColor.RED, "[methods]");
               }

               for (Method method : methods)
               {
                  String entry = out.renderColor(ShellColor.BLUE, method.getVisibility().scope());
                  String parameterString = "(";

                  for (Parameter param : method.getParameters())
                  {
                     parameterString += param.toString();
                  }
                  parameterString += ")";

                  entry += DELIM + method.getName() + parameterString;

                  String returnType = method.getReturnType() == null ? "void" : method.getReturnType();
                  entry += out.renderColor(ShellColor.GREEN, DELIM + returnType + "");
                  output.add(entry);
               }

               if (out.isPiped())
               {
                  GeneralUtils.OutputAttributes attr = new GeneralUtils.OutputAttributes(120, 1);
                  printOutColumns(output, ShellColor.NONE, out, attr, null, false);
               }
               else
               {
                  GeneralUtils.printOutColumns(output, out, shell, true);
                  out.println();
               }
            }
         }
         else if (resource instanceof JavaMemberResource<?>)
         {
            out.println();
            out.println(JavaColorizer.format(out, resource.toString()));
         }
      }
   }
}