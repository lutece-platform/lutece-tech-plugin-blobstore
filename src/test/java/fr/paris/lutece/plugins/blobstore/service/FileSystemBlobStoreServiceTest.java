/*
 * Copyright (c) 2002-2011, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.blobstore.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import fr.paris.lutece.plugins.blobstore.service.database.FileSystemBlobStoreService;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * 
 * FileSystemBlobStoreServiceTest
 *
 */
public class FileSystemBlobStoreServiceTest extends LuteceTestCase
{
	private static final String FILE_NAME = "testblob.txt";
	
	private String getBaseDirectory()
	{
		return getResourcesDir(  ) + "../test-classes/blobstore/";
	}
	
	private FileSystemBlobStoreService getService( String baseDirectory )
	{
		FileSystemBlobStoreService service = new FileSystemBlobStoreService();
		service.setBasePath( baseDirectory );
		
		return service;
	}
	
	public void testCreate() throws IOException
	{
		clearBlobStore();
		FileSystemBlobStoreService service = getService( getBaseDirectory() );
		
		String strFilename = getResourcesDir(  ) + "../test-classes/" + FILE_NAME;
		InputStream is = new FileInputStream( strFilename );
		byte[] bStore = IOUtils.toByteArray( is );
		IOUtils.closeQuietly( is );
		
		String strKey = service.store( bStore );
		assertTrue( new File( getBaseDirectory() + strKey ).exists(  ) );
		
		InputStream is2 = new FileInputStream( getBaseDirectory() + strKey );
		byte[] bRead = IOUtils.toByteArray( is2 );
		IOUtils.closeQuietly( is2 );
		
		assertTrue( Arrays.equals( bRead, bStore ) );
	}
	
	public void testUpdate() throws IOException
	{
		clearBlobStore();
		FileSystemBlobStoreService service = getService( getBaseDirectory() );
		
		String strFilename = getResourcesDir(  ) + "../test-classes/" + FILE_NAME;
		
		byte[] bStore;
		InputStream is = new FileInputStream( strFilename );
		try
		{
			bStore = IOUtils.toByteArray( is );
		}
		finally
		{
			IOUtils.closeQuietly( is );
		}
		
		String strKey = service.store( bStore );
		
		byte[] bUpdate = "Updated bytes".getBytes();
		service.update( strKey, bUpdate );
		byte[] bRead;
		InputStream is2 = new FileInputStream( getBaseDirectory() + strKey );
		try
		{
			bRead = IOUtils.toByteArray( is2 );
		}
		finally
		{
			IOUtils.closeQuietly( is2 );
		}
		
		System.out.println( new String( bUpdate ) );
		System.out.println( new String( bRead ) );
		
		assertTrue( Arrays.equals( bRead, bUpdate ) );
	}
	
	public void testDelete() throws IOException
	{
		clearBlobStore();
		FileSystemBlobStoreService service = getService( getBaseDirectory() );
		
		String strFilename = getResourcesDir(  ) + "../test-classes/" + FILE_NAME;
		InputStream is = new FileInputStream( strFilename );
		byte[] bStore;
		try
		{
			bStore = IOUtils.toByteArray( is );
		}
		finally
		{
			IOUtils.closeQuietly( is );
		}
		
		String strKey = service.store( bStore );
		service.delete( strKey );
		assertFalse( new File( getBaseDirectory() + strKey ).exists(  ) );
	}
	
	private void clearBlobStore() throws IOException
	{
		String baseDirectory = getBaseDirectory();
		File baseFile = new File( baseDirectory );
		if ( baseFile.exists(  ) )
		{
			if ( baseFile.isDirectory())
			{
				FileUtils.cleanDirectory( baseFile );
			}
			else
			{
				baseFile.delete();
				baseFile.mkdir();
			}
		}
		else
		{
			baseFile.mkdir();
		}
	}
}
