# ESSecurityPlugin
This Plugin provides username and password protection to prevent unauthorized access to Elasticsearch(ver 2.4) cluster.
It can secure both REST API and Java Transport Client by defining configurations in elasticsearch.yml file.
This plugin is to demonstrate how to write a Elasticsearch plugin for security purpose and It can be used in internal Development environment. 
This plugin only supports Elasticsearch 2.4. It is not recommended to use this plugin for a production environment especially for a public cluster. For production level protection, please check Shield plugin from Elasticsearch(https://www.elastic.co/guide/en/shield/current)
and Opensource alternative - SearchGuard (https://floragunn.com/searchguard).

1. Install the plugin. 
   Download the source and run Maven command - mvn clean package to build plugin zip file - security-plugin-1.1.zip in target/releases folder.
   In bin folder of Elasticsearch, run - plugin install file://path-to-plugin-zip-file to install plugin.

2. Config the plugin.
   To enable REST API protection, add follwowing configurations in elasticsearch.yml file:
   
   simplesecurity.auth.enable: true
   simplesecurity.auth.user: user
   simplesecurity.auth.password: password
   
   
   REST API protection is implemented using HTTP Basic Authentication mechanism, you need to add user/password in Curl command or submit      authentication form to use a monitoring plugin, such as kopf.
   
   To enable protetion for Java Transport Client, add follwowing configurations in elasticsearch.yml file:
   
   transport.profiles.java.port: 9898
   simplesecurity.java.enable: true
   simplesecurity.java.user: user
   simplesecurity.java.password: password
   
   You can use any available port to Java client. The following code shows how to use plugin in Java Code.
   First add security-plugin-1.1.jar as your project dependency, then config TransportClient like this:
   
   TransportClient client = TransportClient.builder()
                .addPlugin(SimpleSecurityPlugin.class)
                .settings(Settings.builder()
                        .put("javaclient", "user:password")
                        .build())
                .build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9898));

   
   
   
