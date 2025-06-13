![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=tech-plugin-blobstore-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)

# Descriptif du plugin

## Introduction

Ce plugin permet de stocker des données de taille importante, que ce soit en base de données ou en fichier système.

## Configuration

Configurer la clés privées utilisées pour les signatures dans le fichier **blobstore_context.xml** :

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

## Usage

Il existe plusieurs façons d'utiliser le plugin-blobstore.

- A: Utilisation via les FileService avec le blobStoreFileServiceProvider (recommandé)

- B: Utilisation en complément d'un plugin pour que ce dernier puisse stocker des données de taille importantes dans une baseàpart ou sous forme de fichier système.

A chaque données est associée un ID blob qui est généréaléatoirement. L'utilisation de la librairie **java.util.UUID** assure l'unicitédes identifiants.




 **A/ Utilisation via le blobStoreFileServiceProvider** 



Le fileStoreService peut être ajouté à la classe Home :
```

private static IFileStoreServiceProvider _fileStoreService = FileService.getInstance( ).getFileStoreServiceProvider( "blobStoreProvider");

```
UTilisation ensuite du fileStoreServiceProvider :
```

...
	// get the file in multipart request and store it
        IFileStoreServiceProvider fileStoreService = MyHome.getFileStoreServiceProvider( );
        FileItem file = multipartRequest.getFile( "file" );
      
        if ( file != null  	&& file.getSize( ) > 0 )
        {
            try
            {
                String strFileStoreKey = fileStoreService.storeFileItem( file );
...
	// get an URL for display
	String strFileUrl = fileStoreService.getFileDownloadUrlBO( strFileKey );
...

```




 **B/ Etape 1 : Implémentation d'un service utilisant un service blobstore** 
Tout d'abord, créer un service qui possède une variable privée de type **BlobStoreService** :

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

Il faut ensuite définir le service dans le fichier XML de contexte du plugin (ex : webapp/WEB-INF/plugins/myplugin_context.xml). C'est dans ce fichier qu'il faut définir comment seront stockées les données (en base ou en fichier système).

Pour stocker en base (en modifiant ce qui est en gras par ce qui va bien) :

```

<bean id=" **myplugin** .blobStoreService" class="fr.paris.lutece.plugins.blobstore.service.database.DatabaseBlobStoreService">
	<property name="name" value=" **myplugin** .blobStoreService" />
</bean>
<bean id=" **myplugin.myPluginService** " class=" **fr.paris.lutece.plugins.myplugin.service.MyPluginService** ">
	<property name="blobStoreService" ref=" **myplugin** .blobStoreService" />
</bean>

```

Pour stocker en fichier système (en modifiant ce qui est en gras par ce qui va bien) :

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




 **Etape 2 : Implémentation des méthodes CRUD sur blobstore** 
L'API **BlobStoreService** offre de nombreuses fonctionnalitées permettant de réaliser les opérations basiques de façon simple :

Pour une création d'un blob avec un tableau de bytes ou par InputStream :

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

Pour une modification d'un blob avec un tableau de bytes ou par InputStream :

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

Pour une suppression d'un blob :

```

public void deleteBlob( String strKey )
{
	// Plugin operation
	...
	_blobStoreService.delete( strKey );
	...
}

```

Il estégalement possible d'obtenir une URL permettant de télécharger le fichier stocké. Pour cela, il faut implémenter une méthode qui renvoit l'URL du blob ou du fichier :

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