/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.blobstore.business.filesystem;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * Tests the file layout of the file system DAO and its refusal of anything that is not a blob key.
 */
public class FileSystemBlobStoreDAOTest extends LuteceTestCase
{
    private static final String KEY = "abcdef12-1111-4222-8333-444444444444";
    private static final byte [ ] CONTENT = "blob".getBytes( StandardCharsets.UTF_8 );

    private final FileSystemBlobStoreDAO _dao = new FileSystemBlobStoreDAO( );
    private Path _root;
    private String _strBase;

    /**
     * Creates an empty store directory inside a fresh root, next to a file that is not part of the store.
     *
     * @throws IOException
     *             if the directories cannot be created
     */
    @BeforeEach
    public void setUpStore( ) throws IOException
    {
        _root = Files.createTempDirectory( "blobstore-dao" );
        Files.write( _root.resolve( "outside" ), CONTENT );
        _strBase = Files.createDirectory( _root.resolve( "store" ) ).toString( ) + File.separator;
    }

    /**
     * Builds a blob.
     *
     * @param strKey
     *            the key
     * @return the blob
     */
    private static BytesBlobStore blob( String strKey )
    {
        BytesBlobStore blob = new BytesBlobStore( );
        blob.setId( strKey );
        blob.setValue( CONTENT );
        return blob;
    }

    /**
     * A blob stored at depth 2 lands under two folders of three characters, the layout migration_blobstore.sh builds.
     *
     * @throws Exception
     *             if the store fails
     */
    @Test
    public void testDepthTwoLayout( ) throws Exception
    {
        _dao.insert( blob( KEY ), _strBase, 2 );

        assertTrue( new File( _strBase + "abc" + File.separator + "def" + File.separator + KEY ).isFile( ) );
        assertArrayEquals( CONTENT, _dao.load( KEY, _strBase, 2 ).getValue( ) );
        assertTrue( _dao.delete( KEY, _strBase, 2 ) );
        assertFalse( new File( _strBase + "abc" ).exists( ) );
    }

    /**
     * Reading or deleting an unknown key answers nothing and creates no folder.
     *
     * @throws Exception
     *             if the store fails
     */
    @Test
    public void testUnknownKeyCreatesNothing( ) throws Exception
    {
        assertNull( _dao.load( KEY, _strBase, 2 ) );
        assertNull( _dao.loadInputStream( KEY, _strBase, 1 ) );
        assertFalse( _dao.delete( KEY, _strBase, 2 ) );

        assertTrue( new File( _strBase ).list( ).length == 0 );
    }

    /**
     * A key that is not made of letters, digits, '-' and '_' is refused by every operation, at every depth.
     *
     * @param strKey
     *            the refused key
     */
    @ParameterizedTest
    @ValueSource( strings = { "..", "../outside", "abc/../../outside", "abc\\def", "a.b", "", " " } )
    public void refusesUnsafeKey( String strKey )
    {
        for ( int nDepth = 0; nDepth <= 2; nDepth++ )
        {
            final int depth = nDepth;
            assertThrows( IOException.class, ( ) -> _dao.load( strKey, _strBase, depth ) );
            assertThrows( IOException.class, ( ) -> _dao.loadInputStream( strKey, _strBase, depth ) );
            assertThrows( IOException.class, ( ) -> _dao.insert( blob( strKey ), _strBase, depth ) );
            assertThrows( IOException.class, ( ) -> _dao.store( blob( strKey ), _strBase, depth ) );
            assertThrows( IOException.class, ( ) -> _dao.delete( strKey, _strBase, depth ) );
        }
        assertTrue( Files.exists( _root.resolve( "outside" ) ) );
    }

    /**
     * A null key, or a key shorter than the folders its depth needs, is refused instead of failing on a substring.
     */
    @Test
    public void testShortAndNullKeysRefused( )
    {
        assertThrows( IOException.class, ( ) -> _dao.load( null, _strBase, 0 ) );
        assertThrows( IOException.class, ( ) -> _dao.load( "ab", _strBase, 1 ) );
        assertThrows( IOException.class, ( ) -> _dao.load( "abcde", _strBase, 2 ) );
    }
}
