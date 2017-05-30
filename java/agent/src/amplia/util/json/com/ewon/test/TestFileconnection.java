/*
 * TestFileconnection.java
 *
 * Created on 18 juillet 2006, 17:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.ewon.test;

import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.file.*;
import java.util.*;
import javax.microedition.io.Connector;

/**
 *
 * @author Crooks
 */
public class TestFileconnection
{
   
   /** Creates a new instance of TestFileconnection */
   public TestFileconnection()
   {
   }
   
   public static void listRootTest()
   {
      Enumeration RootList;
      
      RootList = FileSystemRegistry.listRoots();
      while (RootList.hasMoreElements())
      {
         System.out.println((String)RootList.nextElement());
      }
      System.out.println("done.");
   }
   
   public static void readDirTest()
   {
      String openParm = "file:////usr";
      FileConnection    fconn  = null;
      InputStream       is  = null;
      OutputStream      os  = null;
      
      try
      {
         fconn = (FileConnection) Connector.open(openParm);
         // If no exception is thrown, then the URI is valid, but the file may or may not exist.
         if (!fconn.exists())
         {
            System.out.println("The file does not exists");
         }
         
         System.out.println("File exists: "+fconn.exists());
         System.out.println("File IsDir: "+fconn.isDirectory());
         
         if (fconn.isDirectory())
         {
            Enumeration listDir;
            
            System.out.println("DiskSize");
            
            System.out.println("TotalSize: "+fconn.totalSize());
            System.out.println("UsedSize: "+fconn.usedSize());
            System.out.println("availableSize: "+fconn.availableSize());
         }
         else
         {
            System.out.println("File Size: "+fconn.fileSize());
         }
         
         fconn.close();
      }
      catch (IOException ioe)
      {
         System.out.println("Error readDirTest: "+ioe.toString());
      }
   }
   
   public void createFileTest()
   {
      try
      {
         FileConnection fconn = 
                 (FileConnection) Connector.open("file:////usr/MyTestO.txt");
         if (fconn.exists())
            fconn.delete();
         fconn.create();
         OutputStream os = fconn.openOutputStream();
         PrintStream ps = new PrintStream(os);
         
         ps.println("0123457-A");
         ps.println("0123457-B");
         ps.println("0123457-C");
         ps.println("0123457-D");
         
         fconn.close();
      }
      catch (IOException ioe)
      {
         System.out.println("Error createFileTest: "+ioe.toString());
      }
   }
   
  
   
}
