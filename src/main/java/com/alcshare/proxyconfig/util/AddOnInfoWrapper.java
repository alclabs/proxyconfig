/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2012 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)AddOnInfoWrapper

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.proxyconfig.util;

import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.access.SystemConnection;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;

/**
 * Exposes AddOnInfo properties in ways that bypass the Enterprise License requirements.
 */
public class AddOnInfoWrapper
{
   public static final boolean is41 = determineIs41();

   public SystemConnection getRootSystemConnection()
   {
      try
      {
         // 1.0.+ compatible
         return AddOnInfo.getAddOnInfo().getRootSystemConnection();
      }
      catch (UnsupportedOperationException e)
      {
         // If the user doesn't have an Enterprise feature in their license then this exception is thrown.
         // We still want this add-on to work, however, so we'll use reflection to get the AddOnInfo.

         try
         {
            Class<?> implClass = Class.forName("com.controlj.green.addonsupport.access.IDirectAccessFactoryImpl");
            Object directAccess = implClass.getMethod("newDirectAccess").invoke(implClass.newInstance());
            return (SystemConnection) directAccess.getClass().getMethod("getRootSystemConnection").invoke(directAccess);
         }
         catch (Exception e1)
         {
            Logging.println("Error getting AddOnInfo using reflection", e1);
            throw e; // rethrow original exception (this is the least change from an external standpoint)
         }
      }
   }

   private static boolean determineIs41()
   {
      try
      {
         AddOnInfo.class.getDeclaredMethod("getDateStampLogger");
         return false;
      }
      catch (NoSuchMethodException e)
      {
         return true;
      }
      catch (NoClassDefFoundError e)
      {
         return true;
      }
   }

   public PrintWriter getDateStampLogger()
   {
      try
      {
         // if we are running on a newer version of the add-on API (1.1 or later) then
         // we can get a logger...
         Method method = AddOnInfo.class.getDeclaredMethod("getDateStampLogger");
         Writer writer = (Writer) method.invoke(getAddOnInfo());
         return new PrintWriter(writer);
      }
      catch (Throwable e)
      {
         // otherwise, just write to System.out (it's the best we can do)...
         return new PrintWriter(System.out);
      }
   }

   public PrintWriter getDateStampLogger(String name)
   {
      try
      {
         // if we are running on a newer version of the add-on API (1.1 or later) then
         // we can get a logger...
         Method method = AddOnInfo.class.getDeclaredMethod("getDateStampLogger", String.class);
         Writer writer = (Writer) method.invoke(getAddOnInfo(), name);
         return new PrintWriter(writer);
      }
      catch (Throwable e)
      {
         // otherwise, just write to System.out (it's the best we can do)...
         return new PrintWriter(System.out);
      }
   }

   public String getName()
   {
      return getAddOnInfo().getName();
   }

   private AddOnInfo getAddOnInfo()
   {
      try
      {
         return AddOnInfo.getAddOnInfo();
      }
      catch (UnsupportedOperationException e)
      {
         // If the user doesn't have an Enterprise feature in their license then this exception is thrown.
         try
         {
            // in 5.5 and later, we can just use AddOnInfoInternal to get it (using the public-internal API)
            Class<?> clazz = Class.forName("com.controlj.green.addonsupport.AddOnInfoInternal");
            return (AddOnInfo) clazz.getDeclaredMethod("getAddOnInfoInternal").invoke(null);
         }
         catch (Exception e1)
         {
            // in 5.2 we can just create the AddOnInfoImpl directly using it's no-arg constructor.  That
            // figures out who we are.
            try {
               Class<?> clazz = Class.forName("com.controlj.green.addonsupport.impl.AddOnInfoImpl");
               return (AddOnInfo) clazz.newInstance();
            }
            catch (Exception e2)
            {
               Logging.println("Error getting AddOnInfo using reflection", e2);
               throw new RuntimeException(e.getMessage(), e2);
            }
         }
      }
   }
}