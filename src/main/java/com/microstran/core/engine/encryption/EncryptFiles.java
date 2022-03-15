/*
 * Copyright(c) 2022 Microstran Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microstran.core.engine.encryption;

import java.io.File;
import java.util.Vector;

import javax.crypto.spec.SecretKeySpec;

/**
 * @author Mstran
 *
 */
public class EncryptFiles
{
    public static final String location = "/home/mstran/Projects/ProjectX/JavaClock2021/VMCC/VMCCClock/VMCCClock/resources/svg";
    public static final String extension = ".svg";
    
	public static void main(String[] args) 
	{
	    EncryptFiles files = new EncryptFiles();
	    files.process();
	}


	public void process()
	{
	    try 
		{
	        byte[] sKey = new byte[]
            { 0x1A, -0x3C, 0x5B, 0x0B, 0x75, 0x64, 0x70, -0x4D };
	        //create DES encryption key using our stored key
		    SecretKeySpec key = new SecretKeySpec(sKey,"DES");
		    //Create encrypter/decrypter class
		    EncryptionServer server = new EncryptionServer(key);
		    //recursively search from a location
		    Vector files = generateFileList(location, extension);
		    int numFiles = files.size();
		    for (int i = 0; i < numFiles; i++)
		    {
		        String fileSpec = (String)files.get(i);
			    //run encryption
		        String output = server.runSVGEncrypt(server, fileSpec); 
		        //display status
		        System.out.println("Processed: "+output);
		    }
		 } 
		catch (Exception e) 
		 {
		    System.out.println(e.getMessage());
		 }

	}
	
	/**
	 * This method returns the list of files by recursively searching down all 
	 * directories for files that end with the fileSpec
	 * @return an array of files in a directory
	 */
	private Vector generateFileList(String directoryToSearch, String fileSpec)
	{
	    Vector svgFiles = new Vector();
	    File dir = new File(directoryToSearch);
	    String path = dir.getPath();
	    String[] files = dir.list();
	    int numFiles = files.length;
	    for (int i = 0; i < numFiles; i++)
	    {
	        String fileName = files[i];
	        File f = new File(path, fileName);
	        if (f.isFile()) 
	        {
	            if(fileName.endsWith(fileSpec)) 
	                svgFiles.add(path + "/" + fileName.substring(0,(fileName.length() - 4)));
	        } 
	        else if (f.isDirectory()) 
	        {
	            svgFiles.addAll(generateFileList(path + "/" + fileName, fileSpec));
	        }
	    }
	    return(svgFiles);
	}
	
}
