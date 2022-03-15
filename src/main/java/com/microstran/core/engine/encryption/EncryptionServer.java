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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.AlgorithmParameterSpec;
import java.util.jar.JarFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


public class EncryptionServer
{
 
    
    private Cipher ecipher;
    private Cipher dcipher;
	// Buffer used to transport the bytes from one stream to another
    private byte[] buf = new byte[1024];

    /**
     * Constructor with secret key
     * @param key
     */
    public EncryptionServer(SecretKey key) 
    {
        //Create an 8-byte initialization vector
	    byte[] iv = new byte[]
	    {
	        (byte)0x7E, 0x13, 0x32, (byte)0x7B,
	        0x17, 0x52, 0x6E, 0x3C
	    };
	    
	    AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
	    try 
	    {
	        ecipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
	        dcipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
	        //CBC requires an initialization vector
	        ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
	        dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
	    } 
	    catch (java.security.InvalidAlgorithmParameterException e) 
	    {
	    } 
	    catch (javax.crypto.NoSuchPaddingException e) 
	    {
	    } 
	    catch (java.security.NoSuchAlgorithmException e) 
	    {
	    } 
	    catch (java.security.InvalidKeyException e) 
	    {
	    }
	}
	
	
    /**
     * Will encrypt SVG files
     * @param server
     * @return
     */
    public String runSVGEncrypt(EncryptionServer server, String inputSource)
    {
        try
        {
            server.encrypt(new FileInputStream(inputSource + ".svg"),
  		          new FileOutputStream(inputSource + ".xvg"));
            return(inputSource + ".xvg");
        }
        catch(Exception e)
        {
        }
        return(null);
    }
    
    /**
     * Will decrypt a SVG file, only works from resource jar files, will not decrypt 
     * files in the file system
     * @param server
     * @param jarFileName 
     * @param fileName
     * @return decrypted byte array to use
     */
    public byte [] runSVGDecrypt(EncryptionServer server, String fileName)
    {
    	JarFile jarFile =  null;
        try {
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    byte [] input = Files.readAllBytes(Paths.get(fileName));
		    ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
		    server.decrypt(inputStream,outputStream);
		    return(outputStream.toByteArray());
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        return(null);
    }

    /**
     * Displays user prompting in terminal window
     * @param inputPrompt
     * @param prompt
     * @return
     * @throws IOException
     */
    public static String getInput(String inputPrompt, String prompt) 
    throws IOException
	{
	    System.out.println("\n");
	    System.out.println(inputPrompt);
	    System.out.print(prompt);
	    int i=0; 
	    char c; 
	    StringBuffer strBuf = new StringBuffer();
	    InputStreamReader isr = new InputStreamReader(System.in); 
	
	    i  = isr.read(); 
	    c = (char) i; 
	    while ( i != 13 ) 
	    { 
	        strBuf.append(c);
	        i  = isr.read(); 
	        c = (char) i; 
	    }
	    return(strBuf.toString());
	}    
    
	/**
	 * encrypt a stream
	 * @param in
	 * @param out
	 */
	private void encrypt(InputStream in, OutputStream out) 
	{
	    try 
	    {
	        //Bytes written to out will be encrypted
	        out = new CipherOutputStream(out, ecipher);
	        //Read in the cleartext bytes and write to out to encrypt
	        int numRead = 0;
	        while ((numRead = in.read(buf)) >= 0) 
	        {
	            out.write(buf, 0, numRead);
	        }
	    out.close();
	    } 
	    catch (java.io.IOException e) 
	    {
	    }
	}
	
	/**
	 * Decrypt a stream
	 * @param in
	 * @param out
	 */
	private void decrypt(InputStream in, OutputStream out) 
	{
	    try 
	    {
	        //Bytes read from in will be decrypted
	        in = new CipherInputStream(in, dcipher);
	        //Read in the decrypted bytes and write the cleartext to out
	        int numRead = 0;
	        while ((numRead = in.read(buf)) >= 0) 
	        {
	            out.write(buf, 0, numRead);
	        }
	        out.close();
	    } 
	    catch (java.io.IOException e) 
	    {
	    }
	}
	
}