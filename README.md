# RDFObfuscator

build:

 mvn clean install

execute:

 java -jar ./target/RDFObfuscator-1.0-SNAPSHOT-jar-with-dependencies.jar test.ttl
 
 or:
 
 java -jar ./target/RDFObfuscator-1.0-SNAPSHOT-jar-with-dependencies.jar test.ttl target=target.ttl
 
 java -jar ./target/RDFObfuscator-1.0-SNAPSHOT-jar-with-dependencies.jar test.ttl target.rdf syntax=RDF/XML

 java -jar ./target/RDFObfuscator-1.0-SNAPSHOT-jar-with-dependencies.jar test.ttl target.rdf syntax=RDF/XML urlStringBase=http://ex.com/a_

 
