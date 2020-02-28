package com.konallc.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class RDFObfuscator {
	
	static long random;
	static String urlStringBase;
	static String targetFilename;
	static String fileSynatx;
	static char first;
	
	public static void main(String[] args) {
		
		first = ((char)(new Random().nextInt(26)+'a'));
					
		if (args.length < 1) { 
			System.err.println("Not enough arguments received.\n"+"usage: RDFObfuscator InputFile OutputFile Fileformat. examples:\n"
					+ "java -jar RDFObfuscator FileName.ttl\n"
					+ "java -jar RDFObfuscator FileName.ttl target=FileNameObfuscated.ttl syntax=TTL\n"
					+ "java -jar RDFObfuscator FileName.ttl target=FileNameObfuscated.ttl syntax=TTL urlStringBase=http://ex.com/a_\\n"); 
			System.exit(0); 			
		}
		
		Model mObfuscated = ModelFactory.createDefaultModel();

		//obfucate well-known URIS as well
		boolean obfuscateFull = false;

		
		Map<String, String> argsMap = new HashMap<>();
		for (String arg: args) {
		    String[] options = arg.split("=");
			if(options.length>1) {
				argsMap.put(options[0], options[1]);
			}
		} 
	        
		random = System.currentTimeMillis();
		
		if(argsMap.containsKey("urlStringBase")) {
			urlStringBase = argsMap.get("urlStringBase");
			mObfuscated.setNsPrefix("ex", urlStringBase);
		}else {
			urlStringBase = "";
		}

		if(argsMap.containsKey("target")) {
			targetFilename = argsMap.get("target");
		}else {
			targetFilename = "";
		}
		
		if(argsMap.containsKey("syntax")) {
			fileSynatx = argsMap.get("syntax");
		}else {
			fileSynatx = "TTL";
		}
		
		Model m = ModelFactory.createDefaultModel();
		
		try {
			m = RDFDataMgr.loadModel(args[0]);
		}catch(RiotNotFoundException ex) {
			System.out.println(ex.getMessage());
		}
		
		
		Iterator<Statement> iter = m.listStatements();
		while (iter.hasNext()) {			
			Statement stmt = iter.next();
			
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode node = stmt.getObject();
			
			RDFNode nodenew;
			Resource newsubject;
			Property newproperty;
			
			//Subject obfuscation
			
			if(obfuscateFull || !((subject.getNameSpace().equals(RDFS.getURI())) || (subject.getNameSpace().equals(RDF.getURI())) || (subject.getNameSpace().equals(OWL.getURI())))) {
				newsubject = mObfuscated.createResource(urlStringBase+obfuscate(subject.toString())); 
			}else {
				newsubject = subject;
			}
			
			//Predicate obfuscation
			
			if(obfuscateFull || !( (predicate.getNameSpace().equals(RDFS.getURI())) || (predicate.getNameSpace().equals(RDF.getURI())) || (predicate.getNameSpace().equals(OWL.getURI())))  ) {
				newproperty = mObfuscated.createProperty(urlStringBase+obfuscate(predicate.toString()));
			} else {
				newproperty = predicate;
			}
			
			//Object obfuscation
			if(node.isLiteral()) {nodenew = mObfuscated.createLiteral(obfuscate(node.asLiteral().toString()));}
			else{
				if(obfuscateFull || !( ((Resource)node).getNameSpace().equals(RDFS.getURI()) || ((Resource)node).getNameSpace().equals(RDF.getURI()) || ((Resource)node).getNameSpace().equals(OWL.getURI()) )) {
					nodenew = mObfuscated.createResource(urlStringBase+obfuscate(node.toString())); 
				} else {
					nodenew = stmt.getObject();
				}				
		}
		
		Statement  stmtnew = mObfuscated.createStatement(newsubject,newproperty,nodenew ) ;	
		mObfuscated.add(stmtnew );
		}

		if (targetFilename.equals("")) {mObfuscated.write(System.out,fileSynatx);}
		else {
			try {
				mObfuscated.write(new FileOutputStream(targetFilename),fileSynatx);
			}catch(Exception ex) {
				System.out.println(ex);
			}
		}
  }
	
	public static String obfuscate(String strOb) {
        StringBuilder strbuilder = new StringBuilder();
		try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((random+strOb).getBytes());
	    byte[] bytes = md.digest();
            for(int i=0; i< bytes.length ;i++)
            {
                strbuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }
        return first+strbuilder.toString();
	}
}
