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

import fr.paris.lutece.plugins.blobstore.service.database.DatabaseBlobStoreService;
import fr.paris.lutece.portal.service.blobstore.BlobStoreFileItem;
import fr.paris.lutece.portal.service.blobstore.NoSuchBlobException;
import fr.paris.lutece.test.LuteceTestCase;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;


/**
 *
 * DatabaseBlobStoreServiceTest
 *
 */
public class DatabaseBlobStoreServiceTest extends LuteceTestCase
{
    private static final String FILE_NAME = "testblob.txt";

    /**
     * Tests input stream
     */
    public void testLoadInputStream(  ) throws IOException
    {
        String strFilename = getResourcesDir(  ) + "../test-classes/" + FILE_NAME;
        byte[] bStore = IOUtils.toByteArray( new FileInputStream( strFilename ) );

        DatabaseBlobStoreService service = new DatabaseBlobStoreService(  );
        String strKey = service.store( bStore );

        InputStream is = service.getBlobInputStream( strKey );
        byte[] bRead = IOUtils.toByteArray( is );

        assertTrue( Arrays.equals( bRead, bStore ) );
    }

    public void testBlobStoreFileItem(  ) throws IOException, NoSuchBlobException
    {
        String strFilename = getResourcesDir(  ) + "../test-classes/testblob.txt";
        byte[] bStore = IOUtils.toByteArray( new FileInputStream( strFilename ) );

        DatabaseBlobStoreService service = new DatabaseBlobStoreService(  );
        String strFileKey = service.store( bStore );
        File file = new File( strFilename );

        String strMetadata = BlobStoreFileItem.buildFileMetadata( FILE_NAME, file.length(  ), strFileKey );

        String strMetadataKey = service.store( strMetadata.getBytes(  ) );

        BlobStoreFileItem fileItem = new BlobStoreFileItem( strMetadataKey, service );

        assertEquals( strFileKey, fileItem.getFileBlobId(  ) );

        assertTrue( Arrays.equals( bStore, fileItem.get(  ) ) );
    }
}
