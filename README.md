![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=tech-plugin-blobstore-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)

# Plugin's description

## Introduction

This plugin handles the big data storage, in database or file system.

# Installation

Configure the private key for the signature in the file **blobstore_context.xml** :

```

<bean id="blobstore.hashService" class="fr.paris.lutece.util.signrequest.security.Sha1HashService" />
<bean id="blobstore.requestAuthenticator" class="fr.paris.lutece.util.signrequest.RequestHashAuthenticator" >
	<property name="hashService" ref="blobstore.hashService" />
	<property name="signatureElements" > 
		<list>
			<value>blobstore</value>
			<value>blob_key</value>
		</list>
	</property>
	<property name="privateKey">
		<value> **change me** </value>
	</property>
</bean>

```

# BlobStore implementation in a plugin

The plugin-blobstore cannot run alone. It is used in addition of a plugin in which the latter will be able to store big data in a distinct database or in file system.

Each data is linked to an ID blob which is generated randomly. The use of the library **java.util.UUID** ensures the unicity of the ids.

## Step 1 : Implentation of a service that will use a BlobStoreService

First of all, create a new service that has a private attribute type **BlobStoreService** :

```

public class MyPluginService
{
    private BlobStoreService _blobStoreService;

    /**
     * Set the BlobStoreService
     * @param blobStoreService the {@link BlobStoreService}
     */
    public void setBlobStoreService( BlobStoreService blobStoreService )
    {
        _blobStoreService = blobStoreService;
    }
    .
    .
    .
}

```

Next, define the service in the XML file of the plugin (ex : webapp/WEB-INF/plugins/myplugin_context.xml). It is in this file where the stored data type is defined.

To store in database (modify the bold words) :

```

<bean id=" **myplugin** .blobStoreService" class="fr.paris.lutece.plugins.blobstore.service.database.DatabaseBlobStoreService">
	<property name="name" value=" **myplugin** .blobStoreService" />
</bean>
<bean id=" **myplugin.myPluginService** " class=" **fr.paris.lutece.plugins.myplugin.service.MyPluginService** ">
	<property name="blobStoreService" ref=" **myplugin** .blobStoreService" />
</bean>

```

To store in file system (modify the bold words) :

```

<bean id=" **myplugin** .blobStoreService" class="fr.paris.lutece.plugins.blobstore.service.filesystem.FileSystemBlobStoreService">
	<property name="name" value="<strong>myplugin</strong>.blobStoreService" />
	<property name="basePath" value=" **D:\data\blobstore** " />
	<property name="depth" value=" **1** " />
</bean>
<bean id=" **myplugin.myPluginService** " class=" **fr.paris.lutece.plugins.myplugin.service.MyPluginService** ">
	<property name="blobStoreService" ref=" **myplugin** .blobStoreService" />
</bean>

```

## Step 2 : Implementation of basic methods of the BlobStoreService

The API of **BlobStoreService** offers several functionnalities that allows to implements basic operation?

To store a blob with an array of bytes or with an InputStream :

```

public void storeBlob( byte[] blob )
{
	// Plugin operation
	...
	_blobStoreService.store( blob );
	...
}

public void storeBlob( InputStream blob )
{
	// Plugin operation
	...
	_blobStoreService.storeInputStream( blob );
	...
}

```

To modify a blob with an array of bytes or with an InputStream :

```

public void updateBlob( String strKey, byte[] blob )
{
	// Plugin operation
	...
	_blobStoreService.update( strKey, blob );
	...
}

public void updateBlob( String strKey, InputStream blob )
{
	// Plugin operation
	...
	_blobStoreService.updateInputStream( strKey, blob );
	...
}

```

To delete a blob :

```

public void deleteBlob( String strKey )
{
	// Plugin operation
	...
	_blobStoreService.delete( strKey );
	...
}

```

It is also possible to retrieve the URL to download the stored file :

```

public String getBlobUrl( String strKey )
{
	// Plugin operation
	...
	String strBlobUrl = _blobStoreService.getBlobUrl( strKey );
	...
}

public String getFileUrl( String strKey )
{
	// Plugin operation
	...
	String strFileUrl = _blobStoreService.getFileUrl( strKey );
	...
}

```


[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-blobstore/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*