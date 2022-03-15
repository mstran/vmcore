/*
Copyright(c) 2022 Microstran Inc..  All rights reserved.
 
This file contains data proprietary to Microstran Inc.
It may contain data proprietary to others, with use granted to Microstran
 under a non-disclosure agreement. Do not release this 
information to any party unless that party has signed all appropriate 
non-disclosure agreements. 
The source code for this software is not published and 
remains protected by trade secret laws, notwithstanding any deposits 
with the U.S. Copyright Office.
*/

package com.microstran.core.engine.util;

import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.resources.ResourceManager;

/**
 * Class for message localization, will use Batik utilities for this
 */
public class MessageLocalizer 
{

    /**
     * The messages bundle class name
     */
    //public static final String MESSAGES = "resources.messages";
    public static final String MESSAGES = "messages_en_US";

    /**
     * Batik class for localization support for messages.
     */
    protected static LocalizableSupport localizableSupport = new LocalizableSupport(MESSAGES, MessageLocalizer.class.getClassLoader());
    
    /**
     * The Batik resource manager class to use.
     */
    protected static ResourceManager resourceManager = new ResourceManager(localizableSupport.getResourceBundle());

    /**
     * Implements {@link org.apache.batik.i18n.Localizable#setLocale(Locale)}.
     */
    public static void setLocale(Locale l) 
    {
        localizableSupport.setLocale(l);
        resourceManager = new ResourceManager(localizableSupport.getResourceBundle());
    }

    /**
     * Implements {@link org.apache.batik.i18n.Localizable#getLocale()}.
     */
    public static Locale getLocale() 
    {
        return(localizableSupport.getLocale());
    }

    /**
     * Implements {@link
     * org.apache.batik.i18n.Localizable#formatMessage(String,Object[])}.
     */
    public static String formatMessage(String key, Object[] args)
        throws MissingResourceException 
    {
        return(localizableSupport.formatMessage(key, args));
    }

    /**
     * Get localized string for key
     * @param key
     * @return
     * @throws MissingResourceException
     */
    public static String getString(String key)
        throws MissingResourceException 
    {
        return(resourceManager.getString(key));
    }

    /**
     * get localized integer for key
     * @param key
     * @return
     * @throws MissingResourceException
     */
    public static int getInteger(String key) 
        throws MissingResourceException 
    {
        return(resourceManager.getInteger(key));
    }

    /**
     * get localized char for key
     * @param key
     * @return
     * @throws MissingResourceException
     */
    public static int getCharacter(String key)
        throws MissingResourceException 
    {
        return(resourceManager.getCharacter(key));
    }

    /**
     * protected class constructor
     */
    protected MessageLocalizer() 
    { }
    
}
