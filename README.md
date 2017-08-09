# ESSecurityPlugin
This Plugin provides username and password protection to prevent unauthorized access to Elasticsearch(ver 2.4) clusters.
It can secure both REST API and Java Transport Client by defining configurations in elasticsearch.yml file.
This plugin is to demonstrate how to write a Elasticsearch plugin for security purpose and it can be used in internal Development environment. This plugin only supports Elasticsearch 2.4. For production level protection, please check [Shield plugin](https://www.elastic.co/guide/en/shield/current) from Elasticsearch  or Opensource alternative - [SearchGuard](https://floragunn.com/searchguard).

## Getting Started
### Installing 
Download the source and run Maven command - ```mvn clean package``` to build plugin zip file - security-plugin-1.1.zip in target/releases folder. In bin folder of Elasticsearch, run:
```
plugin install file://path-to-plugin-zip-file
```
### Config the plugin
* To enable REST API protection, add following configurations in elasticsearch.yml file:
```   
simplesecurity.auth.enable: true
simplesecurity.auth.user: user
simplesecurity.auth.password: password
```   
REST API protection is implemented using HTTP Basic Authentication mechanism, you need to add user/password in Curl command or submit      authentication form to use a monitoring plugin, such as kopf.

* To enable protection for Java Transport Client, add following configurations in elasticsearch.yml file:
```   
transport.profiles.java.port: 9898
simplesecurity.java.enable: true
simplesecurity.java.user: user
simplesecurity.java.password: password
```   
You can use any available port to Java client. The following code shows how to use plugin in Java Code.
First add security-plugin-1.1.jar as your project dependency, then config TransportClient like this:
```   
TransportClient client = TransportClient.builder()
                           .addPlugin(SimpleSecurityPlugin.class)
                           .settings(Settings.builder()
                           .put("javaclient", "user:password")
                           .build()).build()
                           .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9898));
```
